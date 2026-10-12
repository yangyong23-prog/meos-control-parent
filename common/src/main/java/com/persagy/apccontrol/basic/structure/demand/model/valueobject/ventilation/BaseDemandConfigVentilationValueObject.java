package com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation;

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
 * 通风需求-基础配置值对象
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseDemandConfigVentilationValueObject extends BaseDemandConfig implements ValueObject {

    /**
     * 排风需求时间
     *
     * <p>在设置时段内依据环境传感器, 自动运行风机为该空间通风, 如无传感器, 则在设置时间内保持风机开
     */
    private List<DemandTimeIntervalValueObject> exhaustTimeIntervals;

    /**
     * 送风需求时间
     *
     * <p>在设置时段内保持风机开启
     */
    private List<DemandTimeIntervalValueObject> deliverTimeIntervals;

    /**
     * 是否参与反馈调节
     *
     * <p>按照O2浓度/温度进行动态调节
     */
    private Boolean participateFeedbackAdjust;

    // ------------------------- 序列化/反序列化 -------------------------

    @JsonSetter(Fields.exhaustTimeIntervals)
    public void deserializeExhaustTimeIntervals(Collection<Map<String, Object>> exhaustTimeIntervals) {
        this.exhaustTimeIntervals = readValueFromMap(exhaustTimeIntervals, DemandTimeIntervalValueObject.class);
    }

    @JsonSetter(Fields.deliverTimeIntervals)
    public void deserializeDeliverTimeIntervals(Collection<Map<String, Object>> deliverTimeIntervals) {
        this.deliverTimeIntervals = readValueFromMap(deliverTimeIntervals, DemandTimeIntervalValueObject.class);
    }
}
