package com.persagy.apccontrol.common.logtrace.po;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("logtrace_querytemplate")
public class LogQueryTemplatePo extends BasePo{

    private String code;

    private String name;

    private Long groupId;

    private Integer orderNum;

    private String queryIndexNodeCode;

    private String queryParam;

    @TableField(exist = false)
    private List<LogQueryTemplatePo> children;


}
