package com.persagy.apccontrol.basic.structure.infocodeconfig.envet;

import org.springframework.context.ApplicationEvent;

import java.util.Collection;

/**
 * 信息点配置删除事件
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public class InfoCodeConfigRemoveEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param deletedConfigIds 删除的配置ID
     * @since 5.240602.0
     */
    public InfoCodeConfigRemoveEvent(Collection<Long> deletedConfigIds) {
        super(deletedConfigIds);
    }
}
