package com.university.academic.exception.attendance;

import com.university.academic.exception.ErrorCode;

/**
 * 重复签到异常
 * 当学生尝试重复签到时抛出
 *
 * @author Academic System Team
 */
public class DuplicateCheckinException extends AttendanceException {

    public DuplicateCheckinException() {
        super(ErrorCode.DUPLICATE_CHECKIN);
    }

    public DuplicateCheckinException(String message) {
        super(ErrorCode.DUPLICATE_CHECKIN, message);
    }
}

