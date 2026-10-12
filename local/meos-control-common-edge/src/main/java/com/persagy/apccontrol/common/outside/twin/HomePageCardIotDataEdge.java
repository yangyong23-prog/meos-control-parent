package com.persagy.apccontrol.common.outside.twin;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.dto.twin.DutyHomePageGroupDTO;
import com.persagy.apccontrol.basic.model.dto.twin.ObjectIdAndForceCodeQuery;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.SysResponseCode;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.service.adapter.HomePageCardIotDataAdapter;
import com.persagy.apccontrol.common.outside.feign.DtpTwinClient;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class HomePageCardIotDataEdge implements HomePageCardIotDataAdapter {
    @Resource
    private DtpTwinClient dtpTwinClient;

    @Override
    public DutyHomePageGroupDTO queryHomePageCardGroupIotData(Map<String, ObjectIdAndForceCodeQuery> queryParam, RunningMonitorTypeEnum runningMonitorType) {

        if (runningMonitorType == null || MapUtils.isEmpty(queryParam)) {
            return new DutyHomePageGroupDTO();
        }

        CustomerResponse<DutyHomePageGroupDTO> dtpTwinR = null;
        switch (runningMonitorType) {
            case LIGHTING:
                dtpTwinR = dtpTwinClient.ltltcc(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), queryParam);
                break;
            case HEAT_SOURCE:
            case COLD_SOURCE:
                dtpTwinR = dtpTwinClient.coolHeatSys(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), ObjectIdAndForceCodeQuery.getDutyHomePageQuery(queryParam));
                break;
            case AIR_TERMINAL:
                dtpTwinR = dtpTwinClient.terminal(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), queryParam);
                break;
            case VENTILATION:
                dtpTwinR = dtpTwinClient.acrex(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), queryParam);
                break;
            case KITCHEN_RANGE_HOOD:
                dtpTwinR = dtpTwinClient.acklsf(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), queryParam);
                break;
            default:
                throw new BusinessException(SysResponseCode.FAIL_K0001.getCode(), LocaleMessageUtil.getMessage("key_bb73b37b7b7048968897a3c8b1297806") + runningMonitorType);
        }
        DutyHomePageGroupDTO rVo = CustomerResponse.handleResult(dtpTwinR);

        //有子卡片场景
        if (rVo != null && rVo.getSubStat() != null) {
            //总数单独设置
            rVo.getSubStat().forEach((k, v) -> {
                ObjectIdAndForceCodeQuery objectQuery = queryParam.get(k);
                v.setTotal(objectQuery.getObjectIds().size());
            });
            return rVo;
        }
        //只有大卡片数据场景 冷热源
        if (rVo != null && rVo.getAlarmTotal() != 0 && rVo.getOffLineTotal() != 0) {
            return rVo;
        }
        //无返回 构建空数据
        return DutyHomePageGroupDTO.buildNullData(queryParam);
    }
}
