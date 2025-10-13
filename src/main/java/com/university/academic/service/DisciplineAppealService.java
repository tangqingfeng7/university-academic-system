package com.university.academic.service;

import com.university.academic.entity.DisciplineAppeal;
import com.university.academic.entity.enums.AppealResult;
import com.university.academic.entity.enums.AppealStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 处分申诉服务接口
 *
 * @author Academic System Team
 */
public interface DisciplineAppealService {

    /**
     * 创建申诉
     */
    DisciplineAppeal createAppeal(DisciplineAppeal appeal);

    /**
     * 根据ID查询申诉
     */
    DisciplineAppeal findById(Long id);

    /**
     * 查询学生的申诉记录
     */
    List<DisciplineAppeal> findByStudentId(Long studentId);

    /**
     * 查询处分的申诉记录
     */
    List<DisciplineAppeal> findByDisciplineId(Long disciplineId);

    /**
     * 分页查询待审核的申诉
     */
    Page<DisciplineAppeal> findPendingAppeals(Pageable pageable);

    /**
     * 根据条件查询申诉
     */
    Page<DisciplineAppeal> findByConditions(
            AppealStatus status,
            Long studentId,
            Pageable pageable);

    /**
     * 审核申诉
     */
    DisciplineAppeal reviewAppeal(
            Long id,
            AppealResult result,
            String comment,
            Long reviewerId);

    /**
     * 撤销申诉
     */
    void cancelAppeal(Long id);

    /**
     * 检查处分是否有待审核的申诉
     */
    boolean hasPendingAppeal(Long disciplineId);
}

