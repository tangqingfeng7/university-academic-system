package com.university.academic.service.impl;

import com.university.academic.controller.dto.*;
import com.university.academic.entity.*;
import com.university.academic.repository.ClassroomBookingRepository;
import com.university.academic.repository.ClassroomUsageLogRepository;
import com.university.academic.service.ClassroomService;
import com.university.academic.service.SchedulingApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排课系统API服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingApiServiceImpl implements SchedulingApiService {
    
    private final ClassroomService classroomService;
    private final ClassroomUsageLogRepository usageLogRepository;
    private final ClassroomBookingRepository bookingRepository;
    
    @Override
    @Cacheable(value = "availableClassrooms", 
        key = "#request.startTime + '-' + #request.endTime + '-' + #request.minCapacity + '-' + #request.type")
    public List<ClassroomDTO> findAvailableClassrooms(AvailableClassroomRequest request) {
        log.info("查询可用教室: {} - {}, 容量>={}, 类型={}", 
            request.getStartTime(), request.getEndTime(), 
            request.getMinCapacity(), request.getType());
        
        // 调用ClassroomService的基础方法
        List<ClassroomDTO> availableClassrooms = classroomService.getAvailableClassrooms(
            request.getStartTime(),
            request.getEndTime(),
            request.getMinCapacity(),
            request.getType()
        );
        
        // 根据楼栋过滤
        if (request.getBuilding() != null && !request.getBuilding().isEmpty()) {
            availableClassrooms = availableClassrooms.stream()
                .filter(c -> request.getBuilding().equals(c.getBuilding()))
                .collect(Collectors.toList());
        }
        
        // 根据多媒体设备过滤
        if (Boolean.TRUE.equals(request.getMultimediaOnly())) {
            availableClassrooms = availableClassrooms.stream()
                .filter(c -> c.getType() == ClassroomType.MULTIMEDIA || 
                            hasMultimediaEquipment(c))
                .collect(Collectors.toList());
        }
        
        log.info("找到{}个可用教室", availableClassrooms.size());
        return availableClassrooms;
    }
    
    @Override
    public List<ClassroomAvailabilityDTO> checkClassroomAvailability(List<Long> classroomIds,
                                                                      LocalDateTime startTime,
                                                                      LocalDateTime endTime) {
        log.info("批量检查教室可用性: {} 个教室, {} - {}", 
            classroomIds.size(), startTime, endTime);
        
        return classroomIds.stream()
            .map(id -> checkSingleClassroomAvailability(id, startTime, endTime))
            .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "classroomAvailability", 
        key = "#classroomId + '-' + #startTime + '-' + #endTime")
    public ClassroomAvailabilityDTO checkSingleClassroomAvailability(Long classroomId,
                                                                      LocalDateTime startTime,
                                                                      LocalDateTime endTime) {
        log.debug("检查教室可用性: classroomId={}, {} - {}", classroomId, startTime, endTime);
        
        ClassroomAvailabilityDTO result = new ClassroomAvailabilityDTO();
        result.setClassroomId(classroomId);
        result.setStartTime(startTime);
        result.setEndTime(endTime);
        
        // 获取教室信息
        try {
            ClassroomDTO classroom = classroomService.getClassroomById(classroomId);
            result.setRoomNo(classroom.getRoomNo());
            
            // 检查教室状态
            if (classroom.getStatus() != ClassroomStatus.AVAILABLE) {
                result.setAvailable(false);
                result.setConflictReason("教室状态为: " + classroom.getStatusDescription());
                return result;
            }
            
        } catch (Exception e) {
            result.setAvailable(false);
            result.setConflictReason("教室不存在");
            return result;
        }
        
        // 检查时间冲突并获取冲突详情
        List<ClassroomAvailabilityDTO.ConflictTimeSlot> conflicts = new ArrayList<>();
        
        // 1. 检查使用记录日志冲突
        List<ClassroomUsageLog> usageLogs = usageLogRepository.findByClassroomAndTimeRange(
            classroomId, startTime, endTime);
        
        for (ClassroomUsageLog log : usageLogs) {
            ClassroomAvailabilityDTO.ConflictTimeSlot conflict = 
                new ClassroomAvailabilityDTO.ConflictTimeSlot();
            conflict.setType(log.getType().name());
            conflict.setStartTime(log.getStartTime());
            conflict.setEndTime(log.getEndTime());
            conflict.setDescription(log.getDescription() != null ? 
                log.getDescription() : log.getType().getDescription());
            conflicts.add(conflict);
        }
        
        // 2. 检查借用记录冲突
        List<ClassroomBooking> bookings = bookingRepository.findByClassroomAndTimeRange(
            classroomId, startTime, endTime);
        
        for (ClassroomBooking booking : bookings) {
            ClassroomAvailabilityDTO.ConflictTimeSlot conflict = 
                new ClassroomAvailabilityDTO.ConflictTimeSlot();
            conflict.setType("BOOKING");
            conflict.setStartTime(booking.getStartTime());
            conflict.setEndTime(booking.getEndTime());
            conflict.setDescription("教室借用: " + booking.getPurpose());
            conflicts.add(conflict);
        }
        
        // 设置结果
        result.setAvailable(conflicts.isEmpty());
        result.setConflictTimeSlots(conflicts);
        
        if (!conflicts.isEmpty()) {
            result.setConflictReason(String.format("该时间段有%d个冲突", conflicts.size()));
        }
        
        return result;
    }
    
    @Override
    public List<ClassroomDTO> recommendClassrooms(Integer capacity,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime,
                                                   String preferredBuilding) {
        log.info("推荐教室: 容量={}, {} - {}, 偏好楼栋={}", 
            capacity, startTime, endTime, preferredBuilding);
        
        // 查询可用教室
        List<ClassroomDTO> availableClassrooms = classroomService.getAvailableClassrooms(
            startTime, endTime, capacity, null);
        
        // 按优先级排序
        return availableClassrooms.stream()
            .sorted((c1, c2) -> {
                // 1. 优先推荐偏好楼栋
                if (preferredBuilding != null) {
                    boolean c1Match = preferredBuilding.equals(c1.getBuilding());
                    boolean c2Match = preferredBuilding.equals(c2.getBuilding());
                    if (c1Match && !c2Match) return -1;
                    if (!c1Match && c2Match) return 1;
                }
                
                // 2. 容量接近优先（避免浪费）
                int diff1 = Math.abs(c1.getCapacity() - capacity);
                int diff2 = Math.abs(c2.getCapacity() - capacity);
                if (diff1 != diff2) {
                    return Integer.compare(diff1, diff2);
                }
                
                // 3. 多媒体教室优先
                if (c1.getType() == ClassroomType.MULTIMEDIA && 
                    c2.getType() != ClassroomType.MULTIMEDIA) {
                    return -1;
                }
                if (c1.getType() != ClassroomType.MULTIMEDIA && 
                    c2.getType() == ClassroomType.MULTIMEDIA) {
                    return 1;
                }
                
                // 4. 按教室编号排序
                return c1.getRoomNo().compareTo(c2.getRoomNo());
            })
            .limit(10) // 返回前10个推荐
            .collect(Collectors.toList());
    }
    
    @Override
    public List<TimeSlot> getFreeTimeSlots(Long classroomId, LocalDate date) {
        log.debug("查询教室空闲时间段: classroomId={}, date={}", classroomId, date);
        
        LocalDateTime dayStart = date.atTime(8, 0); // 工作开始时间 8:00
        LocalDateTime dayEnd = date.atTime(22, 0);  // 工作结束时间 22:00
        
        // 查询当天的所有使用记录
        List<ClassroomUsageLog> usageLogs = usageLogRepository.findByClassroomAndTimeRange(
            classroomId, dayStart, dayEnd);
        
        List<ClassroomBooking> bookings = bookingRepository.findByClassroomAndTimeRange(
            classroomId, dayStart, dayEnd);
        
        // 合并所有占用时间段
        List<TimeSlot> occupiedSlots = new ArrayList<>();
        
        for (ClassroomUsageLog log : usageLogs) {
            occupiedSlots.add(new TimeSlot(log.getStartTime(), log.getEndTime()));
        }
        
        for (ClassroomBooking booking : bookings) {
            occupiedSlots.add(new TimeSlot(booking.getStartTime(), booking.getEndTime()));
        }
        
        // 按开始时间排序
        occupiedSlots.sort(Comparator.comparing(TimeSlot::getStartTime));
        
        // 计算空闲时间段
        List<TimeSlot> freeSlots = new ArrayList<>();
        LocalDateTime current = dayStart;
        
        for (TimeSlot occupied : occupiedSlots) {
            // 如果当前时间早于占用时间，则有空闲时间段
            if (current.isBefore(occupied.getStartTime())) {
                freeSlots.add(new TimeSlot(current, occupied.getStartTime()));
            }
            // 更新当前时间为占用结束时间
            if (occupied.getEndTime().isAfter(current)) {
                current = occupied.getEndTime();
            }
        }
        
        // 最后一个空闲时间段（从最后占用结束到工作时间结束）
        if (current.isBefore(dayEnd)) {
            freeSlots.add(new TimeSlot(current, dayEnd));
        }
        
        log.debug("找到{}个空闲时间段", freeSlots.size());
        return freeSlots;
    }
    
    /**
     * 检查教室是否有多媒体设备
     */
    private boolean hasMultimediaEquipment(ClassroomDTO classroom) {
        String equipment = classroom.getEquipment();
        if (equipment == null || equipment.isEmpty()) {
            return false;
        }
        // 简单检查设备信息中是否包含投影仪、电脑等关键词
        return equipment.toLowerCase().contains("projector") || 
               equipment.toLowerCase().contains("computer") ||
               equipment.toLowerCase().contains("投影") ||
               equipment.toLowerCase().contains("电脑");
    }
}

