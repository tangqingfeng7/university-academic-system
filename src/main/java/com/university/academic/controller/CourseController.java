package com.university.academic.controller;

import com.university.academic.dto.CourseDTO;
import com.university.academic.dto.CreateCourseRequest;
import com.university.academic.dto.UpdateCourseRequest;
import com.university.academic.entity.Course;
import com.university.academic.entity.Department;
import com.university.academic.service.CourseService;
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
 * 课程管理控制器
 * 提供课程CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseController {

    private final CourseService courseService;
    private final DtoConverter dtoConverter;

    /**
     * 分页查询课程列表
     */
    @GetMapping
    public Result<Map<String, Object>> getCourseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String type) {

        log.info("查询课程列表: page={}, size={}, keyword={}, departmentId={}, type={}", 
                page, size, keyword, departmentId, type);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Course.CourseType courseType = null;
        if (type != null && !type.trim().isEmpty()) {
            courseType = Course.CourseType.valueOf(type);
        }

        Page<Course> coursePage;
        // 只要有任何筛选条件（keyword、departmentId、type），就使用searchCourses方法
        if ((keyword != null && !keyword.trim().isEmpty()) || departmentId != null || courseType != null) {
            coursePage = courseService.searchCourses(keyword, departmentId, courseType, pageable);
        } else {
            coursePage = courseService.findAll(pageable);
        }

        List<CourseDTO> courseDTOList = coursePage.getContent().stream()
                .map(dtoConverter::toCourseDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", courseDTOList);
        response.put("totalElements", coursePage.getTotalElements());
        response.put("totalPages", coursePage.getTotalPages());
        response.put("currentPage", coursePage.getNumber());
        response.put("pageSize", coursePage.getSize());

        return Result.success(response);
    }

    /**
     * 获取所有课程（不分页）
     */
    @GetMapping("/all")
    public Result<List<CourseDTO>> getAllCourses() {
        log.info("查询所有课程");

        List<Course> courses = courseService.findAll();
        List<CourseDTO> courseDTOList = courses.stream()
                .map(dtoConverter::toCourseDTO)
                .collect(Collectors.toList());

        return Result.success(courseDTOList);
    }

    /**
     * 根据院系ID获取课程列表
     */
    @GetMapping("/by-department/{departmentId}")
    public Result<List<CourseDTO>> getCoursesByDepartment(@PathVariable Long departmentId) {
        log.info("根据院系ID查询课程列表: departmentId={}", departmentId);

        List<Course> courses = courseService.findByDepartmentId(departmentId);
        List<CourseDTO> courseDTOList = courses.stream()
                .map(dtoConverter::toCourseDTO)
                .collect(Collectors.toList());

        return Result.success(courseDTOList);
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public Result<CourseDTO> getCourseById(@PathVariable Long id) {
        log.info("查询课程详情: id={}", id);

        Course course = courseService.findById(id);
        CourseDTO courseDTO = dtoConverter.toCourseDTO(course);

        return Result.success(courseDTO);
    }

    /**
     * 创建课程
     */
    @PostMapping
    public Result<CourseDTO> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        log.info("创建课程: courseNo={}, name={}, credits={}, hours={}, type={}, departmentId={}",
                request.getCourseNo(), request.getName(), request.getCredits(), 
                request.getHours(), request.getType(), request.getDepartmentId());

        Department department = new Department();
        department.setId(request.getDepartmentId());

        Course course = Course.builder()
                .courseNo(request.getCourseNo())
                .name(request.getName())
                .credits(request.getCredits())
                .hours(request.getHours())
                .type(Course.CourseType.valueOf(request.getType()))
                .department(department)
                .description(request.getDescription())
                .build();

        Course createdCourse = courseService.createCourse(course);
        CourseDTO courseDTO = dtoConverter.toCourseDTO(createdCourse);

        return Result.success("课程创建成功", courseDTO);
    }

    /**
     * 更新课程信息
     */
    @PutMapping("/{id}")
    public Result<CourseDTO> updateCourse(@PathVariable Long id,
                                          @Valid @RequestBody UpdateCourseRequest request) {
        log.info("更新课程: id={}", id);

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = new Department();
            department.setId(request.getDepartmentId());
        }

        Course course = Course.builder()
                .courseNo(request.getCourseNo())
                .name(request.getName())
                .credits(request.getCredits())
                .hours(request.getHours())
                .type(request.getType() != null ? Course.CourseType.valueOf(request.getType()) : null)
                .department(department)
                .description(request.getDescription())
                .build();

        Course updatedCourse = courseService.updateCourse(id, course);
        CourseDTO courseDTO = dtoConverter.toCourseDTO(updatedCourse);

        return Result.success("课程更新成功", courseDTO);
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteCourse(@PathVariable Long id) {
        log.info("删除课程: id={}", id);

        courseService.deleteCourse(id);

        return Result.success("课程删除成功");
    }

    /**
     * 检查课程编号是否存在
     */
    @GetMapping("/check-course-no")
    public Result<Map<String, Boolean>> checkCourseNo(@RequestParam String courseNo) {
        boolean exists = courseService.existsByCourseNo(courseNo);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success(response);
    }

    /**
     * 获取课程统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getCourseStatistics() {
        log.info("查询课程统计信息");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCourses", courseService.count());

        return Result.success(statistics);
    }

    /**
     * 获取课程的先修课程列表
     */
    @GetMapping("/{id}/prerequisites")
    public Result<List<CourseDTO>> getPrerequisites(@PathVariable Long id) {
        log.info("查询课程先修课程: id={}", id);

        List<Course> prerequisites = courseService.getPrerequisites(id);
        List<CourseDTO> prerequisiteDTOList = prerequisites.stream()
                .map(dtoConverter::toCourseDTO)
                .collect(Collectors.toList());

        return Result.success(prerequisiteDTOList);
    }

    /**
     * 设置课程的先修课程
     */
    @PostMapping("/{id}/prerequisites")
    public Result<String> setPrerequisites(@PathVariable Long id,
                                          @RequestBody List<Long> prerequisiteCourseIds) {
        log.info("设置课程先修课程: id={}, prerequisiteCourseIds={}", id, prerequisiteCourseIds);

        courseService.setPrerequisites(id, prerequisiteCourseIds);

        return Result.success("先修课程设置成功");
    }

    /**
     * 添加先修课程
     */
    @PostMapping("/{id}/prerequisites/{prerequisiteId}")
    public Result<String> addPrerequisite(@PathVariable Long id,
                                         @PathVariable Long prerequisiteId) {
        log.info("添加先修课程: courseId={}, prerequisiteId={}", id, prerequisiteId);

        courseService.addPrerequisite(id, prerequisiteId);

        return Result.success("先修课程添加成功");
    }

    /**
     * 删除先修课程
     */
    @DeleteMapping("/{id}/prerequisites/{prerequisiteId}")
    public Result<String> removePrerequisite(@PathVariable Long id,
                                            @PathVariable Long prerequisiteId) {
        log.info("删除先修课程: courseId={}, prerequisiteId={}", id, prerequisiteId);

        courseService.removePrerequisite(id, prerequisiteId);

        return Result.success("先修课程删除成功");
    }
}

