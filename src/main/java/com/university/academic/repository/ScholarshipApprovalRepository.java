package com.university.academic.repository;

import com.university.academic.entity.ScholarshipApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 奖学金审批Repository接口
 */
@Repository
public interface ScholarshipApprovalRepository extends JpaRepository<ScholarshipApproval, Long> {
    
    /**
     * 查询申请的审批历史
     *
     * @param applicationId 申请ID
     * @return 审批记录列表（按审批级别升序）
     */
    @Query("SELECT sa FROM ScholarshipApproval sa " +
           "JOIN FETCH sa.approver " +
           "WHERE sa.application.id = :applicationId " +
           "ORDER BY sa.approvalLevel ASC, sa.approvedAt ASC")
    List<ScholarshipApproval> findByApplicationIdWithApprover(@Param("applicationId") Long applicationId);
    
    /**
     * 查询指定申请和审批级别的审批记录
     *
     * @param applicationId 申请ID
     * @param approvalLevel 审批级别
     * @return 审批记录
     */
    Optional<ScholarshipApproval> findByApplicationIdAndApprovalLevel(Long applicationId, Integer approvalLevel);
    
    /**
     * 检查指定申请和审批级别是否已审批
     *
     * @param applicationId 申请ID
     * @param approvalLevel 审批级别
     * @return 是否已审批
     */
    boolean existsByApplicationIdAndApprovalLevel(Long applicationId, Integer approvalLevel);
}
