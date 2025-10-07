package com.university.academic.controller;

import com.university.academic.dto.CourseOfferingDTO;
import com.university.academic.dto.CreateCourseOfferingRequest;
import com.university.academic.dto.UpdateCourseOfferingRequest;
import com.university.academic.entity.Course;
import com.university.academic.entity.CourseOffering;
import com.university.academic.entity.Semester;
import com.university.academic.entity.Teacher;
import com.university.academic.service.CourseOfferingService;
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
 * 开课计划管理控制器
 * 提供开课计划CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/offerings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseOfferingController {

    private final CourseOfferingService offeringService;
    private final DtoConverter dtoConverter;

    /**
     * 分页查询开课计划列表
     */
    @GetMapping
    public Result<Map<String, Object>> getOfferingList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId) {

        log.info("查询开课计划列表: page={}, size={}, semesterId={}, courseId={}, teacherId={}", 
                page, size, semesterId, courseId, teacherId);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<CourseOffering> offeringPage = offeringService.findWithFilters(
                semesterId, courseId, teacherId, pageable);

        List<CourseOfferingDTO> offeringDTOList = offeringPage.getContent().stream()
                .map(dtoConverter::toCourseOfferingDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", offeringDTOList);
        response.put("totalElements", offeringPage.getTotalElements());
        response.put("totalPages", offeringPage.getTotalPages());
        response.put("currentPage", offeringPage.getNumber());
        response.put("pageSize", offeringPage.getSize());

        return Result.success(response);
    }

    /**
     * 获取开课计划详情
     */
    @GetMapping("/{id}")
    public Result<CourseOfferingDTO> getOfferingById(@PathVariable Long id) {
        log.info("查询开课计划详情: id={}", id);

        CourseOffering offering = offeringService.findById(id);
        CourseOfferingDTO offeringDTO = dtoConverter.toCourseOfferingDTO(offering);

        return Result.success(offeringDTO);
    }

    /**
     * 创建开课计划
     */
    @PostMapping
    public Result<CourseOfferingDTO> createOffering(@Valid @RequestBody CreateCourseOfferingRequest request) {
        log.info("创建开课计划: semesterId={}, courseId={}, teacherId={}, location={}, capacity={}",
                request.getSemesterId(), request.getCourseId(), request.getTeacherId(),
                request.getLocation(), request.getCapacity());

        Semester semester = new Semester();
        semester.setId(request.getSemesterId());

        Course course = new Course();
        course.setId(request.getCourseId());

        Teacher teacher = new Teacher();
        teacher.setId(request.getTeacherId());

        CourseOffering offering = CourseOffering.builder()
                .semester(semester)
                .course(course)
                .teacher(teacher)
                .schedule(request.getSchedule())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .build();

        CourseOffering createdOffering = offeringService.createOffering(offering);
        CourseOfferingDTO offeringDTO = dtoConverter.toCourseOfferingDTO(createdOffering);

        return Result.success("开课计划创建成功", offeringDTO);
    }

    /**
     * 更新开课计划信息
     */
    @PutMapping("/{id}")
    public Result<CourseOfferingDTO> updateOffering(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateCourseOfferingRequest request) {
        log.info("更新开课计划: id={}", id);

        Teacher teacher = null;
        if (request.getTeacherId() != null) {
            teacher = new Teacher();
            teacher.setId(request.getTeacherId());
        }

        CourseOffering offering = CourseOffering.builder()
                .teacher(teacher)
                .schedule(request.getSchedule())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .build();

        CourseOffering updatedOffering = offeringService.updateOffering(id, offering);
        CourseOfferingDTO offeringDTO = dtoConverter.toCourseOfferingDTO(updatedOffering);

        return Result.success("开课计划更新成功", offeringDTO);
    }

    /**
     * 删除开课计划
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOffering(@PathVariable Long id) {
        log.info("删除开课计划: id={}", id);

        offeringService.deleteOffering(id);

        return Result.success("开课计划删除成功");
    }

    /**
     * 发布开课计划
     */
    @PutMapping("/{id}/publish")
    public Result<CourseOfferingDTO> publishOffering(@PathVariable Long id) {
        log.info("发布开课计划: id={}", id);

        offeringService.publishOffering(id);
        CourseOffering offering = offeringService.findById(id);
        CourseOfferingDTO offeringDTO = dtoConverter.toCourseOfferingDTO(offering);

        return Result.success("开课计划发布成功", offeringDTO);
    }

    /**
     * 取消开课计划
     */
    @PutMapping("/{id}/cancel")
    public Result<CourseOfferingDTO> cancelOffering(@PathVariable Long id) {
        log.info("取消开课计划: id={}", id);

        offeringService.cancelOffering(id);
        CourseOffering offering = offeringService.findById(id);
        CourseOfferingDTO offeringDTO = dtoConverter.toCourseOfferingDTO(offering);

        return Result.success("开课计划取消成功", offeringDTO);
    }

    /**
     * 根据教师ID查询开课计划
     */
    @GetMapping("/by-teacher/{teacherId}")
    public Result<List<CourseOfferingDTO>> getOfferingsByTeacher(@PathVariable Long teacherId) {
        log.info("根据教师ID查询开课计划: teacherId={}", teacherId);

        List<CourseOffering> offerings = offeringService.findByTeacher(teacherId);
        List<CourseOfferingDTO> offeringDTOList = offerings.stream()
                .map(dtoConverter::toCourseOfferingDTO)
                .collect(Collectors.toList());

        return Result.success(offeringDTOList);
    }

    /**
     * 获取开课计划统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getOfferingStatistics() {
        log.info("查询开课计划统计信息");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalOfferings", offeringService.count());

        return Result.success(statistics);
    }
}

