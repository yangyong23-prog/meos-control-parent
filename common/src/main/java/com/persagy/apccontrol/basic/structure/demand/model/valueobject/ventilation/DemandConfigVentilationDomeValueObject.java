package com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.validate.utility.executor.ObjectExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 通风需求-穹顶配置值对象
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigVentilationDomeValueObject
        extends BaseDemandConfigVentilationValueObject
        implements ValueObject {

    /**
     * 温度目标值
     *
     * <p>一般是根据国家标准或者资管标准确定
     */
    private BigDecimal tdbTarget;

    /**
     * 自然通风需求时间
     *
     * <p>在设置时段内, 穹顶空间存在通过普通窗或排烟窗进行自然通风的需求
     *
     * @since v7.260802.2
     */
    private List<DemandTimeIntervalValueObject> naturalVentilationTimeIntervals;

    @Override
    public void fillDefaultConfig(@NonNull DemandInstanceTypeEnum instanceType) {
        if (instanceType == DemandInstanceTypeEnum.PROJECT) {
            ObjectExecutor.nullExecute(this.getParticipateFeedbackAdjust(), () -> this.setParticipateFeedbackAdjust(true));
            ObjectExecutor.nullExecute(this.getTdbTarget(), () -> this.setTdbTarget(BigDecimal.valueOf(40)));
        }
    }

    // ------------------------- 序列化/反序列化 -------------------------

    @JsonSetter(Fields.naturalVentilationTimeIntervals)
    public void deserializeNaturalVentilationTimeIntervals(
            Collection<Map<String, Object>> naturalVentilationTimeIntervals) {
        this.naturalVentilationTimeIntervals = super.readValueFromMap(
                naturalVentilationTimeIntervals, DemandTimeIntervalValueObject.class);
    }
}
