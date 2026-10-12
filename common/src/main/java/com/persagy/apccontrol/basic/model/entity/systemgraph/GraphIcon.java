package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;

import java.io.Serializable;

import com.persagy.apccontrol.basic.model.enumtype.systemgraph.LegendSourceStatus;
import lombok.Data;

/**
 * 图标库
 *
 * @TableName graph_icon
 */
@TableName(value = "graph_icon", autoResultMap = true)
@Data
public class GraphIcon extends BaseAssignIdDO implements Serializable {

    /**
     * 图标id
     */
    @TableField(value = "legend_id")
    private String legendId;

    /**
     * 文件url
     */
    @TableField(value = "file_url")
    private String fileUrl;

    @TableField(value = "pic_url")
    private String picUrl;

    /**
     * 条件
     */
    @TableField(value = "`condition`")
    private String condition;

    /**
     * 图标编码
     */
    @TableField(value = "legend_code")
    private String legendCode;

    /**
     * 图标名称
     */
    @TableField(value = "legend_name")
    private String legendName;

    /**
     * 对象类code
     */
    @TableField(value = "class_code")
    private String classCode;


    /**
     * 状态
     */
    @TableField(value = "`status`")
    private LegendSourceStatus status;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}