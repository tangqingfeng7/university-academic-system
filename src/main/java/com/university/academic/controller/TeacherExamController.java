package com.university.academic.controller;

import com.university.academic.dto.ExamRoomDTO;
import com.university.academic.dto.InvigilationDTO;
import com.university.academic.dto.TeacherExamDTO;
import com.university.academic.entity.Exam;
import com.university.academic.entity.ExamInvigilator;
import com.university.academic.entity.ExamRoom;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ExamInvigilatorRepository;
import com.university.academic.repository.ExamRepository;
import com.university.academic.repository.ExamRoomRepository;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师端考试查询控制器
 * 提供教师课程考试和监考任务查询接口
 *
 * @author Academic System Team
 */
@Tag(name = "考试管理-教师", description = "教师端课程考试和监考任务查询接口")
@Slf4j
@RestController
@RequestMapping("/api/teacher/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherExamController {

    private final ExamRepository examRepository;
    private final ExamRoomRepository examRoomRepository;
    private final ExamInvigilatorRepository examInvigilatorRepository;
    private final DtoConverter dtoConverter;
    private final CustomUserDetailsService userDetailsService;
    private final com.university.academic.service.ExamService examService;

    // ==================== 12.1 课程考试查询接口 ====================

    /**
     * 查询我任教课程的考试列表
     */
    @GetMapping("/courses")
    @Transactional(readOnly = true)
    public Result<List<TeacherExamDTO>> getMyCourseExams(
            Authentication authentication,
            @RequestParam(required = false) Long semesterId) {
        
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师课程考试列表: teacherId={}, semesterId={}", teacherId, semesterId);

        List<Exam> exams;
        if (semesterId != null) {
            exams = examRepository.findByTeacherIdAndSemesterId(teacherId, semesterId);
        } else {
            exams = examRepository.findByTeacherId(teacherId);
        }

        // 主动触发examRooms懒加载
        exams.forEach(exam -> {
            if (exam.getExamRooms() != null) {
                exam.getExamRooms().size();
            }
        });

        List<TeacherExamDTO> examDTOList = exams.stream()
                .map(dtoConverter::toTeacherExamDTO)
                .sorted(Comparator.comparing(TeacherExamDTO::getExamTime))
                .collect(Collectors.toList());

        log.info("查询到 {} 场考试", examDTOList.size());
        return Result.success(examDTOList);
    }

    /**
     * 查询课程考试详情
     */
    @GetMapping("/courses/{id}")
    @Transactional(readOnly = true)
    public Result<TeacherExamDTO> getCourseExamById(
            Authentication authentication,
            @PathVariable Long id) {
        
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询课程考试详情: teacherId={}, examId={}", teacherId, id);

        Exam exam = examRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));

        // 验证是否是该教师的课程考试
        if (!exam.getCourseOffering().getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 主动触发examRooms懒加载
        if (exam.getExamRooms() != null) {
            exam.getExamRooms().size();
        }

        TeacherExamDTO examDTO = dtoConverter.toTeacherExamDTO(exam);
        return Result.success(examDTO);
    }

    /**
     * 查询考试的考场列表
     */
    @GetMapping("/courses/{id}/rooms")
    @Transactional(readOnly = true)
    public Result<List<ExamRoomDTO>> getExamRooms(
            Authentication authentication,
            @PathVariable Long id) {
        
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询考试考场列表: teacherId={}, examId={}", teacherId, id);

        // 验证权限 - 使用带详情的查询
        Exam exam = examRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
        
        if (!exam.getCourseOffering().getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<ExamRoom> rooms = examRoomRepository.findByExamId(id);
        
        // 主动触发懒加载
        rooms.forEach(room -> {
            if (room.getStudents() != null) {
                room.getStudents().size();
            }
            if (room.getInvigilators() != null) {
                room.getInvigilators().size();
            }
        });
        
        List<ExamRoomDTO> roomDTOList = rooms.stream()
                .map(dtoConverter::toExamRoomDTO)
                .collect(Collectors.toList());

        return Result.success(roomDTOList);
    }

    // ==================== 12.2 监考任务查询接口 ====================

    /**
     * 查询我的监考任务列表
     */
    @GetMapping("/invigilation")
    @Transactional(readOnly = true)
    public Result<List<InvigilationDTO>> getMyInvigilation(
            Authentication authentication,
            @RequestParam(required = false) Long semesterId) {
        
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师监考任务: teacherId={}, semesterId={}", teacherId, semesterId);

        List<ExamInvigilator> invigilations;
        if (semesterId != null) {
            invigilations = examInvigilatorRepository.findByTeacherIdAndSemesterId(teacherId, semesterId);
        } else {
            invigilations = examInvigilatorRepository.findByTeacherId(teacherId);
        }

        // 主动触发懒加载
        invigilations.forEach(inv -> {
            if (inv.getExamRoom() != null && inv.getExamRoom().getExam() != null) {
                Exam exam = inv.getExamRoom().getExam();
                if (exam.getCourseOffering() != null) {
                    exam.getCourseOffering().getCourse();
                }
            }
        });

        List<InvigilationDTO> invigilationDTOList = invigilations.stream()
                .map(dtoConverter::toInvigilationDTO)
                .sorted(Comparator.comparing(InvigilationDTO::getExamTime))
                .collect(Collectors.toList());

        log.info("查询到 {} 个监考任务", invigilationDTOList.size());
        return Result.success(invigilationDTOList);
    }

    /**
     * 查询监考任务详情
     */
    @GetMapping("/invigilation/{id}")
    @Transactional(readOnly = true)
    public Result<InvigilationDTO> getInvigilationById(
            Authentication authentication,
            @PathVariable Long id) {
        
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询监考任务详情: teacherId={}, invigilationId={}", teacherId, id);

        ExamInvigilator invigilator = examInvigilatorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVIGILATOR_NOT_FOUND));

        // 验证是否是该教师的监考任务
        if (!invigilator.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 主动触发懒加载
        if (invigilator.getExamRoom() != null && invigilator.getExamRoom().getExam() != null) {
            Exam exam = invigilator.getExamRoom().getExam();
            if (exam.getCourseOffering() != null) {
                exam.getCourseOffering().getCourse();
            }
        }

        InvigilationDTO invigilationDTO = dtoConverter.toInvigilationDTO(invigilator);
        return Result.success(invigilationDTO);
    }

    /**
     * 导出监考任务安排（PDF）
     */
    @GetMapping("/invigilation/export")
    public ResponseEntity<byte[]> exportMyInvigilationSchedule(Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("导出教师监考安排: teacherId={}", teacherId);
        
        byte[] pdfData = examService.exportTeacherInvigilationSchedule(teacherId);
        
        String filename = "监考任务安排_" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename*=UTF-8''" + URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}

