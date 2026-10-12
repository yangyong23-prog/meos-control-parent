package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDuplicate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author persagy
* @description 针对表【system_graph_duplicate(系统图副本表)】的数据库操作Mapper
* @createDate 2024-06-25 11:55:16
* @Entity com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDuplicate
*/
@Mapper
public interface SystemGraphDuplicateMapper extends BaseMapper<SystemGraphDuplicate> {

}




