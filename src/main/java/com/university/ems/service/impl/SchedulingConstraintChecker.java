package com.university.ems.service.impl;

import com.university.ems.dto.ConflictDTO;
import com.university.ems.dto.TeacherPreferenceDTO;
import com.university.ems.model.ScheduleAssignment;
import com.university.ems.model.TimeSlot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排课约束检查器
 * 负责检查各种排课约束
 * 
 * @author Academic System Team
 */
@Component
@Slf4j
public class SchedulingConstraintChecker {

    /**
     * 检查教师时间冲突（硬约束）
     */
    public boolean hasTeacherTimeConflict(List<ScheduleAssignment> assignments, 
                                         ScheduleAssignment newAssignment, 
                                         TimeSlot newTimeSlot) {
        return assignments.stream()
                .filter(a -> a.getTeacherId().equals(newAssignment.getTeacherId()))
                .filter(a -> !a.getCourseOfferingId().equals(newAssignment.getCourseOfferingId()))
                .flatMap(a -> a.getTimeSlots().stream())
                .anyMatch(slot -> slot.equals(newTimeSlot));
    }

    /**
     * 检查教室时间冲突（硬约束）
     */
    public boolean hasClassroomTimeConflict(List<ScheduleAssignment> assignments,
                                           ScheduleAssignment newAssignment,
                                           TimeSlot newTimeSlot) {
        return assignments.stream()
                .filter(a -> a.getClassroomId() != null && a.getClassroomId().equals(newAssignment.getClassroomId()))
                .filter(a -> !a.getCourseOfferingId().equals(newAssignment.getCourseOfferingId()))
                .flatMap(a -> a.getTimeSlots().stream())
                .anyMatch(slot -> slot.equals(newTimeSlot));
    }

    /**
     * 检查教室容量（硬约束）
     */
    public boolean exceedsClassroomCapacity(ScheduleAssignment assignment) {
        if (assignment.getClassroomCapacity() == null || assignment.getStudentCount() == null) {
            return false;
        }
        return assignment.getStudentCount() > assignment.getClassroomCapacity();
    }

    /**
     * 检查教师每天课时限制（软约束）
     */
    public boolean exceedsDailyHoursLimit(List<ScheduleAssignment> assignments,
                                         ScheduleAssignment newAssignment,
                                         TimeSlot newTimeSlot,
                                         Integer maxDailyHours) {
        if (maxDailyHours == null) {
            return false;
        }

        long dailyHours = assignments.stream()
                .filter(a -> a.getTeacherId().equals(newAssignment.getTeacherId()))
                .flatMap(a -> a.getTimeSlots().stream())
                .filter(slot -> slot.getDayOfWeek().equals(newTimeSlot.getDayOfWeek()))
                .count();

        return (dailyHours + 1) > maxDailyHours;
    }

    /**
     * 检查教师偏好（软约束）
     */
    public boolean violatesTeacherPreference(TimeSlot timeSlot, TeacherPreferenceDTO preference) {
        if (preference == null) {
            return false;
        }

        // 检查偏好星期
        if (preference.getPreferredDaysList() != null && !preference.getPreferredDaysList().isEmpty()) {
            if (!preference.getPreferredDaysList().contains(timeSlot.getDayOfWeek())) {
                return true;
            }
        }

        // 检查偏好时段
        if (preference.getPreferredTimeSlotsList() != null && !preference.getPreferredTimeSlotsList().isEmpty()) {
            if (!preference.getPreferredTimeSlotsList().contains(timeSlot.getSlot())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查连续课程过多（软约束）
     * 避免教师或学生连续超过3节课
     */
    public boolean hasTooManyConsecutiveClasses(List<ScheduleAssignment> assignments,
                                               Long teacherId,
                                               TimeSlot newTimeSlot,
                                               int maxConsecutive) {
        // 获取同一天的所有课程时段
        List<Integer> slotsOnSameDay = assignments.stream()
                .filter(a -> a.getTeacherId().equals(teacherId))
                .flatMap(a -> a.getTimeSlots().stream())
                .filter(slot -> slot.getDayOfWeek().equals(newTimeSlot.getDayOfWeek()))
                .map(TimeSlot::getSlot)
                .sorted()
                .collect(Collectors.toList());

        slotsOnSameDay.add(newTimeSlot.getSlot());
        Collections.sort(slotsOnSameDay);

        // 检查连续课程数量
        int consecutive = 1;
        for (int i = 1; i < slotsOnSameDay.size(); i++) {
            if (slotsOnSameDay.get(i) == slotsOnSameDay.get(i - 1) + 1) {
                consecutive++;
                if (consecutive > maxConsecutive) {
                    return true;
                }
            } else {
                consecutive = 1;
            }
        }

        return false;
    }

    /**
     * 计算教师空闲时间碎片数
     * 空闲时间越碎片化，得分越低
     */
    public int calculateIdleTimeFragments(List<ScheduleAssignment> assignments, Long teacherId, Integer dayOfWeek) {
        List<Integer> slots = assignments.stream()
                .filter(a -> a.getTeacherId().equals(teacherId))
                .flatMap(a -> a.getTimeSlots().stream())
                .filter(slot -> slot.getDayOfWeek().equals(dayOfWeek))
                .map(TimeSlot::getSlot)
                .sorted()
                .collect(Collectors.toList());

        if (slots.size() <= 1) {
            return 0;
        }

        // 计算空闲间隙数量
        int fragments = 0;
        for (int i = 1; i < slots.size(); i++) {
            if (slots.get(i) - slots.get(i - 1) > 1) {
                fragments++;
            }
        }

        return fragments;
    }

    /**
     * 检查所有硬约束
     */
    public List<ConflictDTO> checkHardConstraints(List<ScheduleAssignment> assignments) {
        List<ConflictDTO> conflicts = new ArrayList<>();

        // 检查教师时间冲突
        conflicts.addAll(findTeacherTimeConflicts(assignments));

        // 检查教室时间冲突
        conflicts.addAll(findClassroomTimeConflicts(assignments));

        // 检查教室容量
        conflicts.addAll(findCapacityViolations(assignments));

        return conflicts;
    }

    /**
     * 查找教师时间冲突
     */
    private List<ConflictDTO> findTeacherTimeConflicts(List<ScheduleAssignment> assignments) {
        List<ConflictDTO> conflicts = new ArrayList<>();
        Map<String, List<ScheduleAssignment>> teacherTimeMap = new HashMap<>();

        for (ScheduleAssignment assignment : assignments) {
            for (TimeSlot timeSlot : assignment.getTimeSlots()) {
                String key = assignment.getTeacherId() + "_" + timeSlot.getDayOfWeek() + "_" + timeSlot.getSlot();
                teacherTimeMap.computeIfAbsent(key, k -> new ArrayList<>()).add(assignment);
            }
        }

        for (Map.Entry<String, List<ScheduleAssignment>> entry : teacherTimeMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<ScheduleAssignment> conflictingAssignments = entry.getValue();
                ScheduleAssignment first = conflictingAssignments.get(0);
                TimeSlot timeSlot = first.getTimeSlots().get(0); // 简化处理

                conflicts.add(ConflictDTO.builder()
                        .conflictType("TEACHER_TIME_CONFLICT")
                        .severity("HARD")
                        .description("教师 " + first.getTeacherName() + " 在 " + timeSlot + " 有时间冲突")
                        .courseOfferingIds(conflictingAssignments.stream()
                                .map(ScheduleAssignment::getCourseOfferingId)
                                .toArray(Long[]::new))
                        .teacherId(first.getTeacherId())
                        .dayOfWeek(timeSlot.getDayOfWeek())
                        .timeSlot(timeSlot.getSlot())
                        .build());
            }
        }

        return conflicts;
    }

    /**
     * 查找教室时间冲突
     */
    private List<ConflictDTO> findClassroomTimeConflicts(List<ScheduleAssignment> assignments) {
        List<ConflictDTO> conflicts = new ArrayList<>();
        Map<String, List<ScheduleAssignment>> classroomTimeMap = new HashMap<>();

        for (ScheduleAssignment assignment : assignments) {
            if (assignment.getClassroomId() == null) continue;

            for (TimeSlot timeSlot : assignment.getTimeSlots()) {
                String key = assignment.getClassroomId() + "_" + timeSlot.getDayOfWeek() + "_" + timeSlot.getSlot();
                classroomTimeMap.computeIfAbsent(key, k -> new ArrayList<>()).add(assignment);
            }
        }

        for (Map.Entry<String, List<ScheduleAssignment>> entry : classroomTimeMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                List<ScheduleAssignment> conflictingAssignments = entry.getValue();
                ScheduleAssignment first = conflictingAssignments.get(0);
                TimeSlot timeSlot = first.getTimeSlots().get(0);

                conflicts.add(ConflictDTO.builder()
                        .conflictType("CLASSROOM_TIME_CONFLICT")
                        .severity("HARD")
                        .description("教室 " + first.getClassroomNo() + " 在 " + timeSlot + " 有时间冲突")
                        .courseOfferingIds(conflictingAssignments.stream()
                                .map(ScheduleAssignment::getCourseOfferingId)
                                .toArray(Long[]::new))
                        .classroomId(first.getClassroomId())
                        .dayOfWeek(timeSlot.getDayOfWeek())
                        .timeSlot(timeSlot.getSlot())
                        .build());
            }
        }

        return conflicts;
    }

    /**
     * 查找容量违反
     */
    private List<ConflictDTO> findCapacityViolations(List<ScheduleAssignment> assignments) {
        return assignments.stream()
                .filter(this::exceedsClassroomCapacity)
                .map(assignment -> ConflictDTO.builder()
                        .conflictType("CAPACITY_VIOLATION")
                        .severity("HARD")
                        .description("教室 " + assignment.getClassroomNo() + " 容量不足（需要" + 
                                   assignment.getStudentCount() + "，容量" + assignment.getClassroomCapacity() + "）")
                        .courseOfferingIds(new Long[]{assignment.getCourseOfferingId()})
                        .classroomId(assignment.getClassroomId())
                        .build())
                .collect(Collectors.toList());
    }
}

