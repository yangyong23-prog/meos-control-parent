package com.persagy.apccontrol.basic.service.abnormal.impl;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.convert.DtpRwdConvert;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventCommonParam;
import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventQueryParam;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.InfoCodeStaticFilter;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.abnormal.AbnormalEventService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorComputerRoomSecurityStrategy;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum.*;
import static com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum.MODEL_VIEW;
import static com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum.RISK_EVENT;
import static com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeGroupModel.OBJECT_STATUS_GROUP_NAMES;

/**
 * 异常事件通用查询
 *
 * @author XueTao
 * @date 2024/5/29 15:27
 * @since v5.240502.0
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class AbnormalEventServiceImpl implements AbnormalEventService, LogComponent {

    private final AbstractCommonServer abstractCommonServer;
    private final InfoCodeGateway infoCodeGateway;
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public List<ExceptionEventBaseInfoVO> listAbnormalEvent(AbnormalEventQueryParam abnormalEventQueryParam) {
        RunningMonitorTypeEnum runningMonitorType = abnormalEventQueryParam.getRunningMonitorType();
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s = abnormalEventQueryParam.getRiskMeasureAimLevel2();
        List<ExceptionEventBaseInfoVO> exceptionEventBaseInfoVOS = this.abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(abnormalEventQueryParam.getInstanceIds(), riskMeasureAimLevel2s);
        if (Objects.nonNull(runningMonitorType)) {
            Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition = runningMonitorType.getRunningMonitorStrategy().exceptionFilterCondition();
            if (exceptionFilterCondition != null) {
                exceptionEventBaseInfoVOS = exceptionFilterCondition.apply(exceptionEventBaseInfoVOS);
            }
        }
        return exceptionEventBaseInfoVOS;
    }

    @Override
    public List<ExceptionEventBaseInfoVO> listAbnormalEventNew(AbnormalEventQueryParam abnormalEventQueryParam) {
        long totalStart = System.currentTimeMillis();
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s = abnormalEventQueryParam.getRiskMeasureAimLevel2();
        long queryStart = System.currentTimeMillis();
        List<ExceptionEventBaseInfoVO> exceptionEventBaseInfoVOS = this.abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(abnormalEventQueryParam.getInstanceIds(), riskMeasureAimLevel2s);
        logProjectInstance().info("[性能] listAbnormalEventNew-查询异常事件[串行] 耗时: " + (System.currentTimeMillis() - queryStart) + "ms, 数据量: " + exceptionEventBaseInfoVOS.size());
        long getFilterStart = System.currentTimeMillis();
        Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition = COMPUTER_ROOM_SECURITY.getRunningMonitorStrategy().exceptionFilterCondition();
        logProjectInstance().info("[性能] listAbnormalEventNew-获取过滤条件[串行] 耗时: " + (System.currentTimeMillis() - getFilterStart) + "ms");
        if (exceptionFilterCondition != null) {
            long filterStart = System.currentTimeMillis();
            exceptionEventBaseInfoVOS = exceptionFilterCondition.apply(exceptionEventBaseInfoVOS);
            logProjectInstance().info("[性能] listAbnormalEventNew-执行过滤[串行] 耗时: " + (System.currentTimeMillis() - filterStart) + "ms, 过滤后数据量: " + exceptionEventBaseInfoVOS.size());
        }
        logProjectInstance().info("[性能] listAbnormalEventNew-总耗时: " + (System.currentTimeMillis() - totalStart) + "ms");
        return exceptionEventBaseInfoVOS;
    }

    @Override
    public String logTag() {
        return "AbnormalEventService";
    }

    @Override
    public List<ExceptionEventBaseInfoVO> listAbnormalEventBySpaceWithEquipments(@NonNull AbnormalEventQueryParam param) {
        RunningMonitorSecondTypeEnum runningMonitorSecondType = param.getRunningMonitorSecondType();
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s = param.getRiskMeasureAimLevel2();
        if (CollectionUtils.isEmpty(riskMeasureAimLevel2s) && Objects.nonNull(runningMonitorSecondType)) {
            riskMeasureAimLevel2s = runningMonitorSecondType.toRunningMonitorTypeEnum()
                    .getRunningMonitorStrategy().exceptionFilterRiskMeasureAimLevel2();
        }
        return abstractCommonServer.mapInstanceIdAndExceptionEventsWithEquipments(param.getInstanceIds(), riskMeasureAimLevel2s)
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    /**
     * 查询-按系统划分异常事件
     *
     * @param abnormalEventQueryParam 按系统划分异常事件入参
     * @return 异常事件列表
     */
    @Override
    public List<ExceptionEventBaseInfoVO> getAbnormalDataListDivideSystem(AbnormalEventCommonParam abnormalEventQueryParam) {
        RunningMonitorTypeEnum runningMonitorType = abnormalEventQueryParam.getRunningMonitorType();
        DemandInstanceTypeEnum instanceType = abnormalEventQueryParam.getInstanceType();
        String instanceId = abnormalEventQueryParam.getInstanceId();
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2 = abnormalEventQueryParam.getRiskMeasureAimLevel2();
        List<InfoCodeStaticFilter> staticInfoCodeFilters = new ArrayList<>();
        if (instanceType.equals(DemandInstanceTypeEnum.FLOOR)) {
            staticInfoCodeFilters.add(new InfoCodeStaticFilter()
                    .setInfoCode(DtpRwdConvert.INFO_CODE_FLOOR_ID)
                    .setDataValue(instanceId));
        }
        if (instanceType.equals(DemandInstanceTypeEnum.BUILDING)) {
            staticInfoCodeFilters.add(new InfoCodeStaticFilter()
                    .setInfoCode(DtpRwdConvert.INFO_CODE_BUILDING_ID)
                    .setDataValue(instanceId));
        }
        InfoCodeObjectQueryParam infoCodeObjectQueryParam = new InfoCodeObjectQueryParam();
        infoCodeObjectQueryParam.setRunningMonitorType(runningMonitorType);
        infoCodeObjectQueryParam.setTemplateTypes(Lists.newArrayList(MODEL_VIEW));
        if (CollectionUtils.isNotEmpty(staticInfoCodeFilters)) {
            infoCodeObjectQueryParam.setStaticInfoCodeFilters(staticInfoCodeFilters);
        }
        runningMonitorType.getRunningMonitorStrategy().setExceptionEventObjectQueryParam(infoCodeObjectQueryParam);
        List<DtpRwdObjectDTO> objects = infoCodeGateway.listTemplateTypeObjects(infoCodeObjectQueryParam);

        // 额外增加的对象信息
        Set<String> objectIdList = objects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        Collection<String> objectIds = abnormalEventQueryParam.getObjectIdList();
        if (CollectionUtils.isNotEmpty(objectIds)) {
            Collection<String> subtractObjectIdList = CollectionUtils.subtract(objectIds, objectIdList);
            if (CollectionUtils.isNotEmpty(subtractObjectIdList)) {
                List<DtpRwdObjectDTO> objectInfoList = dtpRwdClientService.listObject(
                        false, false, ContextUtil.getProjectId(),
                        new DtpRwdObjectQueryParam().setId(subtractObjectIdList));
                objects.addAll(objectInfoList);
            }
        }

        InfoCodeInitByObjectParam param = new InfoCodeInitByObjectParam();
        param.setRunningMonitorType(runningMonitorType);
        param.setTemplateTypes(Lists.newArrayList(MODEL_VIEW));
        param.setGroupCodes(OBJECT_STATUS_GROUP_NAMES);
        param.setInfoCodeTypes(Collections.singleton(RISK_EVENT));
        param.setObjects(objects);
        List<InfoCodeAggregate> infoCodeAggregateList = infoCodeGateway.of(param);
        objectIdList = infoCodeAggregateList.stream().map(x -> x.getIdentifier().getObject().getId()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(objectIdList)) {
            return new ArrayList<>();
        }

        // 基于监测类型获取条件
        if (CollectionUtils.isEmpty(riskMeasureAimLevel2)) {
            riskMeasureAimLevel2 = runningMonitorType.getRunningMonitorStrategy().exceptionFilterRiskMeasureAimLevel2();
        }
        return abstractCommonServer.listAbstractOverlayRiskControlTargetConditions(objectIdList, riskMeasureAimLevel2);
    }

    @Override
    public Map<String, List<ExceptionEventBaseInfoVO>> mapObjectIdAndAbnormalEventWithConditions(
            @NonNull AbnormalEventQueryParam abnormalDataQueryParam) {
        Collection<String> objectIdList = abnormalDataQueryParam.getInstanceIds();
        RunningMonitorTypeEnum runningMonitorType = abnormalDataQueryParam.getRunningMonitorType();
        Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s = abnormalDataQueryParam.getRiskMeasureAimLevel2();
        if (CollectionUtils.isEmpty(riskMeasureAimLevel2s) && Objects.nonNull(runningMonitorType)) {
            riskMeasureAimLevel2s = runningMonitorType.getRunningMonitorStrategy().exceptionFilterRiskMeasureAimLevel2();
        }
        return this.abstractCommonServer.mapRiskEvent(objectIdList, riskMeasureAimLevel2s);
    }

    @Override
    public Set<String> countAbnormalEvent(@NonNull AbnormalEventQueryParam abnormalDataQueryParam) {
        Map<String, List<ExceptionEventBaseInfoVO>> mapObjectIdAndEventsWithConditions =
                mapObjectIdAndAbnormalEventWithConditions(abnormalDataQueryParam);
        if (MapUtils.isEmpty(mapObjectIdAndEventsWithConditions)) {
            return new HashSet<>();
        }
        return mapObjectIdAndEventsWithConditions.keySet();
    }

}
