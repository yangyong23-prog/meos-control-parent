package com.persagy.apccontrol.common.server;

import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.dto.twin.StatisManageTerminalDTO;
import com.persagy.apccontrol.basic.model.param.airterminal.AirHomePageEquipmentParam;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.AirHomePageEquipmentVO;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.airterminal.impl.AirStatisticsServiceAbstract;
import com.persagy.apccontrol.common.outside.feign.DtpTwinClient;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Slf4j
@Service
public class AirStatisticsServiceEdge extends AirStatisticsServiceAbstract {


    @Resource
    private DtpTwinClient dtpTwinClient;


    @Override
    public List<AirHomePageEquipmentVO> homePageEquipment(AirHomePageEquipmentParam param) {


        Map<String, ObjectIdAndForceCodeQuery> queryParam = new HashMap<>();
        Map<String, Set<String>> eqList = queryEquipmentList(param);
        if (CollectionUtil.isEmpty(eqList)) {
            return Collections.emptyList();
        }
        eqList.forEach((k, v) -> {
            ObjectIdAndForceCodeQuery dutyHomePageQuery = new ObjectIdAndForceCodeQuery(v, null);
            dutyHomePageQuery.getObjectIds().addAll(v);
            queryParam.put(k, dutyHomePageQuery);
        });

        CustomerResponse<Map<String, StatisManageTerminalDTO>> r = dtpTwinClient.statisManageTerminal(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), queryParam);
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
