package com.persagy.apccontrol.common.sse.strategy.business;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.date.StopWatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.AbstractSseEventStrategyTemplate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.validate.utility.enumeration.error.client.RequestParamErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 设备自定义信息点SSE推送
 *
 * @author yuecaipu
 * @date 2024/5/4 15:38
 * @since v5.240602.00
 */
@Component(value = "EQUIPMENT_BUSINESS")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EquipmentBusiness extends AbstractSseEventStrategyTemplate {

    private final InfoCodeGateway infoCodeGateway;
    @Value("${sse.EQUIPMENT_BUSINESS.disabled:false}")
    private Boolean disabled;

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + ":EQUIPMENT_BUSINESS";
    }

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.EQUIPMENT_BUSINESS;
    }

    @Override
    public SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        if (BooleanUtils.isTrue(disabled)) {
            return SseEmitterObject.builder().groupCode(param.getGroupCode())
                    .projectId(param.getProjectId())
                    .eventType(getEventType())
                    .eventName(getEventType().getReasonPhrase())
                    .data(null)
                    .build();
        }
        StopWatch stopWatch = StopWatch.create("EquipmentBusiness耗时");
        stopWatch.start("EquipmentBusiness耗时");

        SubscribeTypeEnum subscribeType = param.getSubscribeType();
        RunningMonitorTypeEnum runningMonitorType = RunningMonitorTypeEnum.of(subscribeType);
        if (Objects.isNull(runningMonitorType)) {
            throw new ApplicationException(RequestParamErrorEnum.INVALID_INPUT, LocaleMessageUtil.getMessage("key_721e123d885943bcae0a4b51c7b0d544"));
        }

        List<SseDataObject> result = new ArrayList<>();

        // 查询模版和关联对象, 模版类型 -> 关联对象
        Map<InfoCodeTemplateTypeModel, List<DtpRwdObjectDTO>> templateTypeAndObjects =
                queryTemplateTypeAndObjects(runningMonitorType);
        for (Map.Entry<InfoCodeTemplateTypeModel, List<DtpRwdObjectDTO>> entry
                : templateTypeAndObjects.entrySet()) {
            InfoCodeTemplateTypeModel templateTypeModel = entry.getKey();
            List<DtpRwdObjectDTO> objects = entry.getValue();
            InfoCodeTemplateTypeEnum templateType = templateTypeModel.getTemplateType();
            if (CollectionUtils.isEmpty(objects)) {
                continue;
            }

            // 查询信息点聚合根
            List<InfoCodeAggregate> aggregates = queryEquipmentInfoCodeAggregates(runningMonitorType, templateType, objects);
            Map<String, List<InfoCodeConfigVO>> objectIdAndConfigs = new HashMap<>(Math.max(objects.size(), 16));
            for (InfoCodeAggregate aggregate : aggregates) {
                String objectId = aggregate.getIdentifier().getObject().getId();
                // 信息点配置VO
                InfoCodeConfigVO configVo = InfoCodeConfigVO.of(aggregate, aggregates);
                MapExecutor.dynamicPut(objectIdAndConfigs, objectId, configVo, ArrayList::new);
            }
            result.add(new SseDataObject(templateType, objectIdAndConfigs));
        }
        stopWatch.stop();
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(result)
                .build();
    }

    /**
     * 查询模版类型模型和对象映射
     *
     * @param runningMonitorType 运行监测类型
     * @return 模版类型模型 -> 关联对象
     * @since 5.240602.0
     */
    private Map<InfoCodeTemplateTypeModel, List<DtpRwdObjectDTO>> queryTemplateTypeAndObjects(
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        List<InfoCodeTemplateTypeEnum> infoCodeTemplateTypes = Arrays.stream(
                InfoCodeTemplateTypeEnum.values()).collect(Collectors.toList());
        // 移除所有系统下的Hover模版
        infoCodeTemplateTypes.remove(InfoCodeTemplateTypeEnum.HOVER);
        // 为客流时移除详情模版, 临时解决, 后续优化
        if (runningMonitorType == RunningMonitorTypeEnum.PASSENGER_FLOW) {
            infoCodeTemplateTypes.remove(InfoCodeTemplateTypeEnum.DETAIL);
        }
        if (CollectionUtils.isEmpty(infoCodeTemplateTypes)) {
            return Maps.newHashMap();
        }
        InfoCodeObjectQueryParam objectQueryParam = new InfoCodeObjectQueryParam();
        objectQueryParam.setRunningMonitorType(runningMonitorType);
        objectQueryParam.setTemplateTypes(infoCodeTemplateTypes);
        return infoCodeGateway.mapTemplateTypeAndObjects(objectQueryParam);
    }

    /**
     * 查询信息点聚合根
     *
     * @param runningMonitorType 运行监测类型
     * @param templateType       模版类型
     * @param objects            对象列表
     * @return 信息点聚合根
     * @since 5.240602.0
     */
    private List<InfoCodeAggregate> queryEquipmentInfoCodeAggregates(
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            @NonNull InfoCodeTemplateTypeEnum templateType,
            @NonNull List<DtpRwdObjectDTO> objects) {
        InfoCodeInitByObjectParam infoCodeInitByObjectParam = new InfoCodeInitByObjectParam();
        infoCodeInitByObjectParam.setRunningMonitorType(runningMonitorType);
        infoCodeInitByObjectParam.setObjects(objects);
        infoCodeInitByObjectParam.setTemplateTypes(Lists.newArrayList(templateType));
        infoCodeInitByObjectParam.setFilterSsePublishInfoCode(true);
        infoCodeInitByObjectParam.setFilterSsePublishInfoCodeOld(true);
        List<InfoCodeAggregate> aggregates = infoCodeGateway.of(infoCodeInitByObjectParam);
        infoCodeGateway.fillInfoCodeDefineModel(aggregates);
        infoCodeGateway.fillInfoCodeDataModel(aggregates, null);
        return aggregates;
    }

    /**
     * SSE数据对象
     *
     * @author JiYinchuan
     * @since 5.240602.0
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SseDataObject {

        /**
         * 模版类型
         */
        private InfoCodeTemplateTypeEnum templateType;

        /**
         * 对象配置
         */
        private Map<String, List<InfoCodeConfigVO>> objectConfigs;

    }
}
