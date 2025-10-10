package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseEvaluationRepository;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程评价服务类
 * 提供课程评价的提交、查询、修改等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseEvaluationService {

    private final CourseEvaluationRepository courseEvaluationRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final EvaluationPeriodService evaluationPeriodService;
    private final StudentService studentService;
    private final ContentModerationService contentModerationService;

    /**
     * 根据ID查询课程评价
     *
     * @param id 评价ID
     * @return 课程评价对象
     */
    @Transactional(readOnly = true)
    public CourseEvaluation findById(Long id) {
        return courseEvaluationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_NOT_FOUND));
    }

    /**
     * 提交课程评价
     *
     * @param studentId   学生ID
     * @param offeringId  开课计划ID
     * @param rating      评分（1-5星）
     * @param comment     评论
     * @param anonymous   是否匿名
     * @return 创建后的评价对象
     */
    @CacheEvict(value = CacheConfig.CACHE_EVALUATION_STATS, allEntries = true)
    @Transactional
    public CourseEvaluation submitEvaluation(Long studentId, Long offeringId,
                                            Integer rating, String comment, Boolean anonymous) {
        // 1. 验证评价周期是否开放
        evaluationPeriodService.validatePeriodActive();

        // 2. 验证学生是否存在
        Student student = studentService.findById(studentId);

        // 3. 验证开课计划是否存在
        CourseOffering offering = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        // 4. 验证学生是否选修了该课程
        if (!courseSelectionRepository.existsByStudentIdAndOfferingIdAndStatus(
                studentId, offeringId, CourseSelection.SelectionStatus.SELECTED)) {
            throw new BusinessException(ErrorCode.EVALUATION_NOT_ELIGIBLE);
        }

        // 5. 检查是否已评价过
        if (courseEvaluationRepository.existsByStudentIdAndCourseOfferingId(studentId, offeringId)) {
            throw new BusinessException(ErrorCode.EVALUATION_ALREADY_SUBMITTED);
        }

        // 6. 验证评分范围
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评分必须在1-5之间");
        }

        // 7. 内容审核
        ContentModerationService.ModerationResult moderationResult = 
            contentModerationService.moderateContent(comment);

        // 8. 创建评价
        CourseEvaluation evaluation = CourseEvaluation.builder()
                .student(student)
                .courseOffering(offering)
                .rating(rating)
                .comment(comment)
                .anonymous(anonymous != null ? anonymous : true)
                .status(EvaluationStatus.SUBMITTED)
                .semesterId(offering.getSemester().getId())
                .flagged(moderationResult.isFlagged())
                .moderationNote(moderationResult.getNote())
                .build();

        CourseEvaluation savedEvaluation = courseEvaluationRepository.save(evaluation);

        log.info("学生提交课程评价: studentId={}, offeringId={}, rating={}, anonymous={}",
                studentId, offeringId, rating, anonymous);

        return savedEvaluation;
    }

    /**
     * 修改课程评价
     *
     * @param studentId  学生ID
     * @param evaluationId 评价ID
     * @param rating     评分
     * @param comment    评论
     * @param anonymous  是否匿名
     * @return 更新后的评价对象
     */
    @CacheEvict(value = CacheConfig.CACHE_EVALUATION_STATS, allEntries = true)
    @Transactional
    public CourseEvaluation updateEvaluation(Long studentId, Long evaluationId,
                                            Integer rating, String comment, Boolean anonymous) {
        // 1. 验证评价周期是否开放
        evaluationPeriodService.validatePeriodActive();

        // 2. 查询评价
        CourseEvaluation evaluation = findById(evaluationId);

        // 3. 验证是否为该学生的评价
        if (!evaluation.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 4. 验证评分范围
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "评分必须在1-5之间");
        }

        // 5. 更新评价
        if (rating != null) {
            evaluation.setRating(rating);
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

        CourseEvaluation updatedEvaluation = courseEvaluationRepository.save(evaluation);

        log.info("学生修改课程评价: studentId={}, evaluationId={}", studentId, evaluationId);

        return updatedEvaluation;
    }

    /**
     * 查询学生的所有评价（分页）
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 评价分页数据
     */
    @Transactional(readOnly = true)
    public Page<CourseEvaluation> getStudentEvaluations(Long studentId, Pageable pageable) {
        return courseEvaluationRepository.findByStudentId(studentId, pageable);
    }

    /**
     * 查询学生可评价的课程列表
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 可评价的开课计划列表
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> getAvailableCoursesForEvaluation(Long studentId, Long semesterId) {
        // 查询学生未评价的课程ID列表
        List<Long> unevaluatedOfferingIds = courseEvaluationRepository
                .findUnevaluatedOfferingIds(studentId, semesterId);

        if (unevaluatedOfferingIds.isEmpty()) {
            return List.of();
        }

        // 查询开课计划详情
        return unevaluatedOfferingIds.stream()
                .map(id -> courseOfferingRepository.findById(id).orElse(null))
                .filter(offering -> offering != null)
                .collect(Collectors.toList());
    }

    /**
     * 查询某门课程的所有评价
     *
     * @param offeringId 开课计划ID
     * @return 评价列表
     */
    @Transactional(readOnly = true)
    public List<CourseEvaluation> getCourseEvaluations(Long offeringId) {
        return courseEvaluationRepository.findByOfferingId(offeringId);
    }

    /**
     * 查询某门课程的所有评价（带学生信息，管理员使用）
     *
     * @param offeringId 开课计划ID
     * @param pageable   分页参数
     * @return 评价分页数据
     */
    @Transactional(readOnly = true)
    public Page<CourseEvaluation> getCourseEvaluationsWithDetails(Long offeringId, Pageable pageable) {
        return courseEvaluationRepository.findByOfferingIdWithDetails(offeringId, pageable);
    }

    /**
     * 统计某门课程的评价数据
     *
     * @param offeringId 开课计划ID
     * @return 评价统计对象
     */
    @Cacheable(value = CacheConfig.CACHE_EVALUATION_STATS, key = "'course_' + #offeringId")
    @Transactional(readOnly = true)
    public EvaluationStatistics calculateCourseStatistics(Long offeringId) {
        // 验证开课计划存在
        CourseOffering offering = courseOfferingRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        // 计算平均分
        Double avgRating = courseEvaluationRepository.calculateAverageRating(offeringId);

        // 统计各星级数量
        long count1Star = courseEvaluationRepository.countByOfferingIdAndRating(offeringId, 1);
        long count2Star = courseEvaluationRepository.countByOfferingIdAndRating(offeringId, 2);
        long count3Star = courseEvaluationRepository.countByOfferingIdAndRating(offeringId, 3);
        long count4Star = courseEvaluationRepository.countByOfferingIdAndRating(offeringId, 4);
        long count5Star = courseEvaluationRepository.countByOfferingIdAndRating(offeringId, 5);

        // 统计总评价数和参与率
        long totalEvaluations = courseEvaluationRepository.countByCourseOfferingIdAndStatus(
                offeringId, EvaluationStatus.SUBMITTED);
        long totalStudents = offering.getEnrolled();
        double participationRate = totalStudents > 0 ? (double) totalEvaluations / totalStudents * 100 : 0;

        return EvaluationStatistics.builder()
                .offeringId(offeringId)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalEvaluations(totalEvaluations)
                .totalStudents(totalStudents)
                .participationRate(participationRate)
                .count1Star(count1Star)
                .count2Star(count2Star)
                .count3Star(count3Star)
                .count4Star(count4Star)
                .count5Star(count5Star)
                .build();
    }

    /**
     * 查询某学期的所有评价
     *
     * @param semesterId 学期ID
     * @param pageable   分页参数
     * @return 评价分页数据
     */
    @Transactional(readOnly = true)
    public Page<CourseEvaluation> getEvaluationsBySemester(Long semesterId, Pageable pageable) {
        return courseEvaluationRepository.findBySemesterId(semesterId, pageable);
    }

    /**
     * 删除评价（仅管理员）
     *
     * @param id 评价ID
     */
    @CacheEvict(value = CacheConfig.CACHE_EVALUATION_STATS, allEntries = true)
    @Transactional
    public void deleteEvaluation(Long id) {
        CourseEvaluation evaluation = findById(id);
        courseEvaluationRepository.delete(evaluation);
        log.info("删除课程评价: evaluationId={}", id);
    }

    /**
     * 评价统计数据类
     */
    @lombok.Data
    @lombok.Builder
    public static class EvaluationStatistics {
        private Long offeringId;
        private Double averageRating;
        private Long totalEvaluations;
        private Long totalStudents;
        private Double participationRate;
        private Long count1Star;
        private Long count2Star;
        private Long count3Star;
        private Long count4Star;
        private Long count5Star;
    }
}

