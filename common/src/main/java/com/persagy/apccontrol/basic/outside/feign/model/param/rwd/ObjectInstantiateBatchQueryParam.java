package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Collection;

/**
 * 物理世界对象规则实例化查询参数
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
public class ObjectInstantiateBatchQueryParam {

    /**
     * 对象信息
     */
    @NotEmpty(message = "{key_035a472eaa494e56af4e59899f870494}")
    @Size(min = 1, max = 1000)
    private Collection<ObjectInstantiateQueryParam> objIdInfos;

}
