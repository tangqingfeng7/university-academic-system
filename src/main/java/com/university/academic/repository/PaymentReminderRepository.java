package com.university.academic.repository;

import com.university.academic.entity.tuition.PaymentReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 缴费提醒数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {

    /**
     * 查询指定账单的所有提醒记录
     */
    @Query("SELECT pr FROM PaymentReminder pr " +
           "WHERE pr.bill.id = :billId " +
           "ORDER BY pr.sentAt DESC")
    List<PaymentReminder> findByBillId(@Param("billId") Long billId);

    /**
     * 查询指定时间范围的提醒记录
     */
    @Query("SELECT pr FROM PaymentReminder pr " +
           "WHERE pr.sentAt >= :startTime " +
           "AND pr.sentAt <= :endTime " +
           "ORDER BY pr.sentAt DESC")
    List<PaymentReminder> findBySentAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询最近一次提醒记录
     */
    @Query("SELECT pr FROM PaymentReminder pr " +
           "WHERE pr.bill.id = :billId " +
           "ORDER BY pr.sentAt DESC " +
           "LIMIT 1")
    PaymentReminder findLatestByBillId(@Param("billId") Long billId);

    /**
     * 统计提醒发送情况
     */
    @Query("SELECT pr.reminderType, COUNT(pr), SUM(CASE WHEN pr.success = true THEN 1 ELSE 0 END) " +
           "FROM PaymentReminder pr " +
           "WHERE pr.sentAt >= :startTime " +
           "AND pr.sentAt <= :endTime " +
           "GROUP BY pr.reminderType")
    List<Object[]> getReminderStatistics(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询发送失败的提醒记录
     */
    @Query("SELECT pr FROM PaymentReminder pr " +
           "WHERE pr.success = false " +
           "ORDER BY pr.sentAt DESC")
    List<PaymentReminder> findFailedReminders();
}

