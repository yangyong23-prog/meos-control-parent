package com.persagy.apccontrol.common.logtrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogQueryTemplateGroupMapper extends BaseMapper<LogQueryTemplateGroupPo> {

    @Select("select info.*  FROM logtrace_querytemplate_group info LEFT JOIN logtrace_querytemplate_group_relation re on info.id = re.group_id  where re.id is NULL and info.application=#{application}")
    List<LogQueryTemplateGroupPo> queryAllTopGroupByApp(@Param(value = "application")String application);

    @Select("select info.*  FROM logtrace_querytemplate_group info LEFT JOIN logtrace_querytemplate_group_relation re on info.id = re.group_id  where re.id is NULL")
    List<LogQueryTemplateGroupPo> queryAllTopGroup();

}
