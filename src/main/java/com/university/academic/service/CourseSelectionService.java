package com.university.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CoursePrerequisiteRepository;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 选课服务类
 * 提供选课、退课、查询等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSelectionService {

    private final CourseSelectionRepository selectionRepository;
    private final CourseOfferingRepository offeringRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final SystemConfigRepository configRepository;
    private final StudentService studentService;
    private final SemesterService semesterService;
    private final ObjectMapper objectMapper;

    /**
     * 根据ID查询选课记录
     *
     * @param id 选课记录ID
     * @return 选课记录对象
     */
    @Transactional(readOnly = true)
    public CourseSelection findById(Long id) {
        return selectionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELECTION_NOT_FOUND));
    }

    /**
     * 查询学生的所有选课记录
     *
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    @Transactional(readOnly = true)
    public List<CourseSelection> findByStudent(Long studentId) {
        return selectionRepository.findByStudentId(studentId);
    }

    /**
     * 查询学生在指定学期的选课记录
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 选课记录列表
     */
    @Transactional(readOnly = true)
    public List<CourseSelection> findByStudentAndSemester(Long studentId, Long semesterId) {
        return selectionRepository.findByStudentAndSemester(studentId, semesterId);
    }

    /**
     * 查询学生在当前学期的选课记录
     *
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    @Transactional(readOnly = true)
    public List<CourseSelection> findByStudentInActiveSemester(Long studentId) {
        Semester activeSemester = semesterService.findActiveSemester();
        return selectionRepository.findByStudentAndSemester(studentId, activeSemester.getId());
    }

    /**
     * 查询开课班级的选课学生列表（只包含已选状态）
     *
     * @param offeringId 开课计划ID
     * @return 选课记录列表
     */
    public List<CourseSelection> findActiveByOffering(Long offeringId) {
        List<CourseSelection> selections = selectionRepository.findByOfferingId(offeringId);
        // 只返回状态为SELECTED的选课记录
        return selections.stream()
                .filter(selection -> selection.getStatus() == CourseSelection.SelectionStatus.SELECTED)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询当前学期可选的开课计划列表
     *
     * @return 开课计划列表
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> findAvailableOfferings() {
        Semester activeSemester = semesterService.findActiveSemester();
        List<CourseOffering> offerings = offeringRepository.findBySemesterId(activeSemester.getId());
        
        // 只返回已发布的开课计划
        return offerings.stream()
                .filter(o -> o.getStatus() == CourseOffering.OfferingStatus.PUBLISHED)
                .toList();
    }

    /**
     * 学生选课
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 选课记录对象
     */
    @Transactional
    public CourseSelection selectCourse(Long studentId, Long offeringId) {
        // 1. 验证学生和开课计划是否存在
        Student student = studentService.findById(studentId);
        CourseOffering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        // 2. 验证开课计划状态
        if (offering.getStatus() != CourseOffering.OfferingStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.OFFERING_NOT_FOUND);
        }

        // 3. 验证选课时间
        validateSelectionPeriod(offering.getSemester());

        // 4. 检查是否已选或已存在退课记录
        Optional<CourseSelection> existingSelection = selectionRepository
                .findByStudentIdAndOfferingId(studentId, offeringId);
        
        if (existingSelection.isPresent()) {
            CourseSelection existing = existingSelection.get();
            if (existing.getStatus() == CourseSelection.SelectionStatus.SELECTED) {
                throw new BusinessException(ErrorCode.SELECTION_ALREADY_EXISTS);
            }
            // 如果是已退课，允许重新选课，更新状态和选课时间
            existing.setStatus(CourseSelection.SelectionStatus.SELECTED);
            existing.setSelectionTime(LocalDateTime.now());
            
            // 验证先修课程
            validatePrerequisites(studentId, offering.getCourse().getId());
            
            // 验证时间冲突（排除自己）
            validateTimeConflict(studentId, offering, offeringId);
            
            // 验证学分上限
            validateCreditLimit(studentId, offering.getSemester().getId(), 
                    offering.getCourse().getCredits());
            
            // 验证并更新人数限制
            validateAndUpdateCapacity(offering);
            
            CourseSelection savedSelection = selectionRepository.save(existing);
            log.info("学生重新选课成功: 学生={}, 课程={}, 教师={}", 
                    student.getName(), 
                    offering.getCourse().getName(),
                    offering.getTeacher().getName());
            
            return savedSelection;
        }

        // 5. 验证先修课程
        validatePrerequisites(studentId, offering.getCourse().getId());

        // 6. 验证时间冲突（新选课不排除任何课程）
        validateTimeConflict(studentId, offering, null);

        // 7. 验证学分上限
        validateCreditLimit(studentId, offering.getSemester().getId(), 
                offering.getCourse().getCredits());

        // 8. 验证人数限制（使用乐观锁）
        validateAndUpdateCapacity(offering);

        // 9. 创建选课记录
        CourseSelection selection = CourseSelection.builder()
                .student(student)
                .offering(offering)
                .selectionTime(LocalDateTime.now())
                .status(CourseSelection.SelectionStatus.SELECTED)
                .build();

        CourseSelection savedSelection = selectionRepository.save(selection);
        log.info("学生选课成功: 学生={}, 课程={}, 教师={}", 
                student.getName(), 
                offering.getCourse().getName(),
                offering.getTeacher().getName());

        return savedSelection;
    }

    /**
     * 学生退课
     *
     * @param studentId  学生ID
     * @param selectionId 选课记录ID
     */
    @Transactional
    public void dropCourse(Long studentId, Long selectionId) {
        // 1. 查询选课记录
        CourseSelection selection = findById(selectionId);

        // 2. 验证是否为该学生的选课记录
        if (!selection.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 3. 验证选课状态
        if (selection.getStatus() == CourseSelection.SelectionStatus.DROPPED) {
            throw new BusinessException(ErrorCode.SELECTION_ALREADY_DROPPED);
        }

        if (selection.getStatus() == CourseSelection.SelectionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.SELECTION_CANNOT_DROP);
        }

        // 4. 验证退课期限
        validateDropDeadline(selection.getOffering().getSemester());

        // 5. 释放课程名额
        CourseOffering offering = selection.getOffering();
        offering.setEnrolled(offering.getEnrolled() - 1);
        offeringRepository.save(offering);

        // 6. 更新选课状态
        selection.setStatus(CourseSelection.SelectionStatus.DROPPED);
        selectionRepository.save(selection);

        log.info("学生退课成功: 学生={}, 课程={}", 
                selection.getStudent().getName(),
                offering.getCourse().getName());
    }

    /**
     * 验证选课时间
     *
     * @param semester 学期
     */
    private void validateSelectionPeriod(Semester semester) {
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isBefore(semester.getCourseSelectionStart())) {
            throw new BusinessException(ErrorCode.SELECTION_PERIOD_NOT_START);
        }
        
        if (now.isAfter(semester.getCourseSelectionEnd())) {
            throw new BusinessException(ErrorCode.SELECTION_PERIOD_ENDED);
        }
    }

    /**
     * 验证退课期限
     *
     * @param semester 学期
     */
    private void validateDropDeadline(Semester semester) {
        LocalDateTime now = LocalDateTime.now();
        
        // 从系统配置读取退课截止天数
        int dropDeadlineDays = getConfigInt("drop_deadline_days", 14);
        LocalDateTime dropDeadline = semester.getStartDate()
                .plusDays(dropDeadlineDays)
                .atTime(23, 59, 59);
        
        if (now.isAfter(dropDeadline)) {
            throw new BusinessException(ErrorCode.SELECTION_DROP_DEADLINE_PASSED);
        }
    }

    /**
     * 验证先修课程
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     */
    private void validatePrerequisites(Long studentId, Long courseId) {
        // 查询该课程的所有先修课程
        List<CoursePrerequisite> prerequisites = prerequisiteRepository.findByCourseId(courseId);
        
        if (prerequisites.isEmpty()) {
            return; // 没有先修课程要求
        }

        // 查询学生的所有已完成课程
        List<CourseSelection> completedSelections = selectionRepository.findByStudentId(studentId)
                .stream()
                .filter(s -> s.getStatus() == CourseSelection.SelectionStatus.COMPLETED)
                .toList();

        Set<Long> completedCourseIds = new HashSet<>();
        for (CourseSelection selection : completedSelections) {
            completedCourseIds.add(selection.getOffering().getCourse().getId());
        }

        // 检查是否满足所有先修课程要求
        for (CoursePrerequisite prerequisite : prerequisites) {
            Long prerequisiteCourseId = prerequisite.getPrerequisiteCourse().getId();
            if (!completedCourseIds.contains(prerequisiteCourseId)) {
                log.warn("学生 {} 未满足先修课程要求: 需要完成课程 {}", 
                        studentId, prerequisite.getPrerequisiteCourse().getName());
                throw new BusinessException(ErrorCode.SELECTION_PREREQUISITE_NOT_MET);
            }
        }
    }

    /**
     * 验证时间冲突
     *
     * @param studentId  学生ID
     * @param offering   开课计划
     * @param excludeOfferingId 要排除的开课计划ID（重新选课时排除自己）
     */
    private void validateTimeConflict(Long studentId, CourseOffering offering, Long excludeOfferingId) {
        // 查询学生在该学期的其他已选课程
        List<CourseSelection> selections = selectionRepository
                .findActiveByStudentAndSemester(studentId, offering.getSemester().getId());

        // 解析当前课程的上课时间
        List<Map<String, Object>> currentSchedule = parseSchedule(offering.getSchedule());

        // 检查是否与已选课程时间冲突
        for (CourseSelection selection : selections) {
            // 排除指定的开课计划（重新选课时排除自己）
            if (excludeOfferingId != null && selection.getOffering().getId().equals(excludeOfferingId)) {
                continue;
            }
            
            List<Map<String, Object>> existingSchedule = 
                    parseSchedule(selection.getOffering().getSchedule());
            
            if (hasTimeConflict(currentSchedule, existingSchedule)) {
                log.warn("学生 {} 选课时间冲突: 与课程 {} 冲突", 
                        studentId, selection.getOffering().getCourse().getName());
                throw new BusinessException(ErrorCode.SELECTION_TIME_CONFLICT);
            }
        }
    }

    /**
     * 验证学分上限
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @param newCredits 新选课程的学分
     */
    private void validateCreditLimit(Long studentId, Long semesterId, Integer newCredits) {
        // 从系统配置读取学分上限
        int maxCredits = getConfigInt("max_credits_per_semester", 30);

        // 统计学生在该学期已选课程的总学分
        Integer currentCredits = selectionRepository
                .sumCreditsByStudentAndSemester(studentId, semesterId);
        
        if (currentCredits == null) {
            currentCredits = 0;
        }

        // 检查是否超过学分上限
        if (currentCredits + newCredits > maxCredits) {
            log.warn("学生 {} 选课超过学分上限: 当前{}学分, 新增{}学分, 上限{}学分", 
                    studentId, currentCredits, newCredits, maxCredits);
            throw new BusinessException(ErrorCode.SELECTION_CREDITS_EXCEED);
        }
    }

    /**
     * 验证并更新课程容量（使用乐观锁）
     *
     * @param offering 开课计划
     */
    private void validateAndUpdateCapacity(CourseOffering offering) {
        // 检查是否还有名额
        if (!offering.hasCapacity()) {
            throw new BusinessException(ErrorCode.SELECTION_COURSE_FULL);
        }

        // 增加已选人数（JPA会使用乐观锁自动处理并发）
        offering.setEnrolled(offering.getEnrolled() + 1);
        try {
            offeringRepository.save(offering);
        } catch (Exception e) {
            // 乐观锁冲突，说明课程已满
            log.error("选课并发冲突: offeringId={}", offering.getId(), e);
            throw new BusinessException(ErrorCode.OPTIMISTIC_LOCK_FAILURE);
        }
    }

    /**
     * 从系统配置读取整型值
     *
     * @param configKey    配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    private int getConfigInt(String configKey, int defaultValue) {
        return configRepository.findByConfigKey(configKey)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        log.error("系统配置值格式错误: key={}, value={}", 
                                configKey, config.getConfigValue());
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    /**
     * 解析上课时间JSON
     *
     * @param schedule 上课时间JSON字符串
     * @return 上课时间列表
     */
    private List<Map<String, Object>> parseSchedule(String schedule) {
        try {
            return objectMapper.readValue(schedule, 
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.error("解析上课时间失败: {}", schedule, e);
            return new ArrayList<>();
        }
    }

    /**
     * 检查两个上课时间是否冲突
     *
     * @param schedule1 上课时间1
     * @param schedule2 上课时间2
     * @return true-冲突，false-不冲突
     */
    private boolean hasTimeConflict(List<Map<String, Object>> schedule1, 
                                   List<Map<String, Object>> schedule2) {
        for (Map<String, Object> time1 : schedule1) {
            for (Map<String, Object> time2 : schedule2) {
                // 检查是否同一天
                Integer day1 = (Integer) time1.get("day");
                Integer day2 = (Integer) time2.get("day");
                
                if (!day1.equals(day2)) {
                    continue;
                }

                // 检查时间段是否重叠
                String period1 = (String) time1.get("period");
                String period2 = (String) time2.get("period");
                
                if (isPeriodOverlap(period1, period2)) {
                    // 如果有weeks字段，还需要检查周次是否重叠
                    if (time1.containsKey("weeks") && time2.containsKey("weeks")) {
                        @SuppressWarnings("unchecked")
                        List<Integer> weeks1 = (List<Integer>) time1.get("weeks");
                        @SuppressWarnings("unchecked")
                        List<Integer> weeks2 = (List<Integer>) time2.get("weeks");
                        
                        if (hasWeekOverlap(weeks1, weeks2)) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查时间段是否重叠
     * 支持单节次格式: "1" 和 "1-2" 有重叠
     *
     * @param period1 时间段1 (格式: "1-2" 或 "1")
     * @param period2 时间段2 (格式: "1-2" 或 "1")
     * @return true-重叠，false-不重叠
     */
    private boolean isPeriodOverlap(String period1, String period2) {
        try {
            String[] parts1 = period1.split("-");
            String[] parts2 = period2.split("-");
            
            int start1 = Integer.parseInt(parts1[0]);
            int end1 = parts1.length > 1 ? Integer.parseInt(parts1[1]) : start1; // 单节次时start=end
            int start2 = Integer.parseInt(parts2[0]);
            int end2 = parts2.length > 1 ? Integer.parseInt(parts2[1]) : start2; // 单节次时start=end
            
            return !(end1 < start2 || end2 < start1);
        } catch (Exception e) {
            log.error("解析时间段失败: {} vs {}", period1, period2, e);
            return false;
        }
    }

    /**
     * 检查周次是否重叠
     *
     * @param weeks1 周次列表1
     * @param weeks2 周次列表2
     * @return true-重叠，false-不重叠
     */
    private boolean hasWeekOverlap(List<Integer> weeks1, List<Integer> weeks2) {
        if (weeks1 == null || weeks2 == null) {
            return true;
        }
        
        Set<Integer> set1 = new HashSet<>(weeks1);
        for (Integer week : weeks2) {
            if (set1.contains(week)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计学生在指定学期的选课数量
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 选课数量
     */
    @Transactional(readOnly = true)
    public long countByStudentAndSemester(Long studentId, Long semesterId) {
        return selectionRepository.countByStudentIdAndSemesterId(studentId, semesterId);
    }
}

