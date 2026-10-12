package com.persagy.apccontrol.basic.model.vo.lighting;


import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandLightingCircuitSettingVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Schema(title = "需求管理-照明需求-空间反馈条件配置VO")
@NoArgsConstructor
@AllArgsConstructor
public class LightingSpaceFeedbackAdjustSettingVO {

    @Schema(description = "当前配置ID值")
    private Long configId;

    @Schema(description = "服务的回路列表")
    private List<DemandLightingCircuitSettingVO> lightingCircuits;


}
