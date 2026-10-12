package com.persagy.apccontrol.basic.mapper.infocodeconfig;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeGroup;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 信息点分组配置表Mapper
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Mapper
@Repository
public interface InfoCodeGroupMapper extends BaseMapper<InfoCodeGroup> {
}