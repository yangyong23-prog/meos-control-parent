package com.persagy.apccontrol.common.logtrace.po;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("logtrace_querytemplate_relation")
public class LogQueryTemplateRelationPo extends BasePo{

    private Long templateId;

    private Long templateParentId;

    @TableField(exist = false)
    private List<LogQueryTemplateRelationPo> children;

}
