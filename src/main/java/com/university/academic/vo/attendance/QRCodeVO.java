package com.university.academic.vo.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 二维码视图对象
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeVO {

    /**
     * 考勤记录ID
     */
    private Long recordId;

    /**
     * 二维码令牌
     */
    private String qrToken;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 剩余有效时间（秒）
     */
    private Long remainingSeconds;

    /**
     * 是否有效
     */
    private Boolean isValid;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 教师姓名
     */
    private String teacherName;
}

