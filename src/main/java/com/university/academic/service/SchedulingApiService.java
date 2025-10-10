package com.university.academic.service;

import com.university.academic.controller.dto.AvailableClassroomRequest;
import com.university.academic.controller.dto.ClassroomAvailabilityDTO;
import com.university.academic.controller.dto.ClassroomDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 排课系统API服务接口
 * 为排课系统提供教室资源查询和占用检查功能
 */
public interface SchedulingApiService {
    
    /**
     * 查询可用教室列表（用于排课）
     * 
     * @param request 查询请求
     * @return 可用教室列表
     */
    List<ClassroomDTO> findAvailableClassrooms(AvailableClassroomRequest request);
    
    /**
     * 批量检查教室可用性
     * 
     * @param classroomIds 教室ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 教室可用性检查结果列表
     */
    List<ClassroomAvailabilityDTO> checkClassroomAvailability(List<Long> classroomIds,
                                                               LocalDateTime startTime,
                                                               LocalDateTime endTime);
    
    /**
     * 检查单个教室的可用性（包含冲突详情）
     * 
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 教室可用性检查结果
     */
    ClassroomAvailabilityDTO checkSingleClassroomAvailability(Long classroomId,
                                                               LocalDateTime startTime,
                                                               LocalDateTime endTime);
    
    /**
     * 为课程推荐教室
     * 根据容量、类型等条件推荐最合适的教室
     * 
     * @param capacity 所需容量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param preferredBuilding 偏好楼栋（可选）
     * @return 推荐的教室列表（按优先级排序）
     */
    List<ClassroomDTO> recommendClassrooms(Integer capacity,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime,
                                           String preferredBuilding);
    
    /**
     * 查询教室在指定日期的空闲时间段
     * 
     * @param classroomId 教室ID
     * @param date 日期
     * @return 空闲时间段列表
     */
    List<TimeSlot> getFreeTimeSlots(Long classroomId, java.time.LocalDate date);
    
    /**
     * 时间段DTO
     */
    class TimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer durationMinutes;
        
        public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        
        public LocalDateTime getStartTime() {
            return startTime;
        }
        
        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }
        
        public LocalDateTime getEndTime() {
            return endTime;
        }
        
        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }
        
        public Integer getDurationMinutes() {
            return durationMinutes;
        }
        
        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
        }
    }
}

