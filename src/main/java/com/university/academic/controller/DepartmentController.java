package com.university.academic.controller;

import com.university.academic.dto.CreateDepartmentRequest;
import com.university.academic.dto.DepartmentDTO;
import com.university.academic.dto.UpdateDepartmentRequest;
import com.university.academic.entity.Department;
import com.university.academic.service.DepartmentService;
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
 * 院系管理控制器
 * 提供院系CRUD接口（仅管理员可访问）
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DtoConverter dtoConverter;

    /**
     * 分页查询院系列表
     */
    @GetMapping
    public Result<Map<String, Object>> getDepartmentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        log.info("查询院系列表: page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Department> departmentPage = departmentService.findAll(pageable);
        List<DepartmentDTO> departmentDTOList = departmentPage.getContent().stream()
                .map(dtoConverter::toDepartmentDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", departmentDTOList);
        response.put("totalElements", departmentPage.getTotalElements());
        response.put("totalPages", departmentPage.getTotalPages());
        response.put("currentPage", departmentPage.getNumber());
        response.put("pageSize", departmentPage.getSize());

        return Result.success(response);
    }

    /**
     * 获取所有院系（不分页）
     */
    @GetMapping("/all")
    public Result<List<DepartmentDTO>> getAllDepartments() {
        log.info("查询所有院系");

        List<Department> departments = departmentService.findAll();
        List<DepartmentDTO> departmentDTOList = departments.stream()
                .map(dtoConverter::toDepartmentDTO)
                .collect(Collectors.toList());

        return Result.success(departmentDTOList);
    }

    /**
     * 获取院系详情
     */
    @GetMapping("/{id}")
    public Result<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        log.info("查询院系详情: id={}", id);

        Department department = departmentService.findById(id);
        DepartmentDTO departmentDTO = dtoConverter.toDepartmentDTO(department);

        return Result.success(departmentDTO);
    }

    /**
     * 创建院系
     */
    @PostMapping
    public Result<DepartmentDTO> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        log.info("创建院系: code={}, name={}", request.getCode(), request.getName());

        Department department = Department.builder()
                .code(request.getCode())
                .name(request.getName())
                .build();

        Department createdDepartment = departmentService.createDepartment(department);
        DepartmentDTO departmentDTO = dtoConverter.toDepartmentDTO(createdDepartment);

        return Result.success("院系创建成功", departmentDTO);
    }

    /**
     * 更新院系信息
     */
    @PutMapping("/{id}")
    public Result<DepartmentDTO> updateDepartment(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateDepartmentRequest request) {
        log.info("更新院系: id={}", id);

        Department department = Department.builder()
                .code(request.getCode())
                .name(request.getName())
                .build();

        Department updatedDepartment = departmentService.updateDepartment(id, department);
        DepartmentDTO departmentDTO = dtoConverter.toDepartmentDTO(updatedDepartment);

        return Result.success("院系更新成功", departmentDTO);
    }

    /**
     * 删除院系
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteDepartment(@PathVariable Long id) {
        log.info("删除院系: id={}", id);

        departmentService.deleteDepartment(id);

        return Result.success("院系删除成功");
    }

    /**
     * 检查院系代码是否存在
     */
    @GetMapping("/check-code")
    public Result<Map<String, Boolean>> checkCode(@RequestParam String code) {
        boolean exists = departmentService.existsByCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return Result.success(response);
    }
}

