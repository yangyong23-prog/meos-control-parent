package com.persagy.apccontrol.basic.structure.demand.evnet;

import com.persagy.apccontrol.basic.model.message.BaseEvent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceConfigChangeTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;

import java.util.Set;

/**
 * 需求实例配置变更事件
 *
 * @author JiYinchuan
 * @since 7.250302.0
 */
public class DemandInstanceConfigChangeEvent extends BaseEvent {

    /**
     * 构造器
     *
     * @param param 配置变更参数
     * @since 7.250302.0
     */
    public DemandInstanceConfigChangeEvent(Param param) {
        super(param);
    }

    /**
     * 参数
     *
     * @author JiYinchuan
     * @since 7.250302.0
     */
    @Data
    public static class Param {

        /**
         * 模版类型
         */
        private DemandTemplateTypeEnum templateType;

        /**
         * 变更类型
         */
        private DemandInstanceConfigChangeTypeEnum changeType;

        /**
         * 进入变更的实例ID
         */
        private Set<String> inInstanceIds;

        /**
         * 移除变更的实例ID
         */
        private Set<String> outInstanceIds;

    }
}
