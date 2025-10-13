package com.university.ems.service.impl;

import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Semester;
import com.university.academic.entity.User;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.SemesterRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.service.NotificationService;
import com.university.ems.dto.SchedulingResultDTO;
import com.university.ems.entity.SchedulingSolution;
import com.university.ems.repository.SchedulingSolutionRepository;
import com.university.ems.service.SchedulingNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 排课通知服务实现类
 * 
 * @author Academic System Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingNotificationServiceImpl implements SchedulingNotificationService {

    private final NotificationService notificationService;
    private final SchedulingSolutionRepository solutionRepository;
    private final SemesterRepository semesterRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendSchedulingCompletedNotification(Long solutionId, SchedulingResultDTO result) {
        log.info("发送排课完成通知: solutionId={}", solutionId);

        try {
            SchedulingSolution solution = solutionRepository.findById(solutionId).orElse(null);
            if (solution == null) {
                log.warn("排课方案不存在，无法发送通知: solutionId={}", solutionId);
                return;
            }

            Long semesterId = solution.getSemester().getId();

            // 发送通知给教师
            notifyTeachers(solutionId, semesterId);

            // 发送通知给学生
            notifyStudents(solutionId, semesterId);

            log.info("排课完成通知发送成功: solutionId={}", solutionId);

        } catch (Exception e) {
            log.error("发送排课通知失败: solutionId=" + solutionId, e);
            // 不抛出异常，避免影响主流程
        }
    }

    @Override
    @Transactional
    public void notifyTeachers(Long solutionId, Long semesterId) {
        log.info("发送排课通知给教师: solutionId={}, semesterId={}", solutionId, semesterId);

        try {
            // 查询学期信息
            Semester semester = semesterRepository.findById(semesterId).orElse(null);
            if (semester == null) {
                log.warn("学期不存在: semesterId={}", semesterId);
                return;
            }

            // 查询学期的所有课程
            List<CourseOffering> offerings = courseOfferingRepository.findBySemesterId(semesterId);
            
            // 按教师分组
            Map<Long, List<CourseOffering>> teacherCoursesMap = offerings.stream()
                    .collect(Collectors.groupingBy(o -> o.getTeacher().getId()));

            // 获取系统用户（用于发送通知）
            User systemUser = getSystemUser();

            // 为每位教师发送通知
            for (Map.Entry<Long, List<CourseOffering>> entry : teacherCoursesMap.entrySet()) {
                Long teacherId = entry.getKey();
                List<CourseOffering> courses = entry.getValue();

                // 构建通知内容
                String title = "课程排课通知";
                StringBuilder content = new StringBuilder();
                content.append("您好！\n\n");
                content.append("本学期（").append(semesterId).append("）的课程排课已完成，");
                content.append("您负责的 ").append(courses.size()).append(" 门课程排课情况如下：\n\n");

                for (CourseOffering offering : courses) {
                    content.append("• ").append(offering.getCourse().getName())
                           .append("（").append(offering.getCourse().getCourseNo()).append("）\n");
                }

                content.append("\n请登录系统查看详细的课程表和教室安排。\n");
                content.append("如有任何问题，请及时联系教务处。");

                // 发送通知
                com.university.academic.dto.CreateNotificationRequest request = 
                        new com.university.academic.dto.CreateNotificationRequest();
                request.setTitle(title);
                request.setContent(content.toString());
                request.setType("SCHEDULE");
                request.setTargetRole("ROLE_TEACHER");

                notificationService.publishNotification(request, systemUser.getId());
                
                log.debug("已发送排课通知给教师: teacherId={}, courses={}", teacherId, courses.size());
            }

            log.info("教师排课通知发送完成: 共{}位教师", teacherCoursesMap.size());

        } catch (Exception e) {
            log.error("发送教师排课通知失败", e);
        }
    }

    @Override
    @Transactional
    public void notifyStudents(Long solutionId, Long semesterId) {
        log.info("发送排课通知给学生: solutionId={}, semesterId={}", solutionId, semesterId);

        try {
            // 查询学期信息
            Semester semester = semesterRepository.findById(semesterId).orElse(null);
            if (semester == null) {
                log.warn("学期不存在: semesterId={}", semesterId);
                return;
            }

            // 获取系统用户
            User systemUser = getSystemUser();

            // 构建通知内容
            String title = "课程表更新通知";
            StringBuilder content = new StringBuilder();
            content.append("同学们好！\n\n");
            content.append("本学期（").append(semesterId).append("）的课程表已更新完成，");
            content.append("请登录系统查看您的最新课程表。\n\n");
            content.append("主要内容包括：\n");
            content.append("• 课程时间安排\n");
            content.append("• 上课教室地点\n");
            content.append("• 授课教师信息\n\n");
            content.append("请合理安排学习时间，按时上课。\n");
            content.append("如有任何疑问，请联系教务处。");

            // 发送通知给所有学生
            com.university.academic.dto.CreateNotificationRequest request = 
                    new com.university.academic.dto.CreateNotificationRequest();
            request.setTitle(title);
            request.setContent(content.toString());
            request.setType("SCHEDULE");
            request.setTargetRole("ROLE_STUDENT");

            notificationService.publishNotification(request, systemUser.getId());
            
            log.info("学生排课通知发送完成");

        } catch (Exception e) {
            log.error("发送学生排课通知失败", e);
        }
    }

    @Override
    @Transactional
    public void notifySchedulingFailed(Long solutionId, String errorMessage) {
        log.info("发送排课失败通知: solutionId={}", solutionId);

        try {
            SchedulingSolution solution = solutionRepository.findById(solutionId).orElse(null);
            if (solution == null) {
                log.warn("排课方案不存在: solutionId={}", solutionId);
                return;
            }

            User systemUser = getSystemUser();

            // 构建通知内容
            String title = "排课失败通知";
            StringBuilder content = new StringBuilder();
            content.append("管理员您好！\n\n");
            content.append("排课方案「").append(solution.getName()).append("」执行失败。\n\n");
            content.append("错误信息：\n");
            content.append(errorMessage != null ? errorMessage : "未知错误");
            content.append("\n\n请检查排课参数和约束条件后重试。");

            // 发送通知给管理员
            com.university.academic.dto.CreateNotificationRequest request = 
                    new com.university.academic.dto.CreateNotificationRequest();
            request.setTitle(title);
            request.setContent(content.toString());
            request.setType("SYSTEM");
            request.setTargetRole("ROLE_ADMIN");

            notificationService.publishNotification(request, systemUser.getId());
            
            log.info("排课失败通知发送完成");

        } catch (Exception e) {
            log.error("发送排课失败通知失败", e);
        }
    }

    /**
     * 获取系统用户（用于发送系统通知）
     * 如果找不到系统用户，创建一个临时用户
     */
    private User getSystemUser() {
        // 尝试查找系统用户
        Optional<User> systemUser = userRepository.findByUsername("system");
        
        if (systemUser.isPresent()) {
            return systemUser.get();
        }

        // 如果没有系统用户，尝试查找第一个管理员用户
        List<User> adminUsers = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().contains("ADMIN"))
                .limit(1)
                .collect(Collectors.toList());

        if (!adminUsers.isEmpty()) {
            return adminUsers.get(0);
        }

        // 如果都找不到，返回第一个用户（兜底方案）
        return userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("系统中没有任何用户"));
    }
}

