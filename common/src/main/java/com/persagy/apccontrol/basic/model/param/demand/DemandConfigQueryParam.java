package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "查询配置参数")
public class DemandConfigQueryParam extends BaseDemandTemplateParam {

    @Schema(description = "实例类型")
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "实例ID, 为空时查询所有")
    private Set<String> instanceIds;

    @Schema(description = "是否查询实例标签配置")
    private boolean queryInstanceTag = true;

    @Schema(description = "是否查询继承配置")
    private boolean queryInheritConfig = true;

    @Schema(description = "返回字段")
    private Set<String> withColumns;

}
