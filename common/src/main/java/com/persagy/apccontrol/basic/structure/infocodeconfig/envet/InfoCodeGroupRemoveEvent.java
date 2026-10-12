package com.persagy.apccontrol.basic.structure.infocodeconfig.envet;

import org.springframework.context.ApplicationEvent;

import java.util.Collection;

/**
 * 信息点分组配置删除事件
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public class InfoCodeGroupRemoveEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param deletedGroupConfigIds 删除的分组配置ID
     * @since 5.240602.0
     */
    public InfoCodeGroupRemoveEvent(Collection<Long> deletedGroupConfigIds) {
        super(deletedGroupConfigIds);
    }
}
