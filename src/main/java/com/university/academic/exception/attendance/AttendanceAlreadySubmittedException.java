package com.university.academic.exception.attendance;

import com.university.academic.exception.ErrorCode;

/**
 * 考勤已提交异常
 * 当尝试修改已提交的考勤记录时抛出
 *
 * @author Academic System Team
 */
public class AttendanceAlreadySubmittedException extends AttendanceException {

    public AttendanceAlreadySubmittedException() {
        super(ErrorCode.ATTENDANCE_ALREADY_SUBMITTED);
    }

    public AttendanceAlreadySubmittedException(String message) {
        super(ErrorCode.ATTENDANCE_ALREADY_SUBMITTED, message);
    }
}

