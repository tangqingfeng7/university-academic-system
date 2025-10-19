package com.university.academic.exception.attendance;

import com.university.academic.exception.ErrorCode;

/**
 * 签到时间无效异常
 * 当学生在无效的时间窗口内签到时抛出
 *
 * @author Academic System Team
 */
public class InvalidAttendanceTimeException extends AttendanceException {

    public InvalidAttendanceTimeException() {
        super(ErrorCode.INVALID_ATTENDANCE_TIME);
    }

    public InvalidAttendanceTimeException(String message) {
        super(ErrorCode.INVALID_ATTENDANCE_TIME, message);
    }

    public InvalidAttendanceTimeException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidAttendanceTimeException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}

