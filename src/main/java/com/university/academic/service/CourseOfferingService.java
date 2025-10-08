package com.university.academic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.academic.dto.CreateNotificationRequest;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 开课计划服务类
 * 提供开课计划CRUD、时间冲突检测、发布取消等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseOfferingService {

    private final CourseOfferingRepository offeringRepository;
    private final CourseSelectionRepository selectionRepository;
    private final SemesterService semesterService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * 根据ID查询开课计划
     *
     * @param id 开课计划ID
     * @return 开课计划对象
     */
    @Transactional(readOnly = true)
    public CourseOffering findById(Long id) {
        return offeringRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));
    }

    /**
     * 查询所有开课计划
     *
     * @param pageable 分页参数
     * @return 开课计划分页数据
     */
    @Transactional(readOnly = true)
    public Page<CourseOffering> findAll(Pageable pageable) {
        return offeringRepository.findAll(pageable);
    }

    /**
     * 根据筛选条件查询开课计划
     *
     * @param semesterId 学期ID
     * @param courseId   课程ID
     * @param teacherId  教师ID
     * @param pageable   分页参数
     * @return 开课计划分页数据
     */
    @Transactional(readOnly = true)
    public Page<CourseOffering> findWithFilters(Long semesterId, Long courseId, 
                                                Long teacherId, Pageable pageable) {
        return offeringRepository.findWithFilters(semesterId, courseId, teacherId, pageable);
    }

    /**
     * 根据学期ID查询开课计划
     *
     * @param semesterId 学期ID
     * @param pageable   分页参数
     * @return 开课计划分页数据
     */
    @Transactional(readOnly = true)
    public Page<CourseOffering> findBySemester(Long semesterId, Pageable pageable) {
        return offeringRepository.findBySemesterId(semesterId, pageable);
    }

    /**
     * 根据教师ID查询开课计划
     *
     * @param teacherId 教师ID
     * @return 开课计划列表
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> findByTeacher(Long teacherId) {
        return offeringRepository.findByTeacherId(teacherId);
    }

    /**
     * 创建开课计划
     *
     * @param offering 开课计划对象
     * @return 创建后的开课计划对象
     */
    @Transactional
    public CourseOffering createOffering(CourseOffering offering) {
        // 验证学期、课程、教师是否存在
        Semester semester = semesterService.findById(offering.getSemester().getId());
        Course course = courseService.findById(offering.getCourse().getId());
        Teacher teacher = teacherService.findById(offering.getTeacher().getId());

        offering.setSemester(semester);
        offering.setCourse(course);
        offering.setTeacher(teacher);

        // 验证上课时间格式
        validateScheduleFormat(offering.getSchedule());

        // 检查教师时间冲突
        checkTeacherTimeConflict(teacher.getId(), semester.getId(), 
                offering.getSchedule(), null);

        // 检查教室时间冲突
        checkLocationTimeConflict(offering.getLocation(), semester.getId(), 
                offering.getSchedule(), null);

        // 初始化状态和已选人数
        offering.setStatus(CourseOffering.OfferingStatus.DRAFT);
        offering.setEnrolled(0);

        CourseOffering savedOffering = offeringRepository.save(offering);
        log.info("创建开课计划成功: {} - {} - {} (教师: {}, 容量: {})",
                savedOffering.getId(),
                semester.getSemesterName(),
                course.getName(),
                teacher.getName(),
                savedOffering.getCapacity());
        return savedOffering;
    }

    /**
     * 更新开课计划信息
     *
     * @param id       开课计划ID
     * @param offering 更新的开课计划信息
     * @return 更新后的开课计划对象
     */
    @Transactional
    public CourseOffering updateOffering(Long id, CourseOffering offering) {
        CourseOffering existingOffering = findById(id);

        // 如果已发布，不允许修改关键信息
        if (existingOffering.getStatus() == CourseOffering.OfferingStatus.PUBLISHED &&
            existingOffering.getEnrolled() > 0) {
            log.warn("开课计划已有学生选课，限制修改: id={}", id);
        }

        // 更新教师
        if (offering.getTeacher() != null && offering.getTeacher().getId() != null) {
            Teacher teacher = teacherService.findById(offering.getTeacher().getId());
            existingOffering.setTeacher(teacher);
        }

        // 更新上课时间
        if (offering.getSchedule() != null) {
            validateScheduleFormat(offering.getSchedule());
            existingOffering.setSchedule(offering.getSchedule());
        }

        // 更新上课地点
        if (offering.getLocation() != null) {
            existingOffering.setLocation(offering.getLocation());
        }

        // 更新容量（不能小于已选人数）
        if (offering.getCapacity() != null) {
            if (offering.getCapacity() < existingOffering.getEnrolled()) {
                throw new BusinessException(ErrorCode.OFFERING_CAPACITY_ERROR);
            }
            existingOffering.setCapacity(offering.getCapacity());
        }

        // 检查时间冲突
        checkTeacherTimeConflict(existingOffering.getTeacher().getId(),
                existingOffering.getSemester().getId(),
                existingOffering.getSchedule(),
                id);

        checkLocationTimeConflict(existingOffering.getLocation(),
                existingOffering.getSemester().getId(),
                existingOffering.getSchedule(),
                id);

        CourseOffering updatedOffering = offeringRepository.save(existingOffering);
        log.info("更新开课计划成功: {} - {}", updatedOffering.getId(), 
                updatedOffering.getCourse().getName());
        return updatedOffering;
    }

    /**
     * 删除开课计划
     *
     * @param id 开课计划ID
     */
    @Transactional
    public void deleteOffering(Long id) {
        CourseOffering offering = findById(id);

        // 如果有学生选课，不允许删除
        if (offering.getEnrolled() > 0) {
            throw new BusinessException(ErrorCode.OFFERING_HAS_STUDENTS);
        }

        // 如果已发布，不允许删除（应该先取消）
        if (offering.getStatus() == CourseOffering.OfferingStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.OFFERING_ALREADY_PUBLISHED);
        }

        offeringRepository.delete(offering);
        log.info("删除开课计划成功: {} - {}", offering.getId(), 
                offering.getCourse().getName());
    }

    /**
     * 发布开课计划
     *
     * @param id 开课计划ID
     */
    @Transactional
    public void publishOffering(Long id) {
        CourseOffering offering = findById(id);

        // 检查状态
        if (offering.getStatus() == CourseOffering.OfferingStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.OFFERING_ALREADY_PUBLISHED);
        }

        if (offering.getStatus() == CourseOffering.OfferingStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.OFFERING_ALREADY_CANCELLED);
        }

        // 设置为已发布状态
        offering.setStatus(CourseOffering.OfferingStatus.PUBLISHED);
        offeringRepository.save(offering);

        log.info("发布开课计划成功: {} - {} - {}", 
                offering.getId(),
                offering.getSemester().getSemesterName(),
                offering.getCourse().getName());
    }

    /**
     * 取消开课计划
     *
     * @param id 开课计划ID
     */
    @Transactional
    public void cancelOffering(Long id) {
        CourseOffering offering = findById(id);

        // 检查状态
        if (offering.getStatus() == CourseOffering.OfferingStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.OFFERING_ALREADY_CANCELLED);
        }

        // 如果有学生选课，需要通知学生并退选
        if (offering.getEnrolled() > 0) {
            log.info("取消开课计划，有 {} 名学生已选课，开始处理退选", offering.getEnrolled());
            
            // 1. 获取所有选课记录
            List<CourseSelection> selections = selectionRepository.findByOfferingId(id);
            
            // 2. 将所有SELECTED状态的选课记录改为DROPPED
            int droppedCount = 0;
            for (CourseSelection selection : selections) {
                if (selection.getStatus() == CourseSelection.SelectionStatus.SELECTED) {
                    selection.setStatus(CourseSelection.SelectionStatus.DROPPED);
                    selectionRepository.save(selection);
                    droppedCount++;
                }
            }
            
            // 3. 更新已选人数
            offering.setEnrolled(0);
            
            // 4. 发送通知给所有相关学生
            if (droppedCount > 0) {
                try {
                    String notificationTitle = String.format("课程取消通知：%s", offering.getCourse().getName());
                    String notificationContent = String.format(
                            "您好，由于特殊原因，课程《%s》（%s学期，授课教师：%s）已被取消。" +
                            "您的选课已被自动退选，给您带来不便敬请谅解。如有疑问，请联系教务处。",
                            offering.getCourse().getName(),
                            offering.getSemester().getSemesterName(),
                            offering.getTeacher().getName()
                    );
                    
                    // 获取第一个学生的用户ID用作发布者（实际应该是管理员或系统账户）
                    Long publisherId = selections.stream()
                            .filter(s -> s.getStudent().getUser() != null)
                            .map(s -> s.getStudent().getUser().getId())
                            .findFirst()
                            .orElse(1L); // 默认使用ID为1的管理员账户
                    
                    CreateNotificationRequest notificationRequest = CreateNotificationRequest.builder()
                            .title(notificationTitle)
                            .content(notificationContent)
                            .type("COURSE")
                            .targetRole("STUDENT")
                            .build();
                    
                    notificationService.publishNotification(notificationRequest, publisherId);
                    
                    log.info("已发送课程取消通知，退选学生数：{}", droppedCount);
                } catch (Exception e) {
                    log.error("发送课程取消通知失败", e);
                    // 不影响取消流程，继续执行
                }
            }
        }

        // 设置为已取消状态
        offering.setStatus(CourseOffering.OfferingStatus.CANCELLED);
        offeringRepository.save(offering);

        log.info("取消开课计划成功: {} - {} - {}", 
                offering.getId(),
                offering.getSemester().getSemesterName(),
                offering.getCourse().getName());
    }

    /**
     * 统计开课计划数量
     *
     * @return 开课计划总数
     */
    @Transactional(readOnly = true)
    public long count() {
        return offeringRepository.count();
    }

    /**
     * 验证上课时间格式
     *
     * @param schedule 上课时间JSON字符串
     */
    private void validateScheduleFormat(String schedule) {
        try {
            // 解析JSON格式
            List<Map<String, Object>> scheduleList = objectMapper.readValue(
                    schedule, new TypeReference<List<Map<String, Object>>>() {});

            if (scheduleList == null || scheduleList.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAM_ERROR);
            }

            // 验证每个时间段的格式
            for (Map<String, Object> item : scheduleList) {
                if (!item.containsKey("day") || !item.containsKey("period")) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR);
                }
            }
        } catch (Exception e) {
            log.error("上课时间格式错误: {}", schedule, e);
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
    }

    /**
     * 检查教师时间冲突
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @param schedule   上课时间
     * @param excludeId  排除的开课计划ID（更新时排除自己）
     */
    private void checkTeacherTimeConflict(Long teacherId, Long semesterId, 
                                         String schedule, Long excludeId) {
        // 查询教师在该学期的其他开课计划
        List<CourseOffering> offerings = offeringRepository
                .findByTeacherAndSemesterExcluding(teacherId, semesterId, excludeId);

        if (offerings.isEmpty()) {
            return;
        }

        // 解析当前上课时间
        List<Map<String, Object>> currentSchedule = parseSchedule(schedule);

        // 检查是否与其他开课计划冲突
        for (CourseOffering offering : offerings) {
            List<Map<String, Object>> existingSchedule = parseSchedule(offering.getSchedule());
            
            if (hasTimeConflict(currentSchedule, existingSchedule)) {
                throw new BusinessException(ErrorCode.OFFERING_TEACHER_CONFLICT);
            }
        }
    }

    /**
     * 检查教室时间冲突
     *
     * @param location   教室
     * @param semesterId 学期ID
     * @param schedule   上课时间
     * @param excludeId  排除的开课计划ID
     */
    private void checkLocationTimeConflict(String location, Long semesterId, 
                                          String schedule, Long excludeId) {
        // 查询该学期使用该教室的其他开课计划
        List<CourseOffering> offerings = offeringRepository
                .findByLocationAndSemesterExcluding(location, semesterId, excludeId);

        if (offerings.isEmpty()) {
            return;
        }

        // 解析当前上课时间
        List<Map<String, Object>> currentSchedule = parseSchedule(schedule);

        // 检查是否与其他开课计划冲突
        for (CourseOffering offering : offerings) {
            List<Map<String, Object>> existingSchedule = parseSchedule(offering.getSchedule());
            
            if (hasTimeConflict(currentSchedule, existingSchedule)) {
                throw new BusinessException(ErrorCode.OFFERING_CLASSROOM_CONFLICT);
            }
        }
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
                        // 如果没有weeks字段，认为是全学期，肯定冲突
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查时间段是否重叠
     * 例如: "1-2" 和 "2-3" 有重叠，"1-2" 和 "3-4" 无重叠
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
            
            // 检查是否有重叠
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
        
        // 检查是否有交集
        Set<Integer> set1 = new HashSet<>(weeks1);
        for (Integer week : weeks2) {
            if (set1.contains(week)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计指定学期的开课数量
     *
     * @param semesterId 学期ID
     * @return 开课数量
     */
    @Transactional(readOnly = true)
    public long countBySemester(Long semesterId) {
        return offeringRepository.countBySemesterId(semesterId);
    }

    /**
     * 查询教师在指定学期的所有授课
     *
     * @param teacherId  教师ID
     * @param semesterId 学期ID
     * @return 开课计划列表
     */
    @Transactional(readOnly = true)
    public List<CourseOffering> findByTeacherAndSemester(Long teacherId, Long semesterId) {
        return offeringRepository.findByTeacherIdAndSemesterId(teacherId, semesterId);
    }
}

