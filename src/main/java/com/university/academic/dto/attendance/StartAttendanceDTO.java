package com.university.academic.dto.attendance;

import com.university.academic.entity.attendance.AttendanceMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始考勤请求DTO
 *
 * @author Academic System Team
 */
@Data
public class StartAttendanceDTO {

    /**
     * 开课计划ID
     */
    @NotNull(message = "开课计划ID不能为空")
    private Long offeringId;

    /**
     * 考勤方式
     */
    @NotNull(message = "考勤方式不能为空")
    private AttendanceMethod method;
}

