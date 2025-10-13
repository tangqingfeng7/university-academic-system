package com.university.ems.service.impl;

import com.university.academic.entity.Classroom;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Semester;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ClassroomRepository;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.SemesterRepository;
import com.university.ems.dto.*;
import com.university.ems.entity.SchedulingSolution;
import com.university.ems.enums.SolutionStatus;
import com.university.ems.model.ScheduleAssignment;
import com.university.ems.model.TimeSlot;
import com.university.ems.repository.SchedulingSolutionRepository;
import com.university.ems.service.SchedulingAlgorithmService;
import com.university.ems.service.TeacherPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 智能排课算法服务实现类
 * 使用约束满足算法进行排课
 * 
 * @author Academic System Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingAlgorithmServiceImpl implements SchedulingAlgorithmService {

    private final SemesterRepository semesterRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final ClassroomRepository classroomRepository;
    private final SchedulingSolutionRepository solutionRepository;
    private final TeacherPreferenceService teacherPreferenceService;
    private final SchedulingConstraintChecker constraintChecker;

    // 排课配置
    private static final int DAYS_PER_WEEK = 5; // 周一到周五
    private static final int SLOTS_PER_DAY = 8; // 每天8节课
    private static final int MAX_CONSECUTIVE_CLASSES = 3; // 最多连续3节课

    @Override
    @Transactional
    public SchedulingResultDTO schedule(SchedulingRequest request) {
        log.info("开始执行智能排课: semesterId={}", request.getSemesterId());
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证和加载数据
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SEMESTER_NOT_FOUND));

            List<CourseOffering> courseOfferings = loadCourseOfferings(request);
            List<Classroom> classrooms = classroomRepository.findAll().stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                    .collect(Collectors.toList());

            if (courseOfferings.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "没有需要排课的课程");
            }

            if (classrooms.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "没有可用的教室");
            }

            // 2. 创建排课方案
            SchedulingSolution solution = createSolution(semester, request.getSolutionName());

            // 3. 执行排课算法
            List<ScheduleAssignment> assignments = executeSchedulingAlgorithm(
                    courseOfferings,
                    classrooms,
                    request
            );

            // 4. 检查约束和评估质量
            List<ConflictDTO> conflicts = constraintChecker.checkHardConstraints(assignments);
            double qualityScore = calculateQualityScore(assignments, conflicts);

            // 5. 更新方案状态
            solution.setQualityScore(qualityScore);
            solution.setConflictCount(conflicts.size());
            solution.setStatus(conflicts.isEmpty() ? SolutionStatus.COMPLETED : SolutionStatus.DRAFT);
            solution.setGeneratedAt(LocalDateTime.now());
            solution = solutionRepository.save(solution);

            // 6. 构建返回结果
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("排课完成: solutionId={}, 耗时={}ms, 质量分数={}, 冲突数={}", 
                    solution.getId(), elapsedTime, qualityScore, conflicts.size());

            return buildResult(solution, assignments, conflicts, elapsedTime, true, null);

        } catch (Exception e) {
            log.error("排课失败", e);
            long elapsedTime = System.currentTimeMillis() - startTime;
            return buildResult(null, Collections.emptyList(), Collections.emptyList(), 
                    elapsedTime, false, e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional
    public CompletableFuture<SchedulingResultDTO> scheduleAsync(SchedulingRequest request) {
        log.info("开始异步执行智能排课: semesterId={}", request.getSemesterId());
        return CompletableFuture.completedFuture(schedule(request));
    }

    @Override
    @Transactional(readOnly = true)
    public Double evaluateSolutionQuality(Long solutionId) {
        SchedulingSolution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOLUTION_NOT_FOUND));
        return solution.getQualityScore();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer detectConflicts(Long solutionId) {
        SchedulingSolution solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOLUTION_NOT_FOUND));
        return solution.getConflictCount();
    }

    /**
     * 执行排课算法核心逻辑
     * 使用贪心算法 + 约束满足
     */
    private List<ScheduleAssignment> executeSchedulingAlgorithm(
            List<CourseOffering> courseOfferings,
            List<Classroom> classrooms,
            SchedulingRequest request) {

        List<ScheduleAssignment> assignments = new ArrayList<>();
        Map<Long, TeacherPreferenceDTO> teacherPreferences = new HashMap<>();

        // 加载教师偏好
        if (Boolean.TRUE.equals(request.getConsiderTeacherPreference())) {
            for (CourseOffering offering : courseOfferings) {
                Long teacherId = offering.getTeacher().getId();
                if (!teacherPreferences.containsKey(teacherId)) {
                    TeacherPreferenceDTO preference = teacherPreferenceService.getPreferenceByTeacherId(teacherId);
                    if (preference != null) {
                        teacherPreferences.put(teacherId, preference);
                    }
                }
            }
        }

        // 按学生人数降序排序（大课程优先排课）
        courseOfferings.sort((a, b) -> {
            int countA = getCourseStudentCount(a);
            int countB = getCourseStudentCount(b);
            return Integer.compare(countB, countA);
        });

        // 为每个课程分配时间和教室
        for (CourseOffering offering : courseOfferings) {
            ScheduleAssignment assignment = createAssignment(offering, classrooms);
            if (assignment == null) {
                log.warn("课程无法排课: {}", offering.getCourse().getName());
                continue;
            }

            // 分配时间槽
            boolean success = assignTimeSlots(assignment, assignments, teacherPreferences, request);
            if (success) {
                assignments.add(assignment);
                log.debug("课程排课成功: {} - {}", offering.getCourse().getName(), assignment.getTimeSlots());
            } else {
                log.warn("课程排课失败: {}", offering.getCourse().getName());
            }
        }

        return assignments;
    }

    /**
     * 为课程分配时间槽
     */
    private boolean assignTimeSlots(ScheduleAssignment assignment,
                                   List<ScheduleAssignment> existingAssignments,
                                   Map<Long, TeacherPreferenceDTO> teacherPreferences,
                                   SchedulingRequest request) {

        int requiredSlots = assignment.getWeeklyHours();
        List<TimeSlot> assignedSlots = new ArrayList<>();
        TeacherPreferenceDTO preference = teacherPreferences.get(assignment.getTeacherId());

        // 生成候选时间槽列表（优先考虑教师偏好）
        List<TimeSlot> candidates = generateCandidateTimeSlots(preference);

        // 尝试分配时间槽
        int attempts = 0;
        int maxAttempts = Math.min(request.getMaxIterations(), candidates.size());

        for (TimeSlot candidate : candidates) {
            if (attempts++ >= maxAttempts) break;
            if (assignedSlots.size() >= requiredSlots) break;

            // 检查硬约束
            if (constraintChecker.hasTeacherTimeConflict(existingAssignments, assignment, candidate)) {
                continue;
            }
            if (constraintChecker.hasClassroomTimeConflict(existingAssignments, assignment, candidate)) {
                continue;
            }

            // 检查软约束
            if (constraintChecker.hasTooManyConsecutiveClasses(
                    existingAssignments, assignment.getTeacherId(), candidate, MAX_CONSECUTIVE_CLASSES)) {
                continue;
            }

            // 分配此时间槽
            assignedSlots.add(candidate);
            assignment.addTimeSlot(candidate);
        }

        // 检查是否成功分配了足够的时间槽
        boolean success = assignedSlots.size() >= requiredSlots;
        assignment.setScheduled(success);
        return success;
    }

    /**
     * 生成候选时间槽列表
     */
    private List<TimeSlot> generateCandidateTimeSlots(TeacherPreferenceDTO preference) {
        List<TimeSlot> candidates = new ArrayList<>();

        // 如果有教师偏好，优先使用偏好时间
        if (preference != null && preference.getPreferredDaysList() != null 
                && !preference.getPreferredDaysList().isEmpty()
                && preference.getPreferredTimeSlotsList() != null
                && !preference.getPreferredTimeSlotsList().isEmpty()) {

            for (Integer day : preference.getPreferredDaysList()) {
                for (Integer slot : preference.getPreferredTimeSlotsList()) {
                    candidates.add(TimeSlot.of(day, slot));
                }
            }
        }

        // 添加所有可能的时间槽
        for (int day = 1; day <= DAYS_PER_WEEK; day++) {
            for (int slot = 1; slot <= SLOTS_PER_DAY; slot++) {
                TimeSlot timeSlot = TimeSlot.of(day, slot);
                if (!candidates.contains(timeSlot)) {
                    candidates.add(timeSlot);
                }
            }
        }

        // 打乱顺序增加随机性
        Collections.shuffle(candidates);

        return candidates;
    }

    /**
     * 创建排课分配对象
     */
    private ScheduleAssignment createAssignment(CourseOffering offering, List<Classroom> classrooms) {
        int studentCount = getCourseStudentCount(offering);

        // 选择合适的教室
        Classroom classroom = findSuitableClassroom(classrooms, studentCount);
        if (classroom == null) {
            log.warn("找不到合适的教室: 课程={}, 学生数={}", offering.getCourse().getName(), studentCount);
            return null;
        }

        return ScheduleAssignment.builder()
                .courseOfferingId(offering.getId())
                .courseName(offering.getCourse().getName())
                .courseNo(offering.getCourse().getCourseNo())
                .teacherId(offering.getTeacher().getId())
                .teacherName(offering.getTeacher().getName())
                .classroomId(classroom.getId())
                .classroomNo(classroom.getRoomNo())
                .classroomCapacity(classroom.getCapacity())
                .studentCount(studentCount)
                .weeklyHours(offering.getCourse().getCredits().intValue() * 2) // 假设每学分2课时
                .timeSlots(new ArrayList<>())
                .scheduled(false)
                .build();
    }

    /**
     * 查找合适的教室
     */
    private Classroom findSuitableClassroom(List<Classroom> classrooms, int studentCount) {
        // 选择容量刚好够用的教室（避免浪费）
        return classrooms.stream()
                .filter(c -> c.getCapacity() >= studentCount)
                .min(Comparator.comparingInt(Classroom::getCapacity))
                .orElse(null);
    }

    /**
     * 计算质量分数
     */
    private double calculateQualityScore(List<ScheduleAssignment> assignments, List<ConflictDTO> conflicts) {
        if (assignments.isEmpty()) {
            return 0.0;
        }

        double score = 100.0;

        // 硬约束违反严重扣分
        long hardConflicts = conflicts.stream()
                .filter(c -> "HARD".equals(c.getSeverity()))
                .count();
        score -= hardConflicts * 20;

        // 未完全排课扣分
        long unscheduled = assignments.stream()
                .filter(a -> !a.isFullyScheduled())
                .count();
        score -= (unscheduled * 10.0 / assignments.size());

        // 计算教师空闲时间碎片（软约束）
        int totalFragments = 0;
        for (int day = 1; day <= DAYS_PER_WEEK; day++) {
            for (ScheduleAssignment assignment : assignments) {
                totalFragments += constraintChecker.calculateIdleTimeFragments(
                        assignments, assignment.getTeacherId(), day);
            }
        }
        score -= Math.min(totalFragments * 0.5, 20); // 最多扣20分

        return Math.max(score, 0.0);
    }

    /**
     * 加载课程列表
     */
    private List<CourseOffering> loadCourseOfferings(SchedulingRequest request) {
        if (request.getCourseOfferingIds() != null && !request.getCourseOfferingIds().isEmpty()) {
            return courseOfferingRepository.findAllById(request.getCourseOfferingIds());
        } else {
            return courseOfferingRepository.findBySemesterId(request.getSemesterId());
        }
    }

    /**
     * 创建排课方案
     */
    private SchedulingSolution createSolution(Semester semester, String solutionName) {
        String name = solutionName != null ? solutionName : 
                "排课方案-" + semester.getId() + "-" + System.currentTimeMillis();

        SchedulingSolution solution = SchedulingSolution.builder()
                .semester(semester)
                .name(name)
                .status(SolutionStatus.OPTIMIZING)
                .build();

        return solutionRepository.save(solution);
    }

    /**
     * 获取课程实际选课人数
     * 
     * @param offering 开课计划
     * @return 选课人数
     */
    private int getCourseStudentCount(CourseOffering offering) {
        // 查询course_selection表统计选课人数
        long count = courseSelectionRepository.countActiveByOfferingId(offering.getId());
        
        // 如果没有选课记录，返回预估值（容量的50%或30人）
        if (count == 0) {
            return offering.getCapacity() != null ? offering.getCapacity() / 2 : 30;
        }
        
        return (int) count;
    }

    /**
     * 构建结果对象
     */
    private SchedulingResultDTO buildResult(SchedulingSolution solution,
                                           List<ScheduleAssignment> assignments,
                                           List<ConflictDTO> conflicts,
                                           long elapsedMillis,
                                           boolean success,
                                           String errorMessage) {
        if (!success) {
            return SchedulingResultDTO.builder()
                    .success(false)
                    .errorMessage(errorMessage)
                    .elapsedMillis(elapsedMillis)
                    .completedAt(LocalDateTime.now())
                    .build();
        }

        List<ScheduleItemDTO> scheduleItems = assignments.stream()
                .flatMap(assignment -> assignment.getTimeSlots().stream()
                        .map(timeSlot -> convertToScheduleItemDTO(assignment, timeSlot)))
                .collect(Collectors.toList());

        long scheduled = assignments.stream().filter(ScheduleAssignment::isFullyScheduled).count();

        return SchedulingResultDTO.builder()
                .solutionId(solution.getId())
                .success(true)
                .qualityScore(solution.getQualityScore())
                .conflictCount(solution.getConflictCount())
                .hardConstraintViolations((int) conflicts.stream()
                        .filter(c -> "HARD".equals(c.getSeverity())).count())
                .softConstraintViolations((int) conflicts.stream()
                        .filter(c -> "SOFT".equals(c.getSeverity())).count())
                .scheduledCourseCount((int) scheduled)
                .unscheduledCourseCount((int) (assignments.size() - scheduled))
                .scheduleItems(scheduleItems)
                .conflicts(conflicts)
                .elapsedMillis(elapsedMillis)
                .completedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 转换为ScheduleItemDTO
     */
    private ScheduleItemDTO convertToScheduleItemDTO(ScheduleAssignment assignment, TimeSlot timeSlot) {
        return ScheduleItemDTO.builder()
                .courseOfferingId(assignment.getCourseOfferingId())
                .courseName(assignment.getCourseName())
                .courseNo(assignment.getCourseNo())
                .teacherId(assignment.getTeacherId())
                .teacherName(assignment.getTeacherName())
                .classroomId(assignment.getClassroomId())
                .classroomNo(assignment.getClassroomNo())
                .dayOfWeek(timeSlot.getDayOfWeek())
                .dayOfWeekDescription(getDayDescription(timeSlot.getDayOfWeek()))
                .startSlot(timeSlot.getSlot())
                .endSlot(timeSlot.getSlot())
                .timeSlotDescription(timeSlot.toString())
                .studentCount(assignment.getStudentCount())
                .satisfiesHardConstraints(true)
                .softConstraintScore(80.0) // 简化计算
                .build();
    }

    /**
     * 获取星期描述
     */
    private String getDayDescription(Integer day) {
        String[] days = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return day >= 1 && day <= 7 ? days[day] : "周" + day;
    }
}

