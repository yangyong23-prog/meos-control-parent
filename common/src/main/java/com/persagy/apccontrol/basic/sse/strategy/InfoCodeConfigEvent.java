package com.persagy.apccontrol.basic.sse.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeConfigByObjectQueryParam;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeGroupVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 信息点配置事件
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Component(value = "INFO_CODE_CONFIG_EVENT")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class InfoCodeConfigEvent extends AbstractSseEventStrategyTemplate implements LogComponent {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final InfoCodeConfigService infoCodeConfigService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.INFO_CODE_CONFIG_EVENT;
    }

    @Override
    public SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        List<SseDataObject> dataObjects = Collections.synchronizedList(new ArrayList<>());
        EventParam eventParam = analyzeEventParam(param);
        Set<InfoCodeTemplateTypeEnum> templateTypes = eventParam.getTemplateTypes();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (InfoCodeTemplateTypeEnum templateType : templateTypes) {
            futures.add(CompletableFuture.runAsync(() -> {
                SseDataObject dataObject = new SseDataObject();
                try {
                    Map<String, List<InfoCodeConfigVO>> objectIdAndConfigs = new LinkedHashMap<>();
                    RunningMonitorSecondTypeEnum runningMonitorSecondType = eventParam.getRunningMonitorSecondType();
                    Map<String, Set<String>> claasCodeAndObjectIds = eventParam.getClaasCodeAndObjectIds();
                    Set<String> groupCodes = eventParam.getGroupCodes();
                    Set<String> objectIds = claasCodeAndObjectIds.values().stream()
                            .flatMap(Collection::stream)
                            .collect(Collectors.toSet());
                    InfoCodeConfigByObjectQueryParam queryParam = new InfoCodeConfigByObjectQueryParam();
                    queryParam.setRunningMonitorType(runningMonitorSecondType.toRunningMonitorTypeEnum());
                    queryParam.setRunningMonitorSecondType(runningMonitorSecondType);
                    queryParam.setTemplateType(templateType);
                    queryParam.setObjectIds(objectIds);
                    queryParam.setGroupCodes(groupCodes);
                    queryParam.setPlaceholders(eventParam.getPlaceholders());
                    queryParam.setFilterSsePublishInfoCode(true);
                    List<InfoCodeObjectGroupVO> infoCodeObjectGroups = LocalContextStorage.deprecatedVersion(
                            () -> infoCodeConfigService.listInfoCodeConfigsByObject(queryParam));

                    if (CollectionUtils.isNotEmpty(infoCodeObjectGroups)) {
                        for (InfoCodeObjectGroupVO infoCodeObjectGroup : infoCodeObjectGroups) {
                            String objectId = infoCodeObjectGroup.getId();
                            MultiKeyMap<String, InfoCodeConfigVO> infoTypeAndCodeAndConfig = new MultiKeyMap<>();
                            infoCodeObjectGroup.getRelInfoCodeGroupConfigs().stream()
                                    .flatMap(item -> deepParseInfoCodeGroup(new ArrayList<>(), item).stream())
                                    .forEach(item -> infoTypeAndCodeAndConfig.put(new MultiKey<>(objectId,
                                            item.getInfoCodeType().value(), item.getInfoCode()), item));
                            infoTypeAndCodeAndConfig.forEach((key, value) -> MapExecutor.dynamicPut(objectIdAndConfigs,
                                    key.getKey(0), value, ArrayList::new));
                        }
                    }
                    dataObject.setTemplateType(templateType);
                    dataObject.setObjectConfigs(objectIdAndConfigs);

                } catch (Exception e) {
                    dataObject.setErrorMessage(e.getLocalizedMessage());
                    log.error("信息点配置事件执行失败", e);
                }
                dataObjects.add(dataObject);
            }, BusinessThreadPoolExecutor.ttlExecutor()));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(dataObjects)
                .build();
    }

    /**
     * 递归解析信息点分组
     *
     * @param target 目标源
     * @param source 数据源
     * @return 所有信息点分组
     * @since 5.241201.0
     */
    private List<InfoCodeConfigVO> deepParseInfoCodeGroup(
            @NonNull List<InfoCodeConfigVO> target,
            @Nullable InfoCodeGroupVO source) {
        if (Objects.isNull(source)) {
            return target;
        }
        List<InfoCodeConfigVO> relInfoCodeConfigs = source.getRelInfoCodeConfigs();
        if (CollectionUtils.isNotEmpty(relInfoCodeConfigs)) {
            target.addAll(relInfoCodeConfigs);
        }
        List<InfoCodeGroupVO> groupChildren = source.getGroupChildren();
        if (CollectionUtils.isNotEmpty(groupChildren)) {
            for (InfoCodeGroupVO groupChild : groupChildren) {
                target = deepParseInfoCodeGroup(target, groupChild);
            }
        }
        return target;
    }

    /**
     * 解析参数
     *
     * @param param 订阅参数
     * @return 参数
     * @since 5.241002.0
     */
    private EventParam analyzeEventParam(@NonNull SseSubscribeParam param) {
        SubscribeTypeEnum subscribeType = param.getSubscribeType();
        Map<String, Set<String>> claasCode2objectIds = param.getClaasCode2objectIds();
        Map<SseEmitterEventEnum, Object> eventParamMap = param.getEventParamMap();
        if (MapUtils.isEmpty(eventParamMap) || !eventParamMap.containsKey(getEventType())) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_d52365bcc194407cabe816747b2bb8d1"));
        }
        Object object = eventParamMap.get(getEventType());
        try {
            EventParam eventParam = objectMapper.readValue(objectMapper.writeValueAsString(object), EventParam.class);
            eventParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.of(subscribeType));
            eventParam.setClaasCodeAndObjectIds(claasCode2objectIds);
            Set<ConstraintViolation<EventParam>> validateResults = validator.validate(eventParam);
            List<String> errorMessages = validateResults.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(errorMessages)) {
                logProjectInstance().error("订阅参数校验失败", Pair.of("errorMessages", errorMessages));
                throw new ValidationException(LocaleMessageUtil.getMessage("key_d6cbfa49d3a5475a886839395b1ebc88"));
            }
            return eventParam;
        } catch (Exception e) {
            logProjectInstance().error("订阅参数解析失败", e, Pair.of("param", param));
            throw new RuntimeException(e);
        }
    }

    @Override
    public String logTag() {
        return "SSE-信息点配置";
    }

    /**
     * 事件参数
     *
     * @author JiYinchuan
     * @since 5.241002.0
     */
    @Data
    @FieldNameConstants
    public static class EventParam implements BaseCheckParamProvider {

        /**
         * 运行监测二级类型
         */
        @NotNull(message = "{key_4a1af0119a4f4fdc8f6cfe10e1ea451e}")
        private RunningMonitorSecondTypeEnum runningMonitorSecondType;

        /**
         * 模版类型
         */
        private InfoCodeTemplateTypeEnum templateType;

        /**
         * 模版类型
         */
        private Set<InfoCodeTemplateTypeEnum> templateTypes;

        /**
         * 设备类编码 -> 对象ID
         */
        @NotEmpty(message = "{key_58e1c03325b247d2854173e07d052d01}")
        private Map<String, Set<String>> claasCodeAndObjectIds;

        @Schema(description = "分组编码, 可以为空")
        private Set<String> groupCodes;

        @Schema(description = "占位符参数, key为占位符名称, value为替换值, 会覆盖自动解析的数据源")
        private @Nullable Map<String, Object> placeholders;

        /**
         * 获取模版类型
         *
         * @return 模版类型
         * @since 7.251202.0
         */
        public Set<InfoCodeTemplateTypeEnum> getTemplateTypes() {
            if (CollectionUtils.isNotEmpty(this.templateTypes)) {
                return this.templateTypes;
            }
            // 当模版类型集合为空时取单个, 以兼容之前已经接入的系统
            if (Objects.nonNull(this.templateType)) {
                return Sets.newHashSet(this.templateType);
            }
            return null;
        }

        @Override
        public void checkAndSet() {
            if (this.checkAssembleCollectionNull(this.templateType, this.templateTypes)) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_8356b8ff6af845ad99d6033c92e55c61"));
            }
        }

    }

    /**
     * SSE数据对象
     *
     * @author JiYinchuan
     * @since 5.241002.0
     */
    @Data
    public static class SseDataObject {

        /**
         * 模版类型
         */
        private InfoCodeTemplateTypeEnum templateType;

        /**
         * 对象配置
         */
        private Map<String, List<InfoCodeConfigVO>> objectConfigs;

        /**
         * 错误信息
         */
        private String errorMessage;

    }
}
