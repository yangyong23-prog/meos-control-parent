package com.persagy.apccontrol.basic.structure.demand.gateway.param;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求自定义配置实例查询参数
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandCustomizedInstanceQueryParam {

    /**
     * 查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     */
    private LocalDate date;

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID, 存在时必填
     */
    private Long sceneId;

    /**
     * 过滤配置, 字段为空时跳过过滤, 多个字段之间关系为 [OR]
     */
    private DemandInstanceConfigValueObject filterConfig;

}
