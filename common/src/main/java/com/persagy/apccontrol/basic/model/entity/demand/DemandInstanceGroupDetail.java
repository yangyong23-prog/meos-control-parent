package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "demand_instance_group_detail", autoResultMap = true)
public class DemandInstanceGroupDetail extends BaseAssignIdDO {

    @TableField(value = "project_id")
    private String projectId;

    @TableField(value = "instance_type")
    private InstanceTypeEnum instanceType;

    @TableField(value = "instance_id")
    private String instanceId;

    @TableField(value = "rel_group_id")
    private Long relGroupId;
}