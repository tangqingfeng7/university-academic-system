package com.university.academic.service;

import com.university.academic.dto.LeaveRequestApprovalRequest;
import com.university.academic.dto.LeaveRequestCreateRequest;
import com.university.academic.dto.LeaveRequestDTO;
import com.university.academic.entity.LeaveRequest;
import com.university.academic.entity.Student;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.LeaveRequestRepository;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.TeacherRepository;
import com.university.academic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 请假申请服务
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    /**
     * 学生申请请假
     */
    @Transactional
    public LeaveRequestDTO createStudentLeaveRequest(LeaveRequestCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 获取学生信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 验证日期
        validateDates(request.getStartDate(), request.getEndDate());

        // 计算请假天数
        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // 创建请假申请
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .applicantType(LeaveRequest.ApplicantType.STUDENT)
                .applicantId(student.getId())
                .applicantName(student.getName())
                .applicantNo(student.getStudentNo())
                .leaveType(LeaveRequest.LeaveType.valueOf(request.getLeaveType()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .days(days)
                .reason(request.getReason())
                .status(LeaveRequest.ApprovalStatus.PENDING)
                .attachmentUrl(request.getAttachmentUrl())
                .build();

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        log.info("学生 {} 提交请假申请，ID: {}", student.getName(), leaveRequest.getId());

        return convertToDTO(leaveRequest);
    }

    /**
     * 教师申请请假
     */
    @Transactional
    public LeaveRequestDTO createTeacherLeaveRequest(LeaveRequestCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 获取教师信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Teacher teacher = teacherRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        // 验证日期
        validateDates(request.getStartDate(), request.getEndDate());

        // 计算请假天数
        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        // 创建请假申请
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .applicantType(LeaveRequest.ApplicantType.TEACHER)
                .applicantId(teacher.getId())
                .applicantName(teacher.getName())
                .applicantNo(teacher.getTeacherNo())
                .leaveType(LeaveRequest.LeaveType.valueOf(request.getLeaveType()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .days(days)
                .reason(request.getReason())
                .status(LeaveRequest.ApprovalStatus.PENDING)
                .attachmentUrl(request.getAttachmentUrl())
                .build();

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        log.info("教师 {} 提交请假申请，ID: {}", teacher.getName(), leaveRequest.getId());

        return convertToDTO(leaveRequest);
    }

    /**
     * 审批请假申请
     */
    @Transactional
    public LeaveRequestDTO approveLeaveRequest(Long id, LeaveRequestApprovalRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 获取审批人信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 查询请假申请
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请假申请不存在"));

        // 检查状态
        if (leaveRequest.getStatus() != LeaveRequest.ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "该请假申请已经审批过了");
        }

        // 更新审批信息
        leaveRequest.setStatus(LeaveRequest.ApprovalStatus.valueOf(request.getStatus()));
        leaveRequest.setApproverId(user.getId());
        leaveRequest.setApproverName(username);
        leaveRequest.setApprovalComment(request.getComment());
        leaveRequest.setApprovalTime(LocalDateTime.now());

        leaveRequest = leaveRequestRepository.save(leaveRequest);
        log.info("审批人 {} 审批请假申请 {}，结果: {}", username, id, request.getStatus());

        return convertToDTO(leaveRequest);
    }

    /**
     * 取消请假申请
     */
    @Transactional
    public void cancelLeaveRequest(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请假申请不存在"));

        // 只能取消待审批的申请
        if (leaveRequest.getStatus() != LeaveRequest.ApprovalStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "只能取消待审批的请假申请");
        }

        leaveRequest.setStatus(LeaveRequest.ApprovalStatus.CANCELLED);
        leaveRequestRepository.save(leaveRequest);

        log.info("取消请假申请: {}", id);
    }

    /**
     * 查询我的请假申请列表
     */
    public Page<LeaveRequestDTO> getMyLeaveRequests(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<LeaveRequest> leaveRequests;

        if (user.getRole() == User.UserRole.STUDENT) {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
            leaveRequests = leaveRequestRepository.findByApplicantTypeAndApplicantIdOrderByCreatedAtDesc(
                    LeaveRequest.ApplicantType.STUDENT, student.getId(), pageable);
        } else if (user.getRole() == User.UserRole.TEACHER) {
            Teacher teacher = teacherRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));
            leaveRequests = leaveRequestRepository.findByApplicantTypeAndApplicantIdOrderByCreatedAtDesc(
                    LeaveRequest.ApplicantType.TEACHER, teacher.getId(), pageable);
        } else {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        return leaveRequests.map(this::convertToDTO);
    }

    /**
     * 查询所有请假申请（管理端）
     */
    public Page<LeaveRequestDTO> getAllLeaveRequests(String status, String applicantType, String keyword, Pageable pageable) {
        Page<LeaveRequest> leaveRequests;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 有搜索关键词
            if (status != null && !status.trim().isEmpty()) {
                leaveRequests = leaveRequestRepository.searchByStatus(
                        LeaveRequest.ApprovalStatus.valueOf(status), keyword, pageable);
            } else if (applicantType != null && !applicantType.trim().isEmpty()) {
                leaveRequests = leaveRequestRepository.searchByApplicantType(
                        LeaveRequest.ApplicantType.valueOf(applicantType), keyword, pageable);
            } else {
                leaveRequests = leaveRequestRepository.searchLeaveRequests(keyword, pageable);
            }
        } else {
            // 无搜索关键词
            if (status != null && !status.trim().isEmpty()) {
                leaveRequests = leaveRequestRepository.findByStatusOrderByCreatedAtDesc(
                        LeaveRequest.ApprovalStatus.valueOf(status), pageable);
            } else {
                leaveRequests = leaveRequestRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }

        return leaveRequests.map(this::convertToDTO);
    }

    /**
     * 根据ID查询请假申请详情
     */
    public LeaveRequestDTO getLeaveRequestById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "请假申请不存在"));
        return convertToDTO(leaveRequest);
    }

    /**
     * 统计待审批数量
     */
    public long countPendingLeaveRequests() {
        return leaveRequestRepository.countByStatus(LeaveRequest.ApprovalStatus.PENDING);
    }

    /**
     * 验证日期
     */
    private void validateDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "开始日期不能晚于结束日期");
        }
        if (startDate.isBefore(java.time.LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "开始日期不能早于今天");
        }
    }

    /**
     * 转换为DTO
     */
    private LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest) {
        return LeaveRequestDTO.builder()
                .id(leaveRequest.getId())
                .applicantType(leaveRequest.getApplicantType().name())
                .applicantTypeDescription(leaveRequest.getApplicantType().getDescription())
                .applicantId(leaveRequest.getApplicantId())
                .applicantName(leaveRequest.getApplicantName())
                .applicantNo(leaveRequest.getApplicantNo())
                .leaveType(leaveRequest.getLeaveType().name())
                .leaveTypeDescription(leaveRequest.getLeaveType().getDescription())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .days(leaveRequest.getDays())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus().name())
                .statusDescription(leaveRequest.getStatus().getDescription())
                .approverId(leaveRequest.getApproverId())
                .approverName(leaveRequest.getApproverName())
                .approvalComment(leaveRequest.getApprovalComment())
                .approvalTime(leaveRequest.getApprovalTime())
                .attachmentUrl(leaveRequest.getAttachmentUrl())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }
}

