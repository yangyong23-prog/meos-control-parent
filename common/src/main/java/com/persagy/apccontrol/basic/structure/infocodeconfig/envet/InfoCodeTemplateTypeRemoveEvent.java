package com.persagy.apccontrol.basic.structure.infocodeconfig.envet;

import org.springframework.context.ApplicationEvent;

import java.util.Collection;

/**
 * 信息点模版类型删除事件
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
public class InfoCodeTemplateTypeRemoveEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param deletedTemplateTypeIds 删除的模版类型ID
     * @since 5.240602.0
     */
    public InfoCodeTemplateTypeRemoveEvent(Collection<Long> deletedTemplateTypeIds) {
        super(deletedTemplateTypeIds);
    }
}
