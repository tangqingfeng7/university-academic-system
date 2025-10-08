package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.Department;
import com.university.academic.entity.Major;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.MajorRepository;
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
 * 专业服务类
 * 提供专业CRUD等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MajorService {

    private final MajorRepository majorRepository;
    private final DepartmentService departmentService;

    /**
     * 根据ID查询专业
     *
     * @param id 专业ID
     * @return 专业对象
     */
    @Cacheable(value = CacheConfig.CACHE_MAJORS, key = "#id")
    @Transactional(readOnly = true)
    public Major findById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAJOR_NOT_FOUND));
    }

    /**
     * 根据代码查询专业
     *
     * @param code 专业代码
     * @return 专业对象
     */
    @Transactional(readOnly = true)
    public Major findByCode(String code) {
        return majorRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.MAJOR_NOT_FOUND));
    }

    /**
     * 查询所有专业
     *
     * @return 专业列表
     */
    @Transactional(readOnly = true)
    public List<Major> findAll() {
        return majorRepository.findAllWithDepartment();
    }

    /**
     * 分页查询专业
     *
     * @param pageable 分页参数
     * @return 专业分页数据
     */
    @Transactional(readOnly = true)
    public Page<Major> findAll(Pageable pageable) {
        return majorRepository.findAllWithDepartment(pageable);
    }

    /**
     * 根据院系ID查询专业列表
     *
     * @param departmentId 院系ID
     * @return 专业列表
     */
    @Transactional(readOnly = true)
    public List<Major> findByDepartmentId(Long departmentId) {
        return majorRepository.findByDepartmentId(departmentId);
    }

    /**
     * 创建专业
     *
     * @param major 专业对象
     * @return 创建后的专业对象
     */
    @CacheEvict(value = CacheConfig.CACHE_MAJORS, allEntries = true)
    @Transactional
    public Major createMajor(Major major) {
        // 检查专业代码是否已存在
        if (majorRepository.existsByCode(major.getCode())) {
            throw new BusinessException(ErrorCode.MAJOR_CODE_ALREADY_EXISTS);
        }

        // 验证院系是否存在
        if (major.getDepartment() != null && major.getDepartment().getId() != null) {
            Department department = departmentService.findById(major.getDepartment().getId());
            major.setDepartment(department);
        }

        Major savedMajor = majorRepository.save(major);
        log.info("创建专业成功: {} - {} (院系: {})",
                savedMajor.getCode(),
                savedMajor.getName(),
                savedMajor.getDepartment().getName());
        return savedMajor;
    }

    /**
     * 更新专业信息
     *
     * @param id    专业ID
     * @param major 更新的专业信息
     * @return 更新后的专业对象
     */
    @CacheEvict(value = CacheConfig.CACHE_MAJORS, allEntries = true)
    @Transactional
    public Major updateMajor(Long id, Major major) {
        Major existingMajor = findById(id);

        // 如果要修改专业代码，检查新代码是否已存在
        if (major.getCode() != null && !existingMajor.getCode().equals(major.getCode())) {
            if (majorRepository.existsByCode(major.getCode())) {
                throw new BusinessException(ErrorCode.MAJOR_CODE_ALREADY_EXISTS);
            }
            existingMajor.setCode(major.getCode());
        }

        // 更新其他字段
        if (major.getName() != null) {
            existingMajor.setName(major.getName());
        }

        // 更新院系
        if (major.getDepartment() != null && major.getDepartment().getId() != null) {
            Department department = departmentService.findById(major.getDepartment().getId());
            existingMajor.setDepartment(department);
        }

        Major updatedMajor = majorRepository.save(existingMajor);
        log.info("更新专业成功: {} - {}", updatedMajor.getCode(), updatedMajor.getName());
        return updatedMajor;
    }

    /**
     * 删除专业
     *
     * @param id 专业ID
     */
    @CacheEvict(value = CacheConfig.CACHE_MAJORS, allEntries = true)
    @Transactional
    public void deleteMajor(Long id) {
        Major major = findById(id);

        // 检查是否有关联的学生，如有则不允许删除
        long studentCount = majorRepository.countStudentsByMajorId(id);
        if (studentCount > 0) {
            log.warn("专业 {} 下有 {} 名学生，无法删除", major.getName(), studentCount);
            throw new BusinessException(ErrorCode.MAJOR_HAS_STUDENTS);
        }

        majorRepository.delete(major);
        log.info("删除专业成功: {} - {}", major.getCode(), major.getName());
    }

    /**
     * 检查专业代码是否存在
     *
     * @param code 专业代码
     * @return true-存在，false-不存在
     */
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return majorRepository.existsByCode(code);
    }

    /**
     * 统计专业数量
     *
     * @return 专业总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return majorRepository.count();
    }
}

