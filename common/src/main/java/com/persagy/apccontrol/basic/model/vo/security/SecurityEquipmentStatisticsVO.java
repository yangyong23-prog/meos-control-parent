package com.persagy.apccontrol.basic.model.vo.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEquipmentStatisticsVO implements Serializable {

    private static final long serialVersionUID = 3035506456206172288L;

    @Schema(description = "设备编码")
    private String objectCode;

    @Schema(description = "设备名")
    private String objectName;

    @Schema(description = "设备总数")
    private int count;

    @Schema(description = "设备状态统计列表")
    private List<SecurityStatusStatisticsVo> statusList = new ArrayList<>();

}
