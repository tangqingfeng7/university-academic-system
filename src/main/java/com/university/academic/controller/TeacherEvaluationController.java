package com.university.academic.controller;

import com.university.academic.entity.TeacherEvaluation;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.EvaluationStatisticsService;
import com.university.academic.service.TeacherEvaluationService;
import com.university.academic.vo.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教师评价控制器
 * 提供教师端评价查询接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/evaluations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherEvaluationController {

    private final TeacherEvaluationService teacherEvaluationService;
    private final EvaluationStatisticsService evaluationStatisticsService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * 查询我的教师评价统计
     */
    @GetMapping("/statistics")
    public Result<EvaluationStatisticsService.TeacherEvaluationStatisticsDTO> getMyStatistics(
            @RequestParam(required = false) Long semesterId,
            Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师评价统计: teacherId={}, semesterId={}", teacherId, semesterId);

        EvaluationStatisticsService.TeacherEvaluationStatisticsDTO statistics = 
                evaluationStatisticsService.getTeacherStatistics(teacherId, semesterId);

        return Result.success(statistics);
    }

    /**
     * 查询我的教师评价列表（匿名处理）
     */
    @GetMapping
    public Result<Page<TeacherEvaluationDTO>> getMyEvaluations(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询教师评价列表: teacherId={}, semesterId={}, page={}, size={}", 
                teacherId, semesterId, page, size);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TeacherEvaluation> evaluations;
        
        if (semesterId != null) {
            evaluations = teacherEvaluationService
                    .getTeacherEvaluationsBySemester(teacherId, semesterId, pageRequest);
        } else {
            evaluations = teacherEvaluationService.getTeacherEvaluations(teacherId, pageRequest);
        }

        // 转换为 DTO，匿名评价不显示学生信息
        Page<TeacherEvaluationDTO> dtoPage = evaluations.map(TeacherEvaluationDTO::fromEntity);

        return Result.success(dtoPage);
    }

    /**
     * 查询某门课程的教师评价
     */
    @GetMapping("/course/{offeringId}")
    public Result<List<TeacherEvaluationDTO>> getCourseEvaluations(
            @PathVariable Long offeringId,
            Authentication authentication) {
        Long teacherId = userDetailsService.getTeacherIdFromAuth(authentication);
        log.info("查询课程教师评价: teacherId={}, offeringId={}", teacherId, offeringId);

        List<TeacherEvaluation> evaluations = teacherEvaluationService
                .getEvaluationsByOffering(offeringId);

        // 转换为 DTO，匿名评价不显示学生信息
        List<TeacherEvaluationDTO> dtoList = evaluations.stream()
                .map(TeacherEvaluationDTO::fromEntity)
                .toList();

        return Result.success(dtoList);
    }
    
    /**
     * 教师评价 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherEvaluationDTO {
        private Long id;
        private Long studentId;
        private String studentName;
        private Long teacherId;
        private String teacherName;
        private Long courseOfferingId;
        private String courseName;
        private String semesterName;
        private Integer teachingRating;
        private Integer attitudeRating;
        private Integer contentRating;
        private String comment;
        private Boolean anonymous;
        private String status;
        private Boolean flagged;
        private String moderationNote;
        private LocalDateTime createdAt;

        public static TeacherEvaluationDTO fromEntity(TeacherEvaluation entity) {
            return TeacherEvaluationDTO.builder()
                    .id(entity.getId())
                    .studentId(entity.getAnonymous() ? null : entity.getStudent().getId())
                    .studentName(entity.getAnonymous() ? "匿名学生" : entity.getStudent().getName())
                    .teacherId(entity.getTeacher().getId())
                    .teacherName(entity.getTeacher().getName())
                    .courseOfferingId(entity.getCourseOffering().getId())
                    .courseName(entity.getCourseOffering().getCourse().getName())
                    .semesterName(entity.getCourseOffering().getSemester().getSemesterName())
                    .teachingRating(entity.getTeachingRating())
                    .attitudeRating(entity.getAttitudeRating())
                    .contentRating(entity.getContentRating())
                    .comment(entity.getComment())
                    .anonymous(entity.getAnonymous())
                    .status(entity.getStatus().getDescription())
                    .flagged(entity.getFlagged())
                    .moderationNote(entity.getModerationNote())
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
    }
}

