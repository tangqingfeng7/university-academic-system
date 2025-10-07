package com.university.academic.controller;

import com.university.academic.dto.CreateSemesterRequest;
import com.university.academic.dto.SemesterDTO;
import com.university.academic.dto.UpdateSemesterRequest;
import com.university.academic.entity.Semester;
import com.university.academic.service.SemesterService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学期管理控制器
 * 提供学期CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/semesters")
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;
    private final DtoConverter dtoConverter;

    /**
     * 查询所有学期（所有角色可访问）
     */
    @GetMapping
    public Result<List<SemesterDTO>> getAllSemesters() {
        log.info("查询所有学期");

        List<Semester> semesters = semesterService.findAll();
        List<SemesterDTO> semesterDTOList = semesters.stream()
                .map(dtoConverter::toSemesterDTO)
                .collect(Collectors.toList());

        return Result.success(semesterDTOList);
    }

    /**
     * 获取当前活动学期（所有角色可访问）
     */
    @GetMapping("/active")
    public Result<SemesterDTO> getActiveSemester() {
        log.info("查询当前活动学期");

        Semester semester = semesterService.findActiveSemester();
        SemesterDTO semesterDTO = dtoConverter.toSemesterDTO(semester);

        return Result.success(semesterDTO);
    }

    /**
     * 获取学期详情
     */
    @GetMapping("/{id}")
    public Result<SemesterDTO> getSemesterById(@PathVariable Long id) {
        log.info("查询学期详情: id={}", id);

        Semester semester = semesterService.findById(id);
        SemesterDTO semesterDTO = dtoConverter.toSemesterDTO(semester);

        return Result.success(semesterDTO);
    }

    /**
     * 创建学期（仅管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SemesterDTO> createSemester(@Valid @RequestBody CreateSemesterRequest request) {
        log.info("创建学期: academicYear={}, semesterType={}", 
                request.getAcademicYear(), request.getSemesterType());

        Semester semester = Semester.builder()
                .academicYear(request.getAcademicYear())
                .semesterType(request.getSemesterType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .courseSelectionStart(request.getCourseSelectionStart())
                .courseSelectionEnd(request.getCourseSelectionEnd())
                .active(false)
                .build();

        Semester createdSemester = semesterService.createSemester(semester);
        SemesterDTO semesterDTO = dtoConverter.toSemesterDTO(createdSemester);

        return Result.success("学期创建成功", semesterDTO);
    }

    /**
     * 更新学期信息（仅管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SemesterDTO> updateSemester(@PathVariable Long id,
                                              @Valid @RequestBody UpdateSemesterRequest request) {
        log.info("更新学期: id={}", id);

        Semester semester = Semester.builder()
                .academicYear(request.getAcademicYear())
                .semesterType(request.getSemesterType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .courseSelectionStart(request.getCourseSelectionStart())
                .courseSelectionEnd(request.getCourseSelectionEnd())
                .build();

        Semester updatedSemester = semesterService.updateSemester(id, semester);
        SemesterDTO semesterDTO = dtoConverter.toSemesterDTO(updatedSemester);

        return Result.success("学期更新成功", semesterDTO);
    }

    /**
     * 删除学期（仅管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteSemester(@PathVariable Long id) {
        log.info("删除学期: id={}", id);

        semesterService.deleteSemester(id);

        return Result.success("学期删除成功");
    }

    /**
     * 设置当前活动学期（仅管理员）
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SemesterDTO> activateSemester(@PathVariable Long id) {
        log.info("设置活动学期: id={}", id);

        semesterService.setActiveSemester(id);
        Semester semester = semesterService.findById(id);
        SemesterDTO semesterDTO = dtoConverter.toSemesterDTO(semester);

        return Result.success("活动学期设置成功", semesterDTO);
    }

    /**
     * 获取学期统计信息（仅管理员）
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getSemesterStatistics() {
        log.info("查询学期统计信息");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalSemesters", semesterService.count());
        statistics.put("hasActiveSemester", semesterService.hasActiveSemester());

        return Result.success(statistics);
    }
}

