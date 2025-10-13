package com.university.academic.service;

import com.university.academic.entity.StudentDiscipline;
import com.university.academic.entity.enums.ApprovalStatus;
import com.university.academic.entity.enums.DisciplineStatus;
import com.university.academic.entity.enums.DisciplineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 学生处分服务接口
 *
 * @author Academic System Team
 */
public interface StudentDisciplineService {

    /**
     * 创建处分记录
     */
    StudentDiscipline createDiscipline(StudentDiscipline discipline);

    /**
     * 更新处分记录
     */
    StudentDiscipline updateDiscipline(Long id, StudentDiscipline discipline);

    /**
     * 删除处分记录（软删除）
     */
    void deleteDiscipline(Long id);

    /**
     * 根据ID查询处分
     */
    StudentDiscipline findById(Long id);

    /**
     * 查询学生的处分记录
     */
    List<StudentDiscipline> findByStudentId(Long studentId);

    /**
     * 查询学生的有效处分
     */
    List<StudentDiscipline> findActiveByStudentId(Long studentId);

    /**
     * 分页查询处分记录
     */
    Page<StudentDiscipline> findAll(Pageable pageable);

    /**
     * 根据条件查询处分
     */
    Page<StudentDiscipline> findByConditions(
            Long studentId,
            Long reporterId,
            DisciplineType disciplineType,
            DisciplineStatus status,
            ApprovalStatus approvalStatus,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);

    /**
     * 解除处分
     */
    StudentDiscipline removeDiscipline(Long id, String reason, Long operatorId);

    /**
     * 检查学生是否有未解除的处分
     */
    boolean hasActiveDiscipline(Long studentId);

    /**
     * 统计学生的处分数量
     */
    Long countByStudentId(Long studentId);

    /**
     * 审批处分
     */
    StudentDiscipline approveDiscipline(Long id, Long approverId);
}

