package com.persagy.apccontrol.basic.model.dto.twin;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 值班首页twin聚合查询返回对象
 */
@Data
public class DutyHomePageGroupDTO {

    /**
     * 离线设备实例总数
     */
    private int offLineTotal;
    /**
     * 控反不一致的设备实例总数
     */
    private int alarmTotal;
    /**
     * 每个模块统计信息 边缘端返回结果
     */
    private Map<String, DutyHomePageDTO> subStat;


    /**
     * 每个模块统计信息 云端返回结果
     */
    private Map<String, DutyHomePageDTO> statusStatistics;
    /**
     * 获取不带分组的数据
     * 取subStat的第一个 key
     */
    public DutyHomePageDTO getKitchenRangeHood() {
        if (CollectionUtil.isNotEmpty(subStat)) {
            return subStat.values().iterator().next();
        }
        return new DutyHomePageDTO();
    }

    /**
     * 构建空返回数据
     *
     * @param queryParam
     * @return
     */
    public static DutyHomePageGroupDTO buildNullData(Map<String, ObjectIdAndForceCodeQuery> queryParam) {

        if (queryParam == null || queryParam.isEmpty()) {
            return new DutyHomePageGroupDTO();
        }

        //构建空数据
        DutyHomePageGroupDTO rVo = new DutyHomePageGroupDTO();
        Map<String, DutyHomePageDTO> subStat = new HashMap<>();
        queryParam.forEach((k, v) -> {
            if (v != null && CollectionUtils.isNotEmpty(v.getObjectIds())) {
                DutyHomePageDTO dutyHomePageDTO = new DutyHomePageDTO();
                dutyHomePageDTO.setTotal(v.getObjectIds().size());
                subStat.put(k, dutyHomePageDTO);
            }
        });
        rVo.setSubStat(subStat);
        return rVo;
    }

}
