package com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.validate.utility.executor.ObjectExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * 通风需求-机房后期配置值对象
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigVentilationLogisticsValueObject
        extends BaseDemandConfigVentilationValueObject
        implements ValueObject {

    /**
     * O₂浓度目标值
     *
     * <p>一般是根据国家标准或者资管标准确定
     */
    private BigDecimal o2Target;

    /**
     * 温度目标值
     *
     * <p>一般是根据国家标准或者资管标准确定
     */
    private BigDecimal tdbTarget;

    @Override
    public void fillDefaultConfig(@NotNull DemandInstanceTypeEnum instanceType) {
        if (instanceType == DemandInstanceTypeEnum.PROJECT) {
            ObjectExecutor.nullExecute(this.getParticipateFeedbackAdjust(), () -> this.setParticipateFeedbackAdjust(true));
            ObjectExecutor.nullExecute(this.getO2Target(), () -> this.setO2Target(BigDecimal.valueOf(0.195)));
            ObjectExecutor.nullExecute(this.getTdbTarget(), () -> this.setTdbTarget(BigDecimal.valueOf(40)));
        }
    }
}
