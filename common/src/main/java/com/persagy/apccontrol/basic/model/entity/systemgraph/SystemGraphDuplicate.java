package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumGraphType;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import lombok.Data;

/**
 * 系统图副本表
 * @TableName system_graph_duplicate
 */
@TableName(value ="system_graph_duplicate",autoResultMap = true)
@Data
public class SystemGraphDuplicate extends BaseAssignIdDO {


    /**
     * 设备类code
     */
    private String classCode;

    /**
     * 模版id
     */
    private String templateId;

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
    private Long systemGraphDataId;

    /**
     * 关系图图名称
     */
    private String name;
    /**
     * 是否不可编辑
     */
    private Boolean isNotEdit;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 生成id
     */
    private String generateId;

    /**
     * 状态：未发布，已发布，已下架
     */
    @TableField(value = "`status`")
    private EnumSystemGraphStatus status;

    /**
     * 副本序号
     */
    @TableField(value = "`order`")
    private Integer order;

    /**
     * 1、原图，2副本
     */
    private EnumGraphType graphType;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}