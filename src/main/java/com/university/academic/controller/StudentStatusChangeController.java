package com.university.academic.controller;

import com.university.academic.vo.Result;
import com.university.academic.dto.CreateStatusChangeRequest;
import com.university.academic.dto.StudentStatusChangeDTO;
import com.university.academic.dto.converter.StatusChangeConverter;
import com.university.academic.entity.StudentStatusChange;
import com.university.academic.security.CustomUserDetailsService;
import com.university.academic.service.StudentStatusChangeService;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生学籍异动Controller
 *
 * @author university
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/student/status-changes")
@RequiredArgsConstructor
@Validated
@Tag(name = "学生学籍异动管理", description = "学生端学籍异动相关API")
public class StudentStatusChangeController {

    private final StudentStatusChangeService statusChangeService;
    private final StatusChangeConverter statusChangeConverter;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "提交学籍异动申请", description = "学生提交休学、复学、转专业或退学申请")
    public Result<StudentStatusChangeDTO> createApplication(
            @Parameter(description = "异动申请信息") @Valid @ModelAttribute CreateStatusChangeRequest request,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}提交学籍异动申请: {}", studentId, request.getType());

        StudentStatusChange statusChange = statusChangeService.createApplication(
                studentId,
                request.getType(),
                request.getReason(),
                request.getStartDate(),
                request.getEndDate(),
                request.getTargetMajorId(),
                request.getAttachment()
        );

        StudentStatusChangeDTO dto = statusChangeConverter.toDTO(statusChange, false);
        return Result.success("申请提交成功，等待审批", dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询我的异动申请", description = "查询当前学生的所有异动申请记录")
    public Result<List<StudentStatusChangeDTO>> getMyApplications(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询异动申请", studentId);

        List<StudentStatusChange> statusChanges = statusChangeService.getStudentHistory(studentId);
        List<StudentStatusChangeDTO> dtoList = statusChanges.stream()
                .map(sc -> statusChangeConverter.toDTO(sc, false))
                .collect(Collectors.toList());

        return Result.success(dtoList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询异动申请详情", description = "查询指定异动申请的详细信息，包含审批历史")
    public Result<StudentStatusChangeDTO> getApplicationDetail(
            @Parameter(description = "异动申请ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询异动申请详情: {}", studentId, id);

        StudentStatusChange statusChange = statusChangeService.getApplicationById(id, studentId);
        StudentStatusChangeDTO dto = statusChangeConverter.toDTO(statusChange, true);

        return Result.success(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "撤销异动申请", description = "撤销待审批状态的异动申请")
    public Result<String> cancelApplication(
            @Parameter(description = "异动申请ID") @PathVariable Long id,
            Authentication authentication
    ) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}撤销异动申请: {}", studentId, id);

        statusChangeService.cancelApplication(id, studentId);
        return Result.success("申请已撤销");
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "查询异动历史", description = "查询学生的所有异动历史记录")
    public Result<List<StudentStatusChangeDTO>> getHistory(Authentication authentication) {
        Long studentId = userDetailsService.getStudentIdFromAuth(authentication);
        log.info("学生{}查询异动历史", studentId);

        List<StudentStatusChange> history = statusChangeService.getStudentHistory(studentId);
        List<StudentStatusChangeDTO> dtoList = history.stream()
                .map(sc -> statusChangeConverter.toDTO(sc, false))
                .collect(Collectors.toList());

        return Result.success(dtoList);
    }
}

