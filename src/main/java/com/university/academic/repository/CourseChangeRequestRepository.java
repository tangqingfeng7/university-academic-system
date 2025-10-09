package com.university.academic.repository;

import com.university.academic.entity.CourseChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 调课申请Repository
 *
 * @author Academic System Team
 */
@Repository
public interface CourseChangeRequestRepository extends JpaRepository<CourseChangeRequest, Long> {

    /**
     * 根据教师ID分页查询调课申请
     */
    Page<CourseChangeRequest> findByTeacherId(Long teacherId, Pageable pageable);

    /**
     * 根据状态分页查询调课申请
     */
    Page<CourseChangeRequest> findByStatus(CourseChangeRequest.RequestStatus status, Pageable pageable);

    /**
     * 根据教师ID和状态查询调课申请
     */
    List<CourseChangeRequest> findByTeacherIdAndStatus(Long teacherId, CourseChangeRequest.RequestStatus status);

    /**
     * 统计待审批的调课申请数量
     */
    long countByStatus(CourseChangeRequest.RequestStatus status);

    /**
     * 查询指定开课计划的待审批或已通过的调课申请
     */
    @Query("SELECT c FROM CourseChangeRequest c WHERE c.offeringId = :offeringId " +
           "AND c.status IN ('PENDING', 'APPROVED')")
    List<CourseChangeRequest> findPendingOrApprovedByOfferingId(@Param("offeringId") Long offeringId);
}

