package com.university.academic.controller;

import com.university.academic.dto.ClassDTO;
import com.university.academic.dto.CreateClassRequest;
import com.university.academic.dto.UpdateClassRequest;
import com.university.academic.service.ClassService;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 班级管理控制器
 * 提供班级CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/classes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ClassController {

    private final ClassService classService;

    /**
     * 分页查询班级列表
     */
    @GetMapping
    public Result<Map<String, Object>> getClassList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "enrollmentYear") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer enrollmentYear) {

        log.info("查询班级列表: page={}, size={}, majorId={}, enrollmentYear={}", 
                page, size, majorId, enrollmentYear);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        Page<ClassDTO> classPage = classService.getClassList(majorId, enrollmentYear, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", classPage.getContent());
        response.put("totalElements", classPage.getTotalElements());
        response.put("totalPages", classPage.getTotalPages());
        response.put("currentPage", classPage.getNumber());
        response.put("pageSize", classPage.getSize());

        return Result.success(response);
    }

    /**
     * 获取所有班级（不分页）
     */
    @GetMapping("/all")
    public Result<List<ClassDTO>> getAllClasses() {
        log.info("查询所有班级");
        List<ClassDTO> classes = classService.getAllClasses();
        return Result.success(classes);
    }

    /**
     * 根据专业ID获取班级列表
     */
    @GetMapping("/by-major/{majorId}")
    public Result<List<ClassDTO>> getClassesByMajor(@PathVariable Long majorId) {
        log.info("根据专业ID查询班级列表: majorId={}", majorId);
        List<ClassDTO> classes = classService.getClassesByMajor(majorId);
        return Result.success(classes);
    }

    /**
     * 根据入学年份获取班级列表
     */
    @GetMapping("/by-year/{enrollmentYear}")
    public Result<List<ClassDTO>> getClassesByYear(@PathVariable Integer enrollmentYear) {
        log.info("根据入学年份查询班级列表: enrollmentYear={}", enrollmentYear);
        List<ClassDTO> classes = classService.getClassesByEnrollmentYear(enrollmentYear);
        return Result.success(classes);
    }

    /**
     * 根据专业和入学年份获取班级列表
     */
    @GetMapping("/by-major-year")
    public Result<List<ClassDTO>> getClassesByMajorAndYear(
            @RequestParam Long majorId,
            @RequestParam Integer enrollmentYear) {
        log.info("根据专业和年份查询班级: majorId={}, year={}", majorId, enrollmentYear);
        List<ClassDTO> classes = classService.getClassesByMajorAndYear(majorId, enrollmentYear);
        return Result.success(classes);
    }

    /**
     * 获取班级详情
     */
    @GetMapping("/{id}")
    public Result<ClassDTO> getClassById(@PathVariable Long id) {
        log.info("查询班级详情: id={}", id);
        ClassDTO classDTO = classService.getClassById(id);
        return Result.success(classDTO);
    }

    /**
     * 创建班级
     */
    @PostMapping
    public Result<ClassDTO> createClass(@Valid @RequestBody CreateClassRequest request) {
        log.info("创建班级: classCode={}, className={}", 
                request.getClassCode(), request.getClassName());
        ClassDTO classDTO = classService.createClass(request);
        return Result.success("班级创建成功", classDTO);
    }

    /**
     * 更新班级信息
     */
    @PutMapping("/{id}")
    public Result<ClassDTO> updateClass(@PathVariable Long id,
                                        @Valid @RequestBody UpdateClassRequest request) {
        log.info("更新班级: id={}", id);
        ClassDTO classDTO = classService.updateClass(id, request);
        return Result.success("班级更新成功", classDTO);
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteClass(@PathVariable Long id) {
        log.info("删除班级: id={}", id);
        classService.deleteClass(id);
        return Result.success("班级删除成功");
    }

    /**
     * 检查班级代码是否存在
     */
    @GetMapping("/check-code")
    public Result<Map<String, Boolean>> checkCode(@RequestParam String classCode) {
        boolean exists = classService.existsByClassCode(classCode);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success(response);
    }

    /**
     * 获取班级的学生列表
     */
    @GetMapping("/{id}/students")
    public Result<List<Map<String, Object>>> getClassStudents(@PathVariable Long id) {
        log.info("查询班级学生列表: classId={}", id);
        List<Map<String, Object>> students = classService.getClassStudents(id);
        return Result.success(students);
    }

    /**
     * 自动分配辅导员（为所有未分配辅导员的班级自动分配）
     */
    @PostMapping("/auto-assign-counselors")
    public Result<Map<String, Object>> autoAssignCounselors() {
        log.info("执行全局自动分配辅导员");
        Map<String, Object> result = classService.autoAssignCounselors();
        return Result.success(result.get("message").toString(), result);
    }

    /**
     * 自动分配辅导员（按专业或年级）
     */
    @PostMapping("/auto-assign-counselors/conditional")
    public Result<Map<String, Object>> autoAssignCounselorsConditional(
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer enrollmentYear) {
        log.info("执行条件自动分配辅导员: majorId={}, enrollmentYear={}", majorId, enrollmentYear);
        Map<String, Object> result = classService.autoAssignCounselors(majorId, enrollmentYear);
        return Result.success(result.get("message").toString(), result);
    }
}

