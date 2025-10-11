package com.university.academic.service;

import com.university.academic.dto.AwardStatisticsDTO;
import com.university.academic.dto.PublishAwardsRequest;
import com.university.academic.dto.ScholarshipAwardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 奖学金获奖记录服务接口
 */
public interface ScholarshipAwardService {
    
    /**
     * 公示获奖名单
     * 根据奖学金ID和学年，将审批通过的申请转为获奖记录并公示
     *
     * @param request 公示请求
     * @return 公示的获奖记录数量
     */
    Integer publishAwards(PublishAwardsRequest request);
    
    /**
     * 查询学生的获奖记录
     *
     * @param studentId 学生ID
     * @return 获奖记录列表
     */
    List<ScholarshipAwardDTO> getStudentAwards(Long studentId);
    
    /**
     * 查询获奖名单（分页）
     *
     * @param scholarshipId 奖学金ID（可选）
     * @param academicYear 学年（可选）
     * @param published 是否仅查询已公示的
     * @param pageable 分页参数
     * @return 获奖记录分页
     */
    Page<ScholarshipAwardDTO> getAwards(Long scholarshipId, String academicYear, 
                                        Boolean published, Pageable pageable);
    
    /**
     * 导出获奖名单（Excel）
     *
     * @param scholarshipId 奖学金ID（可选）
     * @param academicYear 学年
     * @return Excel文件字节数组
     */
    byte[] exportAwardList(Long scholarshipId, String academicYear);
    
    /**
     * 获奖统计
     *
     * @param academicYear 学年
     * @return 统计数据
     */
    AwardStatisticsDTO getStatistics(String academicYear);
}

