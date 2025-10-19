package com.university.academic.exception.attendance;

import com.university.academic.exception.ErrorCode;

/**
 * 定位超出范围异常
 * 当学生签到位置不在地理围栏内时抛出
 *
 * @author Academic System Team
 */
public class LocationOutOfRangeException extends AttendanceException {

    public LocationOutOfRangeException() {
        super(ErrorCode.LOCATION_OUT_OF_RANGE);
    }

    public LocationOutOfRangeException(String message) {
        super(ErrorCode.LOCATION_OUT_OF_RANGE, message);
    }
}

