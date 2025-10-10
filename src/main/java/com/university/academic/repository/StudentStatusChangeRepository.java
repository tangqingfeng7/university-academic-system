package com.university.academic.repository;

import com.university.academic.entity.ApprovalStatus;
import com.university.academic.entity.ChangeType;
import com.university.academic.entity.StudentStatusChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 学籍异动Repository接口
 *
 * @author Academic System Team
 */
@Repository
public interface StudentStatusChangeRepository extends JpaRepository<StudentStatusChange, Long>, JpaSpecificationExecutor<StudentStatusChange> {

    /**
     * 根据学生ID查询异动记录
     *
     * @param studentId 学生ID
     * @param pageable  分页参数
     * @return 异动记录分页列表
     */
    @Query("SELECT sc FROM StudentStatusChange sc " +
           "WHERE sc.student.id = :studentId AND sc.deleted = false " +
           "ORDER BY sc.createdAt DESC")
    Page<StudentStatusChange> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * 根据学生ID查询所有异动记录（不分页）
     *
     * @param studentId 学生ID
     * @return 异动记录列表
     */
    @Query("SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "WHERE sc.student.id = :studentId AND sc.deleted = false " +
           "ORDER BY sc.createdAt DESC")
    List<StudentStatusChange> findAllByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据审批状态查询异动记录
     *
     * @param status   审批状态
     * @param pageable 分页参数
     * @return 异动记录分页列表
     */
    @Query(value = "SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "WHERE sc.status = :status AND sc.deleted = false " +
           "ORDER BY sc.createdAt DESC",
           countQuery = "SELECT COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.status = :status AND sc.deleted = false")
    Page<StudentStatusChange> findByStatus(@Param("status") ApprovalStatus status, Pageable pageable);

    /**
     * 根据异动类型查询异动记录
     *
     * @param type     异动类型
     * @param pageable 分页参数
     * @return 异动记录分页列表
     */
    @Query(value = "SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "WHERE sc.type = :type AND sc.deleted = false " +
           "ORDER BY sc.createdAt DESC",
           countQuery = "SELECT COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.type = :type AND sc.deleted = false")
    Page<StudentStatusChange> findByType(@Param("type") ChangeType type, Pageable pageable);

    /**
     * 根据当前审批人ID查询待审批记录
     *
     * @param approverId 审批人ID
     * @param pageable   分页参数
     * @return 异动记录分页列表
     */
    @Query(value = "SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "WHERE sc.currentApproverId = :approverId AND sc.status = 'PENDING' AND sc.deleted = false " +
           "ORDER BY sc.createdAt ASC",
           countQuery = "SELECT COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.currentApproverId = :approverId AND sc.status = 'PENDING' AND sc.deleted = false")
    Page<StudentStatusChange> findPendingByApproverId(@Param("approverId") Long approverId, Pageable pageable);

    /**
     * 根据审批级别查询待审批记录
     *
     * @param approvalLevel 审批级别
     * @param pageable      分页参数
     * @return 异动记录分页列表
     */
    @Query(value = "SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "WHERE sc.approvalLevel = :approvalLevel AND sc.status = 'PENDING' AND sc.deleted = false " +
           "ORDER BY sc.createdAt ASC",
           countQuery = "SELECT COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.approvalLevel = :approvalLevel AND sc.status = 'PENDING' AND sc.deleted = false")
    Page<StudentStatusChange> findPendingByApprovalLevel(@Param("approvalLevel") Integer approvalLevel, Pageable pageable);

    /**
     * 查询学生是否有进行中的异动申请
     *
     * @param studentId 学生ID
     * @return 是否存在进行中的申请
     */
    @Query("SELECT COUNT(sc) > 0 FROM StudentStatusChange sc " +
           "WHERE sc.student.id = :studentId AND sc.status = 'PENDING' AND sc.deleted = false")
    boolean existsPendingByStudentId(@Param("studentId") Long studentId);

    /**
     * 根据ID和学生ID查询异动记录（包含审批历史）
     *
     * @param id        异动ID
     * @param studentId 学生ID
     * @return 异动记录
     */
    @Query("SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "LEFT JOIN FETCH sc.approvals a " +
           "LEFT JOIN FETCH a.approver " +
           "WHERE sc.id = :id AND sc.student.id = :studentId AND sc.deleted = false")
    Optional<StudentStatusChange> findByIdAndStudentId(@Param("id") Long id, @Param("studentId") Long studentId);

    /**
     * 根据ID查询异动记录（带关联数据和审批历史）
     *
     * @param id 异动ID
     * @return 异动记录
     */
    @Query("SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "LEFT JOIN FETCH sc.approvals a " +
           "LEFT JOIN FETCH a.approver " +
           "WHERE sc.id = :id AND sc.deleted = false")
    Optional<StudentStatusChange> findByIdWithDetails(@Param("id") Long id);

    /**
     * 多条件查询异动记录
     *
     * @param type      异动类型（可选）
     * @param status    审批状态（可选）
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @param pageable  分页参数
     * @return 异动记录分页列表
     */
    @Query("SELECT sc FROM StudentStatusChange sc " +
           "WHERE sc.deleted = false " +
           "AND (:type IS NULL OR sc.type = :type) " +
           "AND (:status IS NULL OR sc.status = :status) " +
           "AND (:startDate IS NULL OR sc.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR sc.createdAt <= :endDate) " +
           "ORDER BY sc.createdAt DESC")
    Page<StudentStatusChange> findByMultipleConditions(
            @Param("type") ChangeType type,
            @Param("status") ApprovalStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * 统计各类异动的数量
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 统计结果
     */
    @Query("SELECT sc.type, COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.deleted = false " +
           "AND (:startDate IS NULL OR sc.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR sc.createdAt <= :endDate) " +
           "GROUP BY sc.type")
    List<Object[]> countByType(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计各审批状态的数量
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 统计结果
     */
    @Query("SELECT sc.status, COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.deleted = false " +
           "AND (:startDate IS NULL OR sc.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR sc.createdAt <= :endDate) " +
           "GROUP BY sc.status")
    List<Object[]> countByStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计总申请数
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 总数
     */
    @Query("SELECT COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.deleted = false " +
           "AND (:startDate IS NULL OR sc.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR sc.createdAt <= :endDate)")
    Long countTotal(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 按月份统计异动数量
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果 [月份, 数量]
     */
    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count " +
                   "FROM student_status_change " +
                   "WHERE deleted = 0 " +
                   "AND (:startDate IS NULL OR created_at >= :startDate) " +
                   "AND (:endDate IS NULL OR created_at <= :endDate) " +
                   "GROUP BY DATE_FORMAT(created_at, '%Y-%m') " +
                   "ORDER BY month", nativeQuery = true)
    List<Object[]> countByMonth(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计转专业流向
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计结果 [专业名称, 数量]
     */
    @Query("SELECT m.name, COUNT(sc) FROM StudentStatusChange sc " +
           "JOIN Major m ON sc.targetMajorId = m.id " +
           "WHERE sc.type = 'MAJOR_TRANSFER' AND sc.deleted = false " +
           "AND (:startDate IS NULL OR sc.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR sc.createdAt <= :endDate) " +
           "GROUP BY m.name " +
           "ORDER BY COUNT(sc) DESC")
    List<Object[]> countTransferByMajor(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 查询超时的异动申请
     *
     * @param pageable 分页参数
     * @return 超时申请列表
     */
    @Query(value = "SELECT DISTINCT sc FROM StudentStatusChange sc " +
           "LEFT JOIN FETCH sc.student s " +
           "LEFT JOIN FETCH s.major " +
           "WHERE sc.isOverdue = true AND sc.deleted = false " +
           "ORDER BY sc.deadline ASC",
           countQuery = "SELECT COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.isOverdue = true AND sc.deleted = false")
    Page<StudentStatusChange> findOverdueApplications(Pageable pageable);

    /**
     * 统计超时申请数量
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 超时数量
     */
    @Query("SELECT COUNT(sc) FROM StudentStatusChange sc " +
           "WHERE sc.isOverdue = true AND sc.deleted = false " +
           "AND (:startDate IS NULL OR sc.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR sc.createdAt <= :endDate)")
    Long countOverdue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 计算平均审批时长（天）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 平均审批天数
     */
    @Query(value = "SELECT AVG(DATEDIFF(updated_at, created_at)) " +
                   "FROM student_status_change " +
                   "WHERE status IN ('APPROVED', 'REJECTED') AND deleted = 0 " +
                   "AND (:startDate IS NULL OR created_at >= :startDate) " +
                   "AND (:endDate IS NULL OR created_at <= :endDate)", nativeQuery = true)
    Double calculateAverageApprovalDays(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

