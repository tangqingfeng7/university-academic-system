package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.dto.CreateGraduationRequirementRequest;
import com.university.academic.dto.GraduationRequirementDTO;
import com.university.academic.dto.UpdateGraduationRequirementRequest;
import com.university.academic.dto.converter.GraduationRequirementConverter;
import com.university.academic.entity.GraduationRequirement;
import com.university.academic.entity.Major;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.GraduationRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 毕业要求服务类
 * 提供毕业要求的创建、查询、更新、删除功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraduationRequirementService {

    private final GraduationRequirementRepository graduationRequirementRepository;
    private final GraduationRequirementConverter graduationRequirementConverter;
    private final MajorService majorService;

    /**
     * 创建毕业要求
     *
     * @param request 创建请求
     * @return 毕业要求DTO
     */
    @CacheEvict(value = CacheConfig.CACHE_GRADUATION_REQUIREMENTS, allEntries = true)
    @Transactional
    public GraduationRequirementDTO createRequirement(CreateGraduationRequirementRequest request) {
        log.info("创建毕业要求: majorId={}, enrollmentYear={}", 
                request.getMajorId(), request.getEnrollmentYear());

        // 验证专业是否存在
        Major major = majorService.findById(request.getMajorId());

        // 检查是否已存在相同专业和入学年份的记录
        if (graduationRequirementRepository.existsByMajorIdAndEnrollmentYear(
                request.getMajorId(), request.getEnrollmentYear())) {
            throw new BusinessException(ErrorCode.GRADUATION_REQUIREMENT_ALREADY_EXISTS);
        }

        // 验证学分要求的合理性
        validateCreditRequirements(request.getTotalCreditsRequired(),
                request.getRequiredCreditsRequired(),
                request.getElectiveCreditsRequired(),
                request.getPracticalCreditsRequired());

        // 转换并保存
        GraduationRequirement requirement = graduationRequirementConverter.toEntity(request, major);
        requirement = graduationRequirementRepository.save(requirement);

        log.info("毕业要求创建成功: id={}", requirement.getId());
        return graduationRequirementConverter.toDTO(requirement);
    }

    /**
     * 根据ID查询毕业要求
     *
     * @param id 毕业要求ID
     * @return 毕业要求DTO
     */
    @Cacheable(value = CacheConfig.CACHE_GRADUATION_REQUIREMENTS, key = "'id:' + #id")
    @Transactional(readOnly = true)
    public GraduationRequirementDTO findById(Long id) {
        log.debug("查询毕业要求: id={}", id);
        
        GraduationRequirement requirement = graduationRequirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADUATION_REQUIREMENT_NOT_FOUND));

        return graduationRequirementConverter.toDTO(requirement);
    }

    /**
     * 根据专业ID和入学年份查询毕业要求
     *
     * @param majorId        专业ID
     * @param enrollmentYear 入学年份
     * @return 毕业要求DTO
     */
    @Cacheable(value = CacheConfig.CACHE_GRADUATION_REQUIREMENTS, 
               key = "'major:' + #majorId + ':year:' + #enrollmentYear")
    @Transactional(readOnly = true)
    public GraduationRequirementDTO findByMajorAndYear(Long majorId, Integer enrollmentYear) {
        log.debug("查询毕业要求: majorId={}, enrollmentYear={}", majorId, enrollmentYear);

        GraduationRequirement requirement = graduationRequirementRepository
                .findByMajorIdAndEnrollmentYearWithDetails(majorId, enrollmentYear)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADUATION_REQUIREMENT_NOT_FOUND));

        return graduationRequirementConverter.toDTO(requirement);
    }

    /**
     * 根据专业ID查询所有年份的毕业要求
     *
     * @param majorId 专业ID
     * @return 毕业要求DTO列表
     */
    @Cacheable(value = CacheConfig.CACHE_GRADUATION_REQUIREMENTS, key = "'major:' + #majorId")
    @Transactional(readOnly = true)
    public List<GraduationRequirementDTO> findByMajorId(Long majorId) {
        log.debug("查询专业的所有毕业要求: majorId={}", majorId);

        // 验证专业是否存在
        majorService.findById(majorId);

        List<GraduationRequirement> requirements = graduationRequirementRepository
                .findByMajorIdWithDetails(majorId);

        return graduationRequirementConverter.toDTOList(requirements);
    }

    /**
     * 查询所有毕业要求
     *
     * @return 毕业要求DTO列表
     */
    @Cacheable(value = CacheConfig.CACHE_GRADUATION_REQUIREMENTS, key = "'all'")
    @Transactional(readOnly = true)
    public List<GraduationRequirementDTO> findAll() {
        log.debug("查询所有毕业要求");

        List<GraduationRequirement> requirements = graduationRequirementRepository
                .findAllWithDetails();

        return graduationRequirementConverter.toDTOList(requirements);
    }

    /**
     * 更新毕业要求
     *
     * @param id      毕业要求ID
     * @param request 更新请求
     * @return 毕业要求DTO
     */
    @CacheEvict(value = CacheConfig.CACHE_GRADUATION_REQUIREMENTS, allEntries = true)
    @Transactional
    public GraduationRequirementDTO updateRequirement(Long id, UpdateGraduationRequirementRequest request) {
        log.info("更新毕业要求: id={}", id);

        // 查询现有记录
        GraduationRequirement requirement = graduationRequirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADUATION_REQUIREMENT_NOT_FOUND));

        // 验证学分要求的合理性
        validateCreditRequirements(request.getTotalCreditsRequired(),
                request.getRequiredCreditsRequired(),
                request.getElectiveCreditsRequired(),
                request.getPracticalCreditsRequired());

        // 更新字段
        requirement.setTotalCreditsRequired(request.getTotalCreditsRequired());
        requirement.setRequiredCreditsRequired(request.getRequiredCreditsRequired());
        requirement.setElectiveCreditsRequired(request.getElectiveCreditsRequired());
        requirement.setPracticalCreditsRequired(request.getPracticalCreditsRequired());
        requirement.setAdditionalRequirements(request.getAdditionalRequirements());

        // 保存
        requirement = graduationRequirementRepository.save(requirement);

        log.info("毕业要求更新成功: id={}", id);
        return graduationRequirementConverter.toDTO(requirement);
    }

    /**
     * 删除毕业要求
     *
     * @param id 毕业要求ID
     */
    @CacheEvict(value = CacheConfig.CACHE_GRADUATION_REQUIREMENTS, allEntries = true)
    @Transactional
    public void deleteRequirement(Long id) {
        log.info("删除毕业要求: id={}", id);

        // 检查是否存在
        if (!graduationRequirementRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.GRADUATION_REQUIREMENT_NOT_FOUND);
        }

        // 删除
        graduationRequirementRepository.deleteById(id);

        log.info("毕业要求删除成功: id={}", id);
    }

    /**
     * 验证学分要求的合理性
     * 总学分必须大于等于各类学分之和
     *
     * @param totalCredits    总学分
     * @param requiredCredits 必修学分
     * @param electiveCredits 选修学分
     * @param practicalCredits 实践学分
     */
    private void validateCreditRequirements(Double totalCredits, Double requiredCredits,
                                           Double electiveCredits, Double practicalCredits) {
        // 计算各类学分之和
        double sum = requiredCredits + electiveCredits + practicalCredits;

        // 验证总学分是否大于等于各类学分之和
        if (totalCredits < sum) {
            String message = String.format("总学分要求(%.1f)不能小于各类学分之和(%.1f)", totalCredits, sum);
            log.warn("学分要求验证失败: {}", message);
            throw new BusinessException(ErrorCode.INVALID_CREDIT_REQUIREMENTS, message);
        }
    }

    /**
     * 根据实体查询毕业要求（内部使用）
     *
     * @param id 毕业要求ID
     * @return 毕业要求实体
     */
    @Transactional(readOnly = true)
    public GraduationRequirement findEntityById(Long id) {
        return graduationRequirementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADUATION_REQUIREMENT_NOT_FOUND));
    }
}

