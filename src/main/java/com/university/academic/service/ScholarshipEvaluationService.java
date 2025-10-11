package com.university.academic.service;

import com.university.academic.dto.ComprehensiveScoreDTO;
import com.university.academic.dto.EvaluationRequest;
import com.university.academic.dto.EvaluationResultDTO;
import com.university.academic.dto.EvaluationRuleDTO;

import java.util.List;

/**
 * 奖学金自动评定服务接口
 */
public interface ScholarshipEvaluationService {
    
    /**
     * 自动评定奖学金
     * 根据评定规则计算综合得分并生成推荐名单
     * 
     * @param request 评定请求
     * @return 评定结果
     */
    EvaluationResultDTO autoEvaluate(EvaluationRequest request);
    
    /**
     * 计算学生综合得分
     * 
     * @param studentId 学生ID
     * @param academicYear 学年
     * @param rule 评定规则
     * @return 综合得分详情
     */
    ComprehensiveScoreDTO calculateComprehensiveScore(Long studentId, String academicYear, EvaluationRuleDTO rule);
    
    /**
     * 批量计算综合得分
     * 
     * @param studentIds 学生ID列表
     * @param academicYear 学年
     * @param rule 评定规则
     * @return 综合得分列表（按总分降序）
     */
    List<ComprehensiveScoreDTO> batchCalculateScores(List<Long> studentIds, String academicYear, EvaluationRuleDTO rule);
    
    /**
     * 生成推荐名单
     * 根据名额和得分排序生成推荐名单
     * 
     * @param scores 综合得分列表
     * @param quota 名额
     * @return 推荐名单
     */
    List<ComprehensiveScoreDTO> generateRecommendedList(List<ComprehensiveScoreDTO> scores, Integer quota);
    
    /**
     * 获取默认评定规则
     * 
     * @return 默认评定规则
     */
    EvaluationRuleDTO getDefaultRule();
    
    /**
     * 从奖学金要求解析评定规则
     * 
     * @param scholarshipId 奖学金ID
     * @return 评定规则
     */
    EvaluationRuleDTO parseRuleFromScholarship(Long scholarshipId);
}

