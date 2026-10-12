package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.model.dto.twin.StatisManageTerminalDTO;
import com.persagy.apccontrol.basic.model.param.airterminal.AirHomePageEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.AirHomePageEquipmentVO;
import com.persagy.apccontrol.basic.outside.dto.param.ExhaustFumeStatisticsParam;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.airterminal.impl.AirStatisticsServiceAbstract;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class AirStatisticsServiceCloud extends AirStatisticsServiceAbstract {

    @Autowired
    protected NacosSwitchConfig nacosSwitchConfig;
    @Resource
    DtpPersistServerClient dtpPersistServerClient;

    @Override
    public List<AirHomePageEquipmentVO> homePageEquipment(AirHomePageEquipmentParam param) {
        Map<String, Set<String>> eqList = queryEquipmentList(param);
        if (CollectionUtil.isEmpty(eqList)) {
            return Collections.emptyList();
        }
        ExhaustFumeStatisticsParam statisticsParam = new ExhaustFumeStatisticsParam();
        statisticsParam.buildObjects(eqList);
        statisticsParam.setIpTime(nacosSwitchConfig.getIotTimeOffset(DateUtils.now()));

        CustomerResponse<Map<String, StatisManageTerminalDTO>> r = dtpPersistServerClient
                .watchTerminalStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);

        Map<String, StatisManageTerminalDTO> map = CustomerResponse.handleResult(r);

        List<AirHomePageEquipmentVO> airHomePageEquipmentVOS = getInitAirHomePageEquipmentVO(eqList);

        if (MapUtils.isEmpty(map)) {
            setExceptionTotal(airHomePageEquipmentVOS, eqList);
            return airHomePageEquipmentVOS;
        }

        map.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(eqList.get(k))) {
                return;
            }
            v.setTotal(eqList.get(k).size());
            airHomePageEquipmentVOS.forEach(vo -> {
                if (vo.getClassCode().equals(k)) {
                    vo.setOpenTotal(v.getRunTotal());
                    vo.setCloseTotal(v.getStopTotal());
                    vo.setNoDataTotal(v.getNoDataTotal());
                    vo.setOrderFailAlarmTotal(v.getAlarmTotal());
                    vo.setOfflineTotal(v.getOffLineTotal());
                    vo.setManualTotal(v.getManualTotal());
                    vo.setLocalityTotal(v.getLocalTotal());
                    vo.setAutoTotal(v.getAutoTotal());
                }
            });
        });
        setExceptionTotal(airHomePageEquipmentVOS, eqList);
        return airHomePageEquipmentVOS;

    }
}
