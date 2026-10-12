package com.persagy.apccontrol.basic.structure.demand.evnet;

import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateChangeTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEvent;

/**
 * 需求管理-模版变化事件
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
public class DemandTemplateChangeEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param message 消息
     * @since 7.250801.0
     */
    public DemandTemplateChangeEvent(Message message) {
        super(message);
    }

    /**
     * 参数
     *
     * @author JiYinchuan
     * @since 7.250801.0
     */
    @Data
    @NoArgsConstructor
    public static class Message {

        /**
         * 数据操作类型
         */
        private @NonNull DemandTemplateChangeTypeEnum dataType;

        /**
         * 操作前模版实体
         *
         * <p>存在场景
         * <ul>
         *     <li>新增</li>
         *     <li>删除</li>
         *     <li>切换应用模版</li>
         * </ul>
         */
        private DemandTemplate oldTemplate;

        /**
         * 操作后模版实体
         *
         * <p>存在场景
         * <ul>
         *     <li>新增</li>
         *     <li>编辑</li>
         *     <li>应用模版</li>
         * </ul>
         */
        private DemandTemplate newTemplate;

    }
}
