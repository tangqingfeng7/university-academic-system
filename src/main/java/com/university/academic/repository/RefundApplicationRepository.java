package com.university.academic.repository;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.tuition.RefundApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 退费申请数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface RefundApplicationRepository extends JpaRepository<RefundApplication, Long> {

    /**
     * 查询学生的退费申请
     */
    @Query("SELECT r FROM RefundApplication r " +
           "WHERE r.student.id = :studentId " +
           "ORDER BY r.submittedAt DESC")
    List<RefundApplication> findByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询指定状态的退费申请
     */
    @Query("SELECT r FROM RefundApplication r " +
           "WHERE r.status = :status " +
           "ORDER BY r.submittedAt DESC")
    Page<RefundApplication> findByStatus(@Param("status") ApprovalStatus status, Pageable pageable);

    /**
     * 查询待审批的退费申请
     */
    @Query("SELECT r FROM RefundApplication r " +
           "WHERE r.status = 'PENDING' " +
           "AND r.approvalLevel = :approvalLevel " +
           "ORDER BY r.submittedAt ASC")
    Page<RefundApplication> findPendingByApprovalLevel(@Param("approvalLevel") Integer approvalLevel, Pageable pageable);

    /**
     * 查询缴费记录的退费申请
     */
    @Query("SELECT r FROM RefundApplication r " +
           "WHERE r.payment.id = :paymentId " +
           "ORDER BY r.submittedAt DESC")
    List<RefundApplication> findByPaymentId(@Param("paymentId") Long paymentId);

    /**
     * 查询退费申请详情（预加载关联对象）
     */
    @Query("SELECT r FROM RefundApplication r " +
           "LEFT JOIN FETCH r.student s " +
           "LEFT JOIN FETCH r.payment p " +
           "WHERE r.id = :id")
    Optional<RefundApplication> findByIdWithDetails(@Param("id") Long id);

    /**
     * 统计退费申请
     */
    @Query("SELECT r.status, COUNT(r), SUM(r.refundAmount) " +
           "FROM RefundApplication r " +
           "GROUP BY r.status")
    List<Object[]> getRefundStatistics();
}

