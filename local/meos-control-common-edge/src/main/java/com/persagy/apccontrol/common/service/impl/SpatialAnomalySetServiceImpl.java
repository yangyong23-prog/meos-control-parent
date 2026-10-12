package com.persagy.apccontrol.common.service.impl;

import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.common.service.SpatialAnomalySetService;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @Description: 获取手自动查询设置
 * @Author: laixingqi
 * @Date: 2023/12/22 15:18
 * @param:
 * @return:
 **/
@Service
@Slf4j
public class SpatialAnomalySetServiceImpl implements SpatialAnomalySetService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    /**
     * @Description:  获取空间异常值
     * @Author: laixingqi
     * @Date: 2024/1/8 18:55
     * @param: [objectId]
     * @return: java.lang.String
     **/
    @Override
    public String getSpatialAnomaly(String objectId,String infoCode,String mark) {
        String key = RedisKey.getSpatialAnomaly(objectId, infoCode,mark);
        Object value = redisCacheUtil.get(key);
        return (String) value;
    }
}
