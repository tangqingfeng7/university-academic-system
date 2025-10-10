package com.university.academic.repository;

import com.university.academic.entity.ApprovalConfiguration;
import com.university.academic.entity.ChangeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 审批配置Repository接口
 *
 * @author Academic System Team
 */
@Repository
public interface ApprovalConfigurationRepository extends JpaRepository<ApprovalConfiguration, Long> {

    /**
     * 根据异动类型查询配置
     *
     * @param changeType 异动类型
     * @return 审批配置
     */
    @Query("SELECT ac FROM ApprovalConfiguration ac WHERE ac.changeType = :changeType AND ac.enabled = true")
    Optional<ApprovalConfiguration> findByChangeType(@Param("changeType") ChangeType changeType);

    /**
     * 检查异动类型是否已配置
     *
     * @param changeType 异动类型
     * @return 是否已配置
     */
    boolean existsByChangeType(ChangeType changeType);
}

