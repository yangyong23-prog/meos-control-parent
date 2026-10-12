package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitGroupSettingDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.circuit.DtpRwdCircuitFuncTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-功能类型配置VO
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Getter
@Setter
@ToString
@Schema(title = "需求管理-照明需求-功能类型配置VO")
public class DemandLightingFuncTypeSettingVO {

    @Schema(description = "照明回路功能类型")
    private DtpRwdCircuitFuncTypeEnum funcType;

    @Schema(description = "照明回路功能类型名称")
    private String funcTypeName;

    @Schema(description = "类型配置模式")
    private DemandLightingTypeSettingModeEnum settingMode;

    @Schema(description = "场景配置模式")
    private List<DemandLightingSceneModel> sceneModes;

    @Schema(description = "回路状态统计")
    private DemandLightingCircuitSummaryVO summary;

    //@JsonIgnore
    @Schema(description = "照明回路灯具类型配置")
    private List<DemandLightingLampFuncTypeSettingVO> lampFuncTypeSettings;

    @Schema(description = "照明回路编组配置")
    private List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings;


    /**********************  扩展字段  *********************/

    @Schema(description = "照明回路ID集合")
    private Set<String> circuitIds;

    @Schema(description = "照明回路集合")
    private List<DemandLightingCircuitSettingVO> circuitSettings;

    public String getFuncTypeName() {
        return funcType == null ? null : funcType.getReasonPhrase();
    }

    public Set<String> getCircuitIds() {
        if (CollectionUtil.isEmpty(circuitIds) && CollectionUtil.isNotEmpty(circuitSettings)) {
            circuitIds = circuitSettings.stream().map(DemandLightingCircuitSettingVO::getCircuitId).collect(Collectors.toSet());
        }
        return circuitIds;
    }

}
