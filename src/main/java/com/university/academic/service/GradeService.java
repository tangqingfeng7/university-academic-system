package com.university.academic.service;

import com.university.academic.entity.CourseSelection;
import com.university.academic.entity.Grade;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.CourseOfferingRepository;
import com.university.academic.repository.CourseSelectionRepository;
import com.university.academic.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 成绩服务类
 * 提供成绩录入、查询、提交、发布等功能
 *
 * @author Academic System Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;
    private final CourseSelectionRepository selectionRepository;
    private final CourseOfferingRepository offeringRepository;

    /**
     * 根据ID查询成绩
     *
     * @param id 成绩ID
     * @return 成绩对象
     */
    @Transactional(readOnly = true)
    public Grade findById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND));
    }

    /**
     * 查询开课计划的所有成绩（教师用）
     *
     * @param offeringId 开课计划ID
     * @return 成绩列表
     */
    @Transactional(readOnly = true)
    public List<Grade> findByOffering(Long offeringId) {
        // 验证开课计划存在
        offeringRepository.findById(offeringId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OFFERING_NOT_FOUND));

        // 查询该开课计划的所有选课记录
        List<CourseSelection> selections = selectionRepository.findByOfferingId(offeringId);

        // 为每个选课记录创建或查找成绩记录
        List<Grade> grades = new ArrayList<>();
        for (CourseSelection selection : selections) {
            Grade grade = gradeRepository.findByCourseSelectionId(selection.getId())
                    .orElseGet(() -> createEmptyGrade(selection));
            grades.add(grade);
        }

        return grades;
    }

    /**
     * 查询学生的所有已公布成绩
     *
     * @param studentId 学生ID
     * @return 成绩列表
     */
    @Transactional(readOnly = true)
    public List<Grade> findPublishedByStudent(Long studentId) {
        List<Grade> grades = gradeRepository.findPublishedByStudentId(studentId);
        // 强制初始化所有需要的关联以避免懒加载异常
        grades.forEach(grade -> {
            if (grade.getCourseSelection() != null) {
                grade.getCourseSelection().getId();
                if (grade.getCourseSelection().getOffering() != null) {
                    grade.getCourseSelection().getOffering().getId();
                    if (grade.getCourseSelection().getOffering().getCourse() != null) {
                        grade.getCourseSelection().getOffering().getCourse().getCredits();
                    }
                }
            }
        });
        return grades;
    }

    /**
     * 查询学生在指定学期的已公布成绩
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 成绩列表
     */
    @Transactional(readOnly = true)
    public List<Grade> findPublishedByStudentAndSemester(Long studentId, Long semesterId) {
        return gradeRepository.findPublishedByStudentAndSemester(studentId, semesterId);
    }

    /**
     * 录入或更新成绩
     *
     * @param courseSelectionId 选课记录ID
     * @param regularScore      平时成绩
     * @param midtermScore      期中成绩
     * @param finalScore        期末成绩
     * @return 成绩对象
     */
    @Transactional
    public Grade inputGrade(Long courseSelectionId, BigDecimal regularScore, 
                           BigDecimal midtermScore, BigDecimal finalScore) {
        // 验证选课记录存在
        CourseSelection selection = selectionRepository.findById(courseSelectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELECTION_NOT_FOUND));

        // 验证成绩范围
        validateScoreRange(regularScore);
        validateScoreRange(midtermScore);
        validateScoreRange(finalScore);

        // 查找或创建成绩记录
        Grade grade = gradeRepository.findByCourseSelectionId(courseSelectionId)
                .orElseGet(() -> createEmptyGrade(selection));

        // 检查状态（已提交的成绩需要特殊处理）
        if (grade.getStatus() == Grade.GradeStatus.PUBLISHED) {
            log.warn("修改已公布的成绩: gradeId={}", grade.getId());
            // 可以记录修改历史（通过审计字段自动记录更新时间）
        }

        // 更新成绩
        grade.setRegularScore(regularScore);
        grade.setMidtermScore(midtermScore);
        grade.setFinalScore(finalScore);
        
        // 自动计算总评成绩
        grade.calculateTotalScore();

        Grade savedGrade = gradeRepository.save(grade);
        log.info("录入成绩成功: 学生={}, 课程={}, 总评={}", 
                selection.getStudent().getName(),
                selection.getOffering().getCourse().getName(),
                savedGrade.getTotalScore());

        return savedGrade;
    }

    /**
     * 批量录入成绩
     *
     * @param gradeInputList 成绩录入列表
     * @return 成功录入的成绩列表
     */
    @Transactional
    public List<Grade> batchInputGrades(List<GradeInput> gradeInputList) {
        List<Grade> grades = new ArrayList<>();
        
        for (GradeInput input : gradeInputList) {
            try {
                Grade grade = inputGrade(
                        input.getCourseSelectionId(),
                        input.getRegularScore(),
                        input.getMidtermScore(),
                        input.getFinalScore()
                );
                grades.add(grade);
            } catch (Exception e) {
                log.error("批量录入成绩失败: courseSelectionId={}", 
                        input.getCourseSelectionId(), e);
                // 继续处理下一条
            }
        }

        return grades;
    }

    /**
     * 提交成绩（教师提交后不可修改）
     *
     * @param offeringId 开课计划ID
     */
    @Transactional
    public void submitGrades(Long offeringId) {
        List<Grade> grades = gradeRepository.findByOfferingId(offeringId);

        for (Grade grade : grades) {
            if (grade.getStatus() == Grade.GradeStatus.DRAFT) {
                grade.setStatus(Grade.GradeStatus.SUBMITTED);
                grade.setSubmittedAt(LocalDateTime.now());
                gradeRepository.save(grade);
            }
        }

        log.info("提交成绩成功: offeringId={}, 提交数量={}", offeringId, grades.size());
    }

    /**
     * 发布成绩（学生可见）
     *
     * @param offeringId 开课计划ID
     */
    @Transactional
    public void publishGrades(Long offeringId) {
        List<Grade> grades = gradeRepository.findByOfferingId(offeringId);

        for (Grade grade : grades) {
            if (grade.getStatus() == Grade.GradeStatus.SUBMITTED) {
                grade.setStatus(Grade.GradeStatus.PUBLISHED);
                gradeRepository.save(grade);
            }
        }

        log.info("发布成绩成功: offeringId={}, 发布数量={}", offeringId, grades.size());
    }

    /**
     * 计算学生在指定学期的平均绩点
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 平均绩点
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateGPA(Long studentId, Long semesterId) {
        List<Grade> grades = gradeRepository.findPublishedByStudentAndSemester(
                studentId, semesterId);

        if (grades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGradePoints = BigDecimal.ZERO;
        int totalCredits = 0;

        for (Grade grade : grades) {
            Integer credits = grade.getCourseSelection().getOffering()
                    .getCourse().getCredits();
            BigDecimal gradePoint = grade.getGradePoint();

            totalGradePoints = totalGradePoints.add(
                    gradePoint.multiply(new BigDecimal(credits))
            );
            totalCredits += credits;
        }

        if (totalCredits == 0) {
            return BigDecimal.ZERO;
        }

        return totalGradePoints.divide(new BigDecimal(totalCredits), 2, 
                java.math.RoundingMode.HALF_UP);
    }

    /**
     * 计算学生的总平均绩点
     *
     * @param studentId 学生ID
     * @return 总平均绩点
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateOverallGPA(Long studentId) {
        List<Grade> grades = gradeRepository.findPublishedByStudentId(studentId);

        if (grades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGradePoints = BigDecimal.ZERO;
        int totalCredits = 0;

        for (Grade grade : grades) {
            Integer credits = grade.getCourseSelection().getOffering()
                    .getCourse().getCredits();
            BigDecimal gradePoint = grade.getGradePoint();

            totalGradePoints = totalGradePoints.add(
                    gradePoint.multiply(new BigDecimal(credits))
            );
            totalCredits += credits;
        }

        if (totalCredits == 0) {
            return BigDecimal.ZERO;
        }

        return totalGradePoints.divide(new BigDecimal(totalCredits), 2, 
                java.math.RoundingMode.HALF_UP);
    }

    /**
     * 验证成绩范围
     *
     * @param score 成绩
     */
    private void validateScoreRange(BigDecimal score) {
        if (score == null) {
            return;
        }

        if (score.compareTo(BigDecimal.ZERO) < 0 || 
            score.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException(ErrorCode.GRADE_SCORE_INVALID);
        }
    }

    /**
     * 创建空成绩记录
     *
     * @param selection 选课记录
     * @return 成绩对象
     */
    private Grade createEmptyGrade(CourseSelection selection) {
        return Grade.builder()
                .courseSelection(selection)
                .status(Grade.GradeStatus.DRAFT)
                .build();
    }

    /**
     * 根据学号和开课计划ID查找选课记录ID（用于成绩导入）
     *
     * @param studentNo  学号
     * @param offeringId 开课计划ID
     * @return 选课记录ID，如果不存在返回null
     */
    @Transactional(readOnly = true)
    public Long findCourseSelectionIdByStudentNoAndOffering(String studentNo, Long offeringId) {
        return selectionRepository.findByStudentNoAndOfferingId(studentNo, offeringId)
                .map(CourseSelection::getId)
                .orElse(null);
    }

    /**
     * 统计教师待批改成绩数
     *
     * @param teacherId 教师ID
     * @return 待批改成绩数量
     */
    @Transactional(readOnly = true)
    public long countPendingGradesByTeacher(Long teacherId) {
        return gradeRepository.countPendingByTeacherId(teacherId);
    }

    /**
     * 获取学生仪表盘统计信息
     * 在事务中完整加载所有需要的数据，避免懒加载异常
     *
     * @param studentId 学生ID
     * @return 统计信息Map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentDashboardStatistics(Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        List<Grade> grades = gradeRepository.findPublishedByStudentId(studentId);
        
        double totalCredits = 0.0;
        double totalScore = 0.0;
        int scoreCount = 0;
        
        for (Grade grade : grades) {
            if (grade.getTotalScore() != null) {
                totalScore += grade.getTotalScore().doubleValue();
                scoreCount++;
                
                // 及格的课程计算学分
                if (grade.getTotalScore().doubleValue() >= 60) {
                    if (grade.getCourseSelection() != null && 
                        grade.getCourseSelection().getOffering() != null &&
                        grade.getCourseSelection().getOffering().getCourse() != null) {
                        totalCredits += grade.getCourseSelection().getOffering().getCourse().getCredits();
                    }
                }
            }
        }
        
        result.put("totalCredits", totalCredits);
        result.put("completedCourses", grades.size());
        
        // 计算GPA
        double avgScore = scoreCount > 0 ? totalScore / scoreCount : 0.0;
        double gpa = avgScore > 0 ? Math.max(0, avgScore / 10.0 - 5.0) : 0.0;
        result.put("gpa", Math.round(gpa * 100.0) / 100.0);
        
        return result;
    }

    /**
     * 成绩录入数据结构
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GradeInput {
        private Long courseSelectionId;
        private BigDecimal regularScore;
        private BigDecimal midtermScore;
        private BigDecimal finalScore;
    }
}

