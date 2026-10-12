package com.persagy.apccontrol.common.logtrace.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("logtrace_querytemplate_header")
public class LogQueryTemplateHeaderPo extends BasePo{

    private Long queryTemplateId;

    private Integer headerType;

    private String headerCode;

    private String headerName;

    private Integer orderNum;
   //处理业务查询时需要
    @TableField(exist = false)
    Long childrenId;

    @TableField(exist = false)
    List<LogQueryTemplateHeaderPo> children;

}
