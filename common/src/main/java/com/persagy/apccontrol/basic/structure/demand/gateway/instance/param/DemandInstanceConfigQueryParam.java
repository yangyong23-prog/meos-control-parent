package com.persagy.apccontrol.basic.structure.demand.gateway.instance.param;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

/**
 * 需求实例查询参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandInstanceConfigQueryParam {

    /**
     * 查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     */
    private LocalDate date;

    /**
     * 模版类型
     */
    @NotNull(message = "{key_8356b8ff6af845ad99d6033c92e55c61}")
    private DemandTemplateTypeEnum templateType;

    /**
     * 实例类型
     */
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID, 为空时查询所有
     */
    private Set<String> instanceIds;

    /**
     * 过滤配置, 字段为空时跳过过滤, 多个字段之间关系为 [OR]
     */
    private DemandInstanceConfigValueObject filterConfig;

}
