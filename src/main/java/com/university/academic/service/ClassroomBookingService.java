package com.university.academic.service;

import com.university.academic.controller.dto.ApprovalRequest;
import com.university.academic.controller.dto.BookingQueryDTO;
import com.university.academic.controller.dto.ClassroomBookingDTO;
import com.university.academic.controller.dto.CreateBookingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

/**
 * 教室借用服务接口
 */
public interface ClassroomBookingService {
    
    /**
     * 提交借用申请
     *
     * @param userId 申请人用户ID
     * @param request 借用申请请求
     * @return 借用记录
     */
    ClassroomBookingDTO createBooking(Long userId, CreateBookingRequest request);
    
    /**
     * 检查时间冲突
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否有冲突
     */
    boolean checkConflict(Long classroomId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 检查时间冲突（排除指定借用记录）
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeBookingId 要排除的借用ID
     * @return 是否有冲突
     */
    boolean checkConflictExcluding(Long classroomId, LocalDateTime startTime, 
                                    LocalDateTime endTime, Long excludeBookingId);
    
    /**
     * 审批借用申请
     *
     * @param bookingId 借用ID
     * @param approverId 审批人用户ID
     * @param request 审批请求
     * @return 借用记录
     */
    ClassroomBookingDTO approveBooking(Long bookingId, Long approverId, ApprovalRequest request);
    
    /**
     * 取消借用
     *
     * @param bookingId 借用ID
     * @param userId 用户ID
     */
    void cancelBooking(Long bookingId, Long userId);
    
    /**
     * 查询借用列表
     *
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 借用列表
     */
    Page<ClassroomBookingDTO> getBookings(BookingQueryDTO query, Pageable pageable);
    
    /**
     * 根据ID查询借用记录
     *
     * @param bookingId 借用ID
     * @return 借用记录
     */
    ClassroomBookingDTO getBookingById(Long bookingId);
    
    /**
     * 查询我的借用申请
     *
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 借用列表
     */
    Page<ClassroomBookingDTO> getMyBookings(Long userId, Pageable pageable);
    
    /**
     * 查询待审批的借用申请
     *
     * @param pageable 分页参数
     * @return 借用列表
     */
    Page<ClassroomBookingDTO> getPendingBookings(Pageable pageable);
}

