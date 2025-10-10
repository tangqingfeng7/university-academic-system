package com.university.academic.repository;

import com.university.academic.entity.Classroom;
import com.university.academic.entity.ClassroomStatus;
import com.university.academic.entity.ClassroomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 教室Repository接口
 */
@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    
    /**
     * 根据教室编号查询教室
     *
     * @param roomNo 教室编号
     * @return 教室信息
     */
    Optional<Classroom> findByRoomNoAndDeletedFalse(String roomNo);
    
    /**
     * 根据楼栋查询教室列表
     *
     * @param building 楼栋
     * @param pageable 分页参数
     * @return 教室列表
     */
    Page<Classroom> findByBuildingAndDeletedFalse(String building, Pageable pageable);
    
    /**
     * 根据教室类型查询教室列表
     *
     * @param type 教室类型
     * @param pageable 分页参数
     * @return 教室列表
     */
    Page<Classroom> findByTypeAndDeletedFalse(ClassroomType type, Pageable pageable);
    
    /**
     * 根据教室状态查询教室列表
     *
     * @param status 教室状态
     * @param pageable 分页参数
     * @return 教室列表
     */
    Page<Classroom> findByStatusAndDeletedFalse(ClassroomStatus status, Pageable pageable);
    
    /**
     * 查询所有未删除的教室
     *
     * @param pageable 分页参数
     * @return 教室列表
     */
    Page<Classroom> findByDeletedFalse(Pageable pageable);
    
    /**
     * 根据容量范围查询教室
     *
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 教室列表
     */
    @Query("SELECT c FROM Classroom c WHERE c.deleted = false " +
           "AND c.capacity >= :minCapacity AND c.capacity <= :maxCapacity " +
           "AND c.status = 'AVAILABLE'")
    List<Classroom> findAvailableByCapacityRange(@Param("minCapacity") Integer minCapacity,
                                                   @Param("maxCapacity") Integer maxCapacity);
    
    /**
     * 根据条件组合查询教室
     *
     * @param building 楼栋（可选）
     * @param type 教室类型（可选）
     * @param status 教室状态（可选）
     * @param minCapacity 最小容量（可选）
     * @param pageable 分页参数
     * @return 教室列表
     */
    @Query("SELECT c FROM Classroom c WHERE c.deleted = false " +
           "AND (:building IS NULL OR c.building = :building) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:minCapacity IS NULL OR c.capacity >= :minCapacity)")
    Page<Classroom> findByConditions(@Param("building") String building,
                                      @Param("type") ClassroomType type,
                                      @Param("status") ClassroomStatus status,
                                      @Param("minCapacity") Integer minCapacity,
                                      Pageable pageable);
    
    /**
     * 统计指定楼栋的教室数量
     *
     * @param building 楼栋
     * @return 教室数量
     */
    long countByBuildingAndDeletedFalse(String building);
    
    /**
     * 统计指定状态的教室数量
     *
     * @param status 教室状态
     * @return 教室数量
     */
    long countByStatusAndDeletedFalse(ClassroomStatus status);
    
    /**
     * 统计未删除/已删除的教室数量
     *
     * @param deleted 是否删除
     * @return 教室数量
     */
    long countByDeleted(Boolean deleted);
    
    /**
     * 检查教室编号是否已存在
     *
     * @param roomNo 教室编号
     * @return 是否存在
     */
    boolean existsByRoomNoAndDeletedFalse(String roomNo);
}

