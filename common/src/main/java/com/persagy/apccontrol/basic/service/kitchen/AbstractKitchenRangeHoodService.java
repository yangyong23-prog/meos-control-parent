package com.persagy.apccontrol.basic.service.kitchen;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.kitchen.KitChenRangeHoodEquipStatisticsParamEnum;
import com.persagy.apccontrol.basic.model.vo.kitchen.AlarmControlVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.RunStatusVo;
import com.persagy.apccontrol.basic.outside.feign.model.response.kitchen.KitchenExhaustResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractKitchenRangeHoodService implements KitchenRangeHoodService {
    @Resource
    private DtpRwdClientService dtpRwdClientService;

    @Override
    public Map<String, Set<String>> handlerStaticAggregateToMap(String groupCode, String projectId) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectId);
        Map<String, Set<String>> result = Maps.newHashMap();
        List<KitchenExhaustResponse> staticAggregateResult = dtpRwdClientService.queryKitchenExhaust();
        if (CollectionUtils.isEmpty(staticAggregateResult)) {
            return result;
        }
        for (KitchenExhaustResponse item : staticAggregateResult) {
            Map<String, List<KitchenExhaustResponse>> aggregateMap = item.getAggregate();
            if (aggregateMap == null || aggregateMap.isEmpty()) {
                continue;
            }
            List<KitchenExhaustResponse> serviceSpace = aggregateMap.get(serviceSpaceKey);
            //排油烟机服务空间为空时，不参与统计
            if (serviceSpace == null || serviceSpace.isEmpty()) {
                continue;
            }
            Set<String> objIds = result.computeIfAbsent(KitChenRangeHoodEquipStatisticsParamEnum.ACKLSF_OBJIDS.getValue(),
                    k -> Sets.newHashSet());
            objIds.add(item.getId());
            aggregateMap.forEach((k, v) -> {
                if (DtpRwdClassCodeEnum.ACATFU.value().equals(k)) {
                    Set<String> objIdSet = result.computeIfAbsent(KitChenRangeHoodEquipStatisticsParamEnum.ACATFU_OBJIDS.value(),
                            k1 -> Sets.newHashSet());
                    if (CollectionUtils.isNotEmpty(v)) {
                        v.forEach(item1 -> objIdSet.add(item1.getId()));
                    }
                }
                if (DtpRwdClassCodeEnum.ACVTSF.value().equals(k)) {
                    Set<String> objIdSet = result.computeIfAbsent(KitChenRangeHoodEquipStatisticsParamEnum.ACVTSF_OBJIDS.value(),
                            k1 -> Sets.newHashSet());
                    if (CollectionUtils.isNotEmpty(v)) {
                        v.forEach(item1 -> objIdSet.add(item1.getId()));
                    }
                }
                if (DtpRwdClassCodeEnum.ACKLAP.value().equals(k)) {
                    Set<String> objIdSet = result.computeIfAbsent(KitChenRangeHoodEquipStatisticsParamEnum.ACKLAP_OBJIDS.value(),
                            k1 -> Sets.newHashSet());
                    if (CollectionUtils.isNotEmpty(v)) {
                        v.forEach(item1 -> objIdSet.add(item1.getId()));
                    }
                }
            });
        }
        return result;
    }

    /**
     * 构建空返回对象
     *
     * @param param
     * @param r
     */
    protected Map<String, KitChenRangeHoodEquipStatisticsVo> buildEmptyResult(Map<String, Set<String>> param) {
        Map<String, KitChenRangeHoodEquipStatisticsVo> r = new HashMap<>();
        //构建初始对象
        param.forEach((k, v) -> {
            KitChenRangeHoodEquipStatisticsVo vo = new KitChenRangeHoodEquipStatisticsVo();
            vo.setAlarmControl(AlarmControlVo.builder().build());
            vo.setTotal(param.get(k).size());
            vo.setRunStatus(new RunStatusVo());
            r.put(k, vo);
        });
        return r;
    }
}
