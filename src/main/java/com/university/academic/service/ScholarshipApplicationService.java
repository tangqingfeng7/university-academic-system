package com.university.academic.service;

import com.university.academic.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 奖学金申请服务接口
 */
public interface ScholarshipApplicationService {
    
    /**
     * 提交申请
     *
     * @param studentId 学生ID
     * @param request 申请请求
     * @return 申请DTO
     */
    ScholarshipApplicationDTO submitApplication(Long studentId, CreateApplicationRequest request);
    
    /**
     * 查询学生的申请记录
     *
     * @param studentId 学生ID
     * @return 申请记录列表
     */
    List<ScholarshipApplicationDTO> getStudentApplications(Long studentId);
    
    /**
     * 查询申请列表
     *
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 申请记录分页
     */
    Page<ScholarshipApplicationDTO> getApplications(ApplicationQueryDTO query, Pageable pageable);
    
    /**
     * 根据ID查询申请
     *
     * @param id 申请ID
     * @return 申请DTO
     */
    ScholarshipApplicationDTO getApplicationById(Long id);
    
    /**
     * 撤销申请
     *
     * @param id 申请ID
     * @param studentId 学生ID
     */
    void cancelApplication(Long id, Long studentId);
    
    /**
     * 验证申请资格
     *
     * @param studentId 学生ID
     * @param scholarshipId 奖学金ID
     * @return 是否符合条件
     */
    boolean validateEligibility(Long studentId, Long scholarshipId);
    
    /**
     * 计算学生综合得分
     *
     * @param studentId 学生ID
     * @param academicYear 学年
     * @return 综合得分
     */
    Double calculateComprehensiveScore(Long studentId, String academicYear);
    
    /**
     * 自动评定奖学金
     *
     * @param request 评定请求
     * @return 评定结果
     */
    EvaluationResultDTO autoEvaluate(EvaluationRequest request);
}

