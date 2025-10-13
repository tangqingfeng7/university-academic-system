package com.university.ems.service;

import com.university.ems.dto.CreateConstraintRequest;
import com.university.ems.dto.SchedulingConstraintDTO;
import com.university.ems.dto.UpdateConstraintRequest;
import com.university.ems.enums.ConstraintType;

import java.util.List;

/**
 * 排课约束服务接口
 * 
 * @author Academic System Team
 */
public interface SchedulingConstraintService {

    /**
     * 添加排课约束
     *
     * @param request 创建请求
     * @return 约束DTO
     */
    SchedulingConstraintDTO createConstraint(CreateConstraintRequest request);

    /**
     * 更新排课约束
     *
     * @param id 约束ID
     * @param request 更新请求
     * @return 约束DTO
     */
    SchedulingConstraintDTO updateConstraint(Long id, UpdateConstraintRequest request);

    /**
     * 根据ID查询约束
     *
     * @param id 约束ID
     * @return 约束DTO
     */
    SchedulingConstraintDTO getConstraintById(Long id);

    /**
     * 查询所有约束列表
     *
     * @return 约束列表
     */
    List<SchedulingConstraintDTO> getAllConstraints();

    /**
     * 查询启用的约束列表（按权重降序）
     *
     * @return 约束列表
     */
    List<SchedulingConstraintDTO> getActiveConstraints();

    /**
     * 按类型查询启用的约束
     *
     * @param type 约束类型
     * @return 约束列表
     */
    List<SchedulingConstraintDTO> getActiveConstraintsByType(ConstraintType type);

    /**
     * 启用/禁用约束
     *
     * @param id 约束ID
     * @param active 是否启用
     */
    void toggleActive(Long id, Boolean active);

    /**
     * 删除约束
     *
     * @param id 约束ID
     */
    void deleteConstraint(Long id);

    /**
     * 验证约束名称是否已存在
     *
     * @param name 约束名称
     * @param excludeId 排除的ID（用于更新时）
     * @return 是否存在
     */
    boolean isConstraintNameExists(String name, Long excludeId);

    /**
     * 验证约束配置是否有效
     *
     * @param type 约束类型
     * @param weight 权重
     * @return 验证结果消息，如果为null表示验证通过
     */
    String validateConstraintConfig(ConstraintType type, Integer weight);
}

