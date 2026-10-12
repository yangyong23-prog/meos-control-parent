package com.persagy.apccontrol.basic.sse.strategy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.map.MapUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDataModel;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SseEmitterObject;
import com.persagy.apccontrol.basic.sse.SseSubscribeParam;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum.IOT_RUNNING;
import static com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum.IOT_SETTING;

/**
 * @author xiaofengwang
 */
@Component(value = "CLOUD_IOT_DATA_CHANGE")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CloudIotDataEvent extends AbstractSseEventStrategyTemplate implements LogComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudIotDataEvent.class);
    private static final String LOG_TAG = "云端iot信息点推送";

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final InfoCodeGateway infoCodeGateway;
    private final LocalRedisCacheHandler localRedisCacheHandler;
    private final BizDtpRwdService bizDtpRwdService;

    @Override
    protected SseEmitterEventEnum getEventType() {
        return SseEmitterEventEnum.CLOUD_IOT_DATA_CHANGE;
    }

    @Override
    protected String getRedisLockKey(String eventKey) {
        return eventKey + getEventType().value();
    }

    @Override
    public SseEmitterObject getSseEmitterObject(SseSubscribeParam param) {
        LogExecutor.project().info(LOGGER, LOG_TAG, "开始执行IOT事件推送");
        List<ObjectIotData> objectIotDataList = new ArrayList<>();
        try {
            // 恢复订阅建立时的请求头标识，供底层查询方法判断是否使用最新数据模式
            if (Boolean.TRUE.equals(param.getRemoveDefaultIpType())) {
                LocalContextStorage.setRemoveDefaultIpType(true);
            }
            EventParam eventParam = analyzeEventParam(param);

            Map<String, Set<String>> classCodeAndObjectIds = eventParam.getClaasCodeAndObjectIds();
            if (MapUtil.isEmpty(classCodeAndObjectIds)) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "claasCodeAndObjectIds为空");
                return null;
            }

            // 按需订阅 按照对象信息点查询,就不走配置表获取数据了
            if (MapUtils.isNotEmpty(eventParam.getObjectIdAndInfoCodes())) {
                objectIotDataList = queryObjectIdAndInfoCodes(eventParam.getObjectIdAndInfoCodes(),
                        eventParam.getClaasCodeAndObjectIds());
                if (CollectionUtils.isEmpty(objectIotDataList)) {
                    LogExecutor.project().info(LOGGER, LOG_TAG, "未传递对象ID和信息点编码，开始进行按需订阅");
                }
                return SseEmitterObject.builder()
                        .projectId(param.getProjectId())
                        .eventType(getEventType())
                        .data(objectIotDataList)
                        .build();
            }

            Set<InfoCodeTemplateTypeEnum> templateTypes = eventParam.getTemplateTypes();
            Set<String> groupCodes = eventParam.getGroupCodes();
            RunningMonitorTypeEnum runningMonitorType = eventParam.getRunningMonitorType();
            InfoCodeInitByObjectParam initByObjectParam = new InfoCodeInitByObjectParam();

            // 确保 objects 是线程安全的
            List<DtpRwdObjectDTO> objects = Collections.synchronizedList(new ArrayList<>());
            classCodeAndObjectIds.forEach((classCode, ids) -> {
                for (String id : ids) {
                    DtpRwdObjectDTO object = new DtpRwdObjectDTO();
                    object.setClassCode(classCode);
                    object.setId(id);
                    objects.add(object);
                }
            });

            initByObjectParam.setRunningMonitorType(runningMonitorType);
            initByObjectParam.setTemplateTypes(templateTypes);
            initByObjectParam.setGroupCodes(groupCodes);
            initByObjectParam.setInfoCodeTypes(Arrays.asList(IOT_RUNNING, IOT_SETTING));
            initByObjectParam.setObjects(objects);
            initByObjectParam.setFilterBothNotExistIotInfoCodeRule(false);
            initByObjectParam.setNotFilterIotInfoCodeRule(true);
            List<InfoCodeAggregate> aggregates = infoCodeGateway.of(initByObjectParam);
            infoCodeGateway.fillInfoCodeDataModel(aggregates, null);

            if (CollectionUtil.isEmpty(aggregates)) {
                LogExecutor.project().error(LOGGER, LOG_TAG, "未查询到信息点配置");
                return null;
            }

            // 批量获取 RcInfoCodeCfg
            Set<String> classCodeAndInfoCodeKeys = new HashSet<>();
            aggregates.forEach(item -> {
                InfoCodeDataModel infoCodeAndDataModel = item.getInfoCodeAndDataModel();
                if (infoCodeAndDataModel != null) {
                    classCodeAndInfoCodeKeys.add(RedisKey.buildInfoCodeConfigStringKey(item.getTemplateTypeModel().getClassCode(), infoCodeAndDataModel.getInfoCode()));
                }
            });
            List<RcInfoCodeCfg> infoCodeCfgs =
                    this.localRedisCacheHandler.batchGetInfoCodeConfigs(classCodeAndInfoCodeKeys);
            Map<String, RcInfoCodeCfg> infoCodeCfgMap =
                    infoCodeCfgs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getClassCodeAndInfoCode, v -> v));


            for (InfoCodeAggregate item : aggregates) {
                InfoCodeDataModel infoCodeAndDataModel = item.getInfoCodeAndDataModel();
                if (Optional.ofNullable(infoCodeAndDataModel).isPresent()) {
                    try {
                        ObjectIotData objectIotData = new ObjectIotData();
                        objectIotData.setObjectId(item.getIdentifier().getObject().getId());
                        objectIotData.setInfoCode(infoCodeAndDataModel.getInfoCode());
                        if (infoCodeAndDataModel.getDataValue() != null) {
                            objectIotData.setDataValue(infoCodeAndDataModel.getDataValue().toString());
                        }
                        objectIotData.setDataTime(Optional.ofNullable(infoCodeAndDataModel.getDataTime())
                                .map(time -> time.format(DatePattern.PURE_DATETIME_FORMATTER))
                                .orElse(null));

                        // 填充单位和名称
                        RcInfoCodeCfg infoCodeCfg =
                                infoCodeCfgMap.get(item.getTemplateTypeModel().getClassCode() + infoCodeAndDataModel.getInfoCode());

                        if (infoCodeCfg != null) {
                            objectIotData.setUnit(infoCodeCfg.getUnit());
                            objectIotData.setDataValueName(infoCodeCfg.getDataValueName(objectIotData.getDataValue()));
                        } else {
                            objectIotData.setDataValueName(objectIotData.getDataValue());
                        }

                        objectIotDataList.add(objectIotData);
                    } catch (Exception e) {
                        LogExecutor.project().error(LOGGER, LOG_TAG, "错误：" + e.getLocalizedMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "错误：" + e.getLocalizedMessage(), e);
            return null;
        } finally {
            LocalContextStorage.clearRemoveDefaultIpType();
        }
        if (CollectionUtils.isEmpty(objectIotDataList)) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "未查询到数据");
        }
        return SseEmitterObject.builder()
                .projectId(param.getProjectId())
                .eventType(getEventType())
                .data(objectIotDataList)
                .build();
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
            // 优先使用一级类型解析, 一级类型无法解析时通过二级类型解析
            RunningMonitorTypeEnum runningMonitorType = RunningMonitorTypeEnum.of(subscribeType);
            if (Objects.isNull(runningMonitorType)) {
                RunningMonitorSecondTypeEnum runningMonitorSecondType = RunningMonitorSecondTypeEnum.of(subscribeType);
                if (Objects.nonNull(runningMonitorSecondType)) {
                    runningMonitorType = runningMonitorSecondType.toRunningMonitorTypeEnum();
                }
            }
            // 如果为冷源、冷热源则在context中写入时间、分精度、以及系统类型
            CommonConst.checkSystemReplacementTime(runningMonitorType);

            eventParam.setRunningMonitorType(runningMonitorType);
            eventParam.setClaasCodeAndObjectIds(claasCode2objectIds);
            eventParam.checkAndSet();
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

    /**
     * 查询对象信息点的 IOT值
     *
     * @param objectIdAndInfoCodes
     * @param classCodeAndObjectIds
     * @return
     */
    private List<ObjectIotData> queryObjectIdAndInfoCodes(Map<String, Set<String>> objectIdAndInfoCodes, Map<String,
            Set<String>> classCodeAndObjectIds) {
        List<ObjectIotData> objectIotDataList = Collections.synchronizedList(new ArrayList<>());

        Set<String> infoCodes = new HashSet<>();
        for (Set<String> set : objectIdAndInfoCodes.values()) {
            infoCodes.addAll(set);
        }
        List<EnergyDataResponse> responses = this.bizDtpRwdService.queryIpDataOnMoment(ContextUtil.getGroupCode(),
                ContextUtil.getProjectId(),
                new ArrayList<>(objectIdAndInfoCodes.keySet()), new ArrayList<>(infoCodes));

        if (CollectionUtils.isEmpty(responses)) {
            return objectIotDataList;
        }
        // 取对象的对象类
        Map<String, String> objIdAndClassCode = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : classCodeAndObjectIds.entrySet()) {
            String originalKey = entry.getKey();
            Set<String> originalValues = entry.getValue();
            for (String value : originalValues) {
                objIdAndClassCode.computeIfAbsent(value, k -> originalKey);
            }
        }
        // 批量获取 RcInfoCodeCfg
        Set<String> classCodeAndInfoCodeKeys = new HashSet<>();
        responses.forEach(response -> classCodeAndInfoCodeKeys.add(RedisKey.buildInfoCodeConfigStringKey(objIdAndClassCode.get(response.getObjectId()), response.getInfoCode())));
        List<RcInfoCodeCfg> infoCodeCfgs =
                this.localRedisCacheHandler.batchGetInfoCodeConfigs(classCodeAndInfoCodeKeys);
        // 通过  classCode + infoCode  作为 key
        Map<String, RcInfoCodeCfg> infoCodeCfgMap =
                infoCodeCfgs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getClassCodeAndInfoCode, v -> v));

        responses.parallelStream().forEach(response -> {
            ObjectIotData objectIotData = new ObjectIotData();
            objectIotData.setObjectId(response.getObjectId());
            objectIotData.setInfoCode(response.getInfoCode());
            objectIotData.setDataValue(String.valueOf(response.getIpValue()));
            objectIotData.setDataTime(response.getIpTime());
            // 填充单位和名称
            RcInfoCodeCfg infoCodeCfg =
                    infoCodeCfgMap.get(objIdAndClassCode.get(response.getObjectId()) + response.getInfoCode());
            if (infoCodeCfg != null) {
                objectIotData.setUnit(infoCodeCfg.getUnit());
                objectIotData.setDataValueName(infoCodeCfg.getDataValueName(objectIotData.getDataValue()));
            } else {
                objectIotData.setDataValueName(objectIotData.getDataValue());
            }
            objectIotDataList.add(objectIotData);
        });

        return objectIotDataList;
    }

    @Override
    public String logTag() {
        return "云端iot信息点推送";
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
         * 运行监测类型
         *
         * <p>云端IOT订阅时仅需要拿到一级类型即可, 通过一级类型可以查询指定模版的所有IOT信息点</p>
         */
        @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
        private RunningMonitorTypeEnum runningMonitorType;

        /**
         * 模版类型
         *
         * @see #checkAndSet()
         */
        private InfoCodeTemplateTypeEnum templateType;

        /**
         * 模版类型
         *
         * @see #checkAndSet()
         */
        private Set<InfoCodeTemplateTypeEnum> templateTypes;

        /**
         * 设备类编码 -> 对象ID
         */
        @NotEmpty(message = "{key_58e1c03325b247d2854173e07d052d01}")
        private Map<String, Set<String>> claasCodeAndObjectIds;

        /**
         * 如果传递了这个参数 按照配置表获取数据会失效
         * 对象ID -> 信息点编码
         */
        private Map<String, Set<String>> objectIdAndInfoCodes;

        @Schema(description = "分组编码, 可以为空")
        private Set<String> groupCodes;

        /**
         * 获取模版类型
         *
         * @return 模版类型
         * @since 7.250201.0
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
            if (checkAssembleCollectionNull(this.templateType, this.templateTypes)) {
                throw new ValidationException(LocaleMessageUtil.getMessage("key_8356b8ff6af845ad99d6033c92e55c61"));
            }
        }
    }

    @Data
    public class ObjectIotData {
        /**
         * 对象实例Id
         */
        private String objectId;
        /**
         * 消息点编码
         */
        private String infoCode;
        /**
         * 单位
         */
        private String unit;
        /**
         * 孪生值
         */
        private String dataValue;
        /**
         * 孪生值
         */
        private String dataValueName;
        /**
         * 时间
         */
        private String dataTime;
        /**
         * 时间常量
         */
        private String dataIndate;

    }
}
