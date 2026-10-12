package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author persagy
* @description 针对表【system_graph】的数据库操作Mapper
* @createDate 2024-06-25 11:55:25
* @Entity com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph
*/
@Mapper
public interface SystemGraphMapper extends BaseMapper<SystemGraph> {

}




