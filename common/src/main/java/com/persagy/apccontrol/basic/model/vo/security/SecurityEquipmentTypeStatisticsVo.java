package com.persagy.apccontrol.basic.model.vo.security;

import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 安防设备类型统计
 *
 * @author CaiZhongHua
 * @since v5.240801.0
 */
@Data
public class SecurityEquipmentTypeStatisticsVo {

    @Schema(description = "设备类型统计总数")
    private int count = 0;

    @Schema(description = "设备类型")
    private SecurityEquipmentTypeEnum objectCode;

    @Schema(description = "设备类型名")
    private String objectName;

    @Schema(description = "设备状态统计列表")
    private List<SecurityEquipmentStatisticsVO> list = new ArrayList<>();


}
