package com.persagy.apccontrol.common.service.kitchen.impl;

import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.model.vo.kitchen.AlarmControlVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.RunStatusVo;
import com.persagy.apccontrol.basic.outside.dto.ExhaustFumeStatisticsVo;
import com.persagy.apccontrol.basic.outside.dto.param.ExhaustFumeStatisticsParam;
import com.persagy.apccontrol.basic.outside.feign.client.DtpPersistServerClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.kitchen.AbstractKitchenRangeHoodService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 厨房排油烟云端接口实现类
 */
@Service
public class KitchenRangeHoodServiceImpl extends AbstractKitchenRangeHoodService {

    @Autowired
    protected NacosSwitchConfig nacosSwitchConfig;

    @Resource
    private DtpPersistServerClient dtpPersistServerClient;

    @Override
    public Map<String, KitChenRangeHoodEquipStatisticsVo> queryEquipStatistics(Map<String, Set<String>> param) {

        if (MapUtils.isEmpty(param)) {
            return new HashMap<>();
        }

        ExhaustFumeStatisticsParam statisticsParam = new ExhaustFumeStatisticsParam();
        statisticsParam.buildObjects(param);
        statisticsParam.setIpTime(nacosSwitchConfig.getIotTimeOffset(DateUtils.now()));

        CustomerResponse<Map<String, ExhaustFumeStatisticsVo>> customerResponse = dtpPersistServerClient
                .watchExhaustFumeStatistics(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), statisticsParam);

        Map<String, ExhaustFumeStatisticsVo> map = CustomerResponse.handleResult(customerResponse);

        Map<String, KitChenRangeHoodEquipStatisticsVo> r = buildEmptyResult(param);
        if (MapUtils.isNotEmpty(map)) {
            map.forEach((k, v) -> {
                KitChenRangeHoodEquipStatisticsVo vo = r.get(k);
                v.setTotal(param.get(k).size());
                vo.setAlarmControl(AlarmControlVo.builder()
                        .autoStatus(v.getAutoTotal())
                        .localStatus(v.getLocalTotal())
                        .manualStatus(v.getManualTotal())
                        .offLine(v.getOffLineTotal())
                        .orderFailAlarm(v.getAlarmTotal())
                        .build());
                vo.setRunStatus(RunStatusVo.builder()
                        .off(v.getStopTotal())
                        .on(v.getRunTotal())
                        .unknown(v.getUnknown())
                        .build());
                r.put(k, vo);
            });
        }
        //之前接入的字段是 acklsfInfo 对数据进行转换
        Map<String, KitChenRangeHoodEquipStatisticsVo> convert = new HashMap<>();
        r.forEach((k, v) -> {
            if (StringUtils.equals(k, "acklsfObjIds")) {
                convert.put("acklsfInfo", v);
            } else if (StringUtils.equals(k, "acatfuObjIds")) {
                convert.put("acatfuInfo", v);
            } else if (StringUtils.equals(k, "acvtsfObjIds")) {
                convert.put("acvtsfInfo", v);
            } else if (StringUtils.equals(k, "acklapObjIds")) {
                convert.put("acklapInfo", v);
            }
        });

        return convert;
    }


}
