package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 缴费提醒DTO
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReminderDTO {

    /**
     * 提醒记录ID
     */
    private Long id;

    /**
     * 账单ID
     */
    private Long billId;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 提醒类型
     */
    private String reminderType;

    /**
     * 发送时间
     */
    private LocalDateTime sentAt;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 提醒内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

