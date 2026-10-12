package com.persagy.apccontrol.basic.model.enumtype.parking;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParkingSpaceStatusEnum implements BaseEnum<String>, IEnum<String> {
    VACANT_PARKING_SPACE("0","key_9c6366901ae6422aac8f25ffd35e3a8b"),
    NO_VACANT_PARKING_SPACE("1","key_0382cb30cf7c4c3788b9414290d075bf");

    final String value;
    final String reasonPhrase;
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ParkingSpaceStatusEnum analyze(String value) {
        return BaseEnum.analyze(value, ParkingSpaceStatusEnum.class);
    }
}
