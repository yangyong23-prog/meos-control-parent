package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;

import lombok.Data;

/**
 * 
 * @TableName system_graph_generate_parameter
 */
@TableName(value ="system_graph_generate_parameter",autoResultMap = true)
@Data
public class SystemGraphGenerateParameter extends BaseAssignIdDO {


    /**
     * 系统图副本id
     */
    @TableField(value = "system_graph_duplicate_id")
    private Long systemGraphDuplicateId;
    /**
     * 设备类code
     */
    @TableField(value = "class_code")
    private String classCode;

    /**
     * 参数数据
     */
    @TableField(value = "data")
    private String data;

    /**
     * 项目id
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 模板id
     */
    @TableField(value = "template_id")
    private String templateId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}