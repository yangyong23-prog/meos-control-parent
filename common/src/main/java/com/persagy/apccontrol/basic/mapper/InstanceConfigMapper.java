package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.instance.config.InstanceConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InstanceConfigMapper extends BaseMapper<InstanceConfig> {
}