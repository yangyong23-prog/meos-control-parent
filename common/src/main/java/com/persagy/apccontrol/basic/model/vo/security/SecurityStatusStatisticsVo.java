package com.persagy.apccontrol.basic.model.vo.security;


import com.persagy.apccontrol.basic.model.enumtype.security.SecurityEquipmentStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityStatusStatisticsVo {

    @Schema(description = "设备状态")
    private SecurityEquipmentStatusEnum objectCode;

    @Schema(description = "z状态名")
    private String objectName;

    @Schema(description = "对应设备状态的数量统计")
    private int count;

}
