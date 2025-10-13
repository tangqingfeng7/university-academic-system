package com.university.ems.service.impl;

import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.ems.dto.CreateConstraintRequest;
import com.university.ems.dto.SchedulingConstraintDTO;
import com.university.ems.dto.UpdateConstraintRequest;
import com.university.ems.entity.SchedulingConstraint;
import com.university.ems.enums.ConstraintType;
import com.university.ems.repository.SchedulingConstraintRepository;
import com.university.ems.repository.SchedulingSolutionRepository;
import com.university.ems.service.SchedulingConstraintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 排课约束服务实现类
 * 
 * @author Academic System Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingConstraintServiceImpl implements SchedulingConstraintService {

    private final SchedulingConstraintRepository constraintRepository;
    private final SchedulingSolutionRepository solutionRepository;

    @Override
    @Transactional
    public SchedulingConstraintDTO createConstraint(CreateConstraintRequest request) {
        log.info("创建排课约束: {}", request.getName());

        // 验证约束名称是否已存在
        if (isConstraintNameExists(request.getName(), null)) {
            throw new BusinessException(ErrorCode.CONSTRAINT_NAME_ALREADY_EXISTS);
        }

        // 验证约束配置
        String validationMessage = validateConstraintConfig(request.getType(), request.getWeight());
        if (validationMessage != null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, validationMessage);
        }

        // 创建约束实体
        SchedulingConstraint constraint = SchedulingConstraint.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .weight(request.getWeight())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        constraint = constraintRepository.save(constraint);
        log.info("排课约束创建成功: id={}, name={}", constraint.getId(), constraint.getName());

        return convertToDTO(constraint);
    }

    @Override
    @Transactional
    public SchedulingConstraintDTO updateConstraint(Long id, UpdateConstraintRequest request) {
        log.info("更新排课约束: id={}", id);

        // 查询约束
        SchedulingConstraint constraint = constraintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSTRAINT_NOT_FOUND));

        // 验证名称唯一性（如果修改了名称）
        if (request.getName() != null && !request.getName().equals(constraint.getName())) {
            if (isConstraintNameExists(request.getName(), id)) {
                throw new BusinessException(ErrorCode.CONSTRAINT_NAME_ALREADY_EXISTS);
            }
            constraint.setName(request.getName());
        }

        // 验证约束配置（如果修改了类型或权重）
        ConstraintType type = request.getType() != null ? request.getType() : constraint.getType();
        Integer weight = request.getWeight() != null ? request.getWeight() : constraint.getWeight();
        String validationMessage = validateConstraintConfig(type, weight);
        if (validationMessage != null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, validationMessage);
        }

        // 更新字段
        if (request.getType() != null) {
            constraint.setType(request.getType());
        }
        if (request.getDescription() != null) {
            constraint.setDescription(request.getDescription());
        }
        if (request.getWeight() != null) {
            constraint.setWeight(request.getWeight());
        }
        if (request.getActive() != null) {
            constraint.setActive(request.getActive());
        }

        constraint = constraintRepository.save(constraint);
        log.info("排课约束更新成功: id={}", id);

        return convertToDTO(constraint);
    }

    @Override
    @Transactional(readOnly = true)
    public SchedulingConstraintDTO getConstraintById(Long id) {
        log.debug("查询排课约束: id={}", id);

        SchedulingConstraint constraint = constraintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSTRAINT_NOT_FOUND));

        return convertToDTO(constraint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchedulingConstraintDTO> getAllConstraints() {
        log.debug("查询所有排课约束");

        List<SchedulingConstraint> constraints = constraintRepository.findAll();
        return constraints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchedulingConstraintDTO> getActiveConstraints() {
        log.debug("查询启用的排课约束（按权重降序）");

        List<SchedulingConstraint> constraints = constraintRepository.findByActiveOrderByWeightDesc(true);
        return constraints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchedulingConstraintDTO> getActiveConstraintsByType(ConstraintType type) {
        log.debug("查询指定类型的启用约束: type={}", type);

        if (type == null) {
            throw new BusinessException(ErrorCode.CONSTRAINT_TYPE_INVALID);
        }

        List<SchedulingConstraint> constraints = constraintRepository.findByTypeAndActive(type, true);
        return constraints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void toggleActive(Long id, Boolean active) {
        log.info("切换排课约束启用状态: id={}, active={}", id, active);

        SchedulingConstraint constraint = constraintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSTRAINT_NOT_FOUND));

        constraint.setActive(active);
        constraintRepository.save(constraint);

        log.info("排课约束状态切换成功: id={}, active={}", id, active);
    }

    @Override
    @Transactional
    public void deleteConstraint(Long id) {
        log.info("删除排课约束: id={}", id);

        SchedulingConstraint constraint = constraintRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSTRAINT_NOT_FOUND));

        // 检查约束是否正在被使用
        // 如果存在正在优化(OPTIMIZING)或已应用(APPLIED)的排课方案，不允许删除约束
        if (solutionRepository.existsActiveSolution()) {
            throw new BusinessException(ErrorCode.CONSTRAINT_IN_USE, 
                "存在正在优化或已应用的排课方案，无法删除约束。请先完成或撤销相关排课方案。");
        }

        constraintRepository.delete(constraint);
        log.info("排课约束删除成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isConstraintNameExists(String name, Long excludeId) {
        SchedulingConstraint constraint = constraintRepository.findByName(name);
        if (constraint == null) {
            return false;
        }
        // 如果指定了excludeId（用于更新时），则排除自身
        return excludeId == null || !constraint.getId().equals(excludeId);
    }

    @Override
    public String validateConstraintConfig(ConstraintType type, Integer weight) {
        // 验证约束类型
        if (type == null) {
            return "约束类型不能为空";
        }

        // 验证权重
        if (weight == null) {
            return "权重不能为空";
        }
        if (weight < 1 || weight > 100) {
            return "权重必须在1-100之间";
        }

        // 硬约束权重应该较高
        if (type == ConstraintType.HARD && weight < 80) {
            log.warn("硬约束权重建议设置为80以上: weight={}", weight);
        }

        // 软约束权重应该较低
        if (type == ConstraintType.SOFT && weight > 50) {
            log.warn("软约束权重建议设置为50以下: weight={}", weight);
        }

        return null; // 验证通过
    }

    /**
     * 转换为DTO
     */
    private SchedulingConstraintDTO convertToDTO(SchedulingConstraint constraint) {
        return SchedulingConstraintDTO.builder()
                .id(constraint.getId())
                .name(constraint.getName())
                .type(constraint.getType())
                .typeDescription(getTypeDescription(constraint.getType()))
                .description(constraint.getDescription())
                .weight(constraint.getWeight())
                .active(constraint.getActive())
                .createdAt(constraint.getCreatedAt())
                .updatedAt(constraint.getUpdatedAt())
                .build();
    }

    /**
     * 获取约束类型描述
     */
    private String getTypeDescription(ConstraintType type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case HARD:
                return "硬约束（必须满足）";
            case SOFT:
                return "软约束（尽量满足）";
            default:
                return type.name();
        }
    }
}

