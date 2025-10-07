package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.entity.Department;
import com.university.academic.entity.Major;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.DepartmentRepository;
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
 * 院系服务类
 * 提供院系CRUD等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;

    /**
     * 根据ID查询院系
     *
     * @param id 院系ID
     * @return 院系对象
     */
    @Cacheable(value = CacheConfig.CACHE_DEPARTMENTS, key = "#id")
    @Transactional(readOnly = true)
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    /**
     * 根据代码查询院系
     *
     * @param code 院系代码
     * @return 院系对象
     */
    @Transactional(readOnly = true)
    public Department findByCode(String code) {
        return departmentRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
    }

    /**
     * 查询所有院系
     *
     * @return 院系列表
     */
    @Transactional(readOnly = true)
    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    /**
     * 分页查询院系
     *
     * @param pageable 分页参数
     * @return 院系分页数据
     */
    @Transactional(readOnly = true)
    public Page<Department> findAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }

    /**
     * 创建院系
     *
     * @param department 院系对象
     * @return 创建后的院系对象
     */
    @CacheEvict(value = CacheConfig.CACHE_DEPARTMENTS, allEntries = true)
    @Transactional
    public Department createDepartment(Department department) {
        // 检查院系代码是否已存在
        if (departmentRepository.existsByCode(department.getCode())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_CODE_ALREADY_EXISTS);
        }

        Department savedDepartment = departmentRepository.save(department);
        log.info("创建院系成功: {} - {}", savedDepartment.getCode(), savedDepartment.getName());
        return savedDepartment;
    }

    /**
     * 更新院系信息
     *
     * @param id         院系ID
     * @param department 更新的院系信息
     * @return 更新后的院系对象
     */
    @CacheEvict(value = CacheConfig.CACHE_DEPARTMENTS, allEntries = true)
    @Transactional
    public Department updateDepartment(Long id, Department department) {
        Department existingDepartment = findById(id);

        // 如果要修改院系代码，检查新代码是否已存在
        if (!existingDepartment.getCode().equals(department.getCode())) {
            if (departmentRepository.existsByCode(department.getCode())) {
                throw new BusinessException(ErrorCode.DEPARTMENT_CODE_ALREADY_EXISTS);
            }
            existingDepartment.setCode(department.getCode());
        }

        // 更新其他字段
        existingDepartment.setName(department.getName());

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        log.info("更新院系成功: {} - {}", updatedDepartment.getCode(), updatedDepartment.getName());
        return updatedDepartment;
    }

    /**
     * 删除院系
     *
     * @param id 院系ID
     */
    @CacheEvict(value = CacheConfig.CACHE_DEPARTMENTS, allEntries = true)
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = findById(id);
        
        // 检查是否有关联的专业，如有则不允许删除
        List<Major> relatedMajors = majorRepository.findByDepartmentId(id);
        if (!relatedMajors.isEmpty()) {
            throw new BusinessException(ErrorCode.DEPARTMENT_HAS_MAJORS);
        }
        
        departmentRepository.delete(department);
        log.info("删除院系成功: {} - {}", department.getCode(), department.getName());
    }

    /**
     * 检查院系代码是否存在
     *
     * @param code 院系代码
     * @return true-存在，false-不存在
     */
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return departmentRepository.existsByCode(code);
    }

    /**
     * 统计院系数量
     *
     * @return 院系总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return departmentRepository.count();
    }
}

