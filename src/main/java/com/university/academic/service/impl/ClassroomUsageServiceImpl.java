package com.university.academic.service.impl;

import com.university.academic.controller.dto.*;
import com.university.academic.entity.*;
import com.university.academic.repository.*;
import com.university.academic.service.ClassroomService;
import com.university.academic.service.ClassroomUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * 教室使用情况查询服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomUsageServiceImpl implements ClassroomUsageService {
    
    private final ClassroomService classroomService;
    private final ClassroomUsageLogRepository usageLogRepository;
    private final ClassroomBookingRepository bookingRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final ExamRepository examRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    
    @Override
    @Cacheable(value = "classroomUsage", key = "#classroomId + '-' + #date")
    public ClassroomUsageDTO getClassroomUsage(Long classroomId, LocalDate date) {
        log.debug("查询教室使用情况: classroomId={}, date={}", classroomId, date);
        
        // 查询教室信息
        ClassroomDTO classroom = classroomService.getClassroomById(classroomId);
        
        // 计算时间范围（当天0:00到23:59）
        LocalDateTime startTime = date.atStartOfDay();
        LocalDateTime endTime = date.atTime(LocalTime.MAX);
        
        // 获取使用记录
        List<ClassroomUsageItemDTO> usageItems = getClassroomUsageInRange(
            classroomId, startTime, endTime);
        
        // 计算统计信息
        ClassroomUsageDTO.UsageStatistics statistics = calculateStatistics(usageItems);
        
        // 组装结果
        ClassroomUsageDTO result = new ClassroomUsageDTO();
        result.setClassroom(classroom);
        result.setDate(date);
        result.setUsageItems(usageItems);
        result.setStatistics(statistics);
        
        return result;
    }
    
    @Override
    public List<ClassroomUsageItemDTO> getClassroomUsageInRange(Long classroomId, 
                                                                  LocalDateTime startTime, 
                                                                  LocalDateTime endTime) {
        log.debug("查询教室时间范围使用情况: classroomId={}, {} - {}", 
            classroomId, startTime, endTime);
        
        List<ClassroomUsageItemDTO> allUsageItems = new ArrayList<>();
        
        // 1. 查询使用记录日志
        List<ClassroomUsageLog> usageLogs = usageLogRepository.findByClassroomAndTimeRange(
            classroomId, startTime, endTime);
        
        for (ClassroomUsageLog log : usageLogs) {
            ClassroomUsageItemDTO item = new ClassroomUsageItemDTO();
            item.setType(log.getType());
            item.setTypeDescription(log.getType().getDescription());
            item.setStartTime(log.getStartTime());
            item.setEndTime(log.getEndTime());
            item.setReferenceId(log.getReferenceId());
            item.setDescription(log.getDescription());
            
            // 根据类型填充详细信息
            if (log.getType() == UsageType.COURSE && log.getReferenceId() != null) {
                fillCourseInfo(item, log.getReferenceId());
            } else if (log.getType() == UsageType.EXAM && log.getReferenceId() != null) {
                fillExamInfo(item, log.getReferenceId());
            }
            
            allUsageItems.add(item);
        }
        
        // 2. 查询已批准的借用记录
        List<ClassroomBooking> bookings = bookingRepository.findByClassroomAndTimeRange(
            classroomId, startTime, endTime);
        
        for (ClassroomBooking booking : bookings) {
            ClassroomUsageItemDTO item = new ClassroomUsageItemDTO();
            item.setType(UsageType.BOOKING);
            item.setTypeDescription(UsageType.BOOKING.getDescription());
            item.setStartTime(booking.getStartTime());
            item.setEndTime(booking.getEndTime());
            item.setReferenceId(booking.getId());
            item.setTitle("教室借用");
            item.setDescription(booking.getPurpose());
            
            // 查询申请人信息（先尝试教师，再尝试学生）
            Optional<Teacher> teacher = teacherRepository.findByUserId(booking.getApplicantId());
            if (teacher.isPresent()) {
                item.setApplicantName(teacher.get().getName());
            } else {
                studentRepository.findByUserId(booking.getApplicantId())
                    .ifPresent(student -> item.setApplicantName(student.getName()));
            }
            
            allUsageItems.add(item);
        }
        
        // 按开始时间排序
        allUsageItems.sort(Comparator.comparing(ClassroomUsageItemDTO::getStartTime));
        
        return allUsageItems;
    }
    
    @Override
    @Cacheable(value = "classroomSchedule", key = "#classroomId + '-' + #startDate")
    public ClassroomScheduleDTO generateWeeklySchedule(Long classroomId, LocalDate startDate) {
        log.debug("生成教室课表: classroomId={}, startDate={}", classroomId, startDate);
        
        // 确保startDate是周一
        LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6); // 周日
        
        // 查询教室信息
        ClassroomDTO classroom = classroomService.getClassroomById(classroomId);
        
        // 查询整周的使用情况
        LocalDateTime startTime = weekStart.atStartOfDay();
        LocalDateTime endTime = weekEnd.atTime(LocalTime.MAX);
        List<ClassroomUsageItemDTO> usageItems = getClassroomUsageInRange(
            classroomId, startTime, endTime);
        
        // 按星期分组
        Map<Integer, List<ClassroomUsageItemDTO>> weeklySchedule = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            weeklySchedule.put(i, new ArrayList<>());
        }
        
        for (ClassroomUsageItemDTO item : usageItems) {
            int dayOfWeek = item.getStartTime().getDayOfWeek().getValue();
            weeklySchedule.get(dayOfWeek).add(item);
        }
        
        // 生成时间槽配置
        List<ClassroomScheduleDTO.TimeSlot> timeSlots = generateTimeSlots();
        
        // 组装结果
        ClassroomScheduleDTO result = new ClassroomScheduleDTO();
        result.setClassroom(classroom);
        result.setStartDate(weekStart);
        result.setEndDate(weekEnd);
        result.setWeeklySchedule(weeklySchedule);
        result.setTimeSlots(timeSlots);
        
        return result;
    }
    
    @Override
    public ClassroomScheduleDTO getCurrentWeekSchedule(Long classroomId) {
        LocalDate today = LocalDate.now();
        return generateWeeklySchedule(classroomId, today);
    }
    
    @Override
    public List<ClassroomUsageItemDTO> getBatchClassroomUsage(List<Long> classroomIds,
                                                               LocalDateTime startTime,
                                                               LocalDateTime endTime) {
        log.debug("批量查询教室使用情况: classroomIds={}, {} - {}", 
            classroomIds, startTime, endTime);
        
        List<ClassroomUsageItemDTO> allItems = new ArrayList<>();
        
        for (Long classroomId : classroomIds) {
            List<ClassroomUsageItemDTO> items = getClassroomUsageInRange(
                classroomId, startTime, endTime);
            allItems.addAll(items);
        }
        
        return allItems;
    }
    
    /**
     * 填充课程信息
     */
    private void fillCourseInfo(ClassroomUsageItemDTO item, Long offeringId) {
        courseOfferingRepository.findById(offeringId).ifPresent(offering -> {
            item.setTitle(offering.getCourse().getName());
            item.setCourseName(offering.getCourse().getName());
            item.setTeacherName(offering.getTeacher().getName());
            item.setDescription(String.format("任课教师: %s", offering.getTeacher().getName()));
        });
    }
    
    /**
     * 填充考试信息
     */
    private void fillExamInfo(ClassroomUsageItemDTO item, Long examId) {
        examRepository.findById(examId).ifPresent(exam -> {
            item.setTitle(exam.getName());
            item.setCourseName(exam.getCourseOffering().getCourse().getName());
            item.setTeacherName(exam.getCourseOffering().getTeacher().getName());
            item.setDescription(String.format("考试时长: %d分钟", exam.getDuration()));
        });
    }
    
    /**
     * 计算统计信息
     */
    private ClassroomUsageDTO.UsageStatistics calculateStatistics(
            List<ClassroomUsageItemDTO> usageItems) {
        
        ClassroomUsageDTO.UsageStatistics stats = new ClassroomUsageDTO.UsageStatistics();
        
        stats.setTotalCount(usageItems.size());
        
        // 统计各类型使用次数
        long courseCount = usageItems.stream()
            .filter(item -> item.getType() == UsageType.COURSE)
            .count();
        long examCount = usageItems.stream()
            .filter(item -> item.getType() == UsageType.EXAM)
            .count();
        long bookingCount = usageItems.stream()
            .filter(item -> item.getType() == UsageType.BOOKING)
            .count();
        
        stats.setCourseCount((int) courseCount);
        stats.setExamCount((int) examCount);
        stats.setBookingCount((int) bookingCount);
        
        // 计算总使用时长（分钟）
        long totalMinutes = usageItems.stream()
            .mapToLong(item -> ChronoUnit.MINUTES.between(
                item.getStartTime(), item.getEndTime()))
            .sum();
        stats.setTotalMinutes(totalMinutes);
        
        // 计算使用率（基于工作时间8:00-22:00，共14小时=840分钟）
        double workMinutes = 840.0; // 14小时
        double utilizationRate = workMinutes > 0 ? 
            (totalMinutes / workMinutes) * 100 : 0.0;
        stats.setUtilizationRate(Math.round(utilizationRate * 100.0) / 100.0);
        
        return stats;
    }
    
    /**
     * 生成标准时间槽配置
     */
    private List<ClassroomScheduleDTO.TimeSlot> generateTimeSlots() {
        List<ClassroomScheduleDTO.TimeSlot> timeSlots = new ArrayList<>();
        
        // 上午
        timeSlots.add(createTimeSlot("08:00-09:40", 480, 580, "第1-2节"));
        timeSlots.add(createTimeSlot("10:00-11:40", 600, 700, "第3-4节"));
        
        // 下午
        timeSlots.add(createTimeSlot("14:00-15:40", 840, 940, "第5-6节"));
        timeSlots.add(createTimeSlot("16:00-17:40", 960, 1060, "第7-8节"));
        
        // 晚上
        timeSlots.add(createTimeSlot("19:00-20:40", 1140, 1240, "第9-10节"));
        
        return timeSlots;
    }
    
    /**
     * 创建时间槽
     */
    private ClassroomScheduleDTO.TimeSlot createTimeSlot(String time, 
                                                          int startMinute, 
                                                          int endMinute, 
                                                          String name) {
        ClassroomScheduleDTO.TimeSlot slot = new ClassroomScheduleDTO.TimeSlot();
        slot.setTime(time);
        slot.setStartMinute(startMinute);
        slot.setEndMinute(endMinute);
        slot.setName(name);
        return slot;
    }
}

