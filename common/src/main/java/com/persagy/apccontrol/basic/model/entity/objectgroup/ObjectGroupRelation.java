package com.persagy.apccontrol.basic.model.entity.objectgroup;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 设备编组关系表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "object_group_relation", autoResultMap = true)
public class ObjectGroupRelation extends BaseAssignIdDO {

    /**
     * 对象id
     */
    @TableField(value = "object_id")
    private String objectId;

    /**
     * 分组id
     */
    @TableField(value = "group_id")
    private Long groupId;

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