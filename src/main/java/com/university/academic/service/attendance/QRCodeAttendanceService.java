package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.AttendanceDetail;

/**
 * 二维码签到服务接口
 * 提供扫码签到相关功能
 *
 * @author Academic System Team
 */
public interface QRCodeAttendanceService {

    /**
     * 生成签到二维码
     * 创建唯一的二维码令牌，并设置过期时间
     *
     * @param recordId 考勤记录ID
     * @return 二维码令牌
     */
    String generateQRCode(Long recordId);

    /**
     * 验证二维码有效性
     * 检查令牌是否存在、是否过期
     *
     * @param qrToken 二维码令牌
     * @return true-有效，false-无效
     */
    boolean validateQRCode(String qrToken);

    /**
     * 学生扫码签到
     * 验证二维码并记录签到信息
     *
     * @param qrToken   二维码令牌
     * @param studentId 学生ID
     * @return 考勤明细
     */
    AttendanceDetail checkIn(String qrToken, Long studentId);

    /**
     * 使二维码失效
     * 删除缓存中的二维码令牌
     *
     * @param recordId 考勤记录ID
     */
    void invalidateQRCode(Long recordId);

    /**
     * 刷新二维码
     * 重新生成新的令牌和过期时间
     *
     * @param recordId 考勤记录ID
     * @return 新的二维码令牌
     */
    String refreshQRCode(Long recordId);
}

