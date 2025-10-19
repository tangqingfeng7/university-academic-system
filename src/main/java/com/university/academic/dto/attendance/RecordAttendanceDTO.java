package com.university.academic.dto.attendance;

import com.university.academic.entity.attendance.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 记录考勤请求DTO
 *
 * @author Academic System Team
 */
@Data
public class RecordAttendanceDTO {

    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    /**
     * 考勤状态
     */
    @NotNull(message = "考勤状态不能为空")
    private AttendanceStatus status;

    /**
     * 备注
     */
    private String remarks;
}

