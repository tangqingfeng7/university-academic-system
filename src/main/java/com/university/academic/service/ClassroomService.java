package com.university.academic.service;

import com.university.academic.controller.dto.ClassroomDTO;
import com.university.academic.controller.dto.CreateClassroomRequest;
import com.university.academic.controller.dto.UpdateClassroomRequest;
import com.university.academic.controller.dto.ClassroomQueryDTO;
import com.university.academic.entity.ClassroomStatus;
import com.university.academic.entity.ClassroomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教室管理服务接口
 */
public interface ClassroomService {
    
    /**
     * 添加教室
     *
     * @param request 创建教室请求
     * @return 教室信息
     */
    ClassroomDTO createClassroom(CreateClassroomRequest request);
    
    /**
     * 更新教室信息
     *
     * @param id 教室ID
     * @param request 更新教室请求
     * @return 教室信息
     */
    ClassroomDTO updateClassroom(Long id, UpdateClassroomRequest request);
    
    /**
     * 删除教室（软删除）
     *
     * @param id 教室ID
     */
    void deleteClassroom(Long id);
    
    /**
     * 根据ID查询教室
     *
     * @param id 教室ID
     * @return 教室信息
     */
    ClassroomDTO getClassroomById(Long id);
    
    /**
     * 根据教室编号查询教室
     *
     * @param roomNo 教室编号
     * @return 教室信息
     */
    ClassroomDTO getClassroomByRoomNo(String roomNo);
    
    /**
     * 查询教室列表（支持筛选）
     *
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 教室列表
     */
    Page<ClassroomDTO> getClassrooms(ClassroomQueryDTO query, Pageable pageable);
    
    /**
     * 查询可用教室列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param capacity 最小容量（可选）
     * @param type 教室类型（可选）
     * @return 可用教室列表
     */
    List<ClassroomDTO> getAvailableClassrooms(LocalDateTime startTime, 
                                               LocalDateTime endTime, 
                                               Integer capacity, 
                                               ClassroomType type);
    
    /**
     * 检查教室在指定时间段是否可用
     *
     * @param classroomId 教室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否可用
     */
    boolean isClassroomAvailable(Long classroomId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 更新教室状态
     *
     * @param id 教室ID
     * @param status 教室状态
     * @return 教室信息
     */
    ClassroomDTO updateClassroomStatus(Long id, ClassroomStatus status);
    
    /**
     * 统计教室总数
     *
     * @return 教室总数
     */
    long countClassrooms();
    
    /**
     * 统计可用教室数
     *
     * @return 可用教室数
     */
    long countAvailableClassrooms();
}

