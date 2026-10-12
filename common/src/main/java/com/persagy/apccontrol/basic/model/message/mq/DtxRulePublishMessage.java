package com.persagy.apccontrol.basic.model.message.mq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.message.BaseMessage;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * DTX规则发布消息
 * <p>
 * 消息格式：{
 * "groupCode":"集团编码",
 * "projectId":"项目ID",
 * "operatorName":"操作人"
 * }
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DtxRulePublishMessage extends BaseMessage implements LogComponent {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DtxRulePublishMessage.class);
    private static final String LOG_TAG = "DTX规则发布消息";

    /**
     * 操作人
     */
    private String operatorName;

    @Override
    public String logTag() {
        return LOG_TAG;
    }

    /**
     * 从字符串反序列化
     * <p>
     * 用于处理消息被双重序列化的情况，即消息体为字符串值而非JSON对象
     *
     * @param value JSON字符串
     * @return DtxRulePublishMessage 实例
     * @since 7.260302.0
     */
    @JsonCreator
    public static DtxRulePublishMessage fromString(String value) {
        try {
            // 解析为Map避免循环调用问题
            Map<String, Object> map = ObjectMapperUtils.readValue(value, new TypeReference<Map<String, Object>>() {
            });
            DtxRulePublishMessage message = new DtxRulePublishMessage();
            message.setGroupCode((String) map.get("groupCode"));
            message.setProjectId((String) map.get("projectId"));
            message.setOperatorName((String) map.get("operatorName"));
            return message;
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "反序列化失败", e, Pair.of("value", value));
            throw new IllegalArgumentException("Failed to deserialize DtxRulePublishMessage from string: " + value, e);
        }
    }
}
