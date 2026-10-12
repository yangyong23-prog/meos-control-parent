package com.persagy.apccontrol.basic.model.enumtype.parking;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ParkingSpaceCountTypeEnum  implements BaseEnum<String>, IEnum<String> {
    PARKING_SPACE_COUNT("parkIngSpaceCount", "key_c97055d9c3564fd0a874e5e1f75b3248"),
    VACANT_PARKING_SPACE_COUNT("vacantParkIngSpaceCount", "key_2abe05d00045455a9dea54fe242a31df"),
    CHARGING_PARKING_SPACE_COUNT("chargingParkingSpaceCount", "key_385f8af457b44b2abafc4076a190d0df"),
    VACANT_CHARGING_PARKING_SPACE_COUNT("vacantChargingParkingSpaceCount", "key_a9ac98c7e4734e49b97482f8ad77b32d"),
    FAST_CHARGING_PARKING_SPACE_COUNT("fastChargingParkingSpaceCount", "key_08039295c06a4763acc09c6b37ef7534"),
    VACANT_FAST_CHARGING_PARKING_SPACE_COUNT("vacantFastChargingParkingSpaceCount", "key_e9f89801bce9453b86338ced11135935");


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
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ParkingSpaceCountTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, ParkingSpaceCountTypeEnum.class);
    }
}
