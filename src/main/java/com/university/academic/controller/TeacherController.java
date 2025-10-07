package com.university.academic.controller;

import com.university.academic.dto.CreateTeacherRequest;
import com.university.academic.dto.TeacherDTO;
import com.university.academic.dto.UpdateTeacherRequest;
import com.university.academic.entity.Department;
import com.university.academic.entity.Student;
import com.university.academic.entity.Teacher;
import com.university.academic.entity.User;
import com.university.academic.service.TeacherService;
import com.university.academic.util.DtoConverter;
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
import java.util.stream.Collectors;

/**
 * 教师管理控制器
 * 提供教师CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TeacherController {

    private final TeacherService teacherService;
    private final DtoConverter dtoConverter;

    /**
     * 分页查询教师列表
     */
    @GetMapping
    public Result<Map<String, Object>> getTeacherList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId) {

        log.info("查询教师列表: page={}, size={}, keyword={}, departmentId={}", 
                page, size, keyword, departmentId);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Teacher> teacherPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            teacherPage = teacherService.searchTeachers(keyword, departmentId, pageable);
        } else {
            teacherPage = teacherService.findAll(pageable);
        }

        List<TeacherDTO> teacherDTOList = teacherPage.getContent().stream()
                .map(dtoConverter::toTeacherDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", teacherDTOList);
        response.put("totalElements", teacherPage.getTotalElements());
        response.put("totalPages", teacherPage.getTotalPages());
        response.put("currentPage", teacherPage.getNumber());
        response.put("pageSize", teacherPage.getSize());

        return Result.success(response);
    }

    /**
     * 获取所有教师（不分页）
     */
    @GetMapping("/all")
    public Result<List<TeacherDTO>> getAllTeachers() {
        log.info("查询所有教师");

        List<Teacher> teachers = teacherService.findAll();
        List<TeacherDTO> teacherDTOList = teachers.stream()
                .map(dtoConverter::toTeacherDTO)
                .collect(Collectors.toList());

        return Result.success(teacherDTOList);
    }

    /**
     * 根据院系ID获取教师列表
     */
    @GetMapping("/by-department/{departmentId}")
    public Result<List<TeacherDTO>> getTeachersByDepartment(@PathVariable Long departmentId) {
        log.info("根据院系ID查询教师列表: departmentId={}", departmentId);

        List<Teacher> teachers = teacherService.findByDepartmentId(departmentId);
        List<TeacherDTO> teacherDTOList = teachers.stream()
                .map(dtoConverter::toTeacherDTO)
                .collect(Collectors.toList());

        return Result.success(teacherDTOList);
    }

    /**
     * 获取教师详情
     */
    @GetMapping("/{id}")
    public Result<TeacherDTO> getTeacherById(@PathVariable Long id) {
        log.info("查询教师详情: id={}", id);

        Teacher teacher = teacherService.findById(id);
        TeacherDTO teacherDTO = dtoConverter.toTeacherDTO(teacher);

        return Result.success(teacherDTO);
    }

    /**
     * 创建教师
     */
    @PostMapping
    public Result<TeacherDTO> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        log.info("创建教师: teacherNo={}, name={}, departmentId={}",
                request.getTeacherNo(), request.getName(), request.getDepartmentId());

        Department department = new Department();
        department.setId(request.getDepartmentId());

        User user = new User();
        user.setUsername(request.getUsername() != null ? request.getUsername() : request.getTeacherNo());
        user.setPassword(request.getPassword());

        Teacher teacher = Teacher.builder()
                .teacherNo(request.getTeacherNo())
                .name(request.getName())
                .gender(Student.Gender.valueOf(request.getGender()))
                .title(request.getTitle())
                .department(department)
                .phone(request.getPhone())
                .email(request.getEmail())
                .user(user)
                .build();

        Teacher createdTeacher = teacherService.createTeacher(teacher);
        TeacherDTO teacherDTO = dtoConverter.toTeacherDTO(createdTeacher);

        return Result.success("教师创建成功", teacherDTO);
    }

    /**
     * 更新教师信息
     */
    @PutMapping("/{id}")
    public Result<TeacherDTO> updateTeacher(@PathVariable Long id,
                                            @Valid @RequestBody UpdateTeacherRequest request) {
        log.info("更新教师: id={}", id);

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = new Department();
            department.setId(request.getDepartmentId());
        }

        Teacher teacher = Teacher.builder()
                .teacherNo(request.getTeacherNo())
                .name(request.getName())
                .gender(request.getGender() != null ? Student.Gender.valueOf(request.getGender()) : null)
                .title(request.getTitle())
                .department(department)
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        Teacher updatedTeacher = teacherService.updateTeacher(id, teacher);
        TeacherDTO teacherDTO = dtoConverter.toTeacherDTO(updatedTeacher);

        return Result.success("教师更新成功", teacherDTO);
    }

    /**
     * 删除教师
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTeacher(@PathVariable Long id) {
        log.info("删除教师: id={}", id);

        teacherService.deleteTeacher(id);

        return Result.success("教师删除成功");
    }

    /**
     * 检查工号是否存在
     */
    @GetMapping("/check-teacher-no")
    public Result<Map<String, Boolean>> checkTeacherNo(@RequestParam String teacherNo) {
        boolean exists = teacherService.existsByTeacherNo(teacherNo);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success(response);
    }

    /**
     * 检查教师是否有授课任务
     */
    @GetMapping("/{id}/has-offerings")
    public Result<Map<String, Boolean>> hasActiveOfferings(@PathVariable Long id) {
        boolean hasOfferings = teacherService.hasActiveOfferings(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasOfferings", hasOfferings);
        return Result.success(response);
    }

    /**
     * 获取教师统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getTeacherStatistics() {
        log.info("查询教师统计信息");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalTeachers", teacherService.count());

        return Result.success(statistics);
    }
}

