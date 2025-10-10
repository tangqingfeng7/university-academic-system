package com.university.academic.service;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.ChangeType;
import com.university.academic.entity.StudentStatusChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 学籍异动管理Service接口
 *
 * @author Academic System Team
 */
public interface StudentStatusChangeService {

    /**
     * 提交学籍异动申请
     *
     * @param studentId     学生ID
     * @param type          异动类型
     * @param reason        异动原因
     * @param startDate     开始日期（休学/复学）
     * @param endDate       结束日期（休学）
     * @param targetMajorId 目标专业ID（转专业）
     * @param attachment    证明材料附件
     * @return 异动申请记录
     */
    StudentStatusChange createApplication(
            Long studentId,
            ChangeType type,
            String reason,
            LocalDate startDate,
            LocalDate endDate,
            Long targetMajorId,
            MultipartFile attachment
    );

    /**
     * 撤销异动申请
     *
     * @param applicationId 申请ID
     * @param studentId     学生ID
     */
    void cancelApplication(Long applicationId, Long studentId);

    /**
     * 查询学生异动历史
     *
     * @param studentId 学生ID
     * @return 异动记录列表
     */
    List<StudentStatusChange> getStudentHistory(Long studentId);

    /**
     * 根据ID查询异动申请（带详细信息）
     *
     * @param applicationId 申请ID
     * @return 异动申请
     */
    StudentStatusChange findByIdWithDetails(Long applicationId);

    /**
     * 根据ID和学生ID查询异动申请
     *
     * @param applicationId 申请ID
     * @param studentId     学生ID
     * @return 异动申请
     */
    StudentStatusChange findByIdAndStudentId(Long applicationId, Long studentId);

    /**
     * 分页查询学生异动记录
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 异动记录分页列表
     */
    Page<StudentStatusChange> findByStudentId(Long studentId, Pageable pageable);

    /**
     * 根据审批状态查询异动记录
     *
     * @param status   审批状态
     * @param pageable 分页参数
     * @return 异动记录分页列表
     */
    Page<StudentStatusChange> findByStatus(ApprovalStatus status, Pageable pageable);

    /**
     * 根据异动类型查询异动记录
     *
     * @param type     异动类型
     * @param pageable 分页参数
     * @return 异动记录分页列表
     */
    Page<StudentStatusChange> findByType(ChangeType type, Pageable pageable);

    /**
     * 多条件查询异动记录
     *
     * @param type      异动类型（可选）
     * @param status    审批状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @param pageable  分页参数
     * @return 异动记录分页列表
     */
    Page<StudentStatusChange> findByMultipleConditions(
            ChangeType type,
            ApprovalStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    /**
     * 检查学生是否有进行中的异动申请
     *
     * @param studentId 学生ID
     * @return 是否有进行中的申请
     */
    boolean hasPendingApplication(Long studentId);

    /**
     * 根据ID查询学生的异动申请
     *
     * @param applicationId 申请ID
     * @param studentId     学生ID
     * @return 异动申请
     */
    StudentStatusChange getApplicationById(Long applicationId, Long studentId);
}

