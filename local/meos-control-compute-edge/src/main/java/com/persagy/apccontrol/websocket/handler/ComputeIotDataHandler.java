package com.persagy.apccontrol.websocket.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.entity.InfoCodeInfo;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.TriggerFormulaInfo;
import com.persagy.apccontrol.basic.model.enumtype.EnumIotDataType;
import com.persagy.apccontrol.basic.model.enumtype.OnOffStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeManualAutoSetEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.airconditioning.ForceControlStatusEditParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.sse.SseEmitterEventEnum;
import com.persagy.apccontrol.basic.sse.SubscribeTypeEnum;
import com.persagy.apccontrol.basic.sse.strategy.SseHandlerContext;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorAirConditioningColdSourceStrategy;
import com.persagy.apccontrol.basic.utils.ControlLogUtil;
import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.handle.InfoCodeLinkControlHandler;
import com.persagy.apccontrol.common.server.RunningMonitorServerEdge;
import com.persagy.apccontrol.common.service.IWSMessageService;
import com.persagy.apccontrol.common.service.IotDataCacheService;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 计算信息点值
 *
 * @author shaohongbo
 * @date 2023年04月07日 10:12
 */
@Component
@Slf4j
public class ComputeIotDataHandler {

    @Value("${paas.decelerate.second:180}")
    private int decelerateSecond;

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private IWSMessageService iWSMessageService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private IotDataCacheService iotDataCacheService;

    @Autowired
    private InfoCodeLinkControlHandler infoCodeLinkControlHandler;

    @Autowired
    private RunningMonitorServerEdge runningMonitorServerEdge;

    @Autowired
    private RunningMonitorAirConditioningColdSourceStrategy runningMonitorAirConditioningColdSourceStrategy;

    @Resource
    SseHandlerContext sshHandlerContext;

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * 计算IOT数据的前置判断逻辑
     *
     * @param source
     * @param infoCodeCfg
     */
    public boolean checkComputeInfoCodeDataCondition(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
        // 当手自动设定信息点为自动时清空强控状态
        if (checkManualAutoSetIsAuto(source)) {
            return true;
        }
        if (infoCodeCfg == null || StrUtil.isBlank(source.getDataValue()) || source.getDataStatus() == null || CommonConst.STRING_VALUE_INFO_CODES.contains(source.getInfoCode())) {
            return false;
        }

        // LogExecutor.instance().error(log, "计算IOT数据的前置判断逻辑",source.getInfoCode());
        TriggerFormulaInfo triggerFormulaInfo = infoCodeCfg.getExtendInfo() == null ? null : infoCodeCfg.getExtendInfo().getTriggerFormulaInfo();
        try {
            // 优先进行.换算系数、保留IOT小数位计算
            BigDecimal value = new BigDecimal(source.getDataValue());
            if (infoCodeCfg.getScale() != null) {
                value = value.multiply(infoCodeCfg.getScale());
            }
            if (infoCodeCfg.getIotDecimal() != null) {
                value = value.setScale(infoCodeCfg.getIotDecimal(), BigDecimal.ROUND_HALF_UP);
            }
            source.setDataValue(value.toPlainString());

            // 孪生数据的二次计算的判断逻辑
            if (EnumIotDataType.FEEDBACK.getCode().equals(source.getType())) {
                if (source.getDataStatus() != 1) {
                    return false;
                }
                // 控返不一致的判断
//                if (InfoCodeEnum.ORDER_FAIL_ALARM.getCode().equals(source.getInfoCode()) && triggerFormulaInfo != null) {
//                    return true;
//                }
                // 空气源热泵的判断逻辑
                boolean heatGroup = "ACAHC".equals(source.getClassCode()) || "ACCCAH".equals(source.getClassCode());
                if (heatGroup && InfoCodeEnum.RUN_STATUS.getCode().equals(source.getInfoCode())) {
                    return true;
                }
            }

            // 控制数据的二次计算判断逻辑
            if (EnumIotDataType.CTRL.getCode().equals(source.getType()) && triggerFormulaInfo != null) {
                // 判断一下前后两次的控制数据，防止完成状态的IOT数据先上来
                FeedbackInfoMsgMqttVO beforeData = this.iotDataCacheService.queryIotCtrlDataVO(source.getObjectId(), source.getInfoCode(), false);
                if (beforeData != null && beforeData.getId().equals(source.getId()) && beforeData.getDataStatus() != null && beforeData.getDataStatus() == 2) {
                    // 如果相同ID的IOT消息，当已完成的状态先上来时，不在处理进行中得状态
                    ControlLogUtil.error("原消息：{}，丢弃消息：{}", beforeData, source);
                    throw new BusinessException(ResponseCode.SERVICE_ERROR_C0122.getCode(), LocaleMessageUtil.getMessage("key_ef7a216674214579ba396ac70e7f6e06"));
                }
                return true;
            }
        } catch (Exception e) {
            log.error("信息点的二次计算，前置条件判断异常，source: {}", source, e);
        }
        return false;
    }

    /**
     * 计算IOT数据，并进行数据的保存与推送
     *
     * @param source
     * @param infoCodeCfg
     */
    public List<FeedbackInfoMsgMqttVO> computeInfoCodeData(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
        ReentrantLock lock = locks.computeIfAbsent(source.getObjectId() + "_" + source.getInfoCode(),
                k -> new ReentrantLock());
        lock.lock();
        try {
            DailyStringCounter.incrementCount("mqtt衍生计算总数");
            // 当手自动设定信息点为自动时清空强控状态
            if (checkManualAutoSetIsAuto(source)) {
                ForceControlStatusEditParam forceControlStatusEditParam = new ForceControlStatusEditParam();
                forceControlStatusEditParam.setObjectId(source.getObjectId());
                forceControlStatusEditParam.setStatus(OnOffStateEnum.CLOSE);
                runningMonitorAirConditioningColdSourceStrategy.editForceControlStatus(forceControlStatusEditParam);
            }
            // 空气源热泵的处理：PRD-0801 去除 updateACAHCInfoValue 调用，runStatus 直接使用 BDTP 直采原始值
            // LogExecutor.instance().error(log, "计算IOT数据",source.getInfoCode());

            // 信息点的扩展计算
            FeedbackInfoMsgMqttVO extendInfoCode = this.computeInfoCodeDataMap(source, infoCodeCfg);
            if (extendInfoCode != null && extendInfoCode.getInfoCode().equals(source.getInfoCode())) {
                // 扩展信息的计算，要是与当前信息点一致，当前信息点的IOT消息不在推送，直接丢弃
                return Lists.newArrayList(extendInfoCode);
            }
            return extendInfoCode == null ? Lists.newArrayList(source) : Lists.newArrayList(source, extendInfoCode);
        } catch (Exception e) {
            log.error("信息点的二次计算异常，source: {}", source, e);
        } finally {
            lock.unlock();
        }
        return Lists.newArrayList(source);
    }

    /**
     * 手工自动设定信息点是否为自动
     *
     * @param source 数据源
     * @return 手工自动设定信息点是否为自动
     * @since 7.250602.0
     */
    private boolean checkManualAutoSetIsAuto(FeedbackInfoMsgMqttVO source) {
        return Objects.equals(source.getInfoCode(), InfoCodeEnum.MANUAL_AUTO_SET.getCode())
                && StringUtils.equals(source.getDataValue(), DtpKnowledgeManualAutoSetEnum.AUTO.getValue());
    }

    /**
     * 数据推送与保存
     *
     * @param infoMsgMqttVOS
     * @param infoCodeCfg
     */
    public void pushMessage(List<FeedbackInfoMsgMqttVO> infoMsgMqttVOS, RcInfoCodeCfg infoCodeCfg) {
        if (CollectionUtil.isEmpty(infoMsgMqttVOS)) {
            return;
        }

        for (FeedbackInfoMsgMqttVO msgMqttVO : infoMsgMqttVOS) {
            if (infoCodeCfg != null) {
                msgMqttVO.setUnit(infoCodeCfg.getUnit());
                if (msgMqttVO.getInfoCode().equals(infoCodeCfg.getInfoCode())) {
                    msgMqttVO.setDataValueName(infoCodeCfg.getDataValueName(msgMqttVO.getDataValue()));
                }
            }

            // 本方数据存入
            this.iotDataCacheService.insert(msgMqttVO, infoCodeCfg);

            // 数据推送
            if (InfoCodeEnum.RC_RUN_STATUS.getCode().equals(msgMqttVO.getInfoCode()) || EnumIotDataType.FEEDBACK.getCode().equals(msgMqttVO.getType())) {
                // 仅对重计算运行状态信息点记录推送结果日志
                boolean isRcRunStatus = InfoCodeEnum.RC_RUN_STATUS.getCode().equals(msgMqttVO.getInfoCode());

                try {
                    iWSMessageService.pushMessage(msgMqttVO);

                    // 推送成功，记录日志
                    if (isRcRunStatus) {
                        ControlLogUtil.info(
                                "[重计算运行状态-已推送] 设备:{} | 信息点:{} | 值:{}({}) | 状态:{} | 结果:推送成功",
                                msgMqttVO.getObjectId(),
                                msgMqttVO.getInfoCode(),
                                msgMqttVO.getDataValue(),
                                getStatusName(msgMqttVO.getDataValue()),
                                msgMqttVO.getDataStatus());

                        // 过渡态需要保留方向：开启中只能由运行状态=运行结束，关闭中只能由运行状态=停止结束。
                        String dataValue = msgMqttVO.getDataValue();
                        if ("2".equals(dataValue) || "3".equals(dataValue)) {
                            this.putRcRunStatusTracking(msgMqttVO.getClassCode(), msgMqttVO.getObjectId(), dataValue);
                            ControlLogUtil.info(
                                    "[重计算运行状态-标记] 设备:{} | 设备类型:{} | 重计算运行状态:{} | 结果:已添加补偿跟踪标记",
                                    msgMqttVO.getObjectId(),
                                    msgMqttVO.getClassCode(),
                                    dataValue);
                        } else {
                            // 重计算运行状态已回到稳定态，清理遗留标记，避免定时任务重复补偿。
                            this.removeRcRunStatusTracking(msgMqttVO.getClassCode(), msgMqttVO.getObjectId());
                        }
                    }
                } catch (Exception e) {
                    // 推送失败，记录日志
                    if (isRcRunStatus) {
                        ControlLogUtil.error(
                                "[重计算运行状态-已推送] 设备:{} | 信息点:{} | 值:{}({}) | 结果:推送失败 | 异常:{}",
                                msgMqttVO.getObjectId(),
                                msgMqttVO.getInfoCode(),
                                msgMqttVO.getDataValue(),
                                getStatusName(msgMqttVO.getDataValue()),
                                e.getMessage(), e);
                    }
                    // 重新抛出异常，保持原有逻辑
                    throw e;
                }
            }
        }
    }

    /**
     * 更新信息点的推送值，但是存储的还是旧版
     *
     * @param msgMqttVO
     */
    private void updateACAHCInfoValue(FeedbackInfoMsgMqttVO msgMqttVO) {
        boolean heatGroup = "ACAHC".equals(msgMqttVO.getClassCode()) || "ACCCAH".equals(msgMqttVO.getClassCode());
        if (!heatGroup || !InfoCodeEnum.RUN_STATUS.getCode().equals(msgMqttVO.getInfoCode())) {
            return;
        }

        try {
            FeedbackInfoMsgMqttVO codeValue = this.runningMonitorServerEdge.getGroupObjectMessage(msgMqttVO.getGroupCode(), msgMqttVO.getProjectId(), msgMqttVO.getObjectId(), msgMqttVO.getClassCode(), msgMqttVO.getInfoCode());
            if (codeValue != null && StrUtil.isNotBlank(codeValue.getDataValue())) {
                msgMqttVO.setDataValue(codeValue.getDataValue());
                msgMqttVO.setDataStatus(codeValue.getDataStatus());
            }
        } catch (Exception e) {
            log.error("空气源热泵-运行状态，重计算异常", e);
        }
    }

    /**
     * sse推送
     *
     * @param source
     * @author yuecaipu
     * @date 2024/5/8 10:09
     * @since 24Q1 T3S2-5.18.0
     */
    public void sseStrategy(FeedbackInfoMsgMqttVO source) {
        // iot数据变更推送
        sshHandlerContext.getStrategy(SseEmitterEventEnum.IOT_DATA_CHANGE).doOperation(source.getProjectId(), source);

        // 照明回路对象
        if (DtpRwdClassCodeEnum.LIGHTING_CIRCUIT.getValue().equals(source.getClassCode())) {
            // 当前需求满足情况计算推送
            if (InfoCodeEnum.RUN_STATUS.getCode().equals(source.getInfoCode())) {
                sshHandlerContext.getStrategy(SseEmitterEventEnum.LIGHT_LOOP).doOperation(source.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
            }
            // 空间状态计算推送
            if (InfoCodeEnum.ORDER_FAIL_ALARM.getCode().equals(source.getInfoCode())
                    || InfoCodeEnum.ONLINE_STATUS.getCode().equals(source.getInfoCode())
                    || InfoCodeEnum.RUN_STATUS.getCode().equals(source.getInfoCode())) {
                sshHandlerContext.getStrategy(SseEmitterEventEnum.LIGHT_SPACE_STATUS).doOperation(source.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
            }
            // 当前设备状态计算推送
            if (InfoCodeEnum.MANUAL_AUTO_STATUS.getCode().equals(source.getInfoCode())
                    || InfoCodeEnum.ONLINE_STATUS.getCode().equals(source.getInfoCode())
                    || InfoCodeEnum.ORDER_FAIL_ALARM.getCode().equals(source.getInfoCode())) {
                sshHandlerContext.getStrategy(SseEmitterEventEnum.LIGHT_EQUIPMENT_STATUS).doOperation(source.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
            }
        }
        // 项目对象
        if (DtpRwdClassCodeEnum.PROJECT.getValue().equals(source.getClassCode())) {
            //  室外照度 触发 照度监测计算推送
            if (InfoCodeEnum.OUTDOOR_ILLUMINANCE.getValue().equals(source.getInfoCode())) {
                sshHandlerContext.getStrategy(SseEmitterEventEnum.ILLUM_INFO).doOperation(source.getProjectId(), SubscribeTypeEnum.LIGHT_MONITORING);
            }
        }

    }

    /**
     * 获取状态值对应的中文名称
     *
     * @param status 状态值
     * @return 中文名称
     */
    private String getStatusName(String status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "-1":
                return "无效";
            case "0":
                return "停止";
            case "1":
                return "运行";
            case "2":
                return "开启中";
            case "3":
                return "关闭中";
            default:
                return "未知(" + status + ")";
        }
    }

    /**
     * 写入{@code rcRunStatus}补偿跟踪标记。
     *
     * @param classCode        设备类型编码
     * @param objectId         设备ID
     * @param transitionStatus 过渡状态
     */
    @SuppressWarnings("unchecked")
    private void putRcRunStatusTracking(String classCode, String objectId, String transitionStatus) {
        String trackingKey = RedisKey.buildRcRunStatusTrackingHashKey(projectCfg.getProjectId(), classCode);
        redisCacheUtil.getRedisTemplate().opsForHash().put(trackingKey, objectId, transitionStatus);
    }

    /**
     * 删除{@code rcRunStatus}补偿跟踪标记。
     *
     * @param classCode 设备类型编码
     * @param objectId  设备ID
     */
    @SuppressWarnings("unchecked")
    private void removeRcRunStatusTracking(String classCode, String objectId) {
        String trackingKey = RedisKey.buildRcRunStatusTrackingHashKey(projectCfg.getProjectId(), classCode);
        redisCacheUtil.getRedisTemplate().opsForHash().delete(trackingKey, objectId);
    }

    /**
     * 扩展信息点处理
     *
     * @param source      原始IOT数据
     * @param infoCodeCfg 信息点配置
     * @return 扩展信息点的IOT数据，如果不需要扩展则返回null
     */
    private FeedbackInfoMsgMqttVO computeInfoCodeDataMap(FeedbackInfoMsgMqttVO source, RcInfoCodeCfg infoCodeCfg) {
        TriggerFormulaInfo triggerFormulaInfo = this.infoCodeLinkControlHandler.checkTriggerFormulaInfo(source.getObjectId(), infoCodeCfg);
        if (triggerFormulaInfo == null) {
            // 记录跳过计算的日志
            ControlLogUtil.warn(
                    "[重计算运行状态-跳过] 设备:{} | 触发:{} | 原因:配置检查未通过 | 说明:详见[重计算运行状态-配置检查]日志",
                    source.getObjectId(),
                    source.getInfoCode());
            return null;
        }

        // 构造扩展信息点的IOT数据
        String infoCodeValue = this.getTriggerFormulaInfoCodeValue(source, triggerFormulaInfo, infoCodeCfg);
        if (infoCodeValue != null) {
            // 计算成功，记录日志
            ControlLogUtil.info(
                    "[重计算运行状态-计算成功] 设备:{} | 触发:{} | 结果信息点:{} | 计算值:{}({}) | 说明:准备推送",
                    source.getObjectId(),
                    source.getInfoCode(),
                    triggerFormulaInfo.getTriggerInfoCode(),
                    infoCodeValue, getStatusName(infoCodeValue));

            FeedbackInfoMsgMqttVO infoMsgMqttVO = new FeedbackInfoMsgMqttVO();
            BeanUtil.copyProperties(source, infoMsgMqttVO);
            infoMsgMqttVO.setInfoCode(triggerFormulaInfo.getTriggerInfoCode());
            infoMsgMqttVO.setDataValue(infoCodeValue);
            infoMsgMqttVO.setDataStatus(1);
            infoMsgMqttVO.setInvalid(0);
            infoMsgMqttVO.setType(EnumIotDataType.FEEDBACK.getCode());
            return infoMsgMqttVO;
        } else {
            // 计算失败，记录日志
            ControlLogUtil.error(
                    "[重计算运行状态-计算失败] 设备:{} | 触发:{} | 结果信息点:{} | 原因:计算结果为空",
                    source.getObjectId(),
                    source.getInfoCode(),
                    triggerFormulaInfo.getTriggerInfoCode());
        }
        return null;
    }

    @Nullable
    private BigDecimal queryInfoCodeDataValueFromCache(FeedbackInfoMsgMqttVO source, InfoCodeInfo infoCode, RcInfoCodeCfg infoCodeCfg) {
        FeedbackInfoMsgMqttVO iotRealDataVO = this.iotDataCacheService.queryIotDataVOByDataType(source.getObjectId(), infoCode.getInfoCode(), infoCodeCfg, EnumIotDataType.resolve(infoCode.getDataType()));
        BigDecimal dataValue = null;
        try {
            if (iotRealDataVO != null) {
                if (iotRealDataVO.getType().equals(EnumIotDataType.FEEDBACK.getCode()) &&
                        iotRealDataVO.getDataStatus() != null &&
                        iotRealDataVO.getDataStatus().equals(EnumIotDataType.CTRL.value())) {
                    // 正常采集值数据过滤
                    dataValue = new BigDecimal(iotRealDataVO.getDataValue());
                } else if (iotRealDataVO.checkValidData() && iotRealDataVO.getDataValue() != null) {
                    // 控制值为完成状态或者控制值等级不为8时，不参与信息点计算逻辑
                    dataValue = new BigDecimal(iotRealDataVO.getDataValue());
                }
            }
            dataValue = dataValue == null ? null : dataValue.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : dataValue;
        } catch (Exception e) {
            e.printStackTrace();
            ControlLogUtil.error("mqtt计算信息点值报错:{}", source, e);
        }
        return dataValue;
    }

    /**
     * 获取当前IOT数据信息点，对应的扩展信息点的计算数据值
     *
     * @param source
     * @return null-不更新扩展信息点的值，not null-更新扩展信息点的值，"null"-丢弃
     */
    private String getTriggerFormulaInfoCodeValue(FeedbackInfoMsgMqttVO source, TriggerFormulaInfo triggerFormulaInfo, RcInfoCodeCfg infoCodeCfg) {

        // 控制指令状态监检查
        if (triggerFormulaInfo.getSourceIotType() == null || triggerFormulaInfo.getSourceIotType() == 0) {
            return this.checkControlRealStatus(source, triggerFormulaInfo);
        }

        // 根据表达式，计算出扩展信息点的值
        try {
            Map<String, Object> infoCodeAndDataValue = new HashMap<>();
            List<InfoCodeInfo> variables = triggerFormulaInfo.getVariables();
            variables.forEach(infoCodeInfo -> {
                if (source.getInfoCode().equals(infoCodeInfo.getInfoCode())) {
                    // 如果当前信息点与上报的IOT消息信息点一致，取当前最新的IOT数据，参与计算
                    infoCodeAndDataValue.put(infoCodeInfo.getInfoCode(), new BigDecimal(source.getDataValue()));
                } else {
                    infoCodeAndDataValue.put(infoCodeInfo.getInfoCode(), this.queryInfoCodeDataValueFromCache(source, infoCodeInfo, infoCodeCfg));
                }
            });

            // IOT数据计算
            Object execute = AviatorEvaluator.execute(triggerFormulaInfo.getTriggerFormula(), infoCodeAndDataValue);
            String result = execute == null ? null : execute.toString();

            // 记录公式计算日志
            ControlLogUtil.info(
                    "[重计算运行状态-公式计算] 设备:{} | 触发:{}={} | 变量:{} | 公式结果:{}({})",
                    source.getObjectId(),
                    source.getInfoCode(), source.getDataValue(),
                    infoCodeAndDataValue,
                    result, getStatusName(result));

            return result;
        } catch (Exception e) {
            ControlLogUtil.error(
                    "[重计算运行状态-异常] 设备:{} | 触发:{}={} | 方法:获取触发公式信息点值 | 异常:{}",
                    source.getObjectId(),
                    source.getInfoCode(), source.getDataValue(),
                    e.getMessage(), e);
        }
        return null;
    }

    /**
     * 判断控制指令当前实际的状态
     *
     * @param source
     * @param triggerFormulaInfo
     * @return
     */
    private String checkControlRealStatus(FeedbackInfoMsgMqttVO source, TriggerFormulaInfo triggerFormulaInfo) {
        try {
            Set<String> infoCodes = triggerFormulaInfo.getVariables().stream().map(InfoCodeInfo::getInfoCode).collect(Collectors.toSet());

            String runStatus = this.iotDataCacheService.queryIotRealData(source.getObjectId(), "runStatus", null);
            String onlineStatus = this.iotDataCacheService.queryIotRealData(source.getObjectId(), "onlineStatus", null);

            String result;
            String decisionReason;

            // 如果当前IOT数据处于 进行中得状态，直接更新为进行中
            if (source.checkProcessData()) {
                result = "1".equals(source.getDataValue()) ? "2" : "3";
                decisionReason = String.format("进行中数据直接判断(数据值=%s)", source.getDataValue());

                ControlLogUtil.info(
                        "[重计算运行状态-计算] 设备:{} | 触发:{}={} | 数据状态:2(进行中) | " +
                                "缓存:在线状态={}, 运行状态={} | 决策:{} → {} | 结果:{}",
                        source.getObjectId(),
                        source.getInfoCode(), source.getDataValue(),
                        onlineStatus, runStatus,
                        decisionReason, result, result);

                return result;
            }

            // 返回运行状态
            boolean hasOnlineCheck = infoCodes.contains("onlineStatus");

            if (hasOnlineCheck) {
                boolean isOnline = "1".equals(onlineStatus) || "1.0".equals(onlineStatus);
                result = isOnline ? runStatus : "-1";
                decisionReason = String.format("在线状态检查(在线状态=%s, 是否在线=%s)", onlineStatus, isOnline);
            } else {
                result = runStatus;
                decisionReason = "无在线状态检查，直接返回运行状态";
            }

            ControlLogUtil.info(
                    "[重计算运行状态-计算] 设备:{} | 触发:{}={} | 数据状态:1(完成) | " +
                            "缓存:在线状态={}, 运行状态={} | 决策:{} → {} | 结果:{}",
                    source.getObjectId(),
                    source.getInfoCode(), source.getDataValue(),
                    onlineStatus, runStatus,
                    decisionReason, result, result);

            return result;
        } catch (Exception e) {
            ControlLogUtil.error(
                    "[重计算运行状态-异常] 设备:{} | 触发:{}={} | 方法:检查控制实际状态 | 异常:{}",
                    source.getObjectId(),
                    source.getInfoCode(), source.getDataValue(),
                    e.getMessage(), e);

            if (e instanceof BusinessException) {
                throw e;
            }
        }
        return null;
    }

    /**
     * 检查控制级别
     *
     * @param current
     * @return -1-当前IOT数据丢弃，0-当前IOT数据不丢弃，但不进行进行中的状态更新，1-当前IOT数据参与到进行中的状态判断
     */
    private int checkCtrlLevel(FeedbackInfoMsgMqttVO current) {
        if (!EnumIotDataType.CTRL.getCode().equals(current.getType())) {
            return 0;
        }
        FeedbackInfoMsgMqttVO infoMsgMqttVO = this.iotDataCacheService.queryIotCtrlDataVO(current.getObjectId(), current.getInfoCode(), false);
        if (infoMsgMqttVO == null) {
            return 1;
        }
        if (current.getCmdLevel().equals(infoMsgMqttVO.getCmdLevel())) {
            return 1;
        }
        if ("12".equals(infoMsgMqttVO.getCmdLevel()) && "8".equals(current.getCmdLevel())) {
            return 1;
        }
        if ("8".equals(infoMsgMqttVO.getCmdLevel()) && "12".equals(current.getCmdLevel())) {
            return -1;
        }
        return 0;
    }

}
