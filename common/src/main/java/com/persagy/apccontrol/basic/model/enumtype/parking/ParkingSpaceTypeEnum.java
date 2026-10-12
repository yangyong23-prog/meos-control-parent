package com.persagy.apccontrol.basic.model.enumtype.parking;


import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParkingSpaceTypeEnum implements BaseEnum<String>, IEnum<String> {


    PARKING_SPACE("PARKING_SPACE", "key_c97055d9c3564fd0a874e5e1f75b3248"),
    VACANT_PARKING_SPACE("VACANT_PARKING_SPACE", "key_2abe05d00045455a9dea54fe242a31df"),
    CHARGING_PARKING_SPACE("CHARGING_PARKING_SPACE", "key_385f8af457b44b2abafc4076a190d0df"),
    VACANT_CHARGING_PARKING_SPACE("VACANT_CHARGING_PARKING_SPACE", "key_a9ac98c7e4734e49b97482f8ad77b32d"),
    FAST_CHARGING_PARKING_SPACE("FAST_CHARGING_PARKING_SPACE", "key_08039295c06a4763acc09c6b37ef7534"),
    VACANT_FAST_CHARGING_PARKING_SPACE("VACANT_FAST_CHARGING_PARKING_SPACE", "key_e9f89801bce9453b86338ced11135935");

    public String value;
    public  String reasonPhrase;


    @Override
    public String value() {
        return value;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ParkingSpaceTypeEnum analyze(String value) {
        return BaseEnum.analyze(value, ParkingSpaceTypeEnum.class);
    }

   public static ParkingSpaceCountTypeEnum contVertParkingSpaceCountTypeEnum(String parkingSpaceTypeEnum){
        if(parkingSpaceTypeEnum.equals(PARKING_SPACE.getValue())){
            return ParkingSpaceCountTypeEnum.PARKING_SPACE_COUNT;
        }
        if(parkingSpaceTypeEnum.equals(VACANT_PARKING_SPACE.getValue())){
            return ParkingSpaceCountTypeEnum.VACANT_PARKING_SPACE_COUNT;
        }
        if(parkingSpaceTypeEnum.equals(CHARGING_PARKING_SPACE.getValue())){
            return ParkingSpaceCountTypeEnum.CHARGING_PARKING_SPACE_COUNT;
        }
        if(parkingSpaceTypeEnum.equals(VACANT_CHARGING_PARKING_SPACE.getValue())){
            return ParkingSpaceCountTypeEnum.VACANT_CHARGING_PARKING_SPACE_COUNT;
        }
       if(parkingSpaceTypeEnum.equals(FAST_CHARGING_PARKING_SPACE.getValue())){
           return ParkingSpaceCountTypeEnum.FAST_CHARGING_PARKING_SPACE_COUNT;
       }
       if(parkingSpaceTypeEnum.equals(VACANT_FAST_CHARGING_PARKING_SPACE.getValue())){
           return ParkingSpaceCountTypeEnum.VACANT_FAST_CHARGING_PARKING_SPACE_COUNT;
       }
        return null;
   }
}
