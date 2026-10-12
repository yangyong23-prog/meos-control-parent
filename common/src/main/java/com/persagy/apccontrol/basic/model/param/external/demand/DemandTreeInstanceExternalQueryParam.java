package com.persagy.apccontrol.basic.model.param.external.demand;

import com.persagy.apccontrol.basic.model.param.demand.BaseDemandTemplateParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

/**
 * 需求管理-实例对象树对外查询参数
 *
 * @author JiYinchuan
 * @since 5.241202.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "需求管理-实例对象树对外查询参数")
public class DemandTreeInstanceExternalQueryParam extends BaseDemandTemplateParam {

    @Schema(description = "过滤服务系统ID, 为空时不过滤")
    private Set<String> serviceSystemIds;

    @Schema(description = "实例类型 -> 查询字段, 为空时查询默认字段")
    private Map<DemandInstanceTypeEnum, Set<String>> instanceTypeAndWithColumns;

}
