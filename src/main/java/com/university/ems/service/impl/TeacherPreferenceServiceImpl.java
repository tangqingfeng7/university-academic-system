package com.university.ems.service.impl;

import com.university.academic.entity.Teacher;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.TeacherRepository;
import com.university.ems.dto.SetPreferenceRequest;
import com.university.ems.dto.TeacherPreferenceDTO;
import com.university.ems.dto.UpdatePreferenceRequest;
import com.university.ems.entity.TeacherPreference;
import com.university.ems.repository.TeacherPreferenceRepository;
import com.university.ems.service.TeacherPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师排课偏好服务实现类
 * 
 * @author Academic System Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherPreferenceServiceImpl implements TeacherPreferenceService {

    private final TeacherPreferenceRepository preferenceRepository;
    private final TeacherRepository teacherRepository;

    private static final String[] WEEK_DAYS = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final String[] TIME_SLOTS = {"", "第1节", "第2节", "第3节", "第4节", "第5节", "第6节", "第7节", "第8节"};

    @Override
    @Transactional
    public TeacherPreferenceDTO setPreference(SetPreferenceRequest request) {
        log.info("设置教师排课偏好: teacherId={}", request.getTeacherId());

        // 验证教师是否存在
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_NOT_FOUND));

        // 验证偏好设置
        String validationMessage = validatePreference(
                request.getPreferredDays(),
                request.getPreferredTimeSlots(),
                request.getMaxDailyHours(),
                request.getMaxWeeklyHours()
        );
        if (validationMessage != null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, validationMessage);
        }

        // 检查是否已有偏好设置
        TeacherPreference preference = preferenceRepository.findByTeacherId(request.getTeacherId())
                .orElse(null);

        if (preference == null) {
            // 创建新的偏好设置
            preference = TeacherPreference.builder()
                    .teacher(teacher)
                    .preferredDays(request.getPreferredDays())
                    .preferredTimeSlots(request.getPreferredTimeSlots())
                    .maxDailyHours(request.getMaxDailyHours())
                    .maxWeeklyHours(request.getMaxWeeklyHours())
                    .notes(request.getNotes())
                    .build();
            log.info("创建新的教师偏好设置");
        } else {
            // 更新现有偏好设置
            preference.setPreferredDays(request.getPreferredDays());
            preference.setPreferredTimeSlots(request.getPreferredTimeSlots());
            preference.setMaxDailyHours(request.getMaxDailyHours());
            preference.setMaxWeeklyHours(request.getMaxWeeklyHours());
            preference.setNotes(request.getNotes());
            log.info("更新现有教师偏好设置: id={}", preference.getId());
        }

        preference = preferenceRepository.save(preference);
        log.info("教师排课偏好设置成功: teacherId={}, preferenceId={}", request.getTeacherId(), preference.getId());

        return convertToDTO(preference);
    }

    @Override
    @Transactional
    public TeacherPreferenceDTO updatePreference(Long teacherId, UpdatePreferenceRequest request) {
        log.info("更新教师排课偏好: teacherId={}", teacherId);

        // 查询现有偏好设置
        TeacherPreference preference = preferenceRepository.findByTeacherId(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_PREFERENCE_NOT_FOUND));

        // 验证偏好设置（使用更新后的值或现有值）
        String preferredDays = request.getPreferredDays() != null ? 
                request.getPreferredDays() : preference.getPreferredDays();
        String preferredTimeSlots = request.getPreferredTimeSlots() != null ? 
                request.getPreferredTimeSlots() : preference.getPreferredTimeSlots();
        Integer maxDailyHours = request.getMaxDailyHours() != null ? 
                request.getMaxDailyHours() : preference.getMaxDailyHours();
        Integer maxWeeklyHours = request.getMaxWeeklyHours() != null ? 
                request.getMaxWeeklyHours() : preference.getMaxWeeklyHours();

        String validationMessage = validatePreference(preferredDays, preferredTimeSlots, 
                maxDailyHours, maxWeeklyHours);
        if (validationMessage != null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, validationMessage);
        }

        // 更新字段
        if (request.getPreferredDays() != null) {
            preference.setPreferredDays(request.getPreferredDays());
        }
        if (request.getPreferredTimeSlots() != null) {
            preference.setPreferredTimeSlots(request.getPreferredTimeSlots());
        }
        if (request.getMaxDailyHours() != null) {
            preference.setMaxDailyHours(request.getMaxDailyHours());
        }
        if (request.getMaxWeeklyHours() != null) {
            preference.setMaxWeeklyHours(request.getMaxWeeklyHours());
        }
        if (request.getNotes() != null) {
            preference.setNotes(request.getNotes());
        }

        preference = preferenceRepository.save(preference);
        log.info("教师排课偏好更新成功: teacherId={}, preferenceId={}", teacherId, preference.getId());

        return convertToDTO(preference);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherPreferenceDTO getPreferenceByTeacherId(Long teacherId) {
        log.debug("查询教师排课偏好: teacherId={}", teacherId);

        return preferenceRepository.findByTeacherId(teacherId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherPreferenceDTO getPreferenceById(Long id) {
        log.debug("查询排课偏好: id={}", id);

        TeacherPreference preference = preferenceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_PREFERENCE_NOT_FOUND));

        return convertToDTO(preference);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeacherPreferenceDTO> getAllPreferences() {
        log.debug("查询所有教师排课偏好");

        List<TeacherPreference> preferences = preferenceRepository.findAll();
        return preferences.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPreference(Long teacherId) {
        return preferenceRepository.existsByTeacherId(teacherId);
    }

    @Override
    @Transactional
    public void deletePreference(Long teacherId) {
        log.info("删除教师排课偏好: teacherId={}", teacherId);

        TeacherPreference preference = preferenceRepository.findByTeacherId(teacherId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEACHER_PREFERENCE_NOT_FOUND));

        preferenceRepository.delete(preference);
        log.info("教师排课偏好删除成功: teacherId={}, preferenceId={}", teacherId, preference.getId());
    }

    @Override
    public String validatePreference(String preferredDays, String preferredTimeSlots,
                                    Integer maxDailyHours, Integer maxWeeklyHours) {
        // 验证偏好星期格式
        if (StringUtils.hasText(preferredDays)) {
            String[] days = preferredDays.split(",");
            for (String day : days) {
                try {
                    int dayNum = Integer.parseInt(day.trim());
                    if (dayNum < 1 || dayNum > 7) {
                        return "偏好星期必须在1-7之间";
                    }
                } catch (NumberFormatException e) {
                    return "偏好星期格式错误";
                }
            }
        }

        // 验证偏好时段格式
        if (StringUtils.hasText(preferredTimeSlots)) {
            String[] slots = preferredTimeSlots.split(",");
            for (String slot : slots) {
                try {
                    int slotNum = Integer.parseInt(slot.trim());
                    if (slotNum < 1 || slotNum > 8) {
                        return "偏好时段必须在1-8之间";
                    }
                } catch (NumberFormatException e) {
                    return "偏好时段格式错误";
                }
            }
        }

        // 验证每周课时不能小于每天课时
        if (maxDailyHours != null && maxWeeklyHours != null) {
            if (maxWeeklyHours < maxDailyHours) {
                return "每周最多课时不能小于每天最多课时";
            }
        }

        return null; // 验证通过
    }

    /**
     * 转换为DTO
     */
    private TeacherPreferenceDTO convertToDTO(TeacherPreference preference) {
        Teacher teacher = preference.getTeacher();

        return TeacherPreferenceDTO.builder()
                .id(preference.getId())
                .teacherId(teacher.getId())
                .teacherName(teacher.getName())
                .teacherNo(teacher.getTeacherNo())
                .preferredDays(preference.getPreferredDays())
                .preferredDaysList(parseToIntList(preference.getPreferredDays()))
                .preferredDaysDescription(formatDaysDescription(preference.getPreferredDays()))
                .preferredTimeSlots(preference.getPreferredTimeSlots())
                .preferredTimeSlotsList(parseToIntList(preference.getPreferredTimeSlots()))
                .preferredTimeSlotsDescription(formatTimeSlotsDescription(preference.getPreferredTimeSlots()))
                .maxDailyHours(preference.getMaxDailyHours())
                .maxWeeklyHours(preference.getMaxWeeklyHours())
                .notes(preference.getNotes())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }

    /**
     * 解析逗号分隔的字符串为整数列表
     */
    private List<Integer> parseToIntList(String str) {
        if (!StringUtils.hasText(str)) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * 格式化星期描述
     */
    private String formatDaysDescription(String preferredDays) {
        if (!StringUtils.hasText(preferredDays)) {
            return "无偏好";
        }
        List<Integer> days = parseToIntList(preferredDays);
        return days.stream()
                .map(day -> day >= 1 && day <= 7 ? WEEK_DAYS[day] : "")
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("、"));
    }

    /**
     * 格式化时段描述
     */
    private String formatTimeSlotsDescription(String preferredTimeSlots) {
        if (!StringUtils.hasText(preferredTimeSlots)) {
            return "无偏好";
        }
        List<Integer> slots = parseToIntList(preferredTimeSlots);
        return slots.stream()
                .map(slot -> slot >= 1 && slot <= 8 ? TIME_SLOTS[slot] : "")
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("、"));
    }
}

