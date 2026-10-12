package com.persagy.apccontrol.common.sse.strategy.light;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSpaceTagDTO;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.service.InstanceService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 空间状态
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240402.00
 */
@Slf4j
@Component(value = "LIGHT_SPACE_STATUS")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightSpaceStatus extends AbstractSseEventStrategyTemplate implements BizComponent<LightSpaceStatus> {

    private final InstanceService instanceService;
    private final LightingService lightingService;

    // 推送时间map
    private static final Map<String, LocalDateTime> pushTimeMap = new HashMap<>();

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.LIGHT_SPACE_STATUS;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ": LIGHT_SPACE_STATUS";
    }

    @Override
    protected SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        if (param.getDemandTemplateType().isNightLighting()) {
            // 仅支持非夜景照明
            return null;
        }

        // 降低推送频率，1分钟一次
        String key = param.getProjectId() + ":" + param.getInstanceId();
        LocalDateTime now = LocalDateTime.now();
        if (pushTimeMap.get(key) != null && now.isBefore(pushTimeMap.get(key).plusMinutes(1))) {
            log.info("{} LIGHT_SPACE_STATUS推送频率过快，跳过推送", key);
            return null;
        }
        pushTimeMap.put(key, now);

        String projectId = param.getProjectId();
        DemandTemplateTypeEnum templateType = param.getDemandTemplateType();
        InstanceTypeEnum instanceType = param.getInstanceType();
        String instanceId = param.getInstanceId();

        Set<String> spaceIds = listSpaceIdsByInstance(DemandInstanceTypeEnum.analyze(instanceType.getValue()), instanceId);
        if (CollectionUtils.isEmpty(spaceIds)) {
            return null;
        }
        Map<String, LightingSpaceTagDTO> spaceIdAndTag = lightingService
                .mapSpaceIdAndTag(projectId, templateType, spaceIds);
        if (MapUtils.isEmpty(spaceIdAndTag)) {
            return null;
        }

        List<Message> messages = new ArrayList<>();
        spaceIdAndTag.forEach((spaceId, spaceTag) -> messages.add(new Message(spaceId, spaceTag)));
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(SseEmitterEventEnum.LIGHT_SPACE_STATUS)
                .data(messages)
                .build();
    }

    /**
     * 查询配置实例关联的空间ID ，使用本地缓存降低查询频率
     *
     * @param instanceType
     * @param instanceId
     * @return {@link Set<String>}
     * @author yuecaipu
     * @date 2024/8/19 10:49
     * @since
     */
    private Set<String> listSpaceIdsByInstance(DemandInstanceTypeEnum instanceType, String instanceId) {

        return instanceService.listSpaceIdsByInstance(instanceType, instanceId);
    }

    @Override
    public String logTag() {
        return "SSE-空间标签推送";
    }

    /**
     * 消息体
     *
     * @author JiYinchuan
     * @since 5.240402.0
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {

        /**
         * 空间ID
         */
        private String spaceId;

        /**
         * 空间标签
         */
        private LightingSpaceTagDTO tag;

    }
}
