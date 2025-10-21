package com.university.academic.repository;

import com.university.academic.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 选课记录数据访问接口
 * 提供选课记录数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {

    /**
     * 根据ID查询选课记录（带所有关联实体）
     *
     * @param id 选课记录ID
     * @return 选课记录对象
     */
    @Query("SELECT DISTINCT cs FROM CourseSelection cs " +
            "LEFT JOIN FETCH cs.student s " +
            "LEFT JOIN FETCH s.major m " +
            "LEFT JOIN FETCH m.department " +
            "LEFT JOIN FETCH cs.offering co " +
            "LEFT JOIN FETCH co.semester " +
            "LEFT JOIN FETCH co.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH co.teacher t " +
            "LEFT JOIN FETCH t.department " +
            "WHERE cs.id = :id")
    @NonNull
    Optional<CourseSelection> findById(@NonNull @Param("id") Long id);

    /**
     * 查询学生的所有选课记录
     *
     * @param studentId 学生ID
     * @return 选课记录列表
     */
    List<CourseSelection> findByStudentId(Long studentId);

    /**
     * 查询学生在指定学期的选课记录（带关联实体）
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 选课记录列表
     */
    @Query("SELECT DISTINCT cs FROM CourseSelection cs " +
            "LEFT JOIN FETCH cs.student s " +
            "LEFT JOIN FETCH s.major m " +
            "LEFT JOIN FETCH m.department " +
            "LEFT JOIN FETCH cs.offering co " +
            "LEFT JOIN FETCH co.semester " +
            "LEFT JOIN FETCH co.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH co.teacher t " +
            "LEFT JOIN FETCH t.department " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.offering.semester.id = :semesterId")
    List<CourseSelection> findByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    /**
     * 查询学生在指定学期的已选课程（未退课，带关联实体）
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 选课记录列表
     */
    @Query("SELECT DISTINCT cs FROM CourseSelection cs " +
            "LEFT JOIN FETCH cs.student s " +
            "LEFT JOIN FETCH s.major m " +
            "LEFT JOIN FETCH m.department " +
            "LEFT JOIN FETCH cs.offering co " +
            "LEFT JOIN FETCH co.semester " +
            "LEFT JOIN FETCH co.course c " +
            "LEFT JOIN FETCH c.department " +
            "LEFT JOIN FETCH co.teacher t " +
            "LEFT JOIN FETCH t.department " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.offering.semester.id = :semesterId " +
            "AND cs.status = 'SELECTED'")
    List<CourseSelection> findActiveByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    /**
     * 查询学生是否已选某个开课计划
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return 选课记录
     */
    Optional<CourseSelection> findByStudentIdAndOfferingId(Long studentId, Long offeringId);

    /**
     * 检查学生是否已选某个开课计划（未退课）
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @return true-已选，false-未选
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END " +
            "FROM CourseSelection cs " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.offering.id = :offeringId " +
            "AND cs.status = 'SELECTED'")
    boolean existsActiveSelection(@Param("studentId") Long studentId, 
                                  @Param("offeringId") Long offeringId);

    /**
     * 检查学生是否已选某个开课计划（指定状态）
     *
     * @param studentId  学生ID
     * @param offeringId 开课计划ID
     * @param status     选课状态
     * @return true-已选，false-未选
     */
    @Query("SELECT CASE WHEN COUNT(cs) > 0 THEN true ELSE false END " +
            "FROM CourseSelection cs " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.offering.id = :offeringId " +
            "AND cs.status = :status")
    boolean existsByStudentIdAndOfferingIdAndStatus(@Param("studentId") Long studentId,
                                                     @Param("offeringId") Long offeringId,
                                                     @Param("status") CourseSelection.SelectionStatus status);

    /**
     * 查询开课计划的所有选课记录（预加载学生信息）
     *
     * @param offeringId 开课计划ID
     * @return 选课记录列表
     */
    @Query("SELECT DISTINCT cs FROM CourseSelection cs " +
           "LEFT JOIN FETCH cs.student s " +
           "LEFT JOIN FETCH s.user " +
           "LEFT JOIN FETCH s.major m " +
           "LEFT JOIN FETCH m.department " +
           "LEFT JOIN FETCH cs.offering " +
           "WHERE cs.offering.id = :offeringId")
    List<CourseSelection> findByOfferingId(@Param("offeringId") Long offeringId);

    /**
     * 统计学生在指定学期的总学分（已选课程）
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 总学分
     */
    @Query("SELECT SUM(cs.offering.course.credits) " +
            "FROM CourseSelection cs " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.offering.semester.id = :semesterId " +
            "AND cs.status = 'SELECTED'")
    Integer sumCreditsByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    /**
     * 统计学生在指定学期的选课数量（已选课程）
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 选课数量
     */
    @Query("SELECT COUNT(cs) " +
            "FROM CourseSelection cs " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.offering.semester.id = :semesterId " +
            "AND cs.status = 'SELECTED'")
    long countActiveByStudentAndSemester(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    /**
     * 根据学号和开课计划ID查询选课记录
     *
     * @param studentNo  学号
     * @param offeringId 开课计划ID
     * @return 选课记录
     */
    @Query("SELECT cs FROM CourseSelection cs " +
            "WHERE cs.student.studentNo = :studentNo " +
            "AND cs.offering.id = :offeringId")
    Optional<CourseSelection> findByStudentNoAndOfferingId(
            @Param("studentNo") String studentNo,
            @Param("offeringId") Long offeringId);

    /**
     * 统计学生在指定学期的选课数量（所有状态）
     *
     * @param studentId  学生ID
     * @param semesterId 学期ID
     * @return 选课数量
     */
    @Query("SELECT COUNT(cs) " +
            "FROM CourseSelection cs " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.offering.semester.id = :semesterId")
    long countByStudentIdAndSemesterId(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId);

    /**
     * 查询学生指定状态的选课记录（带关联实体，用于学分计算）
     *
     * @param studentId 学生ID
     * @param status    选课状态
     * @return 选课记录列表
     */
    @Query("SELECT DISTINCT cs FROM CourseSelection cs " +
            "LEFT JOIN FETCH cs.student s " +
            "LEFT JOIN FETCH s.major " +
            "LEFT JOIN FETCH cs.offering co " +
            "LEFT JOIN FETCH co.course c " +
            "LEFT JOIN FETCH co.semester " +
            "LEFT JOIN FETCH cs.grade g " +
            "WHERE cs.student.id = :studentId " +
            "AND cs.status = :status")
    List<CourseSelection> findByStudentIdAndStatus(
            @Param("studentId") Long studentId,
            @Param("status") CourseSelection.SelectionStatus status);

    /**
     * 统计开课计划的选课人数（已选课程）
     *
     * @param offeringId 开课计划ID
     * @return 选课人数
     */
    @Query("SELECT COUNT(cs) " +
            "FROM CourseSelection cs " +
            "WHERE cs.offering.id = :offeringId " +
            "AND cs.status = 'SELECTED'")
    long countActiveByOfferingId(@Param("offeringId") Long offeringId);
}

