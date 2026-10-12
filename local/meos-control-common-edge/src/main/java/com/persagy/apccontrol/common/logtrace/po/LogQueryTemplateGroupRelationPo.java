package com.persagy.apccontrol.common.logtrace.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("logtrace_querytemplate_group_relation")
public class LogQueryTemplateGroupRelationPo extends BasePo{

    private Long groupId;

    private Long groupParentId;

    @TableField(exist = false)
    private List<LogQueryTemplateGroupRelationPo> children;

}
