package com.persagy.apccontrol.basic.model.vo.ventilation;

import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VentilationModelViewVO {

    @Schema(description = "空间状态列表")
    private List<VentilationSpaceStatusVO> spaces;

    @Schema(description = "设备状态列表")
    private List<VentilationEquipmentStatusVO> equipments;

    @Schema(description = "设备状态统计")
    private Map<String, KitChenRangeHoodEquipStatisticsVo> equipStatisticsMap;


}
