package com.university.academic.service;

import com.university.academic.dto.ClassDTO;
import com.university.academic.dto.CreateClassRequest;
import com.university.academic.dto.UpdateClassRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 班级管理Service接口
 *
 * @author Academic System Team
 */
public interface ClassService {

    /**
     * 创建班级
     */
    ClassDTO createClass(CreateClassRequest request);

    /**
     * 更新班级信息
     */
    ClassDTO updateClass(Long id, UpdateClassRequest request);

    /**
     * 删除班级（软删除）
     */
    void deleteClass(Long id);

    /**
     * 根据ID查询班级
     */
    ClassDTO getClassById(Long id);

    /**
     * 分页查询班级列表
     */
    Page<ClassDTO> getClassList(Long majorId, Integer enrollmentYear, Pageable pageable);

    /**
     * 查询所有班级（不分页）
     */
    List<ClassDTO> getAllClasses();

    /**
     * 根据专业ID查询班级列表
     */
    List<ClassDTO> getClassesByMajor(Long majorId);

    /**
     * 根据入学年份查询班级列表
     */
    List<ClassDTO> getClassesByEnrollmentYear(Integer enrollmentYear);

    /**
     * 根据专业和入学年份查询班级
     */
    List<ClassDTO> getClassesByMajorAndYear(Long majorId, Integer enrollmentYear);

    /**
     * 检查班级代码是否存在
     */
    boolean existsByClassCode(String classCode);

    /**
     * 获取班级的学生列表
     */
    List<Map<String, Object>> getClassStudents(Long classId);

    /**
     * 自动分配辅导员
     * 按照负载均衡原则为所有未分配辅导员的班级自动分配
     * 
     * @return 分配结果统计
     */
    Map<String, Object> autoAssignCounselors();

    /**
     * 为指定专业或年级自动分配辅导员
     * 
     * @param majorId 专业ID（可选）
     * @param enrollmentYear 入学年份（可选）
     * @return 分配结果统计
     */
    Map<String, Object> autoAssignCounselors(Long majorId, Integer enrollmentYear);

    /**
     * 查询教师作为辅导员管理的班级列表
     * 
     * @param counselorId 辅导员ID（用户ID）
     * @return 班级列表
     */
    List<ClassDTO> getClassesByCounselor(Long counselorId);
}

