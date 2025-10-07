package com.university.academic.controller;

import com.university.academic.dto.CourseOfferingDTO;
import com.university.academic.dto.StudentDTO;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.CourseSelection;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.CourseOfferingService;
import com.university.academic.service.CourseSelectionService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师课程管理控制器
 * 提供教师查看授课班级和学生名单接口（教师端）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCourseController {

    private final CourseOfferingService offeringService;
    private final CourseSelectionService selectionService;
    private final DtoConverter dtoConverter;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 获取我的授课列表
     */
    @GetMapping
    public Result<List<CourseOfferingDTO>> getMyCourses(
            @RequestParam(required = false) Long semesterId,
            Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师授课列表: teacherId={}, semesterId={}", teacherId, semesterId);

        List<CourseOffering> offerings;
        if (semesterId != null) {
            offerings = offeringService.findByTeacherAndSemester(teacherId, semesterId);
        } else {
            offerings = offeringService.findByTeacher(teacherId);
        }

        List<CourseOfferingDTO> offeringDTOList = offerings.stream()
                .map(dtoConverter::toCourseOfferingDTO)
                .collect(Collectors.toList());

        return Result.success(offeringDTOList);
    }

    /**
     * 获取开课班级的学生名单
     */
    @GetMapping("/{offeringId}/students")
    public Result<List<StudentDTO>> getCourseStudents(@PathVariable Long offeringId) {
        log.info("查询开课班级学生名单: offeringId={}", offeringId);

        List<CourseSelection> selections = selectionService.findActiveByOffering(offeringId);
        List<StudentDTO> studentDTOList = selections.stream()
                .map(selection -> dtoConverter.toStudentDTO(selection.getStudent()))
                .collect(Collectors.toList());

        return Result.success(studentDTOList);
    }

    /**
     * 获取开课班级详情
     */
    @GetMapping("/{offeringId}")
    public Result<CourseOfferingDTO> getCourseDetail(@PathVariable Long offeringId) {
        log.info("查询开课班级详情: offeringId={}", offeringId);

        CourseOffering offering = offeringService.findById(offeringId);
        CourseOfferingDTO offeringDTO = dtoConverter.toCourseOfferingDTO(offering);

        return Result.success(offeringDTO);
    }
}

