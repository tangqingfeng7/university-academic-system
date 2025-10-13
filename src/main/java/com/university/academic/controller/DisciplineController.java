package com.university.academic.controller;

import com.university.academic.vo.Result;
import com.university.academic.dto.DisciplineAppealDTO;
import com.university.academic.dto.StudentDisciplineDTO;
import com.university.academic.dto.request.CreateAppealRequest;
import com.university.academic.dto.request.CreateDisciplineRequest;
import com.university.academic.dto.request.ReviewAppealRequest;
import com.university.academic.entity.*;
import com.university.academic.entity.enums.AppealResult;
import com.university.academic.entity.enums.ApprovalStatus;
import com.university.academic.entity.enums.DisciplineStatus;
import com.university.academic.entity.enums.DisciplineType;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.StudentRepository;
import com.university.academic.repository.UserRepository;
import com.university.academic.security.SecurityUtils;
import com.university.academic.service.DisciplineAppealService;
import com.university.academic.service.StudentDisciplineService;
import com.university.academic.service.impl.StudentDisciplineServiceImpl;
import com.university.academic.util.DtoConverter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处分管理Controller
 *
 * @author Academic System Team
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DisciplineController {

    private final StudentDisciplineService disciplineService;
    private final DisciplineAppealService appealService;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    // ==================== 管理员端API ====================

    /**
     * 创建处分记录
     */
    @PostMapping("/admin/disciplines")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<StudentDisciplineDTO> createDiscipline(
            @Valid @RequestBody CreateDisciplineRequest request) {
        
        log.info("创建处分记录: studentId={}", request.getStudentId());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        // 获取当前用户作为上报人
        Long reporterId = request.getReporterId();
        if (reporterId == null) {
            reporterId = SecurityUtils.getCurrentUserId();
            if (reporterId == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
        }
        
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("上报人不存在"));

        StudentDiscipline discipline = StudentDiscipline.builder()
                .student(student)
                .disciplineType(DisciplineType.valueOf(request.getDisciplineType()))
                .reason(request.getReason())
                .description(request.getDescription())
                .occurrenceDate(request.getOccurrenceDate())
                .punishmentDate(request.getPunishmentDate())
                .canRemove(request.getCanRemove())
                .attachmentUrl(request.getAttachmentUrl())
                .reporter(reporter)
                .build();

        StudentDiscipline saved = disciplineService.createDiscipline(discipline);
        
        return Result.success("处分记录创建成功", dtoConverter.toStudentDisciplineDTO(saved));
    }

    /**
     * 查询处分列表
     */
    @GetMapping("/admin/disciplines")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Page<StudentDisciplineDTO>> getDisciplines(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) String disciplineType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("punishmentDate").descending());
        
        Page<StudentDiscipline> disciplines = disciplineService.findByConditions(
                studentId,
                reporterId,
                disciplineType != null ? DisciplineType.valueOf(disciplineType) : null,
                status != null ? DisciplineStatus.valueOf(status) : null,
                approvalStatus != null ? ApprovalStatus.valueOf(approvalStatus) : null,
                startDate,
                endDate,
                pageable);

        Page<StudentDisciplineDTO> dtoPage = disciplines.map(dtoConverter::toStudentDisciplineDTO);
        
        return Result.success(dtoPage);
    }

    /**
     * 解除处分
     */
    @PostMapping("/admin/disciplines/{id}/remove")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<StudentDisciplineDTO> removeDiscipline(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {

        Long operatorId = ((User) authentication.getPrincipal()).getId();
        
        StudentDiscipline removed = disciplineService.removeDiscipline(id, reason, operatorId);
        
        return Result.success("处分已解除", dtoConverter.toStudentDisciplineDTO(removed));
    }

    /**
     * 删除处分
     */
    @DeleteMapping("/admin/disciplines/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteDiscipline(@PathVariable Long id) {
        disciplineService.deleteDiscipline(id);
        return Result.success("处分记录已删除");
    }

    /**
     * 审批处分
     */
    @PostMapping("/admin/disciplines/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<StudentDisciplineDTO> approveDiscipline(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment,
            Authentication authentication) {
        
        Long approverId = SecurityUtils.getCurrentUserId();
        if (approverId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        StudentDiscipline reviewed = ((StudentDisciplineServiceImpl) disciplineService)
                .reviewDiscipline(id, approverId, approved, comment);
        
        String message = approved ? "处分已批准" : "处分已拒绝";
        return Result.success(message, dtoConverter.toStudentDisciplineDTO(reviewed));
    }

    // ==================== 学生端API ====================

    /**
     * 获取当前登录学生
     */
    private Student getCurrentStudent() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    }

    /**
     * 查询我的处分记录
     */
    @GetMapping("/student/disciplines")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<StudentDisciplineDTO>> getMyDisciplines() {
        Student student = getCurrentStudent();
        
        List<StudentDiscipline> disciplines = disciplineService.findByStudentId(student.getId());
        List<StudentDisciplineDTO> dtos = disciplines.stream()
                .map(dtoConverter::toStudentDisciplineDTO)
                .collect(Collectors.toList());
        
        return Result.success(dtos);
    }

    /**
     * 提交申诉
     */
    @PostMapping("/student/discipline-appeals")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<DisciplineAppealDTO> createAppeal(
            @Valid @RequestBody CreateAppealRequest request) {
        
        Student student = getCurrentStudent();
        
        StudentDiscipline discipline = disciplineService.findById(request.getDisciplineId());
        
        DisciplineAppeal appeal = DisciplineAppeal.builder()
                .discipline(discipline)
                .student(student)
                .appealReason(request.getAppealReason())
                .evidence(request.getEvidence())
                .attachmentUrl(request.getAttachmentUrl())
                .build();

        DisciplineAppeal saved = appealService.createAppeal(appeal);
        
        return Result.success("申诉已提交", dtoConverter.toDisciplineAppealDTO(saved));
    }

    /**
     * 查询我的申诉
     */
    @GetMapping("/student/discipline-appeals")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<List<DisciplineAppealDTO>> getMyAppeals() {
        Student student = getCurrentStudent();
        
        List<DisciplineAppeal> appeals = appealService.findByStudentId(student.getId());
        List<DisciplineAppealDTO> dtos = appeals.stream()
                .map(dtoConverter::toDisciplineAppealDTO)
                .collect(Collectors.toList());
        
        return Result.success(dtos);
    }

    // ==================== 申诉审核API ====================

    /**
     * 查询待审核的申诉
     */
    @GetMapping("/admin/discipline-appeals")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Page<DisciplineAppealDTO>> getPendingAppeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<DisciplineAppeal> appeals = appealService.findPendingAppeals(pageable);
        Page<DisciplineAppealDTO> dtoPage = appeals.map(dtoConverter::toDisciplineAppealDTO);
        
        return Result.success(dtoPage);
    }

    /**
     * 审核申诉
     */
    @PostMapping("/admin/discipline-appeals/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public Result<DisciplineAppealDTO> reviewAppeal(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAppealRequest request,
            Authentication authentication) {

        Long reviewerId = ((User) authentication.getPrincipal()).getId();
        
        DisciplineAppeal reviewed = appealService.reviewAppeal(
                id,
                AppealResult.valueOf(request.getResult()),
                request.getComment(),
                reviewerId);
        
        return Result.success("审核完成", dtoConverter.toDisciplineAppealDTO(reviewed));
    }
}

