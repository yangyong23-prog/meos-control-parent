package com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 异常事件实例
 *
 * @author JiYinchuan
 * @since 5.241101.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ExceptionEventInstanceResponse extends BaseResponse {

    /**
     * 实例ID
     */
    private String instanceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 对象类型
     *
     * @see com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum
     */
    private String objectType;

    /**
     * 所在空间ID
     */
    private Set<String> spaceIds;

}
