package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import lombok.Data;

/**
 * 系统图模版状态表
 * @TableName system_graph_template_status
 */
@TableName(value = "system_graph_template_status", autoResultMap = true)
@Data
public class SystemGraphTemplateStatus extends BaseAssignIdDO {

    /**
     * 项目id
     */
    private String projectId;
    /**
     * 对象id
     */
    private String objectId;

    /**
     * 模版id
     */
    private String templateId;

    /** 图类型 */
    private String graphTypeCode;
    /**
     * 顺序号
     */
    private Integer sequenceNumberCurrent;
    /**
     * 名称
     */
    private String name;
    /**
     * 别名
     */
    private String aliasName;

    /**
     * 是否更新,1更新，0：未更新
     */
    private Boolean updatedStatus;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}