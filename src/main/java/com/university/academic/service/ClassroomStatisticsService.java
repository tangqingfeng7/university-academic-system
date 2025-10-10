package com.university.academic.service;

import com.university.academic.controller.dto.ClassroomUtilizationDTO;
import com.university.academic.controller.dto.ClassroomUtilizationReportDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * 教室统计服务接口
 */
public interface ClassroomStatisticsService {
    
    /**
     * 统计教室使用率
     *
     * @param classroomId 教室ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 教室使用率
     */
    ClassroomUtilizationDTO getClassroomUtilization(Long classroomId, 
                                                     LocalDate startDate, 
                                                     LocalDate endDate);
    
    /**
     * 生成使用率报告
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 使用率报告
     */
    ClassroomUtilizationReportDTO generateUtilizationReport(LocalDate startDate, 
                                                             LocalDate endDate);
    
    /**
     * 生成指定楼栋的使用率报告
     *
     * @param building 楼栋
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 使用率报告
     */
    ClassroomUtilizationReportDTO generateBuildingReport(String building,
                                                          LocalDate startDate,
                                                          LocalDate endDate);
    
    /**
     * 识别使用率异常的教室（过高或过低）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param highThreshold 高使用率阈值（默认80%）
     * @param lowThreshold 低使用率阈值（默认30%）
     * @return 异常教室列表
     */
    List<ClassroomUtilizationDTO> findAbnormalUtilizationClassrooms(LocalDate startDate,
                                                                     LocalDate endDate,
                                                                     Double highThreshold,
                                                                     Double lowThreshold);
    
    /**
     * 获取使用率最高的教室
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回数量
     * @return 教室列表
     */
    List<ClassroomUtilizationDTO> getTopUtilizedClassrooms(LocalDate startDate,
                                                            LocalDate endDate,
                                                            Integer limit);
    
    /**
     * 获取使用率最低的教室
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回数量
     * @return 教室列表
     */
    List<ClassroomUtilizationDTO> getBottomUtilizedClassrooms(LocalDate startDate,
                                                               LocalDate endDate,
                                                               Integer limit);
    
    /**
     * 统计指定时间段内的整体使用情况
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计摘要
     */
    ClassroomUtilizationReportDTO getOverallStatistics(LocalDate startDate, 
                                                        LocalDate endDate);
}

