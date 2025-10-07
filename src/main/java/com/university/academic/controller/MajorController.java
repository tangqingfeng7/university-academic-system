package com.university.academic.controller;

import com.university.academic.dto.CreateMajorRequest;
import com.university.academic.dto.MajorDTO;
import com.university.academic.dto.UpdateMajorRequest;
import com.university.academic.entity.Department;
import com.university.academic.entity.Major;
import com.university.academic.service.MajorService;
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
 * 专业管理控制器
 * 提供专业CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/majors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MajorController {

    private final MajorService majorService;
    private final DtoConverter dtoConverter;

    /**
     * 分页查询专业列表
     */
    @GetMapping
    public Result<Map<String, Object>> getMajorList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) Long departmentId) {

        log.info("查询专业列表: page={}, size={}, departmentId={}", page, size, departmentId);

        List<Major> majors;
        if (departmentId != null) {
            majors = majorService.findByDepartmentId(departmentId);
        } else {
            Sort.Direction sortDirection = Sort.Direction.fromString(direction);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<Major> majorPage = majorService.findAll(pageable);
            
            List<MajorDTO> majorDTOList = majorPage.getContent().stream()
                    .map(dtoConverter::toMajorDTO)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", majorDTOList);
            response.put("totalElements", majorPage.getTotalElements());
            response.put("totalPages", majorPage.getTotalPages());
            response.put("currentPage", majorPage.getNumber());
            response.put("pageSize", majorPage.getSize());

            return Result.success(response);
        }

        List<MajorDTO> majorDTOList = majors.stream()
                .map(dtoConverter::toMajorDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", majorDTOList);
        response.put("totalElements", majorDTOList.size());

        return Result.success(response);
    }

    /**
     * 获取所有专业（不分页）
     */
    @GetMapping("/all")
    public Result<List<MajorDTO>> getAllMajors() {
        log.info("查询所有专业");

        List<Major> majors = majorService.findAll();
        List<MajorDTO> majorDTOList = majors.stream()
                .map(dtoConverter::toMajorDTO)
                .collect(Collectors.toList());

        return Result.success(majorDTOList);
    }

    /**
     * 根据院系ID获取专业列表
     */
    @GetMapping("/by-department/{departmentId}")
    public Result<List<MajorDTO>> getMajorsByDepartment(@PathVariable Long departmentId) {
        log.info("根据院系ID查询专业列表: departmentId={}", departmentId);

        List<Major> majors = majorService.findByDepartmentId(departmentId);
        List<MajorDTO> majorDTOList = majors.stream()
                .map(dtoConverter::toMajorDTO)
                .collect(Collectors.toList());

        return Result.success(majorDTOList);
    }

    /**
     * 获取专业详情
     */
    @GetMapping("/{id}")
    public Result<MajorDTO> getMajorById(@PathVariable Long id) {
        log.info("查询专业详情: id={}", id);

        Major major = majorService.findById(id);
        MajorDTO majorDTO = dtoConverter.toMajorDTO(major);

        return Result.success(majorDTO);
    }

    /**
     * 创建专业
     */
    @PostMapping
    public Result<MajorDTO> createMajor(@Valid @RequestBody CreateMajorRequest request) {
        log.info("创建专业: code={}, name={}, departmentId={}",
                request.getCode(), request.getName(), request.getDepartmentId());

        Department department = new Department();
        department.setId(request.getDepartmentId());

        Major major = Major.builder()
                .code(request.getCode())
                .name(request.getName())
                .department(department)
                .build();

        Major createdMajor = majorService.createMajor(major);
        MajorDTO majorDTO = dtoConverter.toMajorDTO(createdMajor);

        return Result.success("专业创建成功", majorDTO);
    }

    /**
     * 更新专业信息
     */
    @PutMapping("/{id}")
    public Result<MajorDTO> updateMajor(@PathVariable Long id,
                                        @Valid @RequestBody UpdateMajorRequest request) {
        log.info("更新专业: id={}", id);

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = new Department();
            department.setId(request.getDepartmentId());
        }

        Major major = Major.builder()
                .code(request.getCode())
                .name(request.getName())
                .department(department)
                .build();

        Major updatedMajor = majorService.updateMajor(id, major);
        MajorDTO majorDTO = dtoConverter.toMajorDTO(updatedMajor);

        return Result.success("专业更新成功", majorDTO);
    }

    /**
     * 删除专业
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteMajor(@PathVariable Long id) {
        log.info("删除专业: id={}", id);

        majorService.deleteMajor(id);

        return Result.success("专业删除成功");
    }

    /**
     * 检查专业代码是否存在
     */
    @GetMapping("/check-code")
    public Result<Map<String, Boolean>> checkCode(@RequestParam String code) {
        boolean exists = majorService.existsByCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success(response);
    }
}

