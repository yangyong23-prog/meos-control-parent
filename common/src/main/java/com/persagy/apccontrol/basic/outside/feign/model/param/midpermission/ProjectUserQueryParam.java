package com.persagy.apccontrol.basic.outside.feign.model.param.midpermission;

import com.persagy.apccontrol.basic.outside.feign.model.param.OriginUrlBaseParam;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户查询参数
 *
 * @author JiYinchuan
 * @since v5.0.0
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ProjectUserQueryParam extends OriginUrlBaseParam {

    /**
     * 当前用户id
     */
    private String userId = DEFAULT_USER_ID;

    /**
     * 登录设备 取值：PC/iPhone/Android
     */
    private String loginDevice = DEFAULT_LOGIN_DEVICE;

    /**
     * 用户密码MD5值
     */
    private String pd;

}
