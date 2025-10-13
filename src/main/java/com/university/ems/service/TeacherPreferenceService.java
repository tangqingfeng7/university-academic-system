package com.university.ems.service;

import com.university.ems.dto.SetPreferenceRequest;
import com.university.ems.dto.TeacherPreferenceDTO;
import com.university.ems.dto.UpdatePreferenceRequest;

import java.util.List;

/**
 * 教师排课偏好服务接口
 * 
 * @author Academic System Team
 */
public interface TeacherPreferenceService {

    /**
     * 设置教师排课偏好
     * 如果教师已有偏好设置，则更新；否则创建新的
     *
     * @param request 设置请求
     * @return 教师偏好DTO
     */
    TeacherPreferenceDTO setPreference(SetPreferenceRequest request);

    /**
     * 更新教师排课偏好
     *
     * @param teacherId 教师ID
     * @param request 更新请求
     * @return 教师偏好DTO
     */
    TeacherPreferenceDTO updatePreference(Long teacherId, UpdatePreferenceRequest request);

    /**
     * 查询教师的排课偏好
     *
     * @param teacherId 教师ID
     * @return 教师偏好DTO，如果不存在返回null
     */
    TeacherPreferenceDTO getPreferenceByTeacherId(Long teacherId);

    /**
     * 根据ID查询偏好
     *
     * @param id 偏好ID
     * @return 教师偏好DTO
     */
    TeacherPreferenceDTO getPreferenceById(Long id);

    /**
     * 查询所有教师的排课偏好
     *
     * @return 教师偏好列表
     */
    List<TeacherPreferenceDTO> getAllPreferences();

    /**
     * 检查教师是否已设置偏好
     *
     * @param teacherId 教师ID
     * @return 是否已设置
     */
    boolean hasPreference(Long teacherId);

    /**
     * 删除教师排课偏好
     *
     * @param teacherId 教师ID
     */
    void deletePreference(Long teacherId);

    /**
     * 验证偏好设置的有效性
     *
     * @param preferredDays 偏好星期
     * @param preferredTimeSlots 偏好时段
     * @param maxDailyHours 每天最多课时
     * @param maxWeeklyHours 每周最多课时
     * @return 验证结果消息，如果为null表示验证通过
     */
    String validatePreference(String preferredDays, String preferredTimeSlots, 
                            Integer maxDailyHours, Integer maxWeeklyHours);
}

