package com.persagy.apccontrol.basic.outside.feign.model.response.rwd;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 孪生对象规则响应
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpRwdTwinObjectRuleResponse extends BaseResponse {

    /**
     * 实例对象ID
     */
    private String objectId;

    /**
     * 对象类code
     */
    private String classCode;

    /**
     * 信息点code
     */
    private String infoCode;

}
