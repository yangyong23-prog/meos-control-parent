package com.persagy.apccontrol.basic.model.enumtype.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 页面播报操作类型
 */
@Getter
@AllArgsConstructor
public enum WebSocketTypeEnum {
    PARAM_SET("param_set", "参数设定"),

    PAGE_BROADCAST("page_broadcast", "页面播报"),
    TEQ_NUM_CHANGE("teq_num_change", "末端设备数量变更"),
    RELEASE_INTERVENTION("release_intervention", "解除干预"),
    SYSTEM_DATE_CHANGE("system_date_change", "系统参数变更"),

    INFO_EXTEND_SET("info_extend_set", "对象扩展设定");


    private final String type;

    private final String desc;

}
