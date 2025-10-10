package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.TeacherEvaluationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 教师评价服务类
 * 提供教师评价的提交、查询、修改等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherEvaluationService {

    private final TeacherEvaluationRepository teacherEvaluationRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final EvaluationPeriodService evaluationPeriodService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ContentModerationService contentModerationService;

    /**
     * 根据ID查询教师评价
     *
     * @param id 评价ID
     * @return 教师评价对象
     */
    @Transactional(readOnly = true)
    public TeacherEvaluation findById(Long id) {
        return teacherEvaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
    }

    /**
     * 提交教师评价
     *
     * @param studentId       学生ID
     * @param teacherId       教师ID
     * @param offeringId      开课计划ID
     * @param teachingRating  教学能力评分
     * @param attitudeRating  教学态度评分
     * @param contentRating   教学内容评分
     * @param comment         评论
     * @param anonymous       是否匿名
     * @return 创建后的评价对象
     */
    @CacheEvict(value = CacheConfig.CACHE_EVALUATION_STATS, allEntries = true)
    @Transactional
    public TeacherEvaluation submitEvaluation(Long studentId, Long teacherId, Long offeringId,
                                             Integer teachingRating, Integer attitudeRating,
                                             Integer contentRating, String comment, Boolean anonymous) {
        // 1. 验证评价周期是否开放
        evaluationPeriodService.validatePeriodActive();

        // 2. 验证学生和教师是否存在
        Student student = studentService.findById(studentId);
        Teacher teacher = teacherService.findById(teacherId);

        // 3. 验证开课计划是否存在
        CourseOffering offering = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        // 4. 验证教师是否为该课程的授课教师
        if (!offering.getTeacher().getId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_ELIGIBLE, "该教师未授课此课程");
        }

        // 5. 验证学生是否选修了该课程
        if (!courseSelectionRepository.existsByStudentIdAndOfferingIdAndStatus(
                studentId, offeringId, CourseSelection.SelectionStatus.SELECTED)) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_ELIGIBLE);
        }

        // 6. 检查是否已评价过
        if (teacherEvaluationRepository.existsByStudentIdAndTeacherIdAndCourseOfferingId(
                studentId, teacherId, offeringId)) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_SUBMITTED);
        }

        // 7. 验证评分范围
        validateRatings(teachingRating, attitudeRating, contentRating);

        // 8. 内容审核
        ContentModerationService.ModerationResult moderationResult = 
            contentModerationService.moderateContent(comment);

        // 9. 创建评价
        TeacherEvaluation evaluation = TeacherEvaluation.builder()
                .student(student)
                .teacher(teacher)
                .courseOffering(offering)
                .teachingRating(teachingRating)
                .attitudeRating(attitudeRating)
                .contentRating(contentRating)
                .comment(comment)
                .anonymous(anonymous != null ? anonymous : true)
                .status(EvaluationStatus.SUBMITTED)
                .flagged(moderationResult.isFlagged())
                .moderationNote(moderationResult.getNote())
                .build();

        TeacherEvaluation savedEvaluation = teacherEvaluationRepository.save(evaluation);

        log.info("学生提交教师评价: studentId={}, teacherId={}, offeringId={}, anonymous={}",
                studentId, teacherId, offeringId, anonymous);

        return savedEvaluation;
    }

    /**
     * 修改教师评价
     *
     * @param studentId      学生ID
     * @param evaluationId   评价ID
     * @param teachingRating 教学能力评分
     * @param attitudeRating 教学态度评分
     * @param contentRating  教学内容评分
     * @param comment        评论
     * @param anonymous      是否匿名
     * @return 更新后的评价对象
     */
    @CacheEvict(value = CacheConfig.CACHE_EVALUATION_STATS, allEntries = true)
    @Transactional
    public TeacherEvaluation updateEvaluation(Long studentId, Long evaluationId,
                                             Integer teachingRating, Integer attitudeRating,
                                             Integer contentRating, String comment, Boolean anonymous) {
        // 1. 验证评价周期是否开放
        evaluationPeriodService.validatePeriodActive();

        // 2. 查询评价
        TeacherEvaluation evaluation = findById(evaluationId);

        // 3. 验证是否为该学生的评价
        if (!evaluation.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 4. 验证评分范围
        if (teachingRating != null || attitudeRating != null || contentRating != null) {
            validateRatings(
                    teachingRating != null ? teachingRating : evaluation.getTeachingRating(),
                    attitudeRating != null ? attitudeRating : evaluation.getAttitudeRating(),
                    contentRating != null ? contentRating : evaluation.getContentRating()
            );
        }

        // 5. 更新评价
        if (teachingRating != null) {
            evaluation.setTeachingRating(teachingRating);
        }
        if (attitudeRating != null) {
            evaluation.setAttitudeRating(attitudeRating);
        }
        if (contentRating != null) {
            evaluation.setContentRating(contentRating);
        }
        if (comment != null) {
            evaluation.setComment(comment);
            // 重新审核内容
            ContentModerationService.ModerationResult moderationResult = 
                contentModerationService.moderateContent(comment);
            evaluation.setFlagged(moderationResult.isFlagged());
            evaluation.setModerationNote(moderationResult.getNote());
        }
        if (anonymous != null) {
            evaluation.setAnonymous(anonymous);
        }

        TeacherEvaluation updatedEvaluation = teacherEvaluationRepository.save(evaluation);

        log.info("学生修改教师评价: studentId={}, evaluationId={}", studentId, evaluationId);

        return updatedEvaluation;
    }

    /**
     * 查询学生的所有教师评价（分页）
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Transactional(readOnly = true)
    public Page<TeacherEvaluation> getStudentEvaluations(Long studentId, Pageable pageable) {
        return teacherEvaluationRepository.findByStudentId(studentId, pageable);
    }

    /**
     * 查询某位教师的所有评价
     *
     * @param teacherId 教师ID
     * @return 评价列表
     */
    @Transactional(readOnly = true)
    public List<TeacherEvaluation> getTeacherEvaluations(Long teacherId) {
        return teacherEvaluationRepository.findByTeacherId(teacherId);
    }

    /**
     * 查询某位教师的所有评价（分页）
     *
     * @param teacherId 教师ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Transactional(readOnly = true)
    public Page<TeacherEvaluation> getTeacherEvaluations(Long teacherId, Pageable pageable) {
        Page<TeacherEvaluation> page = teacherEvaluationRepository.findByTeacherId(teacherId, pageable);
        // 在事务内强制加载关联对象，避免 LazyInitializationException
        page.getContent().forEach(te -> {
            if (te.getStudent() != null) {
                te.getStudent().getName(); // 触发加载
            }
            te.getTeacher().getName();
            te.getCourseOffering().getCourse().getName();
            te.getCourseOffering().getSemester().getSemesterName();
        });
        return page;
    }

    /**
     * 查询某位教师在某学期的所有评价
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 评价列表
     */
    @Transactional(readOnly = true)
    public List<TeacherEvaluation> getTeacherEvaluationsBySemester(Long teacherId, Long semesterId) {
        return teacherEvaluationRepository.findByTeacherIdAndSemesterId(teacherId, semesterId);
    }

    /**
     * 查询某位教师在某学期的所有评价（分页）
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @param pageable   分页参数
     * @return 评价分页数据
     */
    @Transactional(readOnly = true)
    public Page<TeacherEvaluation> getTeacherEvaluationsBySemester(Long teacherId, Long semesterId, Pageable pageable) {
        Page<TeacherEvaluation> page = teacherEvaluationRepository.findByTeacherIdAndSemesterId(teacherId, semesterId, pageable);
        // 在事务内强制加载关联对象，避免 LazyInitializationException
        page.getContent().forEach(te -> {
            if (te.getStudent() != null) {
                te.getStudent().getName(); // 触发加载
            }
            te.getTeacher().getName();
            te.getCourseOffering().getCourse().getName();
            te.getCourseOffering().getSemester().getSemesterName();
        });
        return page;
    }

    /**
     * 查询某位教师的所有评价（带学生信息，管理员使用）
     *
     * @param teacherId 教师ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Transactional(readOnly = true)
    public Page<TeacherEvaluation> getTeacherEvaluationsWithDetails(Long teacherId, Pageable pageable) {
        return teacherEvaluationRepository.findByTeacherIdWithDetails(teacherId, pageable);
    }

    /**
     * 统计某位教师的评价数据
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID（可选）
     * @return 评价统计对象
     */
    @Cacheable(value = CacheConfig.CACHE_EVALUATION_STATS, 
               key = "'teacher_' + #teacherId + '_' + (#semesterId != null ? #semesterId : 'all')")
    @Transactional(readOnly = true)
    public TeacherEvaluationStatistics calculateTeacherStatistics(Long teacherId, Long semesterId) {
        // 验证教师存在
        teacherService.findById(teacherId);

        // 计算各项平均分
        Double avgTeachingRating = teacherEvaluationRepository.calculateAverageTeachingRating(teacherId);
        Double avgAttitudeRating = teacherEvaluationRepository.calculateAverageAttitudeRating(teacherId);
        Double avgContentRating = teacherEvaluationRepository.calculateAverageContentRating(teacherId);

        // 统计评价数量
        long totalEvaluations = teacherEvaluationRepository.countByTeacherIdAndStatus(
                teacherId, EvaluationStatus.SUBMITTED);

        // 计算综合平均分
        Double overallRating = 0.0;
        if (avgTeachingRating != null && avgAttitudeRating != null && avgContentRating != null) {
            overallRating = (avgTeachingRating + avgAttitudeRating + avgContentRating) / 3.0;
        }

        return TeacherEvaluationStatistics.builder()
                .teacherId(teacherId)
                .averageTeachingRating(avgTeachingRating != null ? avgTeachingRating : 0.0)
                .averageAttitudeRating(avgAttitudeRating != null ? avgAttitudeRating : 0.0)
                .averageContentRating(avgContentRating != null ? avgContentRating : 0.0)
                .overallRating(overallRating)
                .totalEvaluations(totalEvaluations)
                .build();
    }

    /**
     * 查询某门课程的教师评价
     *
     * @param offeringId 开课计划ID
     * @return 评价列表
     */
    @Transactional(readOnly = true)
    public List<TeacherEvaluation> getEvaluationsByOffering(Long offeringId) {
        return teacherEvaluationRepository.findByOfferingId(offeringId);
    }

    /**
     * 删除评价（仅管理员）
     *
     * @param id 评价ID
     */
    @CacheEvict(value = CacheConfig.CACHE_EVALUATION_STATS, allEntries = true)
    @Transactional
    public void deleteEvaluation(Long id) {
        TeacherEvaluation evaluation = findById(id);
        teacherEvaluationRepository.delete(evaluation);
        log.info("删除教师评价: evaluationId={}", id);
    }

    /**
     * 验证评分范围
     */
    private void validateRatings(Integer teachingRating, Integer attitudeRating, Integer contentRating) {
        if (teachingRating < 1 || teachingRating > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "教学能力评分必须在1-5之间");
        }
        if (attitudeRating < 1 || attitudeRating > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "教学态度评分必须在1-5之间");
        }
        if (contentRating < 1 || contentRating > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "教学内容评分必须在1-5之间");
        }
    }

    /**
     * 教师评价统计数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class TeacherEvaluationStatistics {
        private Long teacherId;
        private Double averageTeachingRating;
        private Double averageAttitudeRating;
        private Double averageContentRating;
        private Double overallRating;
        private Long totalEvaluations;
    }
}

