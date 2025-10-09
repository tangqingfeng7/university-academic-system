package com.university.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.academic.dto.ScheduleDTO;
import com.university.academic.dto.ScheduleItemDTO;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.CourseSelection;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.SemesterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 课表服务类
 * 提供课表查询、解析、格式化功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final CourseSelectionRepository selectionRepository;
    private final CourseOfferingRepository offeringRepository;
    private final SemesterRepository semesterRepository;
    private final ObjectMapper objectMapper;

    /**
     * 获取学生课表
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 课表信息
     */
    @Transactional(readOnly = true)
    public ScheduleDTO getStudentSchedule(Long studentId, Long semesterId) {
        log.info("查询学生课表: studentId={}, semesterId={}", studentId, semesterId);

        // 验证学期存在
        var semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEMESTER_NOT_FOUND));

        // 查询学生的选课记录
        List<CourseSelection> selections = selectionRepository.findActiveByStudentAndSemester(
                studentId, semesterId);

        // 构建课表
        List<ScheduleItemDTO> items = new ArrayList<>();
        for (CourseSelection selection : selections) {
            CourseOffering offering = selection.getOffering();
            List<ScheduleItemDTO> courseItems = parseSchedule(
                    offering,
                    true,  // 学生视角
                    null
            );
            items.addAll(courseItems);
        }

        return buildScheduleDTO(semester.getAcademicYear() + " " + 
                (semester.getSemesterType() == 1 ? "春季学期" : "秋季学期"),
                semesterId, items);
    }

    /**
     * 获取学生指定周次的课表
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @param weekNumber 周次
     * @return 课表项列表
     */
    @Transactional(readOnly = true)
    public List<ScheduleItemDTO> getStudentWeeklySchedule(Long studentId, Long semesterId, Integer weekNumber) {
        ScheduleDTO schedule = getStudentSchedule(studentId, semesterId);
        
        return schedule.getItems().stream()
                .filter(item -> item.getWeeks().contains(weekNumber))
                .collect(Collectors.toList());
    }

    /**
     * 获取教师课表
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 课表信息
     */
    @Transactional(readOnly = true)
    public ScheduleDTO getTeacherSchedule(Long teacherId, Long semesterId) {
        log.info("查询教师课表: teacherId={}, semesterId={}", teacherId, semesterId);

        // 验证学期存在
        var semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEMESTER_NOT_FOUND));

        // 查询教师的授课计划
        List<CourseOffering> offerings = offeringRepository
                .findByTeacherIdAndSemesterId(teacherId, semesterId);
        
        List<CourseOffering> publishedOfferings = offerings.stream()
                .filter(o -> o.getStatus() == CourseOffering.OfferingStatus.PUBLISHED)
                .collect(Collectors.toList());

        // 构建课表
        List<ScheduleItemDTO> items = new ArrayList<>();
        for (CourseOffering offering : publishedOfferings) {
            // 获取选课学生数
            int studentCount = offering.getEnrolled();
            
            List<ScheduleItemDTO> courseItems = parseSchedule(
                    offering,
                    false,  // 教师视角
                    studentCount
            );
            
            items.addAll(courseItems);
        }
        
        return buildScheduleDTO(semester.getAcademicYear() + " " + 
                (semester.getSemesterType() == 1 ? "春季学期" : "秋季学期"),
                semesterId, items);
    }

    /**
     * 获取教师指定周次的课表
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @param weekNumber 周次
     * @return 课表项列表
     */
    @Transactional(readOnly = true)
    public List<ScheduleItemDTO> getTeacherWeeklySchedule(Long teacherId, Long semesterId, Integer weekNumber) {
        ScheduleDTO schedule = getTeacherSchedule(teacherId, semesterId);
        
        return schedule.getItems().stream()
                .filter(item -> item.getWeeks().contains(weekNumber))
                .collect(Collectors.toList());
    }

    /**
     * 解析课程时间安排
     *
     * @param offering      开课计划
     * @param isStudent     是否学生视角
     * @param studentCount  学生数量（教师视角）
     * @return 课表项列表
     */
    private List<ScheduleItemDTO> parseSchedule(CourseOffering offering, 
                                                 boolean isStudent, 
                                                 Integer studentCount) {
        List<ScheduleItemDTO> items = new ArrayList<>();
        
        if (offering.getSchedule() == null || offering.getSchedule().isEmpty()) {
            return items;
        }

        try {
            // 解析JSON格式的时间安排
            log.debug("解析课程时间安排: offeringId={}, schedule={}", 
                    offering.getId(), offering.getSchedule());
            
            List<Map<String, Object>> scheduleList = objectMapper.readValue(
                    offering.getSchedule(), new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> scheduleItem : scheduleList) {
                ScheduleItemDTO item = ScheduleItemDTO.builder()
                        .courseId(offering.getCourse().getId())
                        .courseNo(offering.getCourse().getCourseNo())
                        .courseName(offering.getCourse().getName())
                        .offeringId(offering.getId())
                        .credits(offering.getCourse().getCredits())
                        .hours(offering.getCourse().getHours())
                        .courseType(offering.getCourse().getType().name())
                        .capacity(offering.getCapacity())
                        .description(offering.getCourse().getDescription())
                        .build();

                // 设置视角相关的信息
                if (isStudent) {
                    item.setTeacherName(offering.getTeacher().getName());
                } else {
                    item.setStudentCount(studentCount);
                }

                // 解析day
                if (scheduleItem.containsKey("day")) {
                    item.setDay(((Number) scheduleItem.get("day")).intValue());
                }

                // 解析period
                if (scheduleItem.containsKey("period")) {
                    item.setPeriod(scheduleItem.get("period").toString());
                }

                // 解析weeks
                if (scheduleItem.containsKey("weeks")) {
                    @SuppressWarnings("unchecked")
                    List<Integer> weeks = ((List<Object>) scheduleItem.get("weeks")).stream()
                            .map(w -> ((Number) w).intValue())
                            .collect(Collectors.toList());
                    item.setWeeks(weeks);
                }

                // 解析location（优先使用scheduleItem中的，否则使用offering的）
                if (scheduleItem.containsKey("location")) {
                    item.setLocation(scheduleItem.get("location").toString());
                } else {
                    item.setLocation(offering.getLocation());
                }

                items.add(item);
            }
        } catch (Exception e) {
            log.error("解析课程时间失败: offeringId={}", offering.getId(), e);
        }

        return items;
    }

    /**
     * 构建课表DTO
     *
     * @param semesterName 学期名称
     * @param semesterId   学期ID
     * @param items        课表项列表
     * @return 课表DTO
     */
    private ScheduleDTO buildScheduleDTO(String semesterName, Long semesterId, 
                                        List<ScheduleItemDTO> items) {
        // 按周次分组
        Map<Integer, List<ScheduleItemDTO>> weeklySchedule = new TreeMap<>();
        for (ScheduleItemDTO item : items) {
            if (item.getWeeks() != null) {
                for (Integer week : item.getWeeks()) {
                    weeklySchedule.computeIfAbsent(week, k -> new ArrayList<>()).add(item);
                }
            }
        }

        // 构建网格课表（用于前端展示）
        Map<String, ScheduleItemDTO> gridSchedule = new HashMap<>();
        for (ScheduleItemDTO item : items) {
            if (item.getDay() != null && item.getPeriod() != null) {
                String key = item.getDay() + "-" + item.getPeriod();
                gridSchedule.put(key, item);
            }
        }

        return ScheduleDTO.builder()
                .semesterId(semesterId)
                .semesterName(semesterName)
                .items(items)
                .weeklySchedule(weeklySchedule)
                .gridSchedule(gridSchedule)
                .build();
    }

    /**
     * 格式化课表为文本格式（用于导出）
     *
     * @param schedule 课表
     * @return 格式化后的文本
     */
    public String formatScheduleToText(ScheduleDTO schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("课表\n");
        sb.append("学期：").append(schedule.getSemesterName()).append("\n");
        sb.append("=" .repeat(60)).append("\n\n");

        // 按星期分组
        Map<Integer, List<ScheduleItemDTO>> daySchedule = schedule.getItems().stream()
                .filter(item -> item.getDay() != null)
                .collect(Collectors.groupingBy(ScheduleItemDTO::getDay, TreeMap::new, 
                        Collectors.toList()));

        String[] dayNames = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (Map.Entry<Integer, List<ScheduleItemDTO>> entry : daySchedule.entrySet()) {
            sb.append(dayNames[entry.getKey()]).append(":\n");
            
            for (ScheduleItemDTO item : entry.getValue()) {
                sb.append("  ").append(item.getCourseName())
                  .append(" [").append(item.getPeriod()).append("节]");
                
                if (item.getTeacherName() != null) {
                    sb.append(" 教师: ").append(item.getTeacherName());
                }
                if (item.getStudentCount() != null) {
                    sb.append(" 学生: ").append(item.getStudentCount()).append("人");
                }
                if (item.getLocation() != null) {
                    sb.append(" 地点: ").append(item.getLocation());
                }
                if (item.getWeeks() != null && !item.getWeeks().isEmpty()) {
                    sb.append(" 周次: ").append(formatWeeks(item.getWeeks()));
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化周次列表
     *
     * @param weeks 周次列表
     * @return 格式化后的字符串（如"1-5,7,9-12"）
     */
    private String formatWeeks(List<Integer> weeks) {
        if (weeks == null || weeks.isEmpty()) {
            return "";
        }

        List<Integer> sorted = new ArrayList<>(weeks);
        Collections.sort(sorted);

        StringBuilder sb = new StringBuilder();
        int start = sorted.get(0);
        int prev = start;

        for (int i = 1; i < sorted.size(); i++) {
            int current = sorted.get(i);
            if (current != prev + 1) {
                // 不连续，输出前面的范围
                if (start == prev) {
                    sb.append(start).append(",");
                } else {
                    sb.append(start).append("-").append(prev).append(",");
                }
                start = current;
            }
            prev = current;
        }

        // 输出最后一个范围
        if (start == prev) {
            sb.append(start);
        } else {
            sb.append(start).append("-").append(prev);
        }

        return sb.toString();
    }
}

