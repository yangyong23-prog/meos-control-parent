package com.persagy.apccontrol.common.logtrace.po;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("logtrace_querytemplate_group")
public class LogQueryTemplateGroupPo extends BasePo{


    private String groupCode;

    private String groupName;
    /**
     * 同一层分组的排序
     */
    private Integer orderNum;

    @TableField(exist = false)
    List<LogQueryTemplateGroupPo> children;


}
