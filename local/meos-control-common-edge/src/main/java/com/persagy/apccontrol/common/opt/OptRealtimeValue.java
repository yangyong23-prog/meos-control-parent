package com.persagy.apccontrol.common.opt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * B2：实时值共享缓存的值（与 bdtp / dtp-meos 的契约 DTO）。
 *
 * <p>Redis key：{@code apc:iot_real_time_data:{objectId}:{infoCode}}，redis.paas db 10，裸 key，值为下面字段组成的
 * 普通 JSON 对象（无类型信息、无 TTL）。字段名与 Twin 返回的 DtpTwinIotData / FeedbackInfoMsgMqttVO 同名，
 * 读方用 BeanUtil.toBean / Jackson 都能直接转换。
 *
 * <p>兼容规则：只加字段不改字段；要改格式就换 key 名（如加 :v2），新旧并存一个版本周期。
 * 契约说明见 docs/opt/contract-iot-real-time-data.md，契约测试 OptRealtimeContractTest。
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptRealtimeValue {

    /** 离线标记值：Twin 推送 "~" 表示点位离线 */
    public static final String OFFLINE_VALUE = "~";

    public static final String SOURCE_MQTT = "mqtt";
    public static final String SOURCE_TWIN = "twin";

    private String objectId;

    private String classCode;

    private String infoCode;

    /** Twin 原始值（未乘换算系数、未按小数位取整）；"~" 为离线 */
    private String dataValue;

    /** 采集时间 yyyyMMddHHmmss */
    private String dataTime;

    /** 时间常量（分钟），null 或 0 表示不过期 */
    private Integer dataIndate;

    /** 1 正常，2 异常 */
    private Integer dataStatus;

    /** 写入方：mqtt / twin（回源） */
    private String source;

    /** 写入时间（毫秒时间戳），读方据此判断缓存是否陈旧 */
    private Long writeTime;

    @JsonIgnore
    public boolean isOffline() {
        return OFFLINE_VALUE.equals(dataValue);
    }
}
