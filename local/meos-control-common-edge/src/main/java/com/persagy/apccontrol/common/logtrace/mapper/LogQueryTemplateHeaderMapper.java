package com.persagy.apccontrol.common.logtrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface LogQueryTemplateHeaderMapper extends BaseMapper<LogQueryTemplateHeaderPo> {

    @Select("select info.*  FROM logtrace_querytemplate_header info LEFT JOIN logtrace_querytemplate_header_relation  re on info.id = re.header_id  where re.id is NULL and info.query_template_id =#{templateId}")
    List<LogQueryTemplateHeaderPo> queryAllTopQueryTemplateHeaderByTemplateId(@Param("templateId") Long templateId);
    @Select("<script>" +
            "SELECT h.* ,r.header_id as children_id FROM logtrace_querytemplate_header h JOIN logtrace_querytemplate_header_relation r ON h.id = r.header_parent_id WHERE  h.header_type =2 and r.header_id in " +
            " <foreach item='item' collection='ids' open='(' separator=',' close=')'>" +
            "    #{item} " +
            "  </foreach> " +
            " </script>")
    List<LogQueryTemplateHeaderPo> getParentHeadersByIds(Collection<Long> ids);


}
