package com.university.academic.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 扫码签到请求DTO
 *
 * @author Academic System Team
 */
@Data
public class QRCodeCheckinDTO {

    /**
     * 二维码令牌
     */
    @NotBlank(message = "二维码令牌不能为空")
    private String qrToken;
}

