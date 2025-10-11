package com.university.academic.entity.tuition;

import com.university.academic.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 缴费提醒实体类
 * 记录发送给学生的缴费提醒
 *
 * @author Academic System Team
 */
@Entity
@Table(name = "payment_reminder", indexes = {
    @Index(name = "idx_reminder_bill", columnList = "bill_id"),
    @Index(name = "idx_reminder_sent", columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReminder extends BaseEntity {

    /**
     * 学费账单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private TuitionBill bill;

    /**
     * 提醒类型（EMAIL、SMS、NOTIFICATION）
     */
    @Column(name = "reminder_type", nullable = false, length = 20)
    private String reminderType;

    /**
     * 发送时间
     */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /**
     * 是否发送成功
     */
    @Column(nullable = false)
    private Boolean success;

    /**
     * 失败原因
     */
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    /**
     * 提醒内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;
}

