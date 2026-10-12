package com.persagy.apccontrol.service.impl;

import com.persagy.apccontrol.basic.model.enumtype.outside.energy.StatisticsTypeEnum;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarCircuitVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarDeviceStateVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarLightLoopTotalVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarStatisticalJumpVo;
import com.persagy.apccontrol.basic.model.vo.outside.energy.StatisticsVO;
import com.persagy.apccontrol.basic.outside.enums.ExceptionEventStatus;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import com.persagy.apccontrol.service.LightingStatisticsService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LightingStatisticsServiceImpl implements LightingStatisticsService {

    private final LightingService lightingService;
    private final Executor executor;
    private final JsonComponentModule jsonComponentModule;

    @Override
    public StatisticsVO getLightingStatistics(StatisticsTypeEnum type) {
        StatisticsVO result = new StatisticsVO();
        switch (type) {
            case LIGHTING_EQUIP_IN_SPACE_STATISTICS:
                getLightingEquipInSpaceStatistics(result);
                break;
            case LIGHTING_INFO_CODE_STATISTICS:
                getLightingInfoCodeStatistics(result);
                break;
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(type.getClass().getSimpleName(), type);
        }
        return result;
    }

    /**
     * 在空间统计中获取照明设备
     *
     * @param result 结果
     */
    private void getLightingEquipInSpaceStatistics(StatisticsVO result) {
        List<StatisticsVO.LightingEquipInSpaceStatistics> resultList = new ArrayList<>();
        String projectId = ContextUtil.getProjectId();

        List<CompletableFuture<StatisticsVO.LightingEquipInSpaceStatistics>> taskList = new ArrayList<>();
        for (DemandTemplateTypeEnum typeEnum : ALL_LIGHTING_TYPE_LIST) {
            CompletableFuture<StatisticsVO.LightingEquipInSpaceStatistics> future = CompletableFuture.supplyAsync(() -> {
                StatisticsVO.LightingEquipInSpaceStatistics statistics = new StatisticsVO.LightingEquipInSpaceStatistics();
                LightingInstanceQueryParam param = new LightingInstanceQueryParam();
                param.setProjectId(projectId);
                param.setInstanceId(projectId);
                param.setInstanceType(DemandInstanceTypeEnum.PROJECT);
                param.setDemandTemplateType(typeEnum);
                DemandTemplateVO demandTemplateVO = lightingService.queryCurrentDemandTemplate(typeEnum);
                statistics.setSpaceType(typeEnum);
                statistics.setSpaceTypeName(typeEnum.getReasonPhrase());
                statistics.setTemplateName(demandTemplateVO.getTemplateName());
                statistics.setTemplateTag(demandTemplateVO.getTemplateTag());
                List<LightingSidebarLightLoopTotalVO> lightingSidebarLightLoopTotalVOS = lightingService.listLightLoopTotal(param);
                long lessOpenNumber = lightingSidebarLightLoopTotalVOS.stream().filter(e -> Objects.nonNull(e.getLessOpenNumber())).mapToLong(LightingSidebarLightLoopTotalVO::getLessOpenNumber).sum();
                long moreOpenNumber = lightingSidebarLightLoopTotalVOS.stream().filter(e -> Objects.nonNull(e.getMoreOpenNumber())).mapToLong(LightingSidebarLightLoopTotalVO::getMoreOpenNumber).sum();
                long totalNumber = lightingService.listLightLoopByInstance(param).size();
                long realOnNumber = lightingSidebarLightLoopTotalVOS.stream().filter(e -> Objects.nonNull(e.getRealOnNumber())).mapToLong(LightingSidebarLightLoopTotalVO::getRealOnNumber).sum();
                statistics.setOpenLessNum(lessOpenNumber);
                statistics.setOpenMoreNum(moreOpenNumber);
                statistics.setOpenNum(realOnNumber);
                statistics.setTotal(totalNumber);
                return statistics;
            }, executor);
            taskList.add(future);
        }
        for (CompletableFuture<StatisticsVO.LightingEquipInSpaceStatistics> future : taskList) {
            resultList.add(future.join());
        }

        result.setLightingEquipInSpaceStatistics(resultList);
    }

    /**
     * 获取照明信息代码统计信息
     *
     * @param result 结果
     */
    private void getLightingInfoCodeStatistics(StatisticsVO result) {
        List<StatisticsVO.LightingInfoCodeStatistics> lightingInfoCodeStatistics = new ArrayList<>();
        String projectId = ContextUtil.getProjectId();
        List<CompletableFuture<StatisticsVO.LightingInfoCodeStatistics>> taskList = new ArrayList<>();
        for (DemandTemplateTypeEnum typeEnum : ALL_LIGHTING_TYPE_LIST) {
            CompletableFuture<StatisticsVO.LightingInfoCodeStatistics> future = CompletableFuture.supplyAsync(() -> {
                StatisticsVO.LightingInfoCodeStatistics statistics = new StatisticsVO.LightingInfoCodeStatistics();
                LightingInstanceQueryParam param = new LightingInstanceQueryParam();
                param.setProjectId(projectId);
                param.setInstanceId(projectId);
                param.setInstanceType(DemandInstanceTypeEnum.PROJECT);
                param.setDemandTemplateType(typeEnum);
                statistics.setSpaceType(typeEnum);
                statistics.setSpaceTypeName(typeEnum.getReasonPhrase());
                LightingSidebarDeviceStateVO lightingSidebarDeviceStateVO = lightingService.countEquipmentStatus(param);
                LightingSidebarCircuitVO lightingSidebarCircuitVo = lightingSidebarDeviceStateVO.getLightingSidebarCircuitVo();
                if (Objects.nonNull(lightingSidebarCircuitVo)) {
                    LightingSidebarStatisticalJumpVo inconformity = lightingSidebarCircuitVo.getInconformity();
                    if (Objects.nonNull(inconformity)) {
                        statistics.setOppositeNum(inconformity.getNumber());
                    }
                    LightingSidebarStatisticalJumpVo offline = lightingSidebarCircuitVo.getOffline();
                    if (Objects.nonNull(offline)) {
                        statistics.setOffEqNum(offline.getNumber());
                    }
                }
                List<ExceptionEventBaseInfoVO> abnormalDataList = lightingService.getAbnormalDataList(param);
                if (CollectionUtils.isNotEmpty(abnormalDataList)) {
                    List<ExceptionEventBaseInfoVO> unresolvedList = abnormalDataList.stream().filter(e -> e.getExceptionEventStatus() == ExceptionEventStatus.UNRESOLVED).collect(Collectors.toList());
                    statistics.setAbnormalEventNum(unresolvedList.size());
                }

                return statistics;
            }, executor);
            taskList.add(future);
        }
        for (CompletableFuture<StatisticsVO.LightingInfoCodeStatistics> future : taskList) {
            lightingInfoCodeStatistics.add(future.join());
        }
        result.setLightingInfoCodeStatistics(lightingInfoCodeStatistics);
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "能源外部接口-统计照明信息";
    }


}
