package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.RcObjectRelationCfg;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 3D模型下关联设备-关系配置信息
 *
 * @version 1.0.0
 * @company persagy
 * @author zhangqiankun
 * @date 2023/8/11 11:26
 */
@Mapper
@Repository
public interface RcObjectRelationCfgMapper extends SuperMapper<RcObjectRelationCfg> {

}




