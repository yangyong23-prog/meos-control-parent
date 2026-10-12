package com.persagy.apccontrol.basic.structure.infocodeconfig.envet;

import org.springframework.context.ApplicationEvent;

/**
 * 信息点模版类型新增事件
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
public class InfoCodeTemplateTypeSaveEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param templateTypeId 模版类型ID
     * @since 7.260302.0
     */
    public InfoCodeTemplateTypeSaveEvent(Long templateTypeId) {
        super(templateTypeId);
    }
}
