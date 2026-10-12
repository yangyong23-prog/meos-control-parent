package com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.validate.utility.executor.ObjectExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

/**
 * 通风需求-卫生间配置值对象
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigVentilationToiletValueObject
        extends BaseDemandConfigVentilationValueObject
        implements ValueObject {

    /**
     * NH₃浓度目标值
     *
     * <p>一般是根据国家标准或者资管标准确定
     */
    private BigDecimal nH3Target;

    /**
     * H₂S浓度目标值
     *
     * <p>一般是根据国家标准或者资管标准确定
     */
    private BigDecimal h2STarget;

    @Override
    public void fillDefaultConfig(@NonNull DemandInstanceTypeEnum instanceType) {
        if (instanceType == DemandInstanceTypeEnum.PROJECT) {
            ObjectExecutor.nullExecute(this.getParticipateFeedbackAdjust(), () -> this.setParticipateFeedbackAdjust(true));
            ObjectExecutor.nullExecute(this.getNH3Target(), () -> this.setNH3Target(BigDecimal.valueOf(0.3)));
            ObjectExecutor.nullExecute(this.getH2STarget(), () -> this.setH2STarget(BigDecimal.valueOf(0.01)));
        }
    }
}
