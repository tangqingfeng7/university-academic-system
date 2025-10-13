package com.university.ems.repository;

import com.university.ems.entity.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 排课结果Repository
 * 
 * @author Academic System Team
 */
@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    
    /**
     * 根据方案ID查询所有排课结果
     */
    List<ScheduleItem> findBySolutionId(Long solutionId);
    
    /**
     * 根据课程开课ID查询排课结果
     */
    List<ScheduleItem> findByCourseOfferingId(Long courseOfferingId);
    
    /**
     * 根据方案ID和课程开课ID查询排课结果
     */
    @Query("SELECT si FROM ScheduleItem si WHERE si.solution.id = :solutionId AND si.courseOffering.id = :courseOfferingId")
    List<ScheduleItem> findBySolutionIdAndCourseOfferingId(
            @Param("solutionId") Long solutionId, 
            @Param("courseOfferingId") Long courseOfferingId);
    
    /**
     * 检查教室时间冲突
     */
    @Query("SELECT si FROM ScheduleItem si " +
           "WHERE si.solution.id = :solutionId " +
           "AND si.classroom.id = :classroomId " +
           "AND si.dayOfWeek = :dayOfWeek " +
           "AND si.startSlot <= :endSlot " +
           "AND si.endSlot >= :startSlot")
    List<ScheduleItem> findClassroomConflicts(
            @Param("solutionId") Long solutionId,
            @Param("classroomId") Long classroomId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startSlot") Integer startSlot,
            @Param("endSlot") Integer endSlot);
    
    /**
     * 检查教师时间冲突
     */
    @Query("SELECT si FROM ScheduleItem si " +
           "WHERE si.solution.id = :solutionId " +
           "AND si.courseOffering.teacher.id = :teacherId " +
           "AND si.dayOfWeek = :dayOfWeek " +
           "AND si.startSlot <= :endSlot " +
           "AND si.endSlot >= :startSlot")
    List<ScheduleItem> findTeacherConflicts(
            @Param("solutionId") Long solutionId,
            @Param("teacherId") Long teacherId,
            @Param("dayOfWeek") Integer dayOfWeek,
            @Param("startSlot") Integer startSlot,
            @Param("endSlot") Integer endSlot);
    
    /**
     * 根据方案ID删除所有排课结果
     */
    @Modifying
    @Query("DELETE FROM ScheduleItem si WHERE si.solution.id = :solutionId")
    void deleteBySolutionId(@Param("solutionId") Long solutionId);
    
    /**
     * 统计方案的排课数量
     */
    @Query("SELECT COUNT(si) FROM ScheduleItem si WHERE si.solution.id = :solutionId")
    long countBySolutionId(@Param("solutionId") Long solutionId);
}

