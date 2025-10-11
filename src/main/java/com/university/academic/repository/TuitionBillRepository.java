package com.university.academic.repository;

import com.university.academic.entity.tuition.BillStatus;
import com.university.academic.entity.tuition.TuitionBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 学费账单数据访问接口
 *
 * @author Academic System Team
 */
@Repository
public interface TuitionBillRepository extends JpaRepository<TuitionBill, Long> {

    /**
     * 查询学生的所有账单
     */
    @Query("SELECT tb FROM TuitionBill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tb.student.id = :studentId " +
           "ORDER BY tb.academicYear DESC")
    List<TuitionBill> findByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询学生指定学年的账单
     */
    @Query("SELECT tb FROM TuitionBill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tb.student.id = :studentId " +
           "AND tb.academicYear = :academicYear")
    Optional<TuitionBill> findByStudentIdAndAcademicYear(
        @Param("studentId") Long studentId,
        @Param("academicYear") String academicYear
    );

    /**
     * 检查是否已存在账单
     */
    @Query("SELECT COUNT(tb) > 0 FROM TuitionBill tb " +
           "WHERE tb.student.id = :studentId " +
           "AND tb.academicYear = :academicYear")
    boolean existsByStudentIdAndAcademicYear(
        @Param("studentId") Long studentId,
        @Param("academicYear") String academicYear
    );

    /**
     * 查询指定学年的所有账单
     */
    @Query("SELECT tb FROM TuitionBill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tb.academicYear = :academicYear " +
           "ORDER BY tb.student.studentNo")
    List<TuitionBill> findByAcademicYear(@Param("academicYear") String academicYear);

    /**
     * 查询指定状态的账单
     */
    @Query(value = "SELECT tb FROM TuitionBill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tb.status = :status " +
           "ORDER BY tb.dueDate, tb.student.studentNo",
           countQuery = "SELECT COUNT(tb) FROM TuitionBill tb WHERE tb.status = :status")
    Page<TuitionBill> findByStatus(@Param("status") BillStatus status, Pageable pageable);

    /**
     * 查询逾期未缴费的账单
     */
    @Query("SELECT tb FROM TuitionBill tb " +
           "LEFT JOIN FETCH tb.student " +
           "WHERE tb.status IN ('UNPAID', 'PARTIAL', 'OVERDUE') " +
           "AND tb.dueDate < :currentDate " +
           "ORDER BY tb.dueDate")
    List<TuitionBill> findOverdueBills(@Param("currentDate") LocalDate currentDate);

    /**
     * 统计指定学年的缴费情况
     */
    @Query("SELECT tb.status, COUNT(tb), SUM(tb.totalAmount), SUM(tb.paidAmount) " +
           "FROM TuitionBill tb " +
           "WHERE tb.academicYear = :academicYear " +
           "GROUP BY tb.status")
    List<Object[]> getPaymentStatisticsByYear(@Param("academicYear") String academicYear);

    /**
     * 统计指定专业的缴费情况
     */
    @Query("SELECT tb.status, COUNT(tb), SUM(tb.totalAmount), SUM(tb.paidAmount) " +
           "FROM TuitionBill tb " +
           "JOIN tb.student s " +
           "WHERE tb.academicYear = :academicYear " +
           "AND s.major.id = :majorId " +
           "GROUP BY tb.status")
    List<Object[]> getPaymentStatisticsByMajor(
        @Param("academicYear") String academicYear,
        @Param("majorId") Long majorId
    );

    /**
     * 统计指定院系的缴费情况
     */
    @Query("SELECT tb.status, COUNT(tb), SUM(tb.totalAmount), SUM(tb.paidAmount) " +
           "FROM TuitionBill tb " +
           "JOIN tb.student s " +
           "JOIN s.major m " +
           "WHERE tb.academicYear = :academicYear " +
           "AND m.department.id = :departmentId " +
           "GROUP BY tb.status")
    List<Object[]> getPaymentStatisticsByDepartment(
        @Param("academicYear") String academicYear,
        @Param("departmentId") Long departmentId
    );

    /**
     * 按院系分组统计缴费情况
     */
    @Query("SELECT m.department.id, m.department.name, COUNT(DISTINCT tb.student), " +
           "SUM(tb.totalAmount), SUM(tb.paidAmount) " +
           "FROM TuitionBill tb " +
           "JOIN tb.student s " +
           "JOIN s.major m " +
           "WHERE tb.academicYear = :academicYear " +
           "GROUP BY m.department.id, m.department.name " +
           "ORDER BY m.department.name")
    List<Object[]> getStatisticsByDepartment(@Param("academicYear") String academicYear);

    /**
     * 按专业分组统计缴费情况
     */
    @Query("SELECT s.major.id, s.major.name, COUNT(DISTINCT tb.student), " +
           "SUM(tb.totalAmount), SUM(tb.paidAmount) " +
           "FROM TuitionBill tb " +
           "JOIN tb.student s " +
           "WHERE tb.academicYear = :academicYear " +
           "GROUP BY s.major.id, s.major.name " +
           "ORDER BY s.major.name")
    List<Object[]> getStatisticsByMajor(@Param("academicYear") String academicYear);

    /**
     * 统计指定学年各项费用的总和
     */
    @Query("SELECT SUM(tb.tuitionFee), SUM(tb.accommodationFee), " +
           "SUM(tb.textbookFee), SUM(tb.otherFees) " +
           "FROM TuitionBill tb " +
           "WHERE tb.academicYear = :academicYear " +
           "AND tb.status != 'UNPAID'")
    Object[] getFeeBreakdownByAcademicYear(@Param("academicYear") String academicYear);

    /**
     * 多条件查询账单
     */
    @Query(value = "SELECT tb FROM TuitionBill tb " +
           "LEFT JOIN FETCH tb.student s " +
           "WHERE (:academicYear IS NULL OR tb.academicYear = :academicYear) " +
           "AND (:status IS NULL OR tb.status = :status) " +
           "AND (:studentNo IS NULL OR s.studentNo LIKE CONCAT('%', :studentNo, '%')) " +
           "AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%')) " +
           "ORDER BY tb.createdAt DESC",
           countQuery = "SELECT COUNT(DISTINCT tb) FROM TuitionBill tb " +
           "LEFT JOIN tb.student s " +
           "WHERE (:academicYear IS NULL OR tb.academicYear = :academicYear) " +
           "AND (:status IS NULL OR tb.status = :status) " +
           "AND (:studentNo IS NULL OR s.studentNo LIKE CONCAT('%', :studentNo, '%')) " +
           "AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))")
    Page<TuitionBill> searchBills(
        @Param("academicYear") String academicYear,
        @Param("status") BillStatus status,
        @Param("studentNo") String studentNo,
        @Param("studentName") String studentName,
        Pageable pageable
    );
}

