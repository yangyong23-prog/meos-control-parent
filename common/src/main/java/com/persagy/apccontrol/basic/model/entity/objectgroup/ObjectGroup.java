package com.persagy.apccontrol.basic.model.entity.objectgroup;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.objectgroup.ObjectGroupTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 设备编组表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "object_group", autoResultMap = true)
public class ObjectGroup extends BaseAssignIdDO {

    /**
     * 编组名称
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 编组类型：VEDIO_MONITORING视频监控
     */
    @TableField(value = "group_type")
    private ObjectGroupTypeEnum groupType;

    /**
     * 布局
     */
    @TableField(value = "layout")
    private String layout;

    /**
     * 排序
     */
    @TableField(value = "sequence")
    private Integer sequence;

    /**
     * 项目id
     */
    @TableField(value = "project_id")
    private String projectId;
}