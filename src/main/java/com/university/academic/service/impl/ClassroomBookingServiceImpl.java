package com.university.academic.service.impl;

import com.university.academic.controller.dto.*;
import com.university.academic.dto.CreateNotificationRequest;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.*;
import com.university.academic.service.ClassroomBookingService;
import com.university.academic.service.ClassroomService;
import com.university.academic.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 教室借用服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomBookingServiceImpl implements ClassroomBookingService {
    
    private final ClassroomBookingRepository bookingRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomUsageLogRepository usageLogRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ClassroomService classroomService;
    private final NotificationService notificationService;
    
    @Override
    @Transactional
    public ClassroomBookingDTO createBooking(Long userId, CreateBookingRequest request) {
        log.info("创建教室借用申请: userId={}, classroomId={}", userId, request.getClassroomId());
        
        // 验证时间
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE, 
                "开始时间不能晚于结束时间");
        }
        
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE, 
                "开始时间不能早于当前时间");
        }
        
        // 查询教室
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
            .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND));
        
        // 检查教室状态
        if (classroom.getStatus() != ClassroomStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_AVAILABLE, 
                "教室当前不可用");
        }
        
        // 检查时间冲突
        if (checkConflict(request.getClassroomId(), request.getStartTime(), request.getEndTime())) {
            throw new BusinessException(ErrorCode.CLASSROOM_TIME_CONFLICT, 
                "该时间段教室已被占用");
        }
        
        // 检查容量
        if (request.getExpectedAttendees() != null && 
            request.getExpectedAttendees() > classroom.getCapacity()) {
            throw new BusinessException(ErrorCode.CLASSROOM_CAPACITY_EXCEEDED, 
                "预计人数超过教室容量");
        }
        
        // 创建借用记录
        ClassroomBooking booking = new ClassroomBooking();
        booking.setClassroom(classroom);
        booking.setApplicantId(userId);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setPurpose(request.getPurpose());
        booking.setExpectedAttendees(request.getExpectedAttendees());
        booking.setStatus(BookingStatus.PENDING);
        
        booking = bookingRepository.save(booking);
        
        log.info("教室借用申请创建成功: bookingId={}", booking.getId());
        
        // 发送通知给管理员
        sendNotificationToAdmin(booking, "新教室借用申请");
        
        return convertToDTO(booking);
    }
    
    @Override
    public boolean checkConflict(Long classroomId, LocalDateTime startTime, LocalDateTime endTime) {
        // 检查借用记录冲突
        boolean hasBookingConflict = bookingRepository.existsConflict(
            classroomId, startTime, endTime);
        
        // 检查使用记录冲突
        boolean hasUsageConflict = usageLogRepository.existsConflict(
            classroomId, startTime, endTime);
        
        return hasBookingConflict || hasUsageConflict;
    }
    
    @Override
    public boolean checkConflictExcluding(Long classroomId, LocalDateTime startTime, 
                                           LocalDateTime endTime, Long excludeBookingId) {
        // 检查借用记录冲突（排除指定记录）
        boolean hasBookingConflict = bookingRepository.existsConflictExcluding(
            classroomId, startTime, endTime, excludeBookingId);
        
        // 检查使用记录冲突
        boolean hasUsageConflict = usageLogRepository.existsConflict(
            classroomId, startTime, endTime);
        
        return hasBookingConflict || hasUsageConflict;
    }
    
    @Override
    @Transactional
    public ClassroomBookingDTO approveBooking(Long bookingId, Long approverId, 
                                               ApprovalRequest request) {
        log.info("审批教室借用: bookingId={}, approverId={}, approved={}", 
            bookingId, approverId, request.getApproved());
        
        // 查询借用记录（预加载教室信息）
        ClassroomBooking booking = bookingRepository.findByIdWithClassroom(bookingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND, 
                "借用记录不存在"));
        
        // 检查状态
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_APPROVED, 
                "借用申请已处理");
        }
        
        // 如果批准，再次检查时间冲突（防止并发）
        if (request.getApproved()) {
            if (checkConflictExcluding(booking.getClassroom().getId(), 
                    booking.getStartTime(), booking.getEndTime(), bookingId)) {
                throw new BusinessException(ErrorCode.BOOKING_TIME_CONFLICT, 
                    "该时间段教室已被占用");
            }
            
            booking.setStatus(BookingStatus.APPROVED);
            
            // 创建使用记录日志
            ClassroomUsageLog usageLog = new ClassroomUsageLog();
            usageLog.setClassroom(booking.getClassroom());
            usageLog.setType(UsageType.BOOKING);
            usageLog.setReferenceId(booking.getId());
            usageLog.setStartTime(booking.getStartTime());
            usageLog.setEndTime(booking.getEndTime());
            usageLog.setDescription(booking.getPurpose());
            usageLogRepository.save(usageLog);
            
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        
        booking.setApprovedBy(approverId);
        booking.setApprovalComment(request.getComment());
        booking.setApprovedAt(LocalDateTime.now());
        
        booking = bookingRepository.save(booking);
        
        log.info("教室借用审批成功: bookingId={}, status={}", bookingId, booking.getStatus());
        
        // 发送审批结果通知给申请人
        sendApprovalNotification(booking);
        
        return convertToDTO(booking);
    }
    
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        log.info("取消教室借用: bookingId={}, userId={}", bookingId, userId);
        
        // 查询借用记录（预加载教室信息）
        ClassroomBooking booking = bookingRepository.findByIdWithClassroom(bookingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND, 
                "借用记录不存在"));
        
        // 验证权限（只能取消自己的申请）
        if (!booking.getApplicantId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, 
                "无权取消他人的借用申请");
        }
        
        // 检查状态
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_CANCELLED, 
                "借用申请已取消");
        }
        
        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BOOKING_CANNOT_CANCEL, 
                "已拒绝的申请无法取消");
        }
        
        // 如果已批准，需要删除使用记录日志
        if (booking.getStatus() == BookingStatus.APPROVED) {
            usageLogRepository.deleteByReferenceIdAndType(bookingId, UsageType.BOOKING);
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        log.info("教室借用取消成功: bookingId={}", bookingId);
    }
    
    @Override
    public Page<ClassroomBookingDTO> getBookings(BookingQueryDTO query, Pageable pageable) {
        log.debug("查询教室借用列表: query={}", query);
        
        Page<ClassroomBooking> bookings = bookingRepository.findByConditions(
            query.getApplicantId(),
            query.getClassroomId(),
            query.getStatus(),
            query.getStartDate(),
            query.getEndDate(),
            pageable
        );
        
        return bookings.map(this::convertToDTO);
    }
    
    @Override
    public ClassroomBookingDTO getBookingById(Long bookingId) {
        log.debug("根据ID查询教室借用: bookingId={}", bookingId);
        
        ClassroomBooking booking = bookingRepository.findByIdWithClassroom(bookingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND, 
                "借用记录不存在"));
        
        return convertToDTO(booking);
    }
    
    @Override
    public Page<ClassroomBookingDTO> getMyBookings(Long userId, Pageable pageable) {
        log.debug("查询我的教室借用: userId={}", userId);
        
        Page<ClassroomBooking> bookings = bookingRepository.findByApplicantId(userId, pageable);
        return bookings.map(this::convertToDTO);
    }
    
    @Override
    public Page<ClassroomBookingDTO> getPendingBookings(Pageable pageable) {
        log.debug("查询待审批的教室借用");
        
        Page<ClassroomBooking> bookings = bookingRepository.findByStatus(
            BookingStatus.PENDING, pageable);
        return bookings.map(this::convertToDTO);
    }
    
    /**
     * 转换为DTO
     */
    private ClassroomBookingDTO convertToDTO(ClassroomBooking booking) {
        ClassroomBookingDTO dto = new ClassroomBookingDTO();
        dto.setId(booking.getId());
        dto.setClassroomId(booking.getClassroom().getId());
        dto.setClassroomNo(booking.getClassroom().getRoomNo());
        dto.setClassroomName(booking.getClassroom().getBuilding() + " " + 
            booking.getClassroom().getRoomNo());
        dto.setApplicantId(booking.getApplicantId());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setPurpose(booking.getPurpose());
        dto.setExpectedAttendees(booking.getExpectedAttendees());
        dto.setStatus(booking.getStatus());
        dto.setStatusDescription(booking.getStatus().getDescription());
        dto.setApprovedBy(booking.getApprovedBy());
        dto.setApprovalComment(booking.getApprovalComment());
        dto.setApprovedAt(booking.getApprovedAt());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        
        // 填充申请人姓名
        teacherRepository.findByUserId(booking.getApplicantId())
            .ifPresentOrElse(
                teacher -> dto.setApplicantName(teacher.getName()),
                () -> studentRepository.findByUserId(booking.getApplicantId())
                    .ifPresent(student -> dto.setApplicantName(student.getName()))
            );
        
        // 填充审批人姓名
        if (booking.getApprovedBy() != null) {
            teacherRepository.findByUserId(booking.getApprovedBy())
                .ifPresentOrElse(
                    teacher -> dto.setApproverName(teacher.getName()),
                    () -> userRepository.findById(booking.getApprovedBy())
                        .ifPresent(user -> dto.setApproverName(user.getUsername()))
                );
        }
        
        // 填充教室详细信息
        dto.setClassroom(classroomService.getClassroomById(booking.getClassroom().getId()));
        
        return dto;
    }
    
    /**
     * 发送通知给管理员
     */
    private void sendNotificationToAdmin(ClassroomBooking booking, String title) {
        try {
            String content = String.format(
                "用户申请借用教室 %s，时间：%s 至 %s，目的：%s",
                booking.getClassroom().getRoomNo(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
            );
            
            CreateNotificationRequest notification = new CreateNotificationRequest();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("SYSTEM");
            notification.setTargetRole("ADMIN");
            
            // 使用系统用户ID（假设为1）发送通知
            notificationService.publishNotification(notification, 1L);
            
            log.info("已发送通知给管理员: bookingId={}", booking.getId());
        } catch (Exception e) {
            log.error("发送通知失败: ", e);
            // 不影响主流程
        }
    }
    
    /**
     * 发送审批结果通知
     */
    private void sendApprovalNotification(ClassroomBooking booking) {
        try {
            String title = booking.getStatus() == BookingStatus.APPROVED ? 
                "教室借用申请已批准" : "教室借用申请已拒绝";
            
            String content = String.format(
                "您申请借用教室 %s 的申请已%s。\n时间：%s 至 %s\n审批意见：%s",
                booking.getClassroom().getRoomNo(),
                booking.getStatus() == BookingStatus.APPROVED ? "批准" : "拒绝",
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getApprovalComment()
            );
            
            CreateNotificationRequest notification = new CreateNotificationRequest();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("SYSTEM");
            notification.setTargetRole("ALL");
            
            // 使用审批人ID发送通知
            notificationService.publishNotification(notification, booking.getApprovedBy());
            
            log.info("已发送审批结果通知: bookingId={}", booking.getId());
        } catch (Exception e) {
            log.error("发送通知失败: ", e);
            // 不影响主流程
        }
    }
}

