package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 需求管理-照明需求-照明回路空间分组VO
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求管理-照明需求-照明回路空间类型分组VO")
public class DemandLightingSpaceFuncTypeVO {

    @Schema(description = "空间功能类型编码")
    private String spaceFuncType;

    @Schema(description = "空间功能类型名称")
    private String spaceFuncTypeName;

    @Schema(description = "是否已被选中")
    private Boolean select;

    @Schema(description = "照明回路ID集合")
    private Set<String> circuitIds;

    @JsonIgnore
    @Schema(description = "照明回路集合")
    private List<DemandLightingCircuitSettingVO> circuitSettings;

    @Schema(description = "照明回路照明功能类型")
    private List<DemandLightingFuncTypeSettingVO> circuitFuncTypes;

    public Set<String> getCircuitIds() {
        if (CollectionUtil.isEmpty(circuitIds) && CollectionUtil.isNotEmpty(circuitSettings)) {
            circuitIds = circuitSettings.stream().map(DemandLightingCircuitSettingVO::getCircuitId).collect(Collectors.toSet());
        }
        return circuitIds;
    }

}
