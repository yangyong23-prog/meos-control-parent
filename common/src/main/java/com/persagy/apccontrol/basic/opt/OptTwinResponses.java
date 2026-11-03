package com.persagy.apccontrol.basic.opt;

import java.util.Map;

/**
 * opt 分支 B8：判断 Twin 写接口的响应是否成功。
 * <p>
 * Twin 的响应体为 {@code {respCode, respMsg, ...}}，经 CustomerResponse 反序列化后落在未定义属性中。
 * 只在能确定失败时返回 false：响应为空（调用异常已被吞掉），或带有非 {@code 00000} 的 respCode。
 * 没有 respCode 时无法判断，按成功处理，避免误判导致重复下发。
 */
public final class OptTwinResponses {

    public static final String SUCCESS_CODE = "00000";

    private OptTwinResponses() {
    }

    /**
     * @param responseNull          响应对象是否为空
     * @param undefinedProperties   响应中的未定义属性，可为 null
     */
    public static boolean succeeded(boolean responseNull, Map<String, Object> undefinedProperties) {
        if (responseNull) {
            return false;
        }
        Object code = undefinedProperties == null ? null : undefinedProperties.get("respCode");
        return code == null || SUCCESS_CODE.equals(String.valueOf(code).trim());
    }
}
