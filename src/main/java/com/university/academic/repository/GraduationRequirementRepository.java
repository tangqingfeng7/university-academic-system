package com.university.academic.repository;

import com.university.academic.entity.GraduationRequirement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 毕业要求数据访问接口
 * 提供毕业要求数据的CRUD操作
 *
 * @author Academic System Team
 */
@Repository
public interface GraduationRequirementRepository extends JpaRepository<GraduationRequirement, Long> {

    /**
     * 根据专业ID和入学年份查询毕业要求
     *
     * @param majorId        专业ID
     * @param enrollmentYear 入学年份
     * @return 毕业要求
     */
    Optional<GraduationRequirement> findByMajorIdAndEnrollmentYear(Long majorId, Integer enrollmentYear);

    /**
     * 根据专业ID和入学年份查询毕业要求 - 预加载专业和院系信息
     *
     * @param majorId        专业ID
     * @param enrollmentYear 入学年份
     * @return 毕业要求
     */
    @EntityGraph(attributePaths = {"major", "major.department"})
    @Query("SELECT gr FROM GraduationRequirement gr " +
           "WHERE gr.major.id = :majorId AND gr.enrollmentYear = :enrollmentYear")
    Optional<GraduationRequirement> findByMajorIdAndEnrollmentYearWithDetails(@Param("majorId") Long majorId,
                                                                               @Param("enrollmentYear") Integer enrollmentYear);

    /**
     * 根据专业ID查询所有年份的毕业要求
     *
     * @param majorId 专业ID
     * @return 毕业要求列表
     */
    List<GraduationRequirement> findByMajorId(Long majorId);

    /**
     * 根据专业ID查询所有年份的毕业要求 - 预加载专业信息
     *
     * @param majorId 专业ID
     * @return 毕业要求列表
     */
    @EntityGraph(attributePaths = {"major", "major.department"})
    @Query("SELECT gr FROM GraduationRequirement gr WHERE gr.major.id = :majorId")
    List<GraduationRequirement> findByMajorIdWithDetails(@Param("majorId") Long majorId);

    /**
     * 检查专业和入学年份是否已存在毕业要求
     *
     * @param majorId        专业ID
     * @param enrollmentYear 入学年份
     * @return true-存在，false-不存在
     */
    boolean existsByMajorIdAndEnrollmentYear(Long majorId, Integer enrollmentYear);

    /**
     * 根据入学年份查询所有毕业要求 - 预加载专业信息
     *
     * @param enrollmentYear 入学年份
     * @return 毕业要求列表
     */
    @EntityGraph(attributePaths = {"major", "major.department"})
    @Query("SELECT gr FROM GraduationRequirement gr WHERE gr.enrollmentYear = :enrollmentYear")
    List<GraduationRequirement> findByEnrollmentYearWithDetails(@Param("enrollmentYear") Integer enrollmentYear);

    /**
     * 查询所有毕业要求 - 预加载专业信息
     *
     * @return 毕业要求列表
     */
    @EntityGraph(attributePaths = {"major", "major.department"})
    @Query("SELECT gr FROM GraduationRequirement gr ORDER BY gr.major.id, gr.enrollmentYear DESC")
    List<GraduationRequirement> findAllWithDetails();
}

