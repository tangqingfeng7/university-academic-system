package com.university.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.academic.dto.ApproveCourseChangeRequest;
import com.university.academic.dto.CourseChangeRequestDTO;
import com.university.academic.dto.CreateCourseChangeRequest;
import com.university.academic.entity.CourseChangeRequest;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Teacher;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseChangeRequestRepository;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 调课申请服务
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseChangeRequestService {

    private final CourseChangeRequestRepository changeRequestRepository;
    private final CourseOfferingRepository offeringRepository;
    private final TeacherRepository teacherRepository;
    private final ObjectMapper objectMapper;
    private final com.university.academic.security.CustomUserDetailsService userDetailsService;

    /**
     * 创建调课申请
     */
    @Transactional
    public CourseChangeRequestDTO createChangeRequest(CreateCourseChangeRequest request) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        
        // 获取教师信息
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        // 获取开课计划
        CourseOffering offering = offeringRepository.findById(request.getOfferingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        // 验证教师是否是该课程的授课教师
        if (!offering.getTeacher().getId().equals(teacher.getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "您不是该课程的授课教师");
        }

        // 检查是否有待审批的调课申请
        List<CourseChangeRequest> pendingRequests = changeRequestRepository
                .findPendingOrApprovedByOfferingId(offering.getId());
        if (!pendingRequests.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该课程已有待审批的调课申请");
        }

        // 验证新时间安排格式
        try {
            List<Map<String, Object>> scheduleList = objectMapper.readValue(
                    request.getNewSchedule(), new TypeReference<List<Map<String, Object>>>() {});
            
            if (scheduleList.isEmpty()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "新时间安排不能为空");
            }
        } catch (Exception e) {
            log.error("解析新时间安排失败", e);
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "新时间安排格式错误");
        }

        // 创建调课申请
        CourseChangeRequest changeRequest = CourseChangeRequest.builder()
                .teacherId(teacher.getId())
                .teacherName(teacher.getName())
                .offeringId(offering.getId())
                .courseName(offering.getCourse().getName())
                .originalSchedule(offering.getSchedule())
                .newSchedule(request.getNewSchedule())
                .reason(request.getReason())
                .status(CourseChangeRequest.RequestStatus.PENDING)
                .build();

        changeRequest = changeRequestRepository.save(changeRequest);
        log.info("创建调课申请成功: id={}, teacher={}, offering={}", 
                changeRequest.getId(), teacher.getName(), offering.getCourse().getName());

        return convertToDTO(changeRequest);
    }

    /**
     * 审批调课申请
     */
    @Transactional
    public CourseChangeRequestDTO approveChangeRequest(Long requestId, ApproveCourseChangeRequest request) {
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long userId = userDetailsService.getUserIdFromAuth(authentication);

        // 获取调课申请
        CourseChangeRequest changeRequest = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "调课申请不存在"));

        // 检查状态
        if (changeRequest.getStatus() != CourseChangeRequest.RequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "该申请已被处理");
        }

        // 更新状态
        changeRequest.setStatus(request.getApproved() ? 
                CourseChangeRequest.RequestStatus.APPROVED : 
                CourseChangeRequest.RequestStatus.REJECTED);
        changeRequest.setApproverId(userId);
        changeRequest.setApproverName(username);
        changeRequest.setApprovalComment(request.getComment());
        changeRequest.setApprovalTime(LocalDateTime.now());

        // 如果通过，更新开课计划的时间安排
        if (request.getApproved()) {
            CourseOffering offering = offeringRepository.findById(changeRequest.getOfferingId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));
            
            // 更新时间安排
            offering.setSchedule(changeRequest.getNewSchedule());
            
            // 解析新时间安排，更新地点
            try {
                List<Map<String, Object>> scheduleList = objectMapper.readValue(
                        changeRequest.getNewSchedule(), new TypeReference<List<Map<String, Object>>>() {});
                if (!scheduleList.isEmpty() && scheduleList.get(0).containsKey("location")) {
                    String newLocation = scheduleList.get(0).get("location").toString();
                    offering.setLocation(newLocation);
                }
            } catch (Exception e) {
                log.warn("解析调课后的地点失败", e);
            }
            
            offeringRepository.save(offering);
            
            log.info("调课审批通过，已更新开课计划: offeringId={}, newSchedule={}", 
                    offering.getId(), changeRequest.getNewSchedule());
        }

        changeRequest = changeRequestRepository.save(changeRequest);
        log.info("调课审批完成: id={}, status={}, approver={}", 
                changeRequest.getId(), changeRequest.getStatus(), username);

        return convertToDTO(changeRequest);
    }

    /**
     * 查询教师的调课申请列表
     */
    public Page<CourseChangeRequestDTO> getTeacherChangeRequests(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userDetailsService.getUserIdFromAuth(authentication);
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return changeRequestRepository.findByTeacherId(teacher.getId(), pageable)
                .map(this::convertToDTO);
    }

    /**
     * 查询所有调课申请列表（管理员）
     */
    public Page<CourseChangeRequestDTO> getAllChangeRequests(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (status != null && !status.isEmpty()) {
            CourseChangeRequest.RequestStatus requestStatus = CourseChangeRequest.RequestStatus.valueOf(status);
            return changeRequestRepository.findByStatus(requestStatus, pageable)
                    .map(this::convertToDTO);
        } else {
            return changeRequestRepository.findAll(pageable)
                    .map(this::convertToDTO);
        }
    }

    /**
     * 获取调课申请详情
     */
    public CourseChangeRequestDTO getChangeRequestDetail(Long requestId) {
        CourseChangeRequest changeRequest = changeRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "调课申请不存在"));
        return convertToDTO(changeRequest);
    }

    /**
     * 统计待审批数量
     */
    public long countPendingRequests() {
        return changeRequestRepository.countByStatus(CourseChangeRequest.RequestStatus.PENDING);
    }

    /**
     * 转换为DTO
     */
    private CourseChangeRequestDTO convertToDTO(CourseChangeRequest entity) {
        return CourseChangeRequestDTO.builder()
                .id(entity.getId())
                .teacherId(entity.getTeacherId())
                .teacherName(entity.getTeacherName())
                .offeringId(entity.getOfferingId())
                .courseName(entity.getCourseName())
                .originalSchedule(entity.getOriginalSchedule())
                .newSchedule(entity.getNewSchedule())
                .reason(entity.getReason())
                .status(entity.getStatus().name())
                .approverId(entity.getApproverId())
                .approverName(entity.getApproverName())
                .approvalComment(entity.getApprovalComment())
                .approvalTime(entity.getApprovalTime())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

