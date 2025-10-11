package com.university.academic.repository;

import com.university.academic.entity.tuition.PaymentStatus;
import com.university.academic.entity.tuition.TuitionPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 缴费记录数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface TuitionPaymentRepository extends JpaRepository<TuitionPayment, Long> {

    /**
     * 根据缴费单号查询
     */
    Optional<TuitionPayment> findByPaymentNo(String paymentNo);

    /**
     * 根据第三方交易号查询
     */
    Optional<TuitionPayment> findByTransactionId(String transactionId);

    /**
     * 查询指定账单的所有缴费记录
     */
    @Query("SELECT tp FROM TuitionPayment tp " +
           "LEFT JOIN FETCH tp.bill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tp.bill.id = :billId " +
           "ORDER BY tp.createdAt DESC")
    List<TuitionPayment> findByBillId(@Param("billId") Long billId);

    /**
     * 查询学生的所有缴费记录
     */
    @Query("SELECT tp FROM TuitionPayment tp " +
           "LEFT JOIN FETCH tp.bill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tb.student.id = :studentId " +
           "ORDER BY tp.createdAt DESC")
    List<TuitionPayment> findByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询指定状态的缴费记录
     */
    @Query(value = "SELECT tp FROM TuitionPayment tp " +
           "LEFT JOIN FETCH tp.bill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tp.status = :status " +
           "ORDER BY tp.createdAt DESC",
           countQuery = "SELECT COUNT(tp) FROM TuitionPayment tp WHERE tp.status = :status")
    Page<TuitionPayment> findByStatus(@Param("status") PaymentStatus status, Pageable pageable);

    /**
     * 查询指定时间范围的缴费记录
     */
    @Query("SELECT tp FROM TuitionPayment tp " +
           "WHERE tp.paidAt >= :startTime " +
           "AND tp.paidAt <= :endTime " +
           "AND tp.status = 'SUCCESS' " +
           "ORDER BY tp.paidAt DESC")
    List<TuitionPayment> findByPaidTimeBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计指定时间范围的缴费金额
     */
    @Query("SELECT SUM(tp.amount) FROM TuitionPayment tp " +
           "WHERE tp.paidAt >= :startTime " +
           "AND tp.paidAt <= :endTime " +
           "AND tp.status = 'SUCCESS'")
    Double sumAmountByPaidTimeBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计指定学年的缴费金额
     */
    @Query("SELECT SUM(tp.amount) FROM TuitionPayment tp " +
           "JOIN tp.bill tb " +
           "WHERE tb.academicYear = :academicYear " +
           "AND tp.status = 'SUCCESS'")
    Double sumAmountByAcademicYear(@Param("academicYear") String academicYear);

    /**
     * 多条件查询缴费记录
     */
    @Query(value = "SELECT tp FROM TuitionPayment tp " +
           "LEFT JOIN FETCH tp.bill tb " +
           "LEFT JOIN FETCH tb.student s " +
           "WHERE (:academicYear IS NULL OR tb.academicYear = :academicYear) " +
           "AND (:status IS NULL OR tp.status = :status) " +
           "AND (:studentNo IS NULL OR s.studentNo LIKE CONCAT('%', :studentNo, '%')) " +
           "AND (:paymentNo IS NULL OR tp.paymentNo LIKE CONCAT('%', :paymentNo, '%')) " +
           "ORDER BY tp.createdAt DESC",
           countQuery = "SELECT COUNT(tp) FROM TuitionPayment tp " +
           "LEFT JOIN tp.bill tb " +
           "LEFT JOIN tb.student s " +
           "WHERE (:academicYear IS NULL OR tb.academicYear = :academicYear) " +
           "AND (:status IS NULL OR tp.status = :status) " +
           "AND (:studentNo IS NULL OR s.studentNo LIKE CONCAT('%', :studentNo, '%')) " +
           "AND (:paymentNo IS NULL OR tp.paymentNo LIKE CONCAT('%', :paymentNo, '%'))")
    Page<TuitionPayment> searchPayments(
        @Param("academicYear") String academicYear,
        @Param("status") PaymentStatus status,
        @Param("studentNo") String studentNo,
        @Param("paymentNo") String paymentNo,
        Pageable pageable
    );

    /**
     * 统计各支付方式的使用情况
     */
    @Query("SELECT tp.method, COUNT(tp), SUM(tp.amount) " +
           "FROM TuitionPayment tp " +
           "WHERE tp.status = 'SUCCESS' " +
           "AND tp.paidAt >= :startTime " +
           "AND tp.paidAt <= :endTime " +
           "GROUP BY tp.method")
    List<Object[]> getPaymentMethodStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

