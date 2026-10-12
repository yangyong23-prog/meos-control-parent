package com.persagy.apccontrol.basic.mapper.infocodeconfig;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.InfoCodeConfig;
import com.persagy.apccontrol.basic.model.entity.infocodeconfig.IotMonitorConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 信息点配置表Mapper
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Mapper
@Repository
public interface InfoCodeConfigMapper extends BaseMapper<InfoCodeConfig> {


    /**
     * 查询iot孪生推送配置
     *
     * @return {@link List<IotMonitorConfig>}
     * @author yuecaipu
     * @date 2024/8/31 14:09
     * @since v5.240802
     */
    @Select("SELECT DISTINCT  b.running_monitor_type ,b.class_code ,a.info_code  FROM meos_control.object_info_code_config a , object_info_code_template_type b  WHERE a.rel_template_type_id = b.id and a.info_code_type in ('IOT_SETTING','IOT_RUNNING')")
    List<IotMonitorConfig> listIotMonitorConfig();
}