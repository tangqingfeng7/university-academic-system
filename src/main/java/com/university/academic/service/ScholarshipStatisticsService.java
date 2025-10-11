package com.university.academic.service;

import com.university.academic.dto.AwardStatisticsDTO;

/**
 * 奖学金统计服务接口
 */
public interface ScholarshipStatisticsService {
    
    /**
     * 获奖分布统计
     *
     * @param academicYear 学年
     * @return 统计数据
     */
    AwardStatisticsDTO getAwardDistribution(String academicYear);
    
    /**
     * 按专业统计
     *
     * @param academicYear 学年
     * @param departmentId 院系ID（可选，为null时统计所有院系）
     * @return 专业统计数据（Key: 专业名称, Value: 获奖人数）
     */
    java.util.Map<String, Integer> getStatisticsByMajor(String academicYear, Long departmentId);
    
    /**
     * 按年级统计
     *
     * @param academicYear 学年
     * @return 年级统计数据（Key: 年级, Value: 获奖人数）
     */
    java.util.Map<Integer, Integer> getStatisticsByGrade(String academicYear);
    
    /**
     * 生成统计报告（Excel）
     *
     * @param academicYear 学年
     * @return Excel文件字节数组
     */
    byte[] generateStatisticsReport(String academicYear);
    
    /**
     * 获取年度对比统计
     * 比较多个学年的获奖情况
     *
     * @param startYear 起始学年
     * @param endYear 结束学年
     * @return 年度对比数据
     */
    java.util.List<AwardStatisticsDTO> getYearlyComparison(String startYear, String endYear);
}

