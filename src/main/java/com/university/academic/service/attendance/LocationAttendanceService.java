package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.AttendanceDetail;

/**
 * 定位签到服务接口
 * 提供基于地理位置的签到功能
 *
 * @author Academic System Team
 */
public interface LocationAttendanceService {

    /**
     * 设置地理围栏
     * 为考勤记录设置签到的地理位置和有效范围
     *
     * @param recordId 考勤记录ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param radius 围栏半径（米）
     */
    void setGeofence(Long recordId, Double latitude, Double longitude, Integer radius);

    /**
     * 验证位置是否在地理围栏内
     * 使用Haversine公式计算两点间的距离
     *
     * @param lat1 位置1纬度
     * @param lon1 位置1经度
     * @param lat2 位置2纬度
     * @param lon2 位置2经度
     * @param radius 有效半径（米）
     * @return true-在范围内，false-不在范围内
     */
    boolean validateLocation(Double lat1, Double lon1, Double lat2, Double lon2, Integer radius);

    /**
     * 学生定位签到
     * 验证学生位置并记录签到信息
     *
     * @param recordId 考勤记录ID
     * @param studentId 学生ID
     * @param latitude 学生纬度
     * @param longitude 学生经度
     * @return 考勤明细
     */
    AttendanceDetail checkIn(Long recordId, Long studentId, Double latitude, Double longitude);

    /**
     * 计算两个地理位置之间的距离（米）
     * 使用Haversine公式
     *
     * @param lat1 位置1纬度
     * @param lon1 位置1经度
     * @param lat2 位置2纬度
     * @param lon2 位置2经度
     * @return 距离（米）
     */
    double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2);
}

