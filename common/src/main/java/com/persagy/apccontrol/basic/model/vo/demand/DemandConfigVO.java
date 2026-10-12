package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.structure.common.parser.PropertyDescriptorParser;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.DemandConfigValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.Set;


@Data
@Schema(description = "需求管理-配置VO")
@FieldNameConstants
public class DemandConfigVO<T extends BaseDemandConfig> implements PropertyDescriptorParser {

    @Schema(description = "实例类型")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "实例id")
    private String instanceId;

    @Schema(description = "实例标签")
    private DemandInstanceTagVO instanceTag;

    @Schema(description = "自身配置")
    private DemandConfigValueObject<T> config;

    @Schema(description = "上级配置")
    private DemandConfigValueObject<T> parentConfig;

    @Schema(description = "最终配置")
    private DemandConfigValueObject<T> finalConfig;

    @Schema(description = "集成配置")
    private Set<DemandInstanceValueObject> inheritInstances;

}
