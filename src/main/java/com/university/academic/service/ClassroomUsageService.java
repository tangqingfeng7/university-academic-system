package com.university.academic.service;

import com.university.academic.controller.dto.ClassroomUsageDTO;
import com.university.academic.controller.dto.ClassroomScheduleDTO;
import com.university.academic.controller.dto.ClassroomUsageItemDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 教室使用情况查询服务接口
 */
public interface ClassroomUsageService {
    
    /**
     * 查询教室在指定日期的使用情况
     *
     * @param classroomId 教室ID
     * @param date 查询日期
     * @return 教室使用情况
     */
    ClassroomUsageDTO getClassroomUsage(Long classroomId, LocalDate date);
    
    /**
     * 查询教室在指定时间范围内的使用情况
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表
     */
    List<ClassroomUsageItemDTO> getClassroomUsageInRange(Long classroomId, 
                                                          LocalDateTime startTime, 
                                                          LocalDateTime endTime);
    
    /**
     * 生成教室一周课表
     *
     * @param classroomId 教室ID
     * @param startDate 开始日期（周一）
     * @return 教室课表
     */
    ClassroomScheduleDTO generateWeeklySchedule(Long classroomId, LocalDate startDate);
    
    /**
     * 查询教室当前周的课表
     *
     * @param classroomId 教室ID
     * @return 教室课表
     */
    ClassroomScheduleDTO getCurrentWeekSchedule(Long classroomId);
    
    /**
     * 查询多个教室在指定时间段的使用情况
     *
     * @param classroomIds 教室ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表（按教室ID分组）
     */
    List<ClassroomUsageItemDTO> getBatchClassroomUsage(List<Long> classroomIds,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime);
}

