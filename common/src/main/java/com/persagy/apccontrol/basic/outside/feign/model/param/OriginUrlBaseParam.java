package com.persagy.apccontrol.basic.outside.feign.model.param;

import com.alibaba.fastjson.JSONObject;

/**
 * 原始请求参数
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public class OriginUrlBaseParam {

    /**
     * 默认用户ID
     */
    public static final String DEFAULT_USER_ID = "systemId";

    /**
     * 默认登录设备
     */
    public static final String DEFAULT_LOGIN_DEVICE = "PC";

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
