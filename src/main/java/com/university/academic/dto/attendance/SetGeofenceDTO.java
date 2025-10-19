package com.university.academic.dto.attendance;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 设置地理围栏请求DTO
 *
 * @author Academic System Team
 */
@Data
public class SetGeofenceDTO {

    /**
     * 考勤记录ID
     */
    @NotNull(message = "考勤记录ID不能为空")
    private Long recordId;

    /**
     * 纬度
     */
    @NotNull(message = "纬度不能为空")
    @Min(value = -90, message = "纬度必须在-90到90之间")
    @Max(value = 90, message = "纬度必须在-90到90之间")
    private Double latitude;

    /**
     * 经度
     */
    @NotNull(message = "经度不能为空")
    @Min(value = -180, message = "经度必须在-180到180之间")
    @Max(value = 180, message = "经度必须在-180到180之间")
    private Double longitude;

    /**
     * 围栏半径（米）
     */
    @Min(value = 10, message = "半径不能小于10米")
    @Max(value = 1000, message = "半径不能大于1000米")
    private Integer radius;
}

