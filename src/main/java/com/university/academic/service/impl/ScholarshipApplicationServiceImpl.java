package com.university.academic.service.impl;

import com.university.academic.dto.*;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.*;
import com.university.academic.service.ScholarshipApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 奖学金申请服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScholarshipApplicationServiceImpl implements ScholarshipApplicationService {
    
    private final ScholarshipApplicationRepository applicationRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final com.university.academic.service.ScholarshipEvaluationService evaluationService;
    
    @Override
    @Transactional
    public ScholarshipApplicationDTO submitApplication(Long studentId, CreateApplicationRequest request) {
        log.info("学生提交奖学金申请: studentId={}, scholarshipId={}", studentId, request.getScholarshipId());
        
        // 验证奖学金是否存在且启用
        Scholarship scholarship = scholarshipRepository.findById(request.getScholarshipId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        if (!scholarship.getActive()) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_NOT_ACTIVE);
        }
        
        // 验证学生是否存在
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        
        // 检查是否已申请
        if (applicationRepository.existsByStudentIdAndScholarshipIdAndAcademicYear(
                studentId, request.getScholarshipId(), request.getAcademicYear())) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_ALREADY_EXISTS);
        }
        
        // 验证申请资格
        if (!validateEligibility(studentId, request.getScholarshipId())) {
            throw new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_NOT_ELIGIBLE);
        }
        
        // 计算学生的GPA和学分
        Double gpa = calculateGPA(studentId, request.getAcademicYear());
        Double totalCredits = calculateTotalCredits(studentId);
        
        // 计算综合得分
        Double comprehensiveScore = calculateComprehensiveScore(studentId, request.getAcademicYear());
        
        // 创建申请
        ScholarshipApplication application = new ScholarshipApplication();
        application.setScholarship(scholarship);
        application.setStudent(student);
        application.setAcademicYear(request.getAcademicYear());
        application.setGpa(gpa);
        application.setTotalCredits(totalCredits);
        application.setComprehensiveScore(comprehensiveScore);
        application.setPersonalStatement(request.getPersonalStatement());
        application.setAttachmentUrl(request.getAttachmentUrl());
        application.setStatus(ApplicationStatus.PENDING);
        application.setSubmittedAt(LocalDateTime.now());
        
        application = applicationRepository.save(application);
        log.info("奖学金申请提交成功: id={}", application.getId());
        
        return convertToDTO(application);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipApplicationDTO> getStudentApplications(Long studentId) {
        log.debug("查询学生的申请记录: studentId={}", studentId);
        
        List<ScholarshipApplication> applications = 
                applicationRepository.findByStudentIdWithScholarship(studentId);
        
        return applications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ScholarshipApplicationDTO> getApplications(ApplicationQueryDTO query, Pageable pageable) {
        log.debug("查询申请列表: {}", query);
        
        Specification<ScholarshipApplication> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (query.getScholarshipId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("scholarship").get("id"), query.getScholarshipId()));
            }
            
            if (query.getStudentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("id"), query.getStudentId()));
            }
            
            if (query.getAcademicYear() != null) {
                predicates.add(criteriaBuilder.equal(root.get("academicYear"), query.getAcademicYear()));
            }
            
            if (query.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.getStatus()));
            }
            
            if (query.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("student").get("major").get("department").get("id"), 
                        query.getDepartmentId()));
            }
            
            if (query.getMajorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("student").get("major").get("id"), query.getMajorId()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<ScholarshipApplication> applications = applicationRepository.findAll(spec, pageable);
        return applications.map(this::convertToDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public ScholarshipApplicationDTO getApplicationById(Long id) {
        log.debug("查询申请: id={}", id);
        
        ScholarshipApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_NOT_FOUND));
        
        return convertToDTO(application);
    }
    
    @Override
    @Transactional
    public void cancelApplication(Long id, Long studentId) {
        log.info("撤销申请: id={}, studentId={}", id, studentId);
        
        ScholarshipApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_APPLICATION_NOT_FOUND));
        
        // 验证是否为本人的申请
        if (!application.getStudent().getId().equals(studentId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        // 只有待审核状态可以撤销
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "只有待审核的申请可以撤销");
        }
        
        applicationRepository.delete(application);
        log.info("申请已撤销");
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validateEligibility(Long studentId, Long scholarshipId) {
        log.debug("验证申请资格: studentId={}, scholarshipId={}", studentId, scholarshipId);
        
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        // 计算学生的GPA和学分
        Double gpa = calculateGPA(studentId, getCurrentAcademicYear());
        Double totalCredits = calculateTotalCredits(studentId);
        
        // 检查GPA要求
        if (scholarship.getMinGpa() != null && gpa < scholarship.getMinGpa()) {
            log.debug("GPA不符合要求: actual={}, required={}", gpa, scholarship.getMinGpa());
            return false;
        }
        
        // 检查学分要求
        if (scholarship.getMinCredits() != null && totalCredits < scholarship.getMinCredits()) {
            log.debug("学分不符合要求: actual={}, required={}", totalCredits, scholarship.getMinCredits());
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double calculateComprehensiveScore(Long studentId, String academicYear) {
        log.debug("计算综合得分: studentId={}, academicYear={}", studentId, academicYear);
        
        // 综合得分 = 学业成绩(70%) + 其他综合素质(30%)
        // 这里简化处理，实际可根据需求调整权重和计算方式
        
        Double gpa = calculateGPA(studentId, academicYear);
        
        // 将GPA转换为百分制分数
        Double academicScore = (gpa / 4.0) * 100;
        
        // 综合素质分数（这里简化为固定值，实际应根据获奖情况、社会实践等计算）
        Double comprehensiveQualityScore = 80.0;
        
        Double finalScore = academicScore * 0.7 + comprehensiveQualityScore * 0.3;
        
        return Math.round(finalScore * 100.0) / 100.0;
    }
    
    /**
     * 计算学生GPA
     */
    private Double calculateGPA(Long studentId, String academicYear) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        
        if (grades.isEmpty()) {
            return 0.0;
        }
        
        double totalWeightedScore = 0.0;
        double totalCredits = 0.0;
        
        for (Grade grade : grades) {
            if (grade.getFinalScore() != null) {
                Double gpaPoint = convertScoreToGPA(grade.getFinalScore());
                Double credits = Double.valueOf(grade.getCourseSelection().getOffering().getCourse().getCredits());
                
                totalWeightedScore += gpaPoint * credits;
                totalCredits += credits;
            }
        }
        
        if (totalCredits == 0) {
            return 0.0;
        }
        
        return Math.round((totalWeightedScore / totalCredits) * 100.0) / 100.0;
    }
    
    /**
     * 将分数转换为GPA
     */
    private Double convertScoreToGPA(Number score) {
        double scoreValue = score.doubleValue();
        if (scoreValue >= 90) return 4.0;
        if (scoreValue >= 85) return 3.7;
        if (scoreValue >= 82) return 3.3;
        if (scoreValue >= 78) return 3.0;
        if (scoreValue >= 75) return 2.7;
        if (scoreValue >= 72) return 2.3;
        if (scoreValue >= 68) return 2.0;
        if (scoreValue >= 64) return 1.5;
        if (scoreValue >= 60) return 1.0;
        return 0.0;
    }
    
    /**
     * 计算学生总学分
     */
    private Double calculateTotalCredits(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);
        
        return grades.stream()
                .filter(g -> g.getFinalScore() != null && g.getFinalScore().doubleValue() >= 60)
                .mapToDouble(g -> Double.valueOf(g.getCourseSelection().getOffering().getCourse().getCredits()))
                .sum();
    }
    
    /**
     * 获取当前学年
     */
    private String getCurrentAcademicYear() {
        int currentYear = java.time.Year.now().getValue();
        int currentMonth = java.time.LocalDate.now().getMonthValue();
        
        if (currentMonth < 9) {
            return (currentYear - 1) + "-" + currentYear;
        } else {
            return currentYear + "-" + (currentYear + 1);
        }
    }
    
    /**
     * 转换为DTO
     */
    private ScholarshipApplicationDTO convertToDTO(ScholarshipApplication application) {
        ScholarshipApplicationDTO dto = new ScholarshipApplicationDTO();
        dto.setId(application.getId());
        
        // 转换奖学金信息
        ScholarshipDTO scholarshipDTO = new ScholarshipDTO();
        scholarshipDTO.setId(application.getScholarship().getId());
        scholarshipDTO.setName(application.getScholarship().getName());
        scholarshipDTO.setLevel(application.getScholarship().getLevel());
        scholarshipDTO.setLevelDescription(application.getScholarship().getLevel().getDescription());
        scholarshipDTO.setAmount(application.getScholarship().getAmount());
        scholarshipDTO.setQuota(application.getScholarship().getQuota());
        scholarshipDTO.setDescription(application.getScholarship().getDescription());
        scholarshipDTO.setMinGpa(application.getScholarship().getMinGpa());
        scholarshipDTO.setMinCredits(application.getScholarship().getMinCredits());
        scholarshipDTO.setRequirements(application.getScholarship().getRequirements());
        dto.setScholarship(scholarshipDTO);
        
        // 转换学生信息
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(application.getStudent().getId());
        studentDTO.setStudentNo(application.getStudent().getStudentNo());
        studentDTO.setName(application.getStudent().getName());
        studentDTO.setEnrollmentYear(application.getStudent().getEnrollmentYear());
        studentDTO.setClassName(application.getStudent().getClassName());
        studentDTO.setPhone(application.getStudent().getPhone());
        studentDTO.setEmail(application.getStudent().getEmail());
        studentDTO.setStatus(application.getStudent().getStatus());
        if (application.getStudent().getMajor() != null) {
            studentDTO.setMajorId(application.getStudent().getMajor().getId());
            studentDTO.setMajorName(application.getStudent().getMajor().getName());
            if (application.getStudent().getMajor().getDepartment() != null) {
                studentDTO.setDepartmentId(application.getStudent().getMajor().getDepartment().getId());
                studentDTO.setDepartmentName(application.getStudent().getMajor().getDepartment().getName());
            }
        }
        // 根据入学年份计算年级
        int currentYear = java.time.Year.now().getValue();
        int grade = currentYear - application.getStudent().getEnrollmentYear() + 1;
        studentDTO.setGrade(grade);
        dto.setStudent(studentDTO);
        
        dto.setAcademicYear(application.getAcademicYear());
        dto.setGpa(application.getGpa());
        dto.setTotalCredits(application.getTotalCredits());
        dto.setComprehensiveScore(application.getComprehensiveScore());
        dto.setPersonalStatement(application.getPersonalStatement());
        dto.setAttachmentUrl(application.getAttachmentUrl());
        dto.setStatus(application.getStatus());
        dto.setStatusDescription(application.getStatus().getDescription());
        dto.setSubmittedAt(application.getSubmittedAt());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setUpdatedAt(application.getUpdatedAt());
        
        return dto;
    }
    
    /**
     * 自动评定奖学金
     */
    @Override
    @Transactional
    public EvaluationResultDTO autoEvaluate(EvaluationRequest request) {
        log.info("开始自动评定奖学金: scholarshipId={}, academicYear={}", 
                request.getScholarshipId(), request.getAcademicYear());
        
        // 委托给评定服务处理
        return evaluationService.autoEvaluate(request);
    }
}

