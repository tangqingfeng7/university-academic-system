package com.university.academic.repository;

import com.university.academic.entity.ApprovalLevelApprover;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批级别审批人Repository接口
 *
 * @author Academic System Team
 */
@Repository
public interface ApprovalLevelApproverRepository extends JpaRepository<ApprovalLevelApprover, Long> {

    /**
     * 查询指定级别的所有启用审批人
     *
     * @param approvalLevel 审批级别
     * @return 审批人列表
     */
    @Query("SELECT ala FROM ApprovalLevelApprover ala " +
           "WHERE ala.approvalLevel = :approvalLevel AND ala.enabled = true " +
           "ORDER BY ala.priority ASC, ala.pendingCount ASC")
    List<ApprovalLevelApprover> findByApprovalLevel(@Param("approvalLevel") Integer approvalLevel);

    /**
     * 查询指定级别负载最小的审批人（负载均衡）
     *
     * @param approvalLevel 审批级别
     * @return 审批人
     */
    @Query("SELECT ala FROM ApprovalLevelApprover ala " +
           "WHERE ala.approvalLevel = :approvalLevel AND ala.enabled = true " +
           "ORDER BY ala.pendingCount ASC, ala.priority ASC LIMIT 1")
    ApprovalLevelApprover findLeastLoadedByLevel(@Param("approvalLevel") Integer approvalLevel);

    /**
     * 增加审批人的待处理任务数
     *
     * @param id 审批人ID
     */
    @Modifying
    @Query("UPDATE ApprovalLevelApprover ala SET ala.pendingCount = ala.pendingCount + 1 WHERE ala.id = :id")
    void incrementPendingCount(@Param("id") Long id);

    /**
     * 减少审批人的待处理任务数
     *
     * @param id 审批人ID
     */
    @Modifying
    @Query("UPDATE ApprovalLevelApprover ala SET ala.pendingCount = ala.pendingCount - 1 WHERE ala.id = :id AND ala.pendingCount > 0")
    void decrementPendingCount(@Param("id") Long id);

    /**
     * 根据用户ID和审批级别查询
     *
     * @param userId        用户ID
     * @param approvalLevel 审批级别
     * @return 审批人配置
     */
    @Query("SELECT ala FROM ApprovalLevelApprover ala " +
           "WHERE ala.user.id = :userId AND ala.approvalLevel = :approvalLevel AND ala.enabled = true")
    ApprovalLevelApprover findByUserIdAndLevel(@Param("userId") Long userId, @Param("approvalLevel") Integer approvalLevel);
}

