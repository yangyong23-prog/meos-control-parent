package com.persagy.apccontrol.common.server;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.SysResponseCode;
import com.persagy.apccontrol.basic.outside.dto.param.ExhaustFumeStatisticsParam;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.adapter.HomePageCardIotDataAdapter;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HomePageCardIotDataCloud implements HomePageCardIotDataAdapter {


    @Resource
    DtpPersistServerClient dtpPersistServerClient;
    @Autowired
    protected NacosSwitchConfig nacosSwitchConfig;

    @Override
    public DutyHomePageGroupDTO queryHomePageCardGroupIotData(Map<String, ObjectIdAndForceCodeQuery> queryParam, RunningMonitorTypeEnum runningMonitorType) {
        if (runningMonitorType == null) {
            return new DutyHomePageGroupDTO();
        }
        Map<String, Set<String>> classCodeAndObjects = new HashMap<>();
        queryParam.forEach((k, v) -> {
            if (v == null || CollectionUtils.isEmpty(v.getObjectIds())) {
                return;
            }
            classCodeAndObjects.put(k, new HashSet<>(v.getObjectIds()));
        });

        if (MapUtils.isEmpty(classCodeAndObjects)) {
            return new DutyHomePageGroupDTO();
        }

        ExhaustFumeStatisticsParam statisticsParam = new ExhaustFumeStatisticsParam();
        statisticsParam.buildObjects(classCodeAndObjects);
        statisticsParam.setIpTime(nacosSwitchConfig.getIotTimeOffset(DateUtils.now()));
        CustomerResponse<DutyHomePageGroupDTO> dtpR = null;
        DutyHomePageGroupDTO rVo = null;
        switch (runningMonitorType) {
            case LIGHTING:
                dtpR = dtpPersistServerClient.homepageLightingStatusStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);
                rVo = CustomerResponse.handleResult(dtpR);
                break;
            case HEAT_SOURCE:
            case COLD_SOURCE:
                //取出所有 val
                List<String> objectIds = new ArrayList<>();
                classCodeAndObjects.forEach((k, v) -> objectIds.addAll(v));
                dtpR = dtpPersistServerClient.homepageCoolHeatSysStatusStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(),
                        new ExhaustFumeStatisticsParam.CoolHeatParam(objectIds));
                rVo = CustomerResponse.handleResult(dtpR);
                break;
            case AIR_TERMINAL:
                dtpR = dtpPersistServerClient.homepageTerminalObjectStatusStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);
                rVo = CustomerResponse.handleResult(dtpR);
                break;
            case VENTILATION:
                CustomerResponse<Map<String, DutyHomePageDTO>> tf = dtpPersistServerClient.homepageFanEquipmentStatusStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);
                rVo = new DutyHomePageGroupDTO();
                Map<String, DutyHomePageDTO> statistics = CustomerResponse.handleResult(tf);

                //如果 ss 不为空 求离线和控反不一致总数
                if (MapUtils.isNotEmpty(statistics)) {
                    rVo.setOffLineTotal(statistics.values().stream().mapToInt(DutyHomePageDTO::getOffLineTotal).sum());
                    rVo.setAlarmTotal(statistics.values().stream().mapToInt(DutyHomePageDTO::getAlarmTotal).sum());
                }
                rVo.setStatusStatistics(statistics);
                break;
            case KITCHEN_RANGE_HOOD:
                dtpR = dtpPersistServerClient.homepageExhaustFumeStatusStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);
                rVo = CustomerResponse.handleResult(dtpR);
                break;
            default:
                throw new BusinessException(SysResponseCode.FAIL_K0001.getCode(), LocaleMessageUtil.getMessage("key_bb73b37b7b7048968897a3c8b1297806") + runningMonitorType);
        }

        if (rVo != null && rVo.getStatusStatistics() != null) {
            //云端聚合接口不会返回总数 总数单独设置
            rVo.getStatusStatistics().forEach((k, v) -> {
                ObjectIdAndForceCodeQuery objectQuery = queryParam.get(k);
                v.setTotal(objectQuery.getObjectIds().size());
            });
            rVo.setSubStat(rVo.getStatusStatistics());
            return rVo;
        }
        if (rVo != null && rVo.getAlarmTotal() != 0 && rVo.getOffLineTotal() != 0) {
            //冷热源场景只有大卡片数据
            return rVo;
        }
        return DutyHomePageGroupDTO.buildNullData(queryParam);
    }
}
