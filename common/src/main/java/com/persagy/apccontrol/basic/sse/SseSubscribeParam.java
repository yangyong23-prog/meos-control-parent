package com.persagy.apccontrol.basic.sse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * sse 订阅参数
 *
 * @author yuecaipu
 * @date 2024/4/29 13:45
 * @since v5.240402.00
 */
@Data
@Slf4j
public class SseSubscribeParam {

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 订阅类型
     */
    @NotNull
    private SubscribeTypeEnum subscribeType;

    /**
     * 需求模板类型
     */
    private DemandTemplateTypeEnum demandTemplateType;


    /**
     * 项目ID
     */
    @NotBlank
    private String projectId;

    /**
     * 实例ID
     */
    @NotBlank
    private String instanceId;

    /**
     * 实例类型
     */
    @NotNull
    private InstanceTypeEnum instanceType;

    /**
     * 实例类型编码
     */
    private String classCode;

    /**
     * 订阅的事件类型
     */
    private List<SseEmitterEventEnum> eventType;

    /**
     * 对象classCode和objectIds映射， 对对象iot和标签限制
     */
    private Map<String, Set<String>> claasCode2objectIds;
    private String claasCode2objectIdsByString;

    /**
     * 事件类型及其对应的参数集合
     */
    private Map<SseEmitterEventEnum, Object> eventParamMap;

    /**
     * 是否去除默认分精度类型（由订阅建立时的 HTTP 请求头 {@code removeDefaultIpType} 决定）
     *
     * @since v7.260901.0
     */
    private Boolean removeDefaultIpType;
    

    /**
     * 添加轮询接口的参数
     *
     * @param eventType 事件类型
     * @param param     接口参数
     * @author yuecaipu
     * @date 2024/8/21 15:28
     * @since v5.240801
     */
    public synchronized void addPollingParam(SseEmitterEventEnum eventType, Object param) {
        if (null == eventParamMap) {
            eventParamMap = new HashMap<>(8);
        }
        eventParamMap.put(eventType, param);
    }

    public void setClaasCode2objectIdsByString(String claasCode2objectIdsByString) {
        this.claasCode2objectIdsByString = claasCode2objectIdsByString;
        if (StringUtils.isNotEmpty(claasCode2objectIdsByString)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(claasCode2objectIdsByString);
                Map<String, Set<String>> map = new HashMap<>();
                rootNode.fields().forEachRemaining(entry -> {
                    JsonNode valueNode = entry.getValue();
                    if (valueNode.isArray()) {
                        Set<String> stringSet = new HashSet<>();
                        valueNode.forEach(node -> stringSet.add(node.asText()));
                        map.put(entry.getKey(), stringSet);
                    }
                });
                claasCode2objectIds = map;
            } catch (Exception e) {
                log.error("解析claasCode2objectIdsByString异常", e);
            }

        }

    }
}
