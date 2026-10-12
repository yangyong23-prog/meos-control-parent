package com.persagy.apccontrol.basic.model.param.demand;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandBlacklistTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 需求黑名单新增参数
 */
@Data
@Schema(title = "需求黑名单新增参数")
public class DemandBlacklistConfigAddParam {

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum demandTemplateType;

    @Schema(description = "配置数据")
    private Map<DemandBlacklistTypeEnum, JsonNode> configData;

    public Map<String, JsonNode> convert() {
        Map<String, JsonNode> stringConfigData = Maps.newHashMap();
        if (MapUtils.isEmpty(configData)) {
            return stringConfigData;
        }
        for (DemandBlacklistTypeEnum demandBlacklistTypeEnum : configData.keySet()) {
            stringConfigData.put(demandBlacklistTypeEnum.getValue(), configData.get(demandBlacklistTypeEnum));
        }
        return stringConfigData;
    }

}
