package com.persagy.apccontrol.basic.model.vo.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 安防模型视角
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 19:37
 * @since v5.240801.0
 */
@Data
public class SecurityModelViewVO {

    @Schema(description = "空间状态列表")
    private List<SecuritySpaceStatusVO> spaces;

    @Schema(description = "设备状态列表")
    private List<SecurityEquipmentStatusVO> equipments;

}
