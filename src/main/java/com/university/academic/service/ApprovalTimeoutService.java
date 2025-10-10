package com.university.academic.service;

/**
 * 审批超时检查Service接口
 *
 * @author Academic System Team
 */
public interface ApprovalTimeoutService {

    /**
     * 检查并标记所有超时的申请
     *
     * @return 超时申请数量
     */
    int checkAndMarkOverdueApplications();

    /**
     * 发送超时提醒通知
     *
     * @return 发送通知数量
     */
    int sendOverdueReminders();

    /**
     * 发送即将超时提醒通知（提前1天）
     *
     * @return 发送通知数量
     */
    int sendUpcomingDeadlineReminders();
}

