package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

/**
 * 需求管理-自定义配置实例查询参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "需求管理-自定义配置实例查询参数")
public class DemandCustomizedConfigInstanceQueryParam extends DemandTemplateSceneParam {

    @Schema(description = "实例类型 -> 查询字段, 为空时查询默认字段")
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns;

}
