package com.university.academic.service.attendance;

import com.university.academic.entity.attendance.AttendanceRequest;
import com.university.academic.entity.attendance.RequestStatus;

import java.util.List;

/**
 * 考勤申诉服务接口
 * 处理学生的补签和申诉请求
 *
 * @author Academic System Team
 */
public interface AttendanceAppealService {

    /**
     * 提交补签申请
     *
     * @param studentId     学生ID
     * @param detailId      考勤明细ID
     * @param reason        申请原因
     * @param attachmentUrl 附件URL（可选）
     * @return 申请记录
     */
    AttendanceRequest submitMakeupRequest(Long studentId, Long detailId, String reason, String attachmentUrl);

    /**
     * 提交考勤申诉
     *
     * @param studentId     学生ID
     * @param detailId      考勤明细ID
     * @param reason        申诉原因
     * @param attachmentUrl 附件URL（可选）
     * @return 申请记录
     */
    AttendanceRequest submitAppeal(Long studentId, Long detailId, String reason, String attachmentUrl);

    /**
     * 批准申请
     *
     * @param requestId       申请ID
     * @param approvalComment 审批意见
     * @return 申请记录
     */
    AttendanceRequest approveRequest(Long requestId, String approvalComment);

    /**
     * 拒绝申请
     *
     * @param requestId 申请ID
     * @param reason    拒绝原因
     * @return 申请记录
     */
    AttendanceRequest rejectRequest(Long requestId, String reason);

    /**
     * 获取学生的申请列表
     *
     * @param studentId 学生ID
     * @return 申请列表
     */
    List<AttendanceRequest> getStudentRequests(Long studentId);

    /**
     * 获取学生指定状态的申请
     *
     * @param studentId 学生ID
     * @param status    申请状态
     * @return 申请列表
     */
    List<AttendanceRequest> getStudentRequests(Long studentId, RequestStatus status);

    /**
     * 获取教师的待审批申请列表
     *
     * @param teacherId 教师ID
     * @return 申请列表
     */
    List<AttendanceRequest> getPendingRequests(Long teacherId);

    /**
     * 获取教师的所有申请列表
     *
     * @param teacherId 教师ID
     * @param status    申请状态（null表示全部）
     * @return 申请列表
     */
    List<AttendanceRequest> getTeacherRequests(Long teacherId, RequestStatus status);

    /**
     * 获取申请详情
     *
     * @param requestId 申请ID
     * @return 申请记录
     */
    AttendanceRequest getRequestDetails(Long requestId);

    /**
     * 取消申请（学生）
     *
     * @param requestId 申请ID
     */
    void cancelRequest(Long requestId);
}

