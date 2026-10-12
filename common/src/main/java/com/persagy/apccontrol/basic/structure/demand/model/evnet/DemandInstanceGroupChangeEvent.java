package com.persagy.apccontrol.basic.structure.demand.model.evnet;

import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEvent;

/**
 * 需求实例分组变更事件
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
public class DemandInstanceGroupChangeEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param source 对象
     * @since 7.250602.0
     */
    public DemandInstanceGroupChangeEvent(Message source) {
        super(source);
    }

    /**
     * 消息对象
     *
     * @author JiYinchuan
     * @since 7.250602.0
     */
    @Data
    @NoArgsConstructor
    public static class Message {

        /**
         * 模版类型
         */
        private @NonNull DemandTemplateTypeEnum templateType;

        /**
         * 变更类型
         */
        private @NonNull DataTypeEnum type;

        /**
         * 操作前分组
         */
        private DemandInstanceGroupPO oldGroup;

        /**
         * 操作后分组
         */
        private DemandInstanceGroupPO newGroup;

    }
}
