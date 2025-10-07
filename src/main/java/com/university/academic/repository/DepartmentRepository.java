package com.university.academic.repository;

import com.university.academic.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 院系数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 根据院系代码查询院系
     *
     * @param code 院系代码
     * @return 院系对象
     */
    Optional<Department> findByCode(String code);

    /**
     * 检查院系代码是否存在
     *
     * @param code 院系代码
     * @return true-存在，false-不存在
     */
    boolean existsByCode(String code);

    /**
     * 根据院系名称查询院系
     *
     * @param name 院系名称
     * @return 院系对象
     */
    Optional<Department> findByName(String name);
}

