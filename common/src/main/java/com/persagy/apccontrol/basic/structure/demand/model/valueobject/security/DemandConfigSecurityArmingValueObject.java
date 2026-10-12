package com.persagy.apccontrol.basic.structure.demand.model.valueobject.security;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 安防需求-撤布防配置值对象
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DemandConfigSecurityArmingValueObject extends BaseDemandConfig implements ValueObject {

    /**
     * 布放时段
     */
    private List<DemandTimeIntervalValueObject> armingTimeIntervals;

    /**
     * 撤防时段
     */
    private List<DemandTimeIntervalValueObject> disarmingTimeIntervals;

    @Override
    public void fillDefaultConfig(@NonNull DemandInstanceTypeEnum instanceType) {
        DemandTimeValueObject timeValueObject = DemandTimeValueObject.ofAbsoluteTimeByMin();
        DemandTimeIntervalValueObject disarmingTimeIntervals = new DemandTimeIntervalValueObject()
                .setBeginTime(timeValueObject).setEndTime(timeValueObject);
        this.setDisarmingTimeIntervals(Lists.newArrayList(disarmingTimeIntervals));
    }
}
