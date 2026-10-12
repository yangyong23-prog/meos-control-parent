package com.persagy.apccontrol.basic.structure.demand.gateway.param;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandConfigCancelReferenceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 需求配置取消引用参数
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class DemandConfigReferenceCancelParam {

    /**
     * 模版ID
     */
    @NotNull(message = "{key_02bebf76336942da9503b791f451570d}")
    private Long templateId;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 需求实例
     */
    @NotNull(message = "{key_f9445c3d6d0a40948665c083fd6fd053}")
    private DemandInstanceValueObject instance;

    /**
     * 取消引用类型不能
     */
    @NotNull(message = "{key_72ec6e753f37464593729eba06a5d8b7}")
    private DemandConfigCancelReferenceTypeEnum clearConfigType;

}
