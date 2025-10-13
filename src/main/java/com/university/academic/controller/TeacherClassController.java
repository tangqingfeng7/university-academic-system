package com.university.academic.controller;

import com.university.academic.dto.ClassDTO;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.ClassService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师端班级管理控制器
 * 提供教师查看自己管理的班级相关接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TEACHER')")
public class TeacherClassController {

    private final ClassService classService;

    /**
     * 查询我管理的班级列表
     */
    @GetMapping("/my-classes")
    public Result<List<ClassDTO>> getMyClasses() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("教师查询管理的班级: userId={}", currentUserId);
        
        List<ClassDTO> classes = classService.getClassesByCounselor(currentUserId);
        return Result.success(classes);
    }

    /**
     * 查询我管理的某个班级详情
     */
    @GetMapping("/my-classes/{id}")
    public Result<ClassDTO> getMyClassById(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("教师查询班级详情: userId={}, classId={}", currentUserId, id);
        
        ClassDTO classDTO = classService.getClassById(id);
        
        // 验证该班级是否是当前教师管理的
        if (!currentUserId.equals(classDTO.getCounselorId())) {
            return Result.error("无权查看该班级信息");
        }
        
        return Result.success(classDTO);
    }

    /**
     * 查询我管理的某个班级的学生列表
     */
    @GetMapping("/my-classes/{id}/students")
    public Result<List<Map<String, Object>>> getMyClassStudents(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("教师查询班级学生: userId={}, classId={}", currentUserId, id);
        
        ClassDTO classDTO = classService.getClassById(id);
        
        // 验证该班级是否是当前教师管理的
        if (!currentUserId.equals(classDTO.getCounselorId())) {
            return Result.error("无权查看该班级学生信息");
        }
        
        List<Map<String, Object>> students = classService.getClassStudents(id);
        return Result.success(students);
    }

    /**
     * 获取我管理的班级统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getMyClassStatistics() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("教师查询班级统计: userId={}", currentUserId);
        
        List<ClassDTO> classes = classService.getClassesByCounselor(currentUserId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalClasses", classes.size());
        
        // 统计总学生数
        int totalStudents = 0;
        for (ClassDTO classDTO : classes) {
            List<Map<String, Object>> students = classService.getClassStudents(classDTO.getId());
            totalStudents += students.size();
        }
        statistics.put("totalStudents", totalStudents);
        
        // 按入学年份分组
        Map<Integer, Long> classesPerYear = classes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ClassDTO::getEnrollmentYear,
                        java.util.stream.Collectors.counting()
                ));
        statistics.put("classesPerYear", classesPerYear);
        
        return Result.success(statistics);
    }
}

