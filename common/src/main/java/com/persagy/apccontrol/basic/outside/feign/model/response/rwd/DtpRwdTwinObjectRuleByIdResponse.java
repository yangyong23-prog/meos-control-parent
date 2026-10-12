package com.persagy.apccontrol.basic.outside.feign.model.response.rwd;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 孪生对象规则根据ID响应
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtpRwdTwinObjectRuleByIdResponse extends BaseResponse {

    /**
     * 实例对象ID
     */
    private String objectId;

    /**
     * 对象类code
     */
    private String infoCode;

    /**
     * 计算类型
     *
     * <ul>
     *     <li><b>1</b> - 直采</li>
     *     <li><b>2</b> - 继承</li>
     *     <li><b>3</b> - 衍生</li>
     * </ul>
     */
    private Integer computeType;

}
