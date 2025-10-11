package com.university.academic.service;

import com.university.academic.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 奖学金服务接口
 */
public interface ScholarshipService {
    
    /**
     * 创建奖学金类型
     *
     * @param request 创建请求
     * @return 奖学金DTO
     */
    ScholarshipDTO createScholarship(CreateScholarshipRequest request);
    
    /**
     * 更新奖学金
     *
     * @param id 奖学金ID
     * @param request 更新请求
     * @return 奖学金DTO
     */
    ScholarshipDTO updateScholarship(Long id, UpdateScholarshipRequest request);
    
    /**
     * 查询奖学金列表
     *
     * @param pageable 分页参数
     * @return 奖学金分页
     */
    Page<ScholarshipDTO> getScholarships(Pageable pageable);
    
    /**
     * 查询启用的奖学金列表
     *
     * @return 奖学金列表
     */
    List<ScholarshipDTO> getActiveScholarships();
    
    /**
     * 根据ID查询奖学金
     *
     * @param id 奖学金ID
     * @return 奖学金DTO
     */
    ScholarshipDTO getScholarshipById(Long id);
    
    /**
     * 开启申请
     *
     * @param id 奖学金ID
     * @param request 开启申请请求
     */
    void openApplication(Long id, OpenApplicationRequest request);
    
    /**
     * 启用/禁用奖学金
     *
     * @param id 奖学金ID
     * @param active 是否启用
     */
    void toggleActive(Long id, Boolean active);
    
    /**
     * 删除奖学金
     *
     * @param id 奖学金ID
     */
    void deleteScholarship(Long id);
}

