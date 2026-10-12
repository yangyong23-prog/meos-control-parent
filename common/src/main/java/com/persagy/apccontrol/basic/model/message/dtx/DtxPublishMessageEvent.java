package com.persagy.apccontrol.basic.model.message.dtx;

import com.persagy.apccontrol.basic.model.message.BaseEvent;
import lombok.Data;

/**
 * DTX发布消息事件
 *
 * @author JiYinchuan
 * @since 7.250601.0
 */
public class DtxPublishMessageEvent extends BaseEvent {

    /**
     * 构造器
     *
     * @param source 对象
     * @since 7.250601.0
     */
    public DtxPublishMessageEvent(Message source) {
        super(source);
    }

    @Override
    public String getGroupCode() {
        return ((Message) this.getSource()).getGroupCode();
    }

    @Override
    public String getProjectId() {
        return ((Message) this.getSource()).getProjectId();
    }

    /**
     * 消息对象
     *
     * @author JiYinchuan
     * @since 7.250601.0
     */
    @Data
    public static class Message {

        /**
         * 集团编码
         */
        private String groupCode;

        /**
         * 项目ID
         */
        private String projectId;

        /**
         * 发布时间 YYYYMMDDHHMMSS
         */
        private String publishTime;

    }
}
