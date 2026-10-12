package com.persagy.apccontrol.basic.structure.demand.gateway.param;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 需求配置根据实例查询参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigByInstanceQueryParam {

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
     * 场景ID, 为空时查询所有
     */
    private Long sceneId;

    /**
     * 需求实例
     */
    @NotNull(message = "{key_f9445c3d6d0a40948665c083fd6fd053}")
    private DemandInstanceValueObject instance;

}
