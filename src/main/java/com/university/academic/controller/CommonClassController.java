package com.university.academic.controller;

import com.university.academic.dto.ClassDTO;
import com.university.academic.service.ClassService;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级公共接口控制器
 * 提供班级查询接口（所有用户可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/common/classes")
@RequiredArgsConstructor
public class CommonClassController {

    private final ClassService classService;

    /**
     * 获取所有班级列表
     */
    @GetMapping
    public Result<List<ClassDTO>> getAllClasses() {
        log.info("公共接口 - 查询所有班级");
        List<ClassDTO> classes = classService.getAllClasses();
        return Result.success(classes);
    }

    /**
     * 根据专业ID获取班级列表
     */
    @GetMapping("/by-major/{majorId}")
    public Result<List<ClassDTO>> getClassesByMajor(@PathVariable Long majorId) {
        log.info("公共接口 - 根据专业ID查询班级列表: majorId={}", majorId);
        List<ClassDTO> classes = classService.getClassesByMajor(majorId);
        return Result.success(classes);
    }

    /**
     * 根据入学年份获取班级列表
     */
    @GetMapping("/by-year/{enrollmentYear}")
    public Result<List<ClassDTO>> getClassesByYear(@PathVariable Integer enrollmentYear) {
        log.info("公共接口 - 根据入学年份查询班级列表: enrollmentYear={}", enrollmentYear);
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
        log.info("公共接口 - 根据专业和年份查询班级: majorId={}, year={}", majorId, enrollmentYear);
        List<ClassDTO> classes = classService.getClassesByMajorAndYear(majorId, enrollmentYear);
        return Result.success(classes);
    }

    /**
     * 获取班级详情
     */
    @GetMapping("/{id}")
    public Result<ClassDTO> getClassById(@PathVariable Long id) {
        log.info("公共接口 - 查询班级详情: id={}", id);
        ClassDTO classDTO = classService.getClassById(id);
        return Result.success(classDTO);
    }
}

