package com.university.academic.service;

import com.university.academic.config.CacheConfig;
import com.university.academic.dto.StudentCreditSummaryDTO;
import com.university.academic.dto.converter.StudentCreditSummaryConverter;
import com.university.academic.entity.*;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.StudentCreditSummaryRepository;
import com.university.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学分计算服务类
 * 提供学生学分统计、汇总、更新功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCalculationService {

    private final StudentRepository studentRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final StudentCreditSummaryRepository creditSummaryRepository;
    private final StudentCreditSummaryConverter creditSummaryConverter;

    /**
     * 计算学生学分汇总
     * 包含总学分、必修学分、选修学分、实践学分
     *
     * @param studentId 学生ID
     * @return 学分汇总DTO
     */
    @Transactional(readOnly = true)
    public StudentCreditSummaryDTO calculateStudentCredits(Long studentId) {
        log.info("计算学生学分: studentId={}", studentId);

        // 验证学生是否存在，并预加载专业信息
        Student student = studentRepository.findByIdWithDetails(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        // 查询学生所有已完成的选课记录
        List<CourseSelection> completedSelections = courseSelectionRepository
                .findByStudentIdAndStatus(studentId, CourseSelection.SelectionStatus.COMPLETED);

        // 初始化学分统计
        double totalCredits = 0.0;
        double requiredCredits = 0.0;
        double electiveCredits = 0.0;
        double practicalCredits = 0.0;
        double totalGradePoints = 0.0; // 总绩点 = 学分 * 绩点

        // 遍历选课记录统计学分
        for (CourseSelection selection : completedSelections) {
            Grade grade = selection.getGrade();
            
            // 只统计已公布且及格的成绩
            if (grade != null && grade.getStatus() == Grade.GradeStatus.PUBLISHED && grade.isPassed()) {
                Course course = selection.getOffering().getCourse();
                Integer credits = course.getCredits();
                
                // 累加总学分
                totalCredits += credits;
                
                // 计算GPA（加权平均）
                BigDecimal gradePoint = grade.getGradePoint();
                totalGradePoints += credits * gradePoint.doubleValue();
                
                // 根据课程类型累加各类学分
                switch (course.getType()) {
                    case REQUIRED:
                        requiredCredits += credits;
                        break;
                    case ELECTIVE:
                        electiveCredits += credits;
                        break;
                    case PUBLIC:
                        // 公共课计入选修学分
                        electiveCredits += credits;
                        break;
                }
                
                // 检查是否为实践课程（根据课程名称判断，包含"实验"、"实践"、"实训"等关键词）
                String courseName = course.getName();
                if (isPracticalCourse(courseName)) {
                    practicalCredits += credits;
                }
            }
        }

        // 计算平均GPA（加权平均：总绩点 / 总学分）
        double gpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;
        // 保留2位小数
        gpa = Math.round(gpa * 100.0) / 100.0;

        log.info("学分计算完成: studentId={}, totalCredits={}, requiredCredits={}, electiveCredits={}, practicalCredits={}, gpa={}",
                studentId, totalCredits, requiredCredits, electiveCredits, practicalCredits, gpa);

        // 构建DTO返回
        return StudentCreditSummaryDTO.builder()
                .studentId(studentId)
                .studentNo(student.getStudentNo())
                .studentName(student.getName())
                .majorId(student.getMajor().getId())
                .majorName(student.getMajor().getName())
                .enrollmentYear(student.getEnrollmentYear())
                .totalCredits(totalCredits)
                .requiredCredits(requiredCredits)
                .electiveCredits(electiveCredits)
                .practicalCredits(practicalCredits)
                .gpa(gpa)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * 更新学生学分汇总表
     * 将计算的学分保存到数据库
     *
     * @param studentId 学生ID
     * @return 学分汇总DTO
     */
    @CacheEvict(value = CacheConfig.CACHE_CREDIT_SUMMARY, key = "'student:' + #studentId")
    @Transactional
    public StudentCreditSummaryDTO updateStudentCreditSummary(Long studentId) {
        log.info("更新学生学分汇总: studentId={}", studentId);

        // 计算最新学分
        StudentCreditSummaryDTO creditDTO = calculateStudentCredits(studentId);

        // 查询或创建学分汇总记录
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));

        StudentCreditSummary summary = creditSummaryRepository
                .findByStudentId(studentId)
                .orElse(StudentCreditSummary.builder()
                        .student(student)
                        .build());

        // 更新学分数据
        summary.setTotalCredits(creditDTO.getTotalCredits());
        summary.setRequiredCredits(creditDTO.getRequiredCredits());
        summary.setElectiveCredits(creditDTO.getElectiveCredits());
        summary.setPracticalCredits(creditDTO.getPracticalCredits());
        summary.setGpa(creditDTO.getGpa());
        summary.setLastUpdated(LocalDateTime.now());

        // 保存
        summary = creditSummaryRepository.save(summary);

        log.info("学分汇总更新成功: studentId={}", studentId);

        return creditSummaryConverter.toDTO(summary);
    }

    /**
     * 批量更新学生学分汇总
     * 用于成绩公布后批量更新学分
     *
     * @param studentIds 学生ID列表
     */
    @Transactional
    public void batchUpdateCreditSummary(List<Long> studentIds) {
        log.info("批量更新学生学分汇总: count={}", studentIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long studentId : studentIds) {
            try {
                updateStudentCreditSummary(studentId);
                successCount++;
            } catch (Exception e) {
                log.error("更新学生学分汇总失败: studentId={}, error={}", studentId, e.getMessage());
                failCount++;
            }
        }

        log.info("批量更新完成: 成功={}, 失败={}", successCount, failCount);
    }

    /**
     * 查询学生学分汇总（实时计算）
     * 总是实时计算以确保数据准确性，特别是GPA
     *
     * @param studentId 学生ID
     * @return 学分汇总DTO
     */
    @Cacheable(value = CacheConfig.CACHE_CREDIT_SUMMARY, key = "'student:' + #studentId")
    @Transactional(readOnly = true)
    public StudentCreditSummaryDTO getStudentCreditSummary(Long studentId) {
        log.debug("查询学生学分汇总（实时计算）: studentId={}", studentId);

        // 直接实时计算，确保数据准确
        // 这样可以保证GPA始终是最新的，无需手动更新数据库
        return calculateStudentCredits(studentId);
    }

    /**
     * 清除学生学分缓存
     * 用于成绩变更后刷新学分数据
     *
     * @param studentId 学生ID
     */
    @CacheEvict(value = CacheConfig.CACHE_CREDIT_SUMMARY, key = "'student:' + #studentId")
    public void evictStudentCreditCache(Long studentId) {
        log.debug("清除学生学分缓存: studentId={}", studentId);
    }

    /**
     * 判断是否为实践课程
     * 根据课程名称关键词判断
     *
     * @param courseName 课程名称
     * @return 是否为实践课程
     */
    private boolean isPracticalCourse(String courseName) {
        if (courseName == null) {
            return false;
        }

        // 实践课程关键词
        String[] keywords = {"实验", "实践", "实训", "课程设计", "毕业设计", "实习"};

        for (String keyword : keywords) {
            if (courseName.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取学生已修学分明细
     * 返回详细的课程和学分列表
     *
     * @param studentId 学生ID
     * @return 学分明细列表
     */
    @Transactional(readOnly = true)
    public List<CourseSelection> getStudentCreditDetails(Long studentId) {
        log.debug("查询学生学分明细: studentId={}", studentId);

        // 验证学生是否存在
        if (!studentRepository.existsById(studentId)) {
            throw new BusinessException(ErrorCode.STUDENT_NOT_FOUND);
        }

        // 查询已完成且及格的选课记录
        return courseSelectionRepository
                .findByStudentIdAndStatus(studentId, CourseSelection.SelectionStatus.COMPLETED);
    }

    /**
     * 计算学生GPA（平均绩点）
     *
     * @param studentId 学生ID
     * @return GPA值（4分制）
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateStudentGPA(Long studentId) {
        log.debug("计算学生GPA: studentId={}", studentId);

        // 查询学生所有已完成的选课记录
        List<CourseSelection> completedSelections = courseSelectionRepository
                .findByStudentIdAndStatus(studentId, CourseSelection.SelectionStatus.COMPLETED);

        if (completedSelections.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGradePoints = BigDecimal.ZERO;
        int totalCredits = 0;

        // 遍历计算加权绩点
        for (CourseSelection selection : completedSelections) {
            Grade grade = selection.getGrade();

            // 只统计已公布的成绩
            if (grade != null && grade.getStatus() == Grade.GradeStatus.PUBLISHED) {
                Course course = selection.getOffering().getCourse();
                Integer credits = course.getCredits();
                BigDecimal gradePoint = grade.getGradePoint();

                // 累加加权绩点
                totalGradePoints = totalGradePoints.add(
                        gradePoint.multiply(new BigDecimal(credits))
                );
                totalCredits += credits;
            }
        }

        // 计算平均绩点
        if (totalCredits == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal gpa = totalGradePoints.divide(
                new BigDecimal(totalCredits),
                2,
                java.math.RoundingMode.HALF_UP
        );

        log.info("GPA计算完成: studentId={}, gpa={}, totalCredits={}", studentId, gpa, totalCredits);

        return gpa;
    }

    /**
     * 获取学生已完成课程的学分明细
     * 用于前端显示课程列表
     *
     * @param studentId 学生ID
     * @return 已完成课程列表
     */
    @Transactional(readOnly = true)
    public List<com.university.academic.dto.CourseSelectionDTO> getCompletedCourseDetails(Long studentId) {
        log.info("查询学生已完成课程明细: studentId={}", studentId);

        // 查询所有已完成的选课记录（包含成绩）
        List<CourseSelection> completedSelections = courseSelectionRepository
                .findByStudentIdAndStatus(studentId, CourseSelection.SelectionStatus.COMPLETED);

        // 转换为DTO（使用现有的converter或手动构建）
        return completedSelections.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * 将CourseSelection转换为CourseSelectionDTO
     */
    private com.university.academic.dto.CourseSelectionDTO convertToDTO(CourseSelection selection) {
        Course course = selection.getOffering().getCourse();
        CourseOffering offering = selection.getOffering();
        Grade grade = selection.getGrade();

        com.university.academic.dto.CourseSelectionDTO dto = new com.university.academic.dto.CourseSelectionDTO();
        dto.setId(selection.getId());
        dto.setOfferingId(offering.getId());
        dto.setCourseId(course.getId());
        dto.setCourseNo(course.getCourseNo());
        dto.setCourseName(course.getName());
        dto.setCourseType(course.getType().name());
        dto.setCredits(course.getCredits());
        dto.setSelectionTime(selection.getSelectionTime());
        dto.setStatus(selection.getStatus().name());
        
        // 添加学期信息
        if (offering.getSemester() != null) {
            Semester semester = offering.getSemester();
            String semesterName = semester.getAcademicYear() + "学年第" + semester.getSemesterType() + "学期";
            dto.setSemesterName(semesterName);
        }
        
        // 添加成绩信息
        if (grade != null) {
            dto.setTotalScore(grade.getTotalScore());
            dto.setGradePoint(grade.getGradePoint());
            dto.setPassed(grade.isPassed());
        }

        return dto;
    }
}

