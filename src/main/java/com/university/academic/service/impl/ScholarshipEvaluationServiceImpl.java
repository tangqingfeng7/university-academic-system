package com.university.academic.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.academic.dto.*;
import com.university.academic.entity.Scholarship;
import com.university.academic.entity.ScholarshipApplication;
import com.university.academic.entity.Student;
import com.university.academic.exception.BusinessException;
import com.university.academic.exception.ErrorCode;
import com.university.academic.repository.ScholarshipApplicationRepository;
import com.university.academic.repository.ScholarshipRepository;
import com.university.academic.repository.StudentRepository;
import com.university.academic.service.CreditCalculationService;
import com.university.academic.service.ScholarshipEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 奖学金自动评定服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScholarshipEvaluationServiceImpl implements ScholarshipEvaluationService {
    
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CreditCalculationService creditCalculationService;
    private final ObjectMapper objectMapper;
    
    /**
     * 自动评定奖学金
     */
    @Override
    @Transactional
    public EvaluationResultDTO autoEvaluate(EvaluationRequest request) {
        log.info("开始自动评定奖学金: scholarshipId={}, academicYear={}", 
                request.getScholarshipId(), request.getAcademicYear());
        
        // 1. 查询奖学金信息
        Scholarship scholarship = scholarshipRepository.findById(request.getScholarshipId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        // 2. 获取评定规则
        EvaluationRuleDTO rule = request.getRule();
        if (rule == null) {
            rule = parseRuleFromScholarship(request.getScholarshipId());
        }
        
        // 3. 查询所有申请记录
        List<ScholarshipApplication> applications = applicationRepository
                .findByScholarshipIdAndAcademicYear(
                        request.getScholarshipId(), 
                        request.getAcademicYear()
                );
        
        if (applications.isEmpty()) {
            log.warn("没有找到申请记录: scholarshipId={}, academicYear={}", 
                    request.getScholarshipId(), request.getAcademicYear());
            return buildEmptyResult(scholarship, request.getAcademicYear(), rule);
        }
        
        // 4. 批量计算综合得分
        List<Long> studentIds = applications.stream()
                .map(app -> app.getStudent().getId())
                .collect(Collectors.toList());
        
        List<ComprehensiveScoreDTO> scores = batchCalculateScores(
                studentIds, request.getAcademicYear(), rule);
        
        // 5. 生成推荐名单
        List<ComprehensiveScoreDTO> recommendedList = generateRecommendedList(
                scores, scholarship.getQuota());
        
        // 6. 更新申请记录的综合得分
        updateApplicationScores(applications, scores);
        
        // 7. 构建结果
        long qualifiedCount = scores.stream()
                .filter(ComprehensiveScoreDTO::getRecommended)
                .count();
        
        log.info("自动评定完成: scholarshipId={}, total={}, qualified={}, recommended={}", 
                request.getScholarshipId(), applications.size(), qualifiedCount, 
                recommendedList.size());
        
        return EvaluationResultDTO.builder()
                .scholarshipId(scholarship.getId())
                .scholarshipName(scholarship.getName())
                .academicYear(request.getAcademicYear())
                .quota(scholarship.getQuota())
                .totalApplicants(applications.size())
                .qualifiedApplicants((int) qualifiedCount)
                .recommendedList(recommendedList)
                .rule(rule)
                .build();
    }
    
    /**
     * 计算学生综合得分
     */
    @Override
    @Transactional(readOnly = true)
    public ComprehensiveScoreDTO calculateComprehensiveScore(
            Long studentId, String academicYear, EvaluationRuleDTO rule) {
        
        log.debug("计算学生综合得分: studentId={}, academicYear={}", studentId, academicYear);
        
        // 1. 查询学生信息
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
        
        // 2. 获取学分信息
        StudentCreditSummaryDTO creditSummary = creditCalculationService.getStudentCreditSummary(studentId);
        double gpa = creditSummary.getGpa();
        double totalCredits = creditSummary.getTotalCredits();
        
        // 3. 检查基本条件
        String excludeReason = checkEligibility(gpa, totalCredits, rule);
        if (excludeReason != null) {
            return ComprehensiveScoreDTO.builder()
                    .studentId(studentId)
                    .studentNo(student.getStudentNo())
                    .name(student.getName())
                    .gpa(gpa)
                    .academicScore(0.0)
                    .comprehensiveScore(0.0)
                    .awardBonus(0.0)
                    .practiceBonus(0.0)
                    .totalScore(0.0)
                    .recommended(false)
                    .excludeReason(excludeReason)
                    .build();
        }
        
        // 4. 计算学业成绩分数（基于GPA，满分100）
        // GPA范围通常是0-4.0，转换为0-100分
        double academicScore = (gpa / 4.0) * 100.0;
        
        // 5. 计算综合素质分数（这里简化处理，可根据实际情况扩展）
        // 可以考虑：参与活动次数、担任学生干部、志愿服务等
        double comprehensiveScore = calculateComprehensiveQuality(studentId, academicYear);
        
        // 6. 计算获奖加分
        double awardBonus = calculateAwardBonus(studentId, academicYear) * rule.getAwardBonus();
        
        // 7. 计算社会实践加分
        double practiceBonus = calculatePracticeBonus(studentId, academicYear) * rule.getPracticeBonus();
        
        // 8. 计算总分
        double totalScore = academicScore * rule.getAcademicWeight() 
                + comprehensiveScore * rule.getComprehensiveWeight()
                + awardBonus
                + practiceBonus;
        
        // 保留2位小数
        totalScore = Math.round(totalScore * 100.0) / 100.0;
        
        log.debug("综合得分计算完成: studentId={}, totalScore={}", studentId, totalScore);
        
        return ComprehensiveScoreDTO.builder()
                .studentId(studentId)
                .studentNo(student.getStudentNo())
                .name(student.getName())
                .gpa(gpa)
                .academicScore(Math.round(academicScore * 100.0) / 100.0)
                .comprehensiveScore(Math.round(comprehensiveScore * 100.0) / 100.0)
                .awardBonus(Math.round(awardBonus * 100.0) / 100.0)
                .practiceBonus(Math.round(practiceBonus * 100.0) / 100.0)
                .totalScore(totalScore)
                .recommended(false)
                .build();
    }
    
    /**
     * 批量计算综合得分
     */
    @Override
    @Transactional(readOnly = true)
    public List<ComprehensiveScoreDTO> batchCalculateScores(
            List<Long> studentIds, String academicYear, EvaluationRuleDTO rule) {
        
        log.info("批量计算综合得分: studentCount={}", studentIds.size());
        
        List<ComprehensiveScoreDTO> scores = new ArrayList<>();
        for (Long studentId : studentIds) {
            try {
                ComprehensiveScoreDTO score = calculateComprehensiveScore(studentId, academicYear, rule);
                scores.add(score);
            } catch (Exception e) {
                log.error("计算学生得分失败: studentId={}", studentId, e);
                // 继续处理其他学生
            }
        }
        
        // 按总分降序排序
        scores.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));
        
        // 设置排名
        for (int i = 0; i < scores.size(); i++) {
            scores.get(i).setRanking(i + 1);
        }
        
        return scores;
    }
    
    /**
     * 生成推荐名单
     */
    @Override
    public List<ComprehensiveScoreDTO> generateRecommendedList(
            List<ComprehensiveScoreDTO> scores, Integer quota) {
        
        log.info("生成推荐名单: totalCount={}, quota={}", scores.size(), quota);
        
        // 过滤掉不符合条件的学生
        List<ComprehensiveScoreDTO> qualifiedScores = scores.stream()
                .filter(score -> score.getExcludeReason() == null)
                .collect(Collectors.toList());
        
        // 取前N名作为推荐名单
        List<ComprehensiveScoreDTO> recommendedList = qualifiedScores.stream()
                .limit(quota)
                .collect(Collectors.toList());
        
        // 标记推荐状态
        recommendedList.forEach(score -> score.setRecommended(true));
        
        log.info("推荐名单生成完成: recommendedCount={}", recommendedList.size());
        
        return recommendedList;
    }
    
    /**
     * 获取默认评定规则
     */
    @Override
    public EvaluationRuleDTO getDefaultRule() {
        return EvaluationRuleDTO.builder()
                .academicWeight(0.7)        // 学业成绩权重70%
                .comprehensiveWeight(0.3)   // 综合素质权重30%
                .awardBonus(0.1)            // 获奖加分比例10%
                .practiceBonus(0.05)        // 社会实践加分比例5%
                .minGpa(2.5)                // 最低GPA要求2.5
                .minCredits(20.0)           // 最低学分要求20学分
                .requireCleanRecord(true)   // 要求无处分记录
                .requirePaidTuition(true)   // 要求按时缴费
                .build();
    }
    
    /**
     * 从奖学金要求解析评定规则
     */
    @Override
    @Transactional(readOnly = true)
    public EvaluationRuleDTO parseRuleFromScholarship(Long scholarshipId) {
        Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHOLARSHIP_NOT_FOUND));
        
        // 从奖学金的requirements字段解析规则
        if (scholarship.getRequirements() != null && !scholarship.getRequirements().isEmpty()) {
            try {
                return objectMapper.readValue(scholarship.getRequirements(), EvaluationRuleDTO.class);
            } catch (JsonProcessingException e) {
                log.warn("解析奖学金要求失败，使用默认规则: scholarshipId={}", scholarshipId, e);
            }
        }
        
        // 使用默认规则，但应用奖学金的基本要求
        EvaluationRuleDTO rule = getDefaultRule();
        if (scholarship.getMinGpa() != null) {
            rule.setMinGpa(scholarship.getMinGpa());
        }
        if (scholarship.getMinCredits() != null) {
            rule.setMinCredits(scholarship.getMinCredits());
        }
        
        return rule;
    }
    
    /**
     * 检查学生是否符合基本条件
     *
     * @return 不符合原因，符合则返回null
     */
    private String checkEligibility(double gpa, double totalCredits, EvaluationRuleDTO rule) {
        if (gpa < rule.getMinGpa()) {
            return "GPA不足：" + gpa + "（要求≥" + rule.getMinGpa() + "）";
        }
        
        if (totalCredits < rule.getMinCredits()) {
            return "学分不足：" + totalCredits + "（要求≥" + rule.getMinCredits() + "）";
        }
        
        // TODO: 检查处分记录
        // if (rule.getRequireCleanRecord() && hasDisciplinaryRecord(studentId)) {
        //     return "存在未解除的处分记录";
        // }
        
        // TODO: 检查学费缴纳情况
        // if (rule.getRequirePaidTuition() && hasUnpaidTuition(studentId)) {
        //     return "存在未缴学费";
        // }
        
        return null;
    }
    
    /**
     * 计算综合素质分数
     * 这里简化处理，实际可根据学生活动参与情况、学生干部经历等评定
     */
    private double calculateComprehensiveQuality(Long studentId, String academicYear) {
        // 简化实现：返回一个基础分数60-80之间
        // 实际应该查询学生的综合素质记录
        return 70.0;
    }
    
    /**
     * 计算获奖加分
     * 根据学生获奖情况计算加分
     */
    private double calculateAwardBonus(Long studentId, String academicYear) {
        // 简化实现：返回0-20分
        // 实际应该查询学生的获奖记录
        // 国家级奖项：20分
        // 省级奖项：15分
        // 校级奖项：10分
        // 院级奖项：5分
        return 0.0;
    }
    
    /**
     * 计算社会实践加分
     * 根据学生社会实践参与情况计算加分
     */
    private double calculatePracticeBonus(Long studentId, String academicYear) {
        // 简化实现：返回0-10分
        // 实际应该查询学生的社会实践记录
        // 志愿服务、社会实践、实习经历等
        return 0.0;
    }
    
    /**
     * 更新申请记录的综合得分
     */
    private void updateApplicationScores(
            List<ScholarshipApplication> applications,
            List<ComprehensiveScoreDTO> scores) {
        
        Map<Long, Double> scoreMap = scores.stream()
                .collect(Collectors.toMap(
                        ComprehensiveScoreDTO::getStudentId,
                        ComprehensiveScoreDTO::getTotalScore
                ));
        
        for (ScholarshipApplication application : applications) {
            Long studentId = application.getStudent().getId();
            Double score = scoreMap.get(studentId);
            if (score != null) {
                application.setComprehensiveScore(score);
            }
        }
        
        applicationRepository.saveAll(applications);
        log.info("已更新{}条申请记录的综合得分", applications.size());
    }
    
    /**
     * 构建空结果
     */
    private EvaluationResultDTO buildEmptyResult(
            Scholarship scholarship, String academicYear, EvaluationRuleDTO rule) {
        return EvaluationResultDTO.builder()
                .scholarshipId(scholarship.getId())
                .scholarshipName(scholarship.getName())
                .academicYear(academicYear)
                .quota(scholarship.getQuota())
                .totalApplicants(0)
                .qualifiedApplicants(0)
                .recommendedList(new ArrayList<>())
                .rule(rule)
                .build();
    }
}

