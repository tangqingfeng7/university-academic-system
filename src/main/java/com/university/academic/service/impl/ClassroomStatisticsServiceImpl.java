package com.university.academic.service.impl;

import com.university.academic.controller.dto.ClassroomDTO;
import com.university.academic.controller.dto.ClassroomUtilizationDTO;
import com.university.academic.controller.dto.ClassroomUtilizationReportDTO;
import com.university.academic.entity.Classroom;
import com.university.academic.entity.ClassroomType;
import com.university.academic.entity.UsageType;
import com.university.academic.repository.ClassroomRepository;
import com.university.academic.repository.ClassroomUsageLogRepository;
import com.university.academic.service.ClassroomService;
import com.university.academic.service.ClassroomStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 教室统计服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomStatisticsServiceImpl implements ClassroomStatisticsService {
    
    private final ClassroomRepository classroomRepository;
    private final ClassroomUsageLogRepository usageLogRepository;
    private final ClassroomService classroomService;
    
    // 工作时间：8:00-22:00，共14小时=840分钟
    private static final int WORK_MINUTES_PER_DAY = 840;
    private static final double HIGH_UTILIZATION_THRESHOLD = 90.0;
    private static final double NORMAL_UTILIZATION_THRESHOLD = 60.0;
    private static final double MEDIUM_UTILIZATION_THRESHOLD = 30.0;
    private static final double LOW_UTILIZATION_THRESHOLD = 30.0;
    
    @Override
    @Cacheable(value = "classroomUtilization", key = "#classroomId + '-' + #startDate + '-' + #endDate")
    public ClassroomUtilizationDTO getClassroomUtilization(Long classroomId, 
                                                            LocalDate startDate, 
                                                            LocalDate endDate) {
        log.debug("统计教室使用率: classroomId={}, {} - {}", classroomId, startDate, endDate);
        
        // 查询教室信息
        ClassroomDTO classroom = classroomService.getClassroomById(classroomId);
        
        // 计算时间范围
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);
        
        // 计算使用时长
        Long totalMinutes = usageLogRepository.calculateUsageMinutes(
            classroomId, startTime, endTime);
        if (totalMinutes == null) {
            totalMinutes = 0L;
        }
        
        // 计算统计天数
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        
        // 计算工作时长
        long workMinutes = totalDays * WORK_MINUTES_PER_DAY;
        
        // 计算使用率
        double utilizationRate = workMinutes > 0 ? 
            (totalMinutes.doubleValue() / workMinutes) * 100 : 0.0;
        utilizationRate = Math.round(utilizationRate * 100.0) / 100.0;
        
        // 统计各类型使用次数
        Map<UsageType, Integer> usageCounts = countUsageByType(classroomId, startTime, endTime);
        
        // 组装结果
        ClassroomUtilizationDTO dto = new ClassroomUtilizationDTO();
        dto.setClassroom(classroom);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setTotalUsageCount(usageCounts.values().stream().mapToInt(Integer::intValue).sum());
        dto.setTotalUsageMinutes(totalMinutes);
        dto.setTotalUsageHours(Math.round(totalMinutes / 60.0 * 100.0) / 100.0);
        dto.setWorkMinutes(workMinutes);
        dto.setUtilizationRate(utilizationRate);
        dto.setCourseUsageCount(usageCounts.getOrDefault(UsageType.COURSE, 0));
        dto.setExamUsageCount(usageCounts.getOrDefault(UsageType.EXAM, 0));
        dto.setBookingUsageCount(usageCounts.getOrDefault(UsageType.BOOKING, 0));
        dto.setAvgDailyUsageHours(
            Math.round((totalMinutes / 60.0 / totalDays) * 100.0) / 100.0);
        
        // 判断使用率等级
        dto.setUtilizationLevel(determineUtilizationLevel(utilizationRate));
        dto.setIsAbnormal(isAbnormalUtilization(utilizationRate));
        
        return dto;
    }
    
    @Override
    @Cacheable(value = "utilizationReport", key = "#startDate + '-' + #endDate")
    public ClassroomUtilizationReportDTO generateUtilizationReport(LocalDate startDate, 
                                                                    LocalDate endDate) {
        log.info("生成使用率报告: {} - {}", startDate, endDate);
        
        // 查询所有未删除的教室
        List<Classroom> classrooms = classroomRepository.findByDeletedFalse(
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        // 计算每个教室的使用率
        List<ClassroomUtilizationDTO> utilizations = classrooms.stream()
            .map(classroom -> getClassroomUtilization(
                classroom.getId(), startDate, endDate))
            .collect(Collectors.toList());
        
        // 生成报告
        return buildReport(utilizations, startDate, endDate);
    }
    
    @Override
    public ClassroomUtilizationReportDTO generateBuildingReport(String building,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate) {
        log.info("生成楼栋使用率报告: building={}, {} - {}", building, startDate, endDate);
        
        // 查询指定楼栋的教室
        List<Classroom> classrooms = classroomRepository.findByBuildingAndDeletedFalse(
            building, org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        // 计算每个教室的使用率
        List<ClassroomUtilizationDTO> utilizations = classrooms.stream()
            .map(classroom -> getClassroomUtilization(
                classroom.getId(), startDate, endDate))
            .collect(Collectors.toList());
        
        // 生成报告
        return buildReport(utilizations, startDate, endDate);
    }
    
    @Override
    public List<ClassroomUtilizationDTO> findAbnormalUtilizationClassrooms(
            LocalDate startDate, LocalDate endDate,
            Double highThreshold, Double lowThreshold) {
        
        log.info("识别使用率异常教室: {} - {}, 高阈值={}, 低阈值={}", 
            startDate, endDate, highThreshold, lowThreshold);
        
        // 使用默认阈值
        double high = highThreshold != null ? highThreshold : HIGH_UTILIZATION_THRESHOLD;
        double low = lowThreshold != null ? lowThreshold : LOW_UTILIZATION_THRESHOLD;
        
        // 查询所有教室的使用率
        List<Classroom> classrooms = classroomRepository.findByDeletedFalse(
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        // 筛选异常教室
        return classrooms.stream()
            .map(classroom -> getClassroomUtilization(
                classroom.getId(), startDate, endDate))
            .filter(util -> util.getUtilizationRate() > high || 
                           util.getUtilizationRate() < low)
            .sorted(Comparator.comparing(ClassroomUtilizationDTO::getUtilizationRate).reversed())
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ClassroomUtilizationDTO> getTopUtilizedClassrooms(LocalDate startDate,
                                                                   LocalDate endDate,
                                                                   Integer limit) {
        log.info("查询使用率最高的教室: {} - {}, limit={}", startDate, endDate, limit);
        
        List<Classroom> classrooms = classroomRepository.findByDeletedFalse(
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        return classrooms.stream()
            .map(classroom -> getClassroomUtilization(
                classroom.getId(), startDate, endDate))
            .sorted(Comparator.comparing(ClassroomUtilizationDTO::getUtilizationRate).reversed())
            .limit(limit != null ? limit : 10)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<ClassroomUtilizationDTO> getBottomUtilizedClassrooms(LocalDate startDate,
                                                                      LocalDate endDate,
                                                                      Integer limit) {
        log.info("查询使用率最低的教室: {} - {}, limit={}", startDate, endDate, limit);
        
        List<Classroom> classrooms = classroomRepository.findByDeletedFalse(
            org.springframework.data.domain.Pageable.unpaged()).getContent();
        
        return classrooms.stream()
            .map(classroom -> getClassroomUtilization(
                classroom.getId(), startDate, endDate))
            .sorted(Comparator.comparing(ClassroomUtilizationDTO::getUtilizationRate))
            .limit(limit != null ? limit : 10)
            .collect(Collectors.toList());
    }
    
    @Override
    public ClassroomUtilizationReportDTO getOverallStatistics(LocalDate startDate, 
                                                               LocalDate endDate) {
        return generateUtilizationReport(startDate, endDate);
    }
    
    /**
     * 统计各类型使用次数
     */
    private Map<UsageType, Integer> countUsageByType(Long classroomId, 
                                                      LocalDateTime startTime, 
                                                      LocalDateTime endTime) {
        Map<UsageType, Integer> counts = new HashMap<>();
        counts.put(UsageType.COURSE, 0);
        counts.put(UsageType.EXAM, 0);
        counts.put(UsageType.BOOKING, 0);
        
        // 这里可以优化为一次查询，暂时使用简单方式
        usageLogRepository.findByClassroomAndTimeRange(classroomId, startTime, endTime)
            .forEach(log -> {
                UsageType type = log.getType();
                counts.put(type, counts.getOrDefault(type, 0) + 1);
            });
        
        return counts;
    }
    
    /**
     * 判断使用率等级
     */
    private String determineUtilizationLevel(double utilizationRate) {
        if (utilizationRate >= HIGH_UTILIZATION_THRESHOLD) {
            return "HIGH";
        } else if (utilizationRate >= NORMAL_UTILIZATION_THRESHOLD) {
            return "NORMAL";
        } else if (utilizationRate >= MEDIUM_UTILIZATION_THRESHOLD) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
    
    /**
     * 判断是否异常
     */
    private boolean isAbnormalUtilization(double utilizationRate) {
        return utilizationRate > HIGH_UTILIZATION_THRESHOLD || 
               utilizationRate < LOW_UTILIZATION_THRESHOLD;
    }
    
    /**
     * 构建报告
     */
    private ClassroomUtilizationReportDTO buildReport(List<ClassroomUtilizationDTO> utilizations,
                                                       LocalDate startDate,
                                                       LocalDate endDate) {
        ClassroomUtilizationReportDTO report = new ClassroomUtilizationReportDTO();
        report.setReportDate(LocalDate.now());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalDays((int) (ChronoUnit.DAYS.between(startDate, endDate) + 1));
        report.setTotalClassrooms(utilizations.size());
        
        // 计算平均使用率
        double avgRate = utilizations.stream()
            .mapToDouble(ClassroomUtilizationDTO::getUtilizationRate)
            .average()
            .orElse(0.0);
        report.setAverageUtilizationRate(Math.round(avgRate * 100.0) / 100.0);
        
        // 计算最高和最低使用率
        report.setMaxUtilizationRate(
            utilizations.stream()
                .mapToDouble(ClassroomUtilizationDTO::getUtilizationRate)
                .max()
                .orElse(0.0)
        );
        report.setMinUtilizationRate(
            utilizations.stream()
                .mapToDouble(ClassroomUtilizationDTO::getUtilizationRate)
                .min()
                .orElse(0.0)
        );
        
        // 统计各等级数量
        long highCount = utilizations.stream()
            .filter(u -> "HIGH".equals(u.getUtilizationLevel()))
            .count();
        long normalCount = utilizations.stream()
            .filter(u -> "NORMAL".equals(u.getUtilizationLevel()))
            .count();
        long mediumCount = utilizations.stream()
            .filter(u -> "MEDIUM".equals(u.getUtilizationLevel()))
            .count();
        long lowCount = utilizations.stream()
            .filter(u -> "LOW".equals(u.getUtilizationLevel()))
            .count();
        
        report.setHighUtilizationCount((int) highCount);
        report.setNormalUtilizationCount((int) normalCount);
        report.setMediumUtilizationCount((int) mediumCount);
        report.setLowUtilizationCount((int) lowCount);
        
        // 设置详情列表
        report.setClassroomUtilizations(utilizations);
        
        // 筛选高使用率教室
        report.setHighUtilizationClassrooms(
            utilizations.stream()
                .filter(u -> "HIGH".equals(u.getUtilizationLevel()))
                .sorted(Comparator.comparing(
                    ClassroomUtilizationDTO::getUtilizationRate).reversed())
                .collect(Collectors.toList())
        );
        
        // 筛选低使用率教室
        report.setLowUtilizationClassrooms(
            utilizations.stream()
                .filter(u -> "LOW".equals(u.getUtilizationLevel()))
                .sorted(Comparator.comparing(ClassroomUtilizationDTO::getUtilizationRate))
                .collect(Collectors.toList())
        );
        
        // 按楼栋统计
        report.setBuildingUtilizations(buildBuildingStatistics(utilizations));
        
        // 按类型统计
        report.setTypeUtilizations(buildTypeStatistics(utilizations));
        
        return report;
    }
    
    /**
     * 构建楼栋统计
     */
    private List<ClassroomUtilizationReportDTO.BuildingUtilization> buildBuildingStatistics(
            List<ClassroomUtilizationDTO> utilizations) {
        
        Map<String, List<ClassroomUtilizationDTO>> byBuilding = utilizations.stream()
            .collect(Collectors.groupingBy(u -> u.getClassroom().getBuilding()));
        
        return byBuilding.entrySet().stream()
            .map(entry -> {
                ClassroomUtilizationReportDTO.BuildingUtilization stat = 
                    new ClassroomUtilizationReportDTO.BuildingUtilization();
                stat.setBuilding(entry.getKey());
                stat.setClassroomCount(entry.getValue().size());
                
                double avgRate = entry.getValue().stream()
                    .mapToDouble(ClassroomUtilizationDTO::getUtilizationRate)
                    .average()
                    .orElse(0.0);
                stat.setAvgUtilizationRate(Math.round(avgRate * 100.0) / 100.0);
                
                double totalHours = entry.getValue().stream()
                    .mapToDouble(ClassroomUtilizationDTO::getTotalUsageHours)
                    .sum();
                stat.setTotalUsageHours(Math.round(totalHours * 100.0) / 100.0);
                
                return stat;
            })
            .sorted(Comparator.comparing(
                ClassroomUtilizationReportDTO.BuildingUtilization::getAvgUtilizationRate)
                .reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * 构建类型统计
     */
    private List<ClassroomUtilizationReportDTO.TypeUtilization> buildTypeStatistics(
            List<ClassroomUtilizationDTO> utilizations) {
        
        Map<ClassroomType, List<ClassroomUtilizationDTO>> byType = utilizations.stream()
            .collect(Collectors.groupingBy(u -> u.getClassroom().getType()));
        
        return byType.entrySet().stream()
            .map(entry -> {
                ClassroomUtilizationReportDTO.TypeUtilization stat = 
                    new ClassroomUtilizationReportDTO.TypeUtilization();
                stat.setType(entry.getKey().name());
                stat.setTypeDescription(entry.getKey().getDescription());
                stat.setClassroomCount(entry.getValue().size());
                
                double avgRate = entry.getValue().stream()
                    .mapToDouble(ClassroomUtilizationDTO::getUtilizationRate)
                    .average()
                    .orElse(0.0);
                stat.setAvgUtilizationRate(Math.round(avgRate * 100.0) / 100.0);
                
                double totalHours = entry.getValue().stream()
                    .mapToDouble(ClassroomUtilizationDTO::getTotalUsageHours)
                    .sum();
                stat.setTotalUsageHours(Math.round(totalHours * 100.0) / 100.0);
                
                return stat;
            })
            .sorted(Comparator.comparing(
                ClassroomUtilizationReportDTO.TypeUtilization::getAvgUtilizationRate)
                .reversed())
            .collect(Collectors.toList());
    }
}

