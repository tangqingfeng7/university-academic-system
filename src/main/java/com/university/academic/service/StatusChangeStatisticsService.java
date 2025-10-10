package com.university.academic.service;

import com.university.academic.dto.StatusChangeQueryDTO;
import com.university.academic.dto.StatusChangeStatisticsDTO;
import com.university.academic.entity.StudentStatusChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * 学籍异动统计服务接口
 *
 * @author university
 * @since 2024-01-01
 */
public interface StatusChangeStatisticsService {

    /**
     * 查询异动记录（支持多条件筛选）
     *
     * @param queryDTO 查询条件
     * @param pageable 分页参数
     * @return 异动记录分页结果
     */
    Page<StudentStatusChange> queryStatusChanges(StatusChangeQueryDTO queryDTO, Pageable pageable);

    /**
     * 统计异动数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果
     */
    StatusChangeStatisticsDTO getStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 按类型统计异动数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 各类型异动数量
     */
    StatusChangeStatisticsDTO getStatisticsByType(LocalDate startDate, LocalDate endDate);

    /**
     * 按状态统计异动数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 各状态异动数量
     */
    StatusChangeStatisticsDTO getStatisticsByStatus(LocalDate startDate, LocalDate endDate);

    /**
     * 按月份统计异动趋势
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 月度统计数据
     */
    StatusChangeStatisticsDTO getStatisticsByMonth(LocalDate startDate, LocalDate endDate);

    /**
     * 统计转专业流向
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 转专业统计数据
     */
    StatusChangeStatisticsDTO getTransferStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 导出异动记录（Excel格式）
     *
     * @param queryDTO 查询条件
     * @return Excel文件字节数组
     */
    byte[] exportStatusChanges(StatusChangeQueryDTO queryDTO);

    /**
     * 生成异动统计报告（Excel格式）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return Excel文件字节数组
     */
    byte[] exportStatisticsReport(LocalDate startDate, LocalDate endDate);

    /**
     * 查询待审批的异动申请
     *
     * @param approverId 审批人ID
     * @param pageable   分页参数
     * @return 待审批申请分页结果
     */
    Page<StudentStatusChange> getPendingApplications(Long approverId, Pageable pageable);

    /**
     * 查询超时的异动申请
     *
     * @param pageable 分页参数
     * @return 超时申请分页结果
     */
    Page<StudentStatusChange> getOverdueApplications(Pageable pageable);

    /**
     * 查询学生的所有异动记录
     *
     * @param studentId 学生ID
     * @return 异动记录列表
     */
    List<StudentStatusChange> getStudentStatusChanges(Long studentId);
}

