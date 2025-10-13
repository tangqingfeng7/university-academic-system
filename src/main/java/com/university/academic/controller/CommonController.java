package com.university.academic.controller;

import com.university.academic.dto.MajorDTO;
import com.university.academic.entity.Major;
import com.university.academic.service.MajorService;
import com.university.academic.util.DtoConverter;
import com.university.academic.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 公共接口控制器
 * 提供所有角色都可以访问的公共数据接口
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {

    private final MajorService majorService;
    private final DtoConverter dtoConverter;

    /**
     * 获取所有专业列表（简化版，供选择使用）
     * 所有认证用户都可以访问
     */
    @GetMapping("/majors")
    public Result<List<MajorDTO>> getAllMajors() {
        log.info("查询所有专业列表（公共接口）");

        List<Major> majors = majorService.findAll();
        List<MajorDTO> majorDTOList = majors.stream()
                .map(dtoConverter::toMajorDTO)
                .collect(Collectors.toList());

        return Result.success(majorDTOList);
    }

    /**
     * 根据院系ID获取专业列表
     * 所有认证用户都可以访问
     */
    @GetMapping("/majors/by-department/{departmentId}")
    public Result<List<MajorDTO>> getMajorsByDepartment(@PathVariable Long departmentId) {
        log.info("根据院系ID查询专业列表（公共接口）: departmentId={}", departmentId);

        List<Major> majors = majorService.findByDepartmentId(departmentId);
        List<MajorDTO> majorDTOList = majors.stream()
                .map(dtoConverter::toMajorDTO)
                .collect(Collectors.toList());

        return Result.success(majorDTOList);
    }
}

