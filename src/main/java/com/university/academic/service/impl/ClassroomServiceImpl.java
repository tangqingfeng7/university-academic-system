package com.university.academic.service.impl;

import com.university.academic.controller.dto.ClassroomDTO;
import com.university.academic.controller.dto.CreateClassroomRequest;
import com.university.academic.controller.dto.UpdateClassroomRequest;
import com.university.academic.controller.dto.ClassroomQueryDTO;
import com.university.academic.entity.Classroom;
import com.university.academic.entity.ClassroomStatus;
import com.university.academic.entity.ClassroomType;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ClassroomRepository;
import com.university.academic.repository.ClassroomUsageLogRepository;
import com.university.academic.repository.ClassroomBookingRepository;
import com.university.academic.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教室管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClassroomServiceImpl implements ClassroomService {
    
    private final ClassroomRepository classroomRepository;
    private final ClassroomUsageLogRepository usageLogRepository;
    private final ClassroomBookingRepository bookingRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = "classrooms", allEntries = true)
    public ClassroomDTO createClassroom(CreateClassroomRequest request) {
        log.info("创建教室: {}", request.getRoomNo());
        
        // 检查教室编号是否已存在
        if (classroomRepository.existsByRoomNoAndDeletedFalse(request.getRoomNo())) {
            throw new BusinessException(ErrorCode.CLASSROOM_ALREADY_EXISTS, 
                "教室编号已存在: " + request.getRoomNo());
        }
        
        // 创建教室实体
        Classroom classroom = new Classroom();
        classroom.setRoomNo(request.getRoomNo());
        classroom.setBuilding(request.getBuilding());
        classroom.setCapacity(request.getCapacity());
        classroom.setType(request.getType());
        classroom.setEquipment(request.getEquipment());
        classroom.setStatus(ClassroomStatus.AVAILABLE);
        classroom.setDeleted(false);
        
        // 保存教室
        classroom = classroomRepository.save(classroom);
        
        log.info("教室创建成功: {}", classroom.getId());
        return convertToDTO(classroom);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "classrooms", allEntries = true)
    public ClassroomDTO updateClassroom(Long id, UpdateClassroomRequest request) {
        log.info("更新教室: {}", id);
        
        // 查询教室
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室不存在: " + id));
        
        // 检查是否已删除
        if (classroom.getDeleted()) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室已删除: " + id);
        }
        
        // 更新字段
        if (request.getBuilding() != null) {
            classroom.setBuilding(request.getBuilding());
        }
        if (request.getCapacity() != null) {
            classroom.setCapacity(request.getCapacity());
        }
        if (request.getType() != null) {
            classroom.setType(request.getType());
        }
        if (request.getEquipment() != null) {
            classroom.setEquipment(request.getEquipment());
        }
        if (request.getStatus() != null) {
            classroom.setStatus(request.getStatus());
        }
        
        // 保存更新
        classroom = classroomRepository.save(classroom);
        
        log.info("教室更新成功: {}", id);
        return convertToDTO(classroom);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "classrooms", allEntries = true)
    public void deleteClassroom(Long id) {
        log.info("删除教室: {}", id);
        
        // 查询教室
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室不存在: " + id));
        
        // 检查是否已删除
        if (classroom.getDeleted()) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室已删除: " + id);
        }
        
        // 检查是否有未来的借用或使用记录
        LocalDateTime now = LocalDateTime.now();
        boolean hasBooking = bookingRepository.existsConflict(id, now, now.plusYears(10));
        if (hasBooking) {
            throw new BusinessException(ErrorCode.CLASSROOM_HAS_BOOKING, 
                "教室有未来的借用记录，不能删除");
        }
        
        boolean hasUsage = usageLogRepository.existsConflict(id, now, now.plusYears(10));
        if (hasUsage) {
            throw new BusinessException(ErrorCode.CLASSROOM_HAS_USAGE, 
                "教室有未来的使用记录，不能删除");
        }
        
        // 软删除
        classroom.setDeleted(true);
        classroomRepository.save(classroom);
        
        log.info("教室删除成功: {}", id);
    }
    
    @Override
    @Cacheable(value = "classrooms", key = "'id:' + #id")
    public ClassroomDTO getClassroomById(Long id) {
        log.debug("根据ID查询教室: {}", id);
        
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室不存在: " + id));
        
        if (classroom.getDeleted()) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室已删除: " + id);
        }
        
        return convertToDTO(classroom);
    }
    
    @Override
    @Cacheable(value = "classrooms", key = "'roomNo:' + #roomNo")
    public ClassroomDTO getClassroomByRoomNo(String roomNo) {
        log.debug("根据编号查询教室: {}", roomNo);
        
        Classroom classroom = classroomRepository.findByRoomNoAndDeletedFalse(roomNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室不存在: " + roomNo));
        
        return convertToDTO(classroom);
    }
    
    @Override
    public Page<ClassroomDTO> getClassrooms(ClassroomQueryDTO query, Pageable pageable) {
        log.debug("查询教室列表: {}", query);
        
        Page<Classroom> classrooms = classroomRepository.findByConditions(
            query.getBuilding(),
            query.getType(),
            query.getStatus(),
            query.getMinCapacity(),
            pageable
        );
        
        return classrooms.map(this::convertToDTO);
    }
    
    @Override
    @Cacheable(value = "availableClassrooms", key = "#startTime + '-' + #endTime + '-' + #capacity + '-' + #type")
    public List<ClassroomDTO> getAvailableClassrooms(LocalDateTime startTime, 
                                                      LocalDateTime endTime, 
                                                      Integer capacity, 
                                                      ClassroomType type) {
        log.debug("查询可用教室: {} - {}, 容量: {}, 类型: {}", startTime, endTime, capacity, type);
        
        // 验证时间
        if (startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE, 
                "开始时间不能晚于结束时间");
        }
        
        // 查询所有可用状态的教室
        List<Classroom> allClassrooms;
        if (capacity != null) {
            allClassrooms = classroomRepository.findAvailableByCapacityRange(
                capacity, Integer.MAX_VALUE);
        } else {
            // 如果没有指定容量，查询所有可用教室
            ClassroomQueryDTO query = new ClassroomQueryDTO();
            query.setStatus(ClassroomStatus.AVAILABLE);
            allClassrooms = classroomRepository.findByConditions(
                null, type, ClassroomStatus.AVAILABLE, null, 
                Pageable.unpaged()).getContent();
        }
        
        // 按类型筛选
        if (type != null) {
            allClassrooms = allClassrooms.stream()
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
        }
        
        // 过滤出在指定时间段可用的教室
        List<ClassroomDTO> availableClassrooms = new ArrayList<>();
        for (Classroom classroom : allClassrooms) {
            if (isClassroomAvailable(classroom.getId(), startTime, endTime)) {
                availableClassrooms.add(convertToDTO(classroom));
            }
        }
        
        log.debug("找到{}个可用教室", availableClassrooms.size());
        return availableClassrooms;
    }
    
    @Override
    public boolean isClassroomAvailable(Long classroomId, LocalDateTime startTime, LocalDateTime endTime) {
        // 检查教室是否存在且可用
        Classroom classroom = classroomRepository.findById(classroomId)
            .orElse(null);
        
        if (classroom == null || classroom.getDeleted() || 
            classroom.getStatus() != ClassroomStatus.AVAILABLE) {
            return false;
        }
        
        // 检查是否有使用记录冲突
        boolean hasUsageConflict = usageLogRepository.existsConflict(
            classroomId, startTime, endTime);
        
        // 检查是否有借用记录冲突
        boolean hasBookingConflict = bookingRepository.existsConflict(
            classroomId, startTime, endTime);
        
        return !hasUsageConflict && !hasBookingConflict;
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "classrooms", allEntries = true)
    public ClassroomDTO updateClassroomStatus(Long id, ClassroomStatus status) {
        log.info("更新教室状态: {}, 状态: {}", id, status);
        
        Classroom classroom = classroomRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室不存在: " + id));
        
        if (classroom.getDeleted()) {
            throw new BusinessException(ErrorCode.CLASSROOM_NOT_FOUND, 
                "教室已删除: " + id);
        }
        
        classroom.setStatus(status);
        classroom = classroomRepository.save(classroom);
        
        log.info("教室状态更新成功: {}", id);
        return convertToDTO(classroom);
    }
    
    @Override
    public long countClassrooms() {
        return classroomRepository.countByDeleted(false);
    }
    
    @Override
    public long countAvailableClassrooms() {
        return classroomRepository.countByStatusAndDeletedFalse(ClassroomStatus.AVAILABLE);
    }
    
    /**
     * 转换为DTO
     *
     * @param classroom 教室实体
     * @return 教室DTO
     */
    private ClassroomDTO convertToDTO(Classroom classroom) {
        ClassroomDTO dto = new ClassroomDTO();
        dto.setId(classroom.getId());
        dto.setRoomNo(classroom.getRoomNo());
        dto.setBuilding(classroom.getBuilding());
        dto.setCapacity(classroom.getCapacity());
        dto.setType(classroom.getType());
        dto.setTypeDescription(classroom.getType().getDescription());
        dto.setEquipment(classroom.getEquipment());
        dto.setStatus(classroom.getStatus());
        dto.setStatusDescription(classroom.getStatus().getDescription());
        dto.setCreatedAt(classroom.getCreatedAt());
        dto.setUpdatedAt(classroom.getUpdatedAt());
        return dto;
    }
}

