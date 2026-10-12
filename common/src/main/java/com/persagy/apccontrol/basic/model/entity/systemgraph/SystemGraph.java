package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;

import lombok.Data;

/**
 * 
 * @TableName system_graph
 */
@TableName(value ="system_graph",autoResultMap = true)
@Data
public class SystemGraph extends BaseAssignIdDO {


    /**
     * 设备类code
     */
    private String classCode;

    /**
     * 系统实例id
     */
    private String systemObjectId;

    /**
     * 关系图类
     */
    private String graphTypeCode;

    /**
     * 系统图数据id
     */
    private Long systemGraphDuplicateId;

    /**
     * 关系图图名称
     */
    private String graphName;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 状态：1成功，0失败
     */
    private Boolean status;

    /**
     * 小附件创建标识：1 有 0 无
     */
    private Boolean passBy;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}