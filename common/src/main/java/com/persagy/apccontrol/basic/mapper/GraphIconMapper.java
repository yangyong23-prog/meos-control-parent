package com.persagy.apccontrol.basic.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.persagy.apccontrol.basic.model.entity.systemgraph.GraphIcon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author persagy
* @description 针对表【graph_icon(图标库)】的数据库操作Mapper
* @createDate 2024-07-15 15:33:07
* @Entity com.persagy.apccontrol.basic.model.entity.systemgraph.GraphIcon
*/
@Mapper
@InterceptorIgnore(tenantLine = "true")
public interface GraphIconMapper extends BaseMapper<GraphIcon> {

}




