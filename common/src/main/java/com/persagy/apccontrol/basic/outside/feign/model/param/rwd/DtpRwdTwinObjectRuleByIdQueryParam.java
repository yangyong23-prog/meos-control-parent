package com.persagy.apccontrol.basic.outside.feign.model.param.rwd;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * 孪生对象规则根据ID查询参数
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
public class DtpRwdTwinObjectRuleByIdQueryParam {

    /**
     * 对象ID
     */
    @NotEmpty(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private Set<String> objectIds;

}
