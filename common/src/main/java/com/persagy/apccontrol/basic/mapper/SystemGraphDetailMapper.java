package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author persagy
* @description 针对表【system_graph_detail(系统图细节调整表)】的数据库操作Mapper
* @createDate 2024-06-25 11:55:03
* @Entity com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphDetail
*/
@Mapper
public interface SystemGraphDetailMapper extends BaseMapper<SystemGraphDetail> {

}




