package com.university.academic.repository;

import com.university.academic.entity.Scholarship;
import com.university.academic.entity.ScholarshipLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 奖学金Repository接口
 */
@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {
    
    /**
     * 查询启用的奖学金
     *
     * @param active 是否启用
     * @return 奖学金列表
     */
    List<Scholarship> findByActive(Boolean active);
    
    /**
     * 按等级查询启用的奖学金
     *
     * @param level 奖学金等级
     * @param active 是否启用
     * @return 奖学金列表
     */
    List<Scholarship> findByLevelAndActive(ScholarshipLevel level, Boolean active);
    
    /**
     * 按名称查询奖学金
     *
     * @param name 奖学金名称
     * @return 奖学金
     */
    Scholarship findByName(String name);
}

