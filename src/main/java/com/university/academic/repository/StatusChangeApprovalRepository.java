package com.university.academic.repository;

import com.university.academic.entity.ApprovalAction;
import com.university.academic.entity.StatusChangeApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学籍异动审批记录Repository接口
 *
 * @author Academic System Team
 */
@Repository
public interface StatusChangeApprovalRepository extends JpaRepository<StatusChangeApproval, Long> {

    /**
     * 根据异动ID查询审批记录
     *
     * @param statusChangeId 异动ID
     * @return 审批记录列表
     */
    @Query("SELECT sa FROM StatusChangeApproval sa " +
           "LEFT JOIN FETCH sa.approver " +
           "WHERE sa.statusChange.id = :statusChangeId AND sa.deleted = false " +
           "ORDER BY sa.approvalLevel ASC, sa.approvedAt ASC")
    List<StatusChangeApproval> findByStatusChangeId(@Param("statusChangeId") Long statusChangeId);

    /**
     * 根据异动ID和审批级别查询审批记录
     *
     * @param statusChangeId 异动ID
     * @param approvalLevel  审批级别
     * @return 审批记录列表
     */
    @Query("SELECT sa FROM StatusChangeApproval sa " +
           "WHERE sa.statusChange.id = :statusChangeId AND sa.approvalLevel = :approvalLevel AND sa.deleted = false " +
           "ORDER BY sa.approvedAt DESC")
    List<StatusChangeApproval> findByStatusChangeIdAndLevel(
            @Param("statusChangeId") Long statusChangeId,
            @Param("approvalLevel") Integer approvalLevel
    );

    /**
     * 根据审批人ID查询审批记录
     *
     * @param approverId 审批人ID
     * @return 审批记录列表
     */
    @Query("SELECT sa FROM StatusChangeApproval sa " +
           "LEFT JOIN FETCH sa.statusChange sc " +
           "LEFT JOIN FETCH sc.student " +
           "WHERE sa.approver.id = :approverId AND sa.deleted = false " +
           "ORDER BY sa.approvedAt DESC")
    List<StatusChangeApproval> findByApproverId(@Param("approverId") Long approverId);

    /**
     * 统计审批人处理的审批数量
     *
     * @param approverId 审批人ID
     * @param action     审批操作（可选）
     * @return 审批数量
     */
    @Query("SELECT COUNT(sa) FROM StatusChangeApproval sa " +
           "WHERE sa.approver.id = :approverId AND sa.deleted = false " +
           "AND (:action IS NULL OR sa.action = :action)")
    long countByApproverIdAndAction(
            @Param("approverId") Long approverId,
            @Param("action") ApprovalAction action
    );

    /**
     * 检查异动是否已在指定级别审批过
     *
     * @param statusChangeId 异动ID
     * @param approvalLevel  审批级别
     * @return 是否已审批
     */
    @Query("SELECT COUNT(sa) > 0 FROM StatusChangeApproval sa " +
           "WHERE sa.statusChange.id = :statusChangeId AND sa.approvalLevel = :approvalLevel AND sa.deleted = false")
    boolean existsByStatusChangeIdAndLevel(
            @Param("statusChangeId") Long statusChangeId,
            @Param("approvalLevel") Integer approvalLevel
    );
}

