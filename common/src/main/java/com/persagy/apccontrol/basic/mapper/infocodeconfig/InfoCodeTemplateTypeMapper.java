package com.persagy.apccontrol.basic.mapper.infocodeconfig;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeTemplateType;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 设备模版类型配置Mapper
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Mapper
@Repository
public interface InfoCodeTemplateTypeMapper extends BaseMapper<InfoCodeTemplateType> {
}