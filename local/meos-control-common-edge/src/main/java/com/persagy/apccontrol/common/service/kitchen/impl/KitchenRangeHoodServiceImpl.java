package com.persagy.apccontrol.common.service.kitchen.impl;

import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.kitchen.AbstractKitchenRangeHoodService;
import com.persagy.apccontrol.common.outside.feign.DtpTwinClient;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 厨房排油烟边缘端
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KitchenRangeHoodServiceImpl extends AbstractKitchenRangeHoodService {

    private final DtpTwinClient dtpTwinClient;

    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(Map<String, Set<String>> param) {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        DtpRwdCommonParam commonParam = DtpRwdCommonParam.instance(groupCode, projectId);
        CustomerResponse<Map<String, KitChenRangeHoodEquipStatisticsVo>> response = dtpTwinClient.kitChenRangeHoodEquipStatistics(commonParam, param);

        Map<String, KitChenRangeHoodEquipStatisticsVo> map = CustomerResponse.handleResult(response);

        //边缘端数据返回的是 acklsfInfo acvtsfInfo acklapInfo 对数据特殊处理
        Map<String, Set<String>> paramConvert = new HashMap<>();
        param.forEach((k, v) -> {
            if (StringUtils.equals(k, "acklsfObjIds")) {
                paramConvert.put("acklsfInfo", v);
            } else if (StringUtils.equals(k, "acatfuObjIds")) {
                paramConvert.put("acatfuInfo", v);
            } else if (StringUtils.equals(k, "acvtsfObjIds")) {
                paramConvert.put("acvtsfInfo", v);
            } else if (StringUtils.equals(k, "acklapObjIds")) {
                paramConvert.put("acklapInfo", v);
            }
        });

        Map<String, KitChenRangeHoodEquipStatisticsVo> r = buildEmptyResult(paramConvert);
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((k, v) -> {
                KitChenRangeHoodEquipStatisticsVo vo = r.get(k);
                if (vo == null) {
                    return;
                }
                v.setTotal(paramConvert.get(k).size());
                vo.setAlarmControl(v.getAlarmControl());
                vo.setRunStatus(v.getRunStatus());
                r.put(k, vo);
            });
        }
        return r;
    }

}
