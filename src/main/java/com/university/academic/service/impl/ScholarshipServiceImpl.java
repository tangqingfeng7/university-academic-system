package com.university.academic.service.impl;

import com.university.academic.dto.*;
import com.university.academic.entity.Scholarship;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ScholarshipApplicationRepository;
import com.university.academic.repository.ScholarshipRepository;
import com.university.academic.service.NotificationService;
import com.university.academic.service.ScholarshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 奖学金服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScholarshipServiceImpl implements ScholarshipService {
    
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipApplicationRepository applicationRepository;
    private final NotificationService notificationService;
    
    @Override
    @Transactional
    public ScholarshipDTO createScholarship(CreateScholarshipRequest request) {
        log.info("创建奖学金: {}", request.getName());
        
        // 检查名称是否已存在
        if (scholarshipRepository.findByName(request.getName()) != null) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_ALREADY_EXISTS);
        }
        
        Scholarship scholarship = new Scholarship();
        scholarship.setName(request.getName());
        scholarship.setLevel(request.getLevel());
        scholarship.setAmount(request.getAmount());
        scholarship.setQuota(request.getQuota());
        scholarship.setDescription(request.getDescription());
        scholarship.setMinGpa(request.getMinGpa());
        scholarship.setMinCredits(request.getMinCredits());
        scholarship.setRequirements(request.getRequirements());
        scholarship.setActive(true);
        
        scholarship = scholarshipRepository.save(scholarship);
        log.info("奖学金创建成功: id={}", scholarship.getId());
        
        return convertToDTO(scholarship);
    }
    
    @Override
    @Transactional
    public ScholarshipDTO updateScholarship(Long id, UpdateScholarshipRequest request) {
        log.info("更新奖学金: id={}", id);
        
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        // 更新字段
        if (request.getAmount() != null) {
            scholarship.setAmount(request.getAmount());
        }
        if (request.getQuota() != null) {
            scholarship.setQuota(request.getQuota());
        }
        if (request.getDescription() != null) {
            scholarship.setDescription(request.getDescription());
        }
        if (request.getMinGpa() != null) {
            scholarship.setMinGpa(request.getMinGpa());
        }
        if (request.getMinCredits() != null) {
            scholarship.setMinCredits(request.getMinCredits());
        }
        if (request.getRequirements() != null) {
            scholarship.setRequirements(request.getRequirements());
        }
        if (request.getActive() != null) {
            scholarship.setActive(request.getActive());
        }
        
        scholarship = scholarshipRepository.save(scholarship);
        log.info("奖学金更新成功: id={}", id);
        
        return convertToDTO(scholarship);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ScholarshipDTO> getScholarships(Pageable pageable) {
        log.debug("查询奖学金列表: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Scholarship> scholarships = scholarshipRepository.findAll(pageable);
        return scholarships.map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipDTO> getActiveScholarships() {
        log.debug("查询启用的奖学金列表");
        
        List<Scholarship> scholarships = scholarshipRepository.findByActive(true);
        return scholarships.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ScholarshipDTO getScholarshipById(Long id) {
        log.debug("查询奖学金: id={}", id);
        
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        return convertToDTO(scholarship);
    }
    
    @Override
    @Transactional
    public void openApplication(Long id, OpenApplicationRequest request) {
        log.info("开启奖学金申请: id={}, academicYear={}", id, request.getAcademicYear());
        
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        if (!scholarship.getActive()) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_NOT_ACTIVE);
        }
        
        // 发送通知给所有学生
        String notificationContent = String.format(
                "%s（%s）已开启申请，学年：%s，名额：%d，金额：%.2f元。请符合条件的同学及时申请。",
                scholarship.getName(),
                scholarship.getLevel().getDescription(),
                request.getAcademicYear(),
                scholarship.getQuota(),
                scholarship.getAmount()
        );
        
        CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
        notificationRequest.setTitle(scholarship.getName() + "开启申请");
        notificationRequest.setContent(notificationContent);
        notificationRequest.setTargetRole("STUDENT");
        
        // 使用系统管理员ID（假设为1）作为发布者
        notificationService.publishNotification(notificationRequest, 1L);
        
        log.info("奖学金申请开启成功，已发送通知");
    }
    
    @Override
    @Transactional
    public void toggleActive(Long id, Boolean active) {
        log.info("切换奖学金状态: id={}, active={}", id, active);
        
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        scholarship.setActive(active);
        scholarshipRepository.save(scholarship);
        
        log.info("奖学金状态已更新");
    }
    
    @Override
    @Transactional
    public void deleteScholarship(Long id) {
        log.info("删除奖学金: id={}", id);
        
        Scholarship scholarship = scholarshipRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        // 检查是否有申请记录
        long applicationCount = applicationRepository.countByScholarshipIdAndAcademicYear(
                id, getCurrentAcademicYear());
        
        if (applicationCount > 0) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_HAS_APPLICATIONS);
        }
        
        scholarshipRepository.delete(scholarship);
        log.info("奖学金删除成功");
    }
    
    /**
     * 转换为DTO
     */
    private ScholarshipDTO convertToDTO(Scholarship scholarship) {
        ScholarshipDTO dto = new ScholarshipDTO();
        dto.setId(scholarship.getId());
        dto.setName(scholarship.getName());
        dto.setLevel(scholarship.getLevel());
        dto.setLevelDescription(scholarship.getLevel().getDescription());
        dto.setAmount(scholarship.getAmount());
        dto.setQuota(scholarship.getQuota());
        dto.setDescription(scholarship.getDescription());
        dto.setMinGpa(scholarship.getMinGpa());
        dto.setMinCredits(scholarship.getMinCredits());
        dto.setRequirements(scholarship.getRequirements());
        dto.setActive(scholarship.getActive());
        dto.setCreatedAt(scholarship.getCreatedAt());
        dto.setUpdatedAt(scholarship.getUpdatedAt());
        return dto;
    }
    
    /**
     * 获取当前学年
     */
    private String getCurrentAcademicYear() {
        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        
        // 9月之前算上一学年
        if (currentMonth < 9) {
            return (currentYear - 1) + "-" + currentYear;
        } else {
            return currentYear + "-" + (currentYear + 1);
        }
    }
}

