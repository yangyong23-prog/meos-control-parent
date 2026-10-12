package com.persagy.apccontrol.basic.model.entity.systemgraph;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import lombok.Data;

/**
 * 系统图数据表
 *
 * @TableName system_graph_data
 */
@TableName(value = "system_graph_data", autoResultMap = true)
@Data
public class SystemGraphData extends BaseAssignIdDO {

    /**
     * 访问参数
     */
    private String requestParam;

    /**
     * 系统图数据
     */
    private String data;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 状态，1已发布，0未发布，2、已下架
     */
    private EnumSystemGraphStatus status;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}