package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "demand_instance_group", autoResultMap = true)
public class DemandInstanceGroup extends BaseAssignIdDO {

    @TableField(value = "project_id")
    private String projectId;

    @TableField(value = "template_type")
    private DemandTemplateTypeEnum templateType;

    @TableField(value = "name")
    private String name;

    @TableField(value = "purpose")
    private String purpose;

    @TableField(value = "tag")
    private String tag;
}