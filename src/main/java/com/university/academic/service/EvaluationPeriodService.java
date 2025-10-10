package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.EvaluationPeriod;
import com.university.academic.entity.Semester;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.EvaluationPeriodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价周期服务类
 * 提供评价周期的创建、管理、查询等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationPeriodService {

    private final EvaluationPeriodRepository evaluationPeriodRepository;
    private final SemesterService semesterService;

    /**
     * 根据ID查询评价周期
     *
     * @param id 评价周期ID
     * @return 评价周期对象
     */
    @Cacheable(value = CacheConfig.CACHE_EVALUATION_PERIODS, key = "#id")
    @Transactional(readOnly = true)
    public EvaluationPeriod findById(Long id) {
        return evaluationPeriodRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVALUATION_PERIOD_NOT_FOUND));
    }

    /**
     * 创建评价周期
     *
     * @param semesterId  学期ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param description 描述
     * @return 创建后的评价周期对象
     */
    @CacheEvict(value = {CacheConfig.CACHE_EVALUATION_PERIODS, CacheConfig.CACHE_ACTIVE_EVALUATION_PERIOD}, allEntries = true)
    @Transactional
    public EvaluationPeriod createPeriod(Long semesterId, LocalDateTime startTime, 
                                        LocalDateTime endTime, String description) {
        // 验证学期是否存在
        Semester semester = semesterService.findById(semesterId);

        // 验证时间范围
        if (endTime.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.EVALUATION_PERIOD_CONFLICT);
        }

        // 检查该学期是否已存在评价周期
        if (evaluationPeriodRepository.findBySemesterId(semesterId).isPresent()) {
            throw new BusinessException(ErrorCode.EVALUATION_PERIOD_ALREADY_EXISTS);
        }

        // 创建评价周期
        EvaluationPeriod period = EvaluationPeriod.builder()
                .semester(semester)
                .startTime(startTime)
                .endTime(endTime)
                .description(description)
                .active(false)
                .build();

        EvaluationPeriod savedPeriod = evaluationPeriodRepository.save(period);
        log.info("创建评价周期成功: {} - {} ({})",
                savedPeriod.getId(),
                semester.getSemesterName(),
                savedPeriod.getActive() ? "活跃" : "非活跃");

        return savedPeriod;
    }

    /**
     * 更新评价周期
     *
     * @param id          评价周期ID
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param description 描述
     * @return 更新后的评价周期对象
     */
    @CacheEvict(value = {CacheConfig.CACHE_EVALUATION_PERIODS, CacheConfig.CACHE_ACTIVE_EVALUATION_PERIOD}, allEntries = true)
    @Transactional
    public EvaluationPeriod updatePeriod(Long id, LocalDateTime startTime,
                                        LocalDateTime endTime, String description) {
        EvaluationPeriod period = findById(id);

        // 验证时间范围
        if (endTime.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.EVALUATION_PERIOD_CONFLICT);
        }

        // 更新字段
        if (startTime != null) {
            period.setStartTime(startTime);
        }
        if (endTime != null) {
            period.setEndTime(endTime);
        }
        if (description != null) {
            period.setDescription(description);
        }

        EvaluationPeriod updatedPeriod = evaluationPeriodRepository.save(period);
        log.info("更新评价周期成功: {}", updatedPeriod.getId());

        return updatedPeriod;
    }

    /**
     * 开启/关闭评价周期
     *
     * @param id     评价周期ID
     * @param active 是否活跃
     * @return 更新后的评价周期对象
     */
    @CacheEvict(value = {CacheConfig.CACHE_EVALUATION_PERIODS, CacheConfig.CACHE_ACTIVE_EVALUATION_PERIOD}, allEntries = true)
    @Transactional
    public EvaluationPeriod togglePeriod(Long id, Boolean active) {
        EvaluationPeriod period = findById(id);

        if (active) {
            // 开启周期前，先关闭所有其他活跃的周期
            List<EvaluationPeriod> activePeriods = evaluationPeriodRepository.findAllActive();
            for (EvaluationPeriod activePeriod : activePeriods) {
                if (!activePeriod.getId().equals(id)) {
                    activePeriod.setActive(false);
                    evaluationPeriodRepository.save(activePeriod);
                    log.info("关闭评价周期: {}", activePeriod.getId());
                }
            }
        }

        period.setActive(active);
        EvaluationPeriod updatedPeriod = evaluationPeriodRepository.save(period);

        log.info("{}评价周期: {} - {}",
                active ? "开启" : "关闭",
                updatedPeriod.getId(),
                updatedPeriod.getSemester().getSemesterName());

        return updatedPeriod;
    }

    /**
     * 查询当前活跃的评价周期
     *
     * @return 评价周期对象，如果不存在则返回null
     */
    @Cacheable(value = CacheConfig.CACHE_ACTIVE_EVALUATION_PERIOD, key = "'current'")
    @Transactional(readOnly = true)
    public EvaluationPeriod findActivePeriod() {
        return evaluationPeriodRepository.findActiveByDate(LocalDateTime.now())
                .orElse(null);
    }

    /**
     * 检查当前是否有活跃的评价周期
     *
     * @return true-有活跃周期，false-无活跃周期
     */
    @Transactional(readOnly = true)
    public boolean hasActivePeriod() {
        return evaluationPeriodRepository.findActiveByDate(LocalDateTime.now()).isPresent();
    }

    /**
     * 查询所有评价周期
     *
     * @return 评价周期列表（按开始时间倒序）
     */
    @Transactional(readOnly = true)
    public List<EvaluationPeriod> findAll() {
        return evaluationPeriodRepository.findAllOrderByStartTimeDesc();
    }

    /**
     * 查询某学期的评价周期
     *
     * @param semesterId 学期ID
     * @return 评价周期对象，如果不存在则返回null
     */
    @Transactional(readOnly = true)
    public EvaluationPeriod findBySemesterId(Long semesterId) {
        return evaluationPeriodRepository.findBySemesterId(semesterId)
                .orElse(null);
    }

    /**
     * 查询某学期的所有评价周期
     *
     * @param semesterId 学期ID
     * @return 评价周期列表
     */
    @Transactional(readOnly = true)
    public List<EvaluationPeriod> findAllBySemesterId(Long semesterId) {
        return evaluationPeriodRepository.findAllBySemesterId(semesterId);
    }

    /**
     * 删除评价周期
     *
     * @param id 评价周期ID
     */
    @CacheEvict(value = {CacheConfig.CACHE_EVALUATION_PERIODS, CacheConfig.CACHE_ACTIVE_EVALUATION_PERIOD}, allEntries = true)
    @Transactional
    public void deletePeriod(Long id) {
        EvaluationPeriod period = findById(id);

        // 如果是活跃周期，不允许删除
        if (period.getActive()) {
            throw new BusinessException(ErrorCode.ACTIVE_PERIOD_ALREADY_EXISTS);
        }

        evaluationPeriodRepository.delete(period);
        log.info("删除评价周期成功: {}", period.getId());
    }

    /**
     * 验证评价周期是否开放
     * 如果未开放或已过期，抛出异常
     */
    @Transactional(readOnly = true)
    public void validatePeriodActive() {
        EvaluationPeriod period = findActivePeriod();
        
        if (period == null) {
            throw new BusinessException(ErrorCode.EVALUATION_PERIOD_NOT_ACTIVE);
        }

        if (period.isExpired()) {
            throw new BusinessException(ErrorCode.EVALUATION_PERIOD_EXPIRED);
        }

        if (!period.isInPeriod()) {
            throw new BusinessException(ErrorCode.EVALUATION_PERIOD_NOT_ACTIVE);
        }
    }

    /**
     * 统计评价周期数量
     *
     * @return 评价周期总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return evaluationPeriodRepository.count();
    }
}

