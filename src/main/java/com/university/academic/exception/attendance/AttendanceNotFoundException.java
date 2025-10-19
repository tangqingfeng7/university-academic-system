package com.university.academic.exception.attendance;

import com.university.academic.exception.ErrorCode;

/**
 * 考勤记录不存在异常
 *
 * @author Academic System Team
 */
public class AttendanceNotFoundException extends AttendanceException {

    public AttendanceNotFoundException() {
        super(ErrorCode.ATTENDANCE_RECORD_NOT_FOUND);
    }

    public AttendanceNotFoundException(String message) {
        super(ErrorCode.ATTENDANCE_RECORD_NOT_FOUND, message);
    }
}

