package com.persagy.apccontrol.common.outside.rs;

import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.common.model.param.strategy.chiller.QuerySeasonOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

/**
 * 云端客户端服务
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface RcSaasClientService extends BaseFeignClientService {

    /**
     * 需求管理的空间范围/需求管理配置的空间
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    List<String> getDemandConfigSpace(String groupCode, String projectId);

    /**
     * 查询时间区间有的供暖工冷季
     *
     * @param param
     * @return
     */
    TreeMap<LocalDateTime, String> querySeasonByTime(QuerySeasonOrder param);

}
