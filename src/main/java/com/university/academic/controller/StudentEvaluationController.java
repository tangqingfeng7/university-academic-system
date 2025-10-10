package com.university.academic.controller;

import com.university.academic.annotation.OperationLog;
import com.university.academic.controller.dto.AvailableCourseDTO;
import com.university.academic.controller.dto.CourseEvaluationDTO;
import com.university.academic.controller.dto.TeacherEvaluationDTO;
import com.university.academic.entity.CourseEvaluation;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.TeacherEvaluation;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.CourseEvaluationService;
import com.university.academic.service.TeacherEvaluationService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生评价控制器
 * 提供学生端评价相关接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/student/evaluations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentEvaluationController {

    private final CourseEvaluationService courseEvaluationService;
    private final TeacherEvaluationService teacherEvaluationService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 查询可评价的课程列表
     */
    @GetMapping("/available")
    public Result<List<AvailableCourseDTO>> getAvailableCoursesForEvaluation(
            @RequestParam Long semesterId,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询可评价课程: studentId={}, semesterId={}", studentId, semesterId);

        List<CourseOffering> offerings = courseEvaluationService
                .getAvailableCoursesForEvaluation(studentId, semesterId);
        
        List<AvailableCourseDTO> dtos = offerings.stream()
                .map(AvailableCourseDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());

        return Result.success(dtos);
    }

    /**
     * 提交课程评价
     */
    @PostMapping("/course")
    @OperationLog(value = "提交课程评价", type = "CREATE")
    public Result<CourseEvaluationDTO> submitCourseEvaluation(
            @RequestBody CourseEvaluationRequest request,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("提交课程评价: studentId={}, offeringId={}", studentId, request.getOfferingId());

        CourseEvaluation evaluation = courseEvaluationService.submitEvaluation(
                studentId,
                request.getOfferingId(),
                request.getRating(),
                request.getComment(),
                request.getAnonymous()
        );

        return Result.success(CourseEvaluationDTO.fromEntity(evaluation));
    }

    /**
     * 修改课程评价
     */
    @PutMapping("/course/{evaluationId}")
    @OperationLog(value = "修改课程评价", type = "UPDATE")
    public Result<CourseEvaluationDTO> updateCourseEvaluation(
            @PathVariable Long evaluationId,
            @RequestBody CourseEvaluationRequest request,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("修改课程评价: studentId={}, evaluationId={}", studentId, evaluationId);

        CourseEvaluation evaluation = courseEvaluationService.updateEvaluation(
                studentId,
                evaluationId,
                request.getRating(),
                request.getComment(),
                request.getAnonymous()
        );

        return Result.success(CourseEvaluationDTO.fromEntity(evaluation));
    }

    /**
     * 查询我的课程评价列表
     */
    @GetMapping("/course")
    public Result<Page<CourseEvaluationDTO>> getMyCourseEvaluations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询我的课程评价: studentId={}, page={}, size={}", studentId, page, size);

        PageRequest pageRequest = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CourseEvaluation> evaluations = courseEvaluationService
                .getStudentEvaluations(studentId, pageRequest);

        return Result.success(evaluations.map(CourseEvaluationDTO::fromEntity));
    }

    /**
     * 提交教师评价
     */
    @PostMapping("/teacher")
    @OperationLog(value = "提交教师评价", type = "CREATE")
    public Result<TeacherEvaluationDTO> submitTeacherEvaluation(
            @RequestBody TeacherEvaluationRequest request,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("提交教师评价: studentId={}, teacherId={}", studentId, request.getTeacherId());

        TeacherEvaluation evaluation = teacherEvaluationService.submitEvaluation(
                studentId,
                request.getTeacherId(),
                request.getOfferingId(),
                request.getTeachingRating(),
                request.getAttitudeRating(),
                request.getContentRating(),
                request.getComment(),
                request.getAnonymous()
        );

        return Result.success(TeacherEvaluationDTO.fromEntity(evaluation));
    }

    /**
     * 修改教师评价
     */
    @PutMapping("/teacher/{evaluationId}")
    @OperationLog(value = "修改教师评价", type = "UPDATE")
    public Result<TeacherEvaluationDTO> updateTeacherEvaluation(
            @PathVariable Long evaluationId,
            @RequestBody TeacherEvaluationRequest request,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("修改教师评价: studentId={}, evaluationId={}", studentId, evaluationId);

        TeacherEvaluation evaluation = teacherEvaluationService.updateEvaluation(
                studentId,
                evaluationId,
                request.getTeachingRating(),
                request.getAttitudeRating(),
                request.getContentRating(),
                request.getComment(),
                request.getAnonymous()
        );

        return Result.success(TeacherEvaluationDTO.fromEntity(evaluation));
    }

    /**
     * 查询我的教师评价列表
     */
    @GetMapping("/teacher")
    public Result<Page<TeacherEvaluationDTO>> getMyTeacherEvaluations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("查询我的教师评价: studentId={}, page={}, size={}", studentId, page, size);

        PageRequest pageRequest = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TeacherEvaluation> evaluations = teacherEvaluationService
                .getStudentEvaluations(studentId, pageRequest);

        return Result.success(evaluations.map(TeacherEvaluationDTO::fromEntity));
    }

    /**
     * 课程评价请求DTO
     */
    @lombok.Data
    public static class CourseEvaluationRequest {
        private Long offeringId;
        private Integer rating;
        private String comment;
        private Boolean anonymous;
    }

    /**
     * 教师评价请求DTO
     */
    @lombok.Data
    public static class TeacherEvaluationRequest {
        private Long teacherId;
        private Long offeringId;
        private Integer teachingRating;
        private Integer attitudeRating;
        private Integer contentRating;
        private String comment;
        private Boolean anonymous;
    }
}

