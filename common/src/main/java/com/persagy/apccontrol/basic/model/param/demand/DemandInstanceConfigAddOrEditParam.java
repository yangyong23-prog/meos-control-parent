package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 需求对象配置新增参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@Schema(title = "需求空间黑名单参数")
public class DemandInstanceConfigAddOrEditParam {

    @Schema(description = "模版类型")
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "实例类型")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "实例ID, 为空时操作数据库中指定类型下所有实例")
    private Set<String> instanceIds;

    @Schema(description = "配置内容")
    private DemandInstanceConfigValueObject config;

}
