package com.university.academic.dto;

import com.university.academic.entity.InvigilatorType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加监考请求
 *
 * @author Academic System Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddInvigilatorRequest {

    /**
     * 考场ID（由控制器从路径参数设置，不需要前端传递）
     */
    private Long examRoomId;

    /**
     * 教师ID
     */
    @NotNull(message = "教师ID不能为空")
    private Long teacherId;

    /**
     * 监考类型
     */
    @NotNull(message = "监考类型不能为空")
    private InvigilatorType type;
}

