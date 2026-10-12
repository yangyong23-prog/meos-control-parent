package com.persagy.apccontrol.common.logtrace.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogNodeMapper extends BaseMapper<LogNodePo> {

    @Select("select info.*  FROM logtrace_node_info info LEFT JOIN logtrace_node_relation re on info.id = re.node_id  where re.id is NULL")
    List<LogNodePo> queryAllTopNode();
}
