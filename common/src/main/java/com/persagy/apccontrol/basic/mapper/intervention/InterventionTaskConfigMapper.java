package com.persagy.apccontrol.basic.mapper.intervention;

import com.persagy.apccontrol.basic.model.entity.intervention.InterventionTaskConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
* @author yuyao
* @description 针对表【intervention_task_config(干预任务配置表)】的数据库操作Mapper
* @createDate 2025-06-16 14:50:44
* @Entity generator.domain.InterventionTaskConfig
*/
@Mapper
@Repository
public interface InterventionTaskConfigMapper extends BaseMapper<InterventionTaskConfig> {

}




