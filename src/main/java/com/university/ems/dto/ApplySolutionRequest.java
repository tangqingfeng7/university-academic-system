package com.university.ems.dto;

import lombok.Data;

/**
 * 应用排课方案请求DTO
 * 
 * @author Academic System Team
 */
@Data
public class ApplySolutionRequest {

    /**
     * 是否强制应用（即使有冲突）
     */
    private Boolean force = false;

    /**
     * 是否覆盖现有排课
     */
    private Boolean overwrite = false;

    /**
     * 备注
     */
    private String notes;
}

