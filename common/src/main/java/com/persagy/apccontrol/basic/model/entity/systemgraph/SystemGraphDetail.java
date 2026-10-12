package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import lombok.Data;

/**
 * 系统图细节调整表
 * @TableName system_graph_detail
 */
@TableName(value ="system_graph_detail",autoResultMap = true)
@Data
public class SystemGraphDetail extends BaseAssignIdDO {

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 编码
     */
    private String code;
    /**
     * 系统图副本id
     */
    private Long systemGraphDuplicateId;

    /**
     * 1:设备2:信息点3:标签4:分组
     */
    private Integer type;

    /**
     * 细节调整相关数据，如信息点列表，名称，位置等
     */
    private String data;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}