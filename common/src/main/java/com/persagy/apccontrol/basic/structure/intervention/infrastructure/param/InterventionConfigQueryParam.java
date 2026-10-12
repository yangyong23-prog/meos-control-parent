package com.persagy.apccontrol.basic.structure.intervention.infrastructure.param;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.common.model.valueobject.DateTimeRangeValueObject;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.intervention.enumeration.InterventionStatusEnum;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * 干预配置查询参数
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Data
public class InterventionConfigQueryParam {

    /**
     * 运行监测二级类型
     */
    @NotNull(message = "{key_4a1af0119a4f4fdc8f6cfe10e1ea451e}")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 实例类型
     */
    @NotNull(message = "{key_3b13463321154c7695bc97753eeede5e}")
    private DemandInstanceTypeEnum instanceType;

    /**
     * 实例ID
     */
    @NotEmpty(message = "{key_e8dc2fefc0e244ff95cc77a3352c1826}")
    private Collection<String> filterInstanceIds;

    /**
     * 时间包含范围, 查询时间具有重叠的干预时段
     */
    @NotNull(message = "{key_7ab349561c2e4171a2e4c03271c43404}")
    private DateTimeRangeValueObject dateTimeContainRange;

    /**
     * 干预状态, 为空时查询所有
     */
    private InterventionStatusEnum interventionStatus;

}
