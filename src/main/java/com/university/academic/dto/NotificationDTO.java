package com.university.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知公告数据传输对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 类型 (SYSTEM/COURSE/GRADE/SELECTION)
     */
    private String type;

    /**
     * 类型描述
     */
    private String typeDescription;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 发布者姓名
     */
    private String publisherName;

    /**
     * 目标角色 (ALL/ADMIN/TEACHER/STUDENT)
     */
    private String targetRole;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 是否有效
     */
    private Boolean active;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 是否已读（仅查询时返回）
     */
    private Boolean read;
}

