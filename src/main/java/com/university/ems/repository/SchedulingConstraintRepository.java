package com.university.ems.repository;

import com.university.ems.entity.SchedulingConstraint;
import com.university.ems.enums.ConstraintType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 排课约束Repository接口
 * 
 * @author Academic System Team
 */
@Repository
public interface SchedulingConstraintRepository extends JpaRepository<SchedulingConstraint, Long> {

    /**
     * 查询启用的约束
     *
     * @param active 是否启用
     * @return 约束列表
     */
    List<SchedulingConstraint> findByActive(Boolean active);

    /**
     * 按类型查询启用的约束
     *
     * @param type 约束类型
     * @param active 是否启用
     * @return 约束列表
     */
    List<SchedulingConstraint> findByTypeAndActive(ConstraintType type, Boolean active);

    /**
     * 查询所有启用的约束并按权重降序排序
     *
     * @param active 是否启用
     * @return 约束列表
     */
    List<SchedulingConstraint> findByActiveOrderByWeightDesc(Boolean active);

    /**
     * 按名称查询约束
     *
     * @param name 约束名称
     * @return 约束
     */
    SchedulingConstraint findByName(String name);
}

