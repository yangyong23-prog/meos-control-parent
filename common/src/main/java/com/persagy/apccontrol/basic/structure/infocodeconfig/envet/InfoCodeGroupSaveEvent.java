package com.persagy.apccontrol.basic.structure.infocodeconfig.envet;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 信息点分组配置新增事件
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public class InfoCodeGroupSaveEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param groupCodeAndId 分组编码 -> 分组ID
     * @since 5.240602.0
     */
    public InfoCodeGroupSaveEvent(Map<String, Long> groupCodeAndId) {
        super(groupCodeAndId);
    }
}
