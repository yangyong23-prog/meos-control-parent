package com.persagy.apccontrol.common.logtrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderExtractKeyPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LogQueryTemplateHeaderExtractKeyMapper extends BaseMapper<LogQueryTemplateHeaderExtractKeyPo> {
    @Select("select re.*  FROM logtrace_querytemplate_header info right JOIN logtrace_querytemplate_header_extractkey_relation re  "+
            "on info.id = re.header_id  WHERE info.id is not NULL and info.query_template_id=#{templateId}")
    List<LogQueryTemplateHeaderExtractKeyPo> getLogHeaderExtractByTemplateId(@Param("templateId") Long templateId);
}
