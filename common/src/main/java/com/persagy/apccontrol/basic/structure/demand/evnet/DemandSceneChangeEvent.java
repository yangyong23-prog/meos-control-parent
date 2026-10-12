package com.persagy.apccontrol.basic.structure.demand.evnet;

import com.persagy.apccontrol.basic.model.enumtype.system.DataTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 需求管理-场景变化事件
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
public class DemandSceneChangeEvent extends ApplicationEvent {

    /**
     * 构造器
     *
     * @param message 参数
     * @since 7.250801.0
     */
    public DemandSceneChangeEvent(Message message) {
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
         * 模版ID
         */
        private @NonNull Long templateId;

        /**
         * 变更内容
         */
        private List<Detail> details;

    }

    /**
     * 详情
     *
     * @author JiYinchuan
     * @since 7.250801.0
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Detail {

        /**
         * 数据操作类型
         */
        private @NonNull DataTypeEnum dataType;

        /**
         * 变更内容
         */
        private List<Config> configs;

    }

    /**
     * 配置
     *
     * @author JiYinchuan
     * @since 7.250801.0
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Config {

        /**
         * 旧场景
         * <hr/>
         * <ul>
         *     <li>编辑</li>
         *     <li>删除</li>
         * </ul>
         */
        private DemandSceneEntity oldScene;

        /**
         * 新场景
         * <hr/>
         * <ul>
         *     <li>新增</li>
         *     <li>编辑</li>
         * </ul>
         */
        private DemandSceneEntity newScene;

    }
}
