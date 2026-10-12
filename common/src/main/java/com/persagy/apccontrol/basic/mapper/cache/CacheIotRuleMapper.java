package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheIotRule;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 缓存-IOT规则Mapper
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Mapper
@Repository
public interface CacheIotRuleMapper extends BaseMapper<CacheIotRule> {

}
