package com.persagy.apccontrol.basic.structure.common.model.valueobject;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.validate.utility.enumeration.TimeTypeEnum;
import lombok.Data;

/**
 * 时间偏移值对象
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public class TimeDeviationValueObject implements ValueObject {

    /**
     * 时间类型
     */
    private TimeTypeEnum timeType;

    /**
     * 偏移量
     */
    private Integer offset;

}
