package com.university.academic.repository;

import com.university.academic.entity.tuition.RefundApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退费审批记录数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface RefundApprovalRepository extends JpaRepository<RefundApproval, Long> {

    /**
     * 查询退费申请的审批历史
     */
    @Query("SELECT ra FROM RefundApproval ra " +
           "LEFT JOIN FETCH ra.approver " +
           "WHERE ra.refundApplication.id = :applicationId " +
           "ORDER BY ra.approvalLevel ASC, ra.approvedAt ASC")
    List<RefundApproval> findByRefundApplicationId(@Param("applicationId") Long applicationId);
}

