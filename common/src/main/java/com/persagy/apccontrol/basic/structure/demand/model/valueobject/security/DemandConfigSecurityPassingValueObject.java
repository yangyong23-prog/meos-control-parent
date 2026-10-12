package com.persagy.apccontrol.basic.structure.demand.model.valueobject.security;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeIntervalValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * 安防需求-通行配置值对象
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class DemandConfigSecurityPassingValueObject extends BaseDemandConfig implements ValueObject {

    /**
     * 可通行时段
     */
    private List<DemandTimeIntervalValueObject> passableTimeIntervals;

    /**
     * 不可通行时段
     */
    private List<DemandTimeIntervalValueObject> unpassableTimeIntervals;

    /**
     * 权限通行
     */
    private List<DemandTimeIntervalValueObject> permissionPassTimeIntervals;

    @Override
    public void fillDefaultConfig(@NonNull DemandInstanceTypeEnum instanceType) {
        DemandTimeValueObject timeValueObject = DemandTimeValueObject.ofAbsoluteTimeByMin();
        DemandTimeIntervalValueObject permissionPassTimeInterval = new DemandTimeIntervalValueObject()
                .setBeginTime(timeValueObject).setEndTime(timeValueObject);
        this.setPermissionPassTimeIntervals(Lists.newArrayList(permissionPassTimeInterval));
    }
}
