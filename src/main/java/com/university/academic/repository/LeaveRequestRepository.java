package com.university.academic.repository;

import com.university.academic.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 请假申请数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * 根据申请人类型和申请人ID查询请假列表（分页）
     *
     * @param applicantType 申请人类型
     * @param applicantId   申请人ID
     * @param pageable      分页参数
     * @return 请假分页数据
     */
    Page<LeaveRequest> findByApplicantTypeAndApplicantIdOrderByCreatedAtDesc(
            LeaveRequest.ApplicantType applicantType,
            Long applicantId,
            Pageable pageable
    );

    /**
     * 根据申请人类型和申请人ID查询请假列表
     *
     * @param applicantType 申请人类型
     * @param applicantId   申请人ID
     * @return 请假列表
     */
    List<LeaveRequest> findByApplicantTypeAndApplicantIdOrderByCreatedAtDesc(
            LeaveRequest.ApplicantType applicantType,
            Long applicantId
    );

    /**
     * 根据审批状态查询所有请假申请（分页）
     *
     * @param status   审批状态
     * @param pageable 分页参数
     * @return 请假分页数据
     */
    Page<LeaveRequest> findByStatusOrderByCreatedAtDesc(
            LeaveRequest.ApprovalStatus status,
            Pageable pageable
    );

    /**
     * 查询所有请假申请（分页，按创建时间倒序）
     *
     * @param pageable 分页参数
     * @return 请假分页数据
     */
    Page<LeaveRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 搜索请假申请
     *
     * @param keyword  关键词（申请人姓名或学号/工号）
     * @param pageable 分页参数
     * @return 请假分页数据
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "lr.applicantName LIKE %:keyword% OR " +
            "lr.applicantNo LIKE %:keyword%")
    Page<LeaveRequest> searchLeaveRequests(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据申请人类型搜索请假申请
     *
     * @param applicantType 申请人类型
     * @param keyword       关键词
     * @param pageable      分页参数
     * @return 请假分页数据
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "lr.applicantType = :applicantType AND (" +
            "lr.applicantName LIKE %:keyword% OR " +
            "lr.applicantNo LIKE %:keyword%)")
    Page<LeaveRequest> searchByApplicantType(
            @Param("applicantType") LeaveRequest.ApplicantType applicantType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 根据审批状态搜索请假申请
     *
     * @param status   审批状态
     * @param keyword  关键词
     * @param pageable 分页参数
     * @return 请假分页数据
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
            "lr.status = :status AND (" +
            "lr.applicantName LIKE %:keyword% OR " +
            "lr.applicantNo LIKE %:keyword%)")
    Page<LeaveRequest> searchByStatus(
            @Param("status") LeaveRequest.ApprovalStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 统计待审批的请假申请数量
     *
     * @return 待审批数量
     */
    long countByStatus(LeaveRequest.ApprovalStatus status);

    /**
     * 统计指定申请人的待审批请假数量
     *
     * @param applicantType 申请人类型
     * @param applicantId   申请人ID
     * @param status        审批状态
     * @return 待审批数量
     */
    long countByApplicantTypeAndApplicantIdAndStatus(
            LeaveRequest.ApplicantType applicantType,
            Long applicantId,
            LeaveRequest.ApprovalStatus status
    );
}

