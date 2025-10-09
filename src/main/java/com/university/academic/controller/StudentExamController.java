package com.university.academic.controller;

import com.university.academic.dto.StudentExamDTO;
import com.university.academic.entity.ExamRoomStudent;
import com.university.academic.entity.ExamStatus;
import com.university.academic.repository.ExamRoomStudentRepository;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生端考试查询控制器
 * 提供学生考试查询接口
 *
 * @author Academic System Team
 */
@Tag(name = "考试管理-学生", description = "学生端考试查询接口")
@Slf4j
@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentExamController {

    private final ExamRoomStudentRepository examRoomStudentRepository;
    private final DtoConverter dtoConverter;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 查询我的考试列表
     */
    @GetMapping
    @Transactional(readOnly = true)
    public Result<List<StudentExamDTO>> getMyExams(
            Authentication authentication,
            @RequestParam(required = false) Long semesterId) {
        
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询学生考试列表: studentId={}, semesterId={}", studentId, semesterId);

        // 查询学生的所有考试分配
        List<ExamRoomStudent> examRoomStudents = examRoomStudentRepository
                .findByStudentIdWithDetails(studentId);

        // 过滤并转换为DTO
        List<StudentExamDTO> examDTOList = examRoomStudents.stream()
                // 只返回已发布的考试
                .filter(ers -> {
                    ExamStatus status = ers.getExamRoom().getExam().getStatus();
                    return status == ExamStatus.PUBLISHED 
                        || status == ExamStatus.IN_PROGRESS 
                        || status == ExamStatus.FINISHED;
                })
                // 按学期筛选（如果提供了semesterId）
                .filter(ers -> {
                    if (semesterId == null) {
                        return true;
                    }
                    Long examSemesterId = ers.getExamRoom().getExam()
                            .getCourseOffering().getSemester().getId();
                    return examSemesterId.equals(semesterId);
                })
                // 转换为StudentExamDTO
                .map(dtoConverter::toStudentExamDTO)
                // 按考试时间排序
                .sorted(Comparator.comparing(StudentExamDTO::getExamTime))
                .collect(Collectors.toList());

        log.info("查询到 {} 场考试", examDTOList.size());
        return Result.success(examDTOList);
    }

    /**
     * 查询考试详情
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Result<StudentExamDTO> getExamById(
            Authentication authentication,
            @PathVariable Long id) {
        
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询考试详情: studentId={}, examId={}", studentId, id);

        // 查询该学生在该考试中的分配记录
        ExamRoomStudent examRoomStudent = examRoomStudentRepository
                .findByStudentIdAndExamId(studentId, id)
                .orElseThrow(() -> new RuntimeException("未找到考试安排"));

        // 检查考试是否已发布
        ExamStatus status = examRoomStudent.getExamRoom().getExam().getStatus();
        if (status == ExamStatus.DRAFT || status == ExamStatus.CANCELLED) {
            throw new RuntimeException("考试未发布或已取消");
        }

        StudentExamDTO examDTO = dtoConverter.toStudentExamDTO(examRoomStudent);
        return Result.success(examDTO);
    }

}

