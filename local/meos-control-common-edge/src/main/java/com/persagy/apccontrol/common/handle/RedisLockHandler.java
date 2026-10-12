package com.persagy.apccontrol.common.handle;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.model.dto.AirConditionDemandDTO;
import com.persagy.framework.common.util.json.JsonUtils;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 涉及到需要进行加锁的redis操作
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/22 15:30
 */
@Slf4j
@Component
public class RedisLockHandler {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    /**
     * 获取空调需求时间
     *
     * @param projectId
     * @param spaceId
     */
    public List<AirConditionDemandDTO> getAirConditionDemand(String projectId, String spaceId) {
        String airConditionDemand = this.redisCacheUtil.hGet(RedisKey.getAirConditionDemandKey(projectId), spaceId);
        if (StrUtil.isBlank(airConditionDemand)) {
            return Lists.newArrayList();
        }
        return JsonUtils.fromJson(airConditionDemand, new TypeReference<List<AirConditionDemandDTO>>() {});
    }

    /**
     * 获取空调需求时间
     * @param projectId
     * @param spaceIds
     * @return
     */
    public Map<String, List<AirConditionDemandDTO>> queryAirConditionDemands(String projectId, List<String> spaceIds) {
        Map<String, List<AirConditionDemandDTO>> resultMap = new HashMap<>();

        for (String spaceId : spaceIds) {
            resultMap.put(spaceId, this.getAirConditionDemand(projectId, spaceId));
        }

        return resultMap;
    }
}
