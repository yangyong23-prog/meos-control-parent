package com.persagy.apccontrol.common.logtrace.po;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("logtrace_querytemplate_header_relation")
public class LogQueryTemplateHeaderRelationPo extends BasePo {

    private Long headerId;

    private Long headerParentId;

    @TableField(exist = false)
    List<LogQueryTemplateHeaderRelationPo> children ;

}
