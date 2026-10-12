package com.persagy.apccontrol.basic.structure.demand.model.valueobject;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.framework.common.context.ContextUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 需求实例类型值对象
 *
 * @author JiYinchuan
 * @since 5.240701.0
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DemandInstanceValueObject implements ValueObject {

    /**
     * 实例类型
     */
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    private String instanceId;

    // ------------------------- 拓展字段 -------------------------

    /**
     * 实例名称
     */
    @EqualsAndHashCode.Exclude
    private String instanceName;

    /**
     * 构造器
     *
     * @param instanceType 实例类型
     * @param instanceId   实例ID
     * @since 5.240702.0
     */
    public DemandInstanceValueObject(DemandInstanceTypeEnum instanceType, String instanceId) {
        this.instanceType = instanceType;
        this.instanceId = instanceId;
    }

    /**
     * 创建项目实例
     *
     * @return 项目实例
     * @since 5.241202.0
     */
    public static DemandInstanceValueObject ofProject() {
        return new DemandInstanceValueObject(DemandInstanceTypeEnum.PROJECT, ContextUtil.getProjectId());
    }
}
