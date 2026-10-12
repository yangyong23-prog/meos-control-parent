package com.persagy.apccontrol.common.logtrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplatePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogQueryTemplateMapper extends BaseMapper<LogQueryTemplatePo> {

    @Select("select info.*  FROM logtrace_querytemplate info LEFT JOIN logtrace_querytemplate_relation re on info.id = re.template_id  where re.id is NULL and info.application=#{application} and info.group_id =#{groupId}")
    List<LogQueryTemplatePo> queryAllTopTemplate(@Param(value = "groupId")Long groupId,@Param(value = "application")String application);
    @Select("select info.*  FROM logtrace_querytemplate info LEFT JOIN logtrace_querytemplate_relation re on info.id = re.template_id  where re.id is NULL and info.group_id =#{groupId}")
    List<LogQueryTemplatePo> queryAllTopTemplateByGroupId(@Param(value = "groupId")Long groupId);

}
