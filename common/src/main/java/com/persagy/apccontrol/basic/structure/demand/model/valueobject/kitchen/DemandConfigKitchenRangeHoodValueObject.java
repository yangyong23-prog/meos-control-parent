package com.persagy.apccontrol.basic.structure.demand.model.valueobject.kitchen;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 厨房排油烟需求-配置值对象
 *
 * @author JiYinchuan
 * @since v7.261001.0
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigKitchenRangeHoodValueObject extends BaseDemandConfig implements ValueObject {

    /**
     * 排油烟需求时间
     *
     * <p>在设置时段内自动运行排油烟风机
     *
     * @since v7.261001.0
     */
    private List<DemandTimeIntervalValueObject> exhaustTimeIntervals;

    // ------------------------- 序列化/反序列化 -------------------------

    /**
     * 反序列化排油烟需求时间
     *
     * @param exhaustTimeIntervals 排油烟需求时间
     * @since v7.261001.0
     */
    @JsonSetter(Fields.exhaustTimeIntervals)
    public void deserializeExhaustTimeIntervals(Collection<Map<String, Object>> exhaustTimeIntervals) {
        this.exhaustTimeIntervals = super.readValueFromMap(exhaustTimeIntervals, DemandTimeIntervalValueObject.class);
    }
}
