package com.persagy.apccontrol.basic.model.dto.systemgraph;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

public enum InfoProperty {
    ID("id", "id"),
    CLASS_CODE("classCode", "key_7a29dd123f6842078c4be081c1d02600"),
    NAME("name", "key_7416b6b2ac4c474ebb9888922a4c14d4"),
    CODE("code", "key_de8b093499dd400bbf4f9e4654302bc3"),
    LOCAL_ID("localId", "key_faadeea4b21c4b18b2c5096be544297a"),
    FIRST_TAG("firstTag", "key_ef951d9cc15c421aa9e600aee5822fbd"),
    BIM_ID("bimId", "key_ddb37f4ece9b41dc9206f4bd235270e5"),
    BIM_LOCATION("bimLocation", "key_d9eddd9424b349d88be65af2cd83179d"),
    OUTLINE("outline", "key_ddca4c5935d9452d999ad598fd48907c"),
    AREA("area", "key_450c387c781c4c52a5bf616e795708ce"),
    LEASE_AREA("leaseArea", "key_c8ac9be67b83438db80abefdf2c9924f"),
    LOCAL_NAME("localName", "key_4c899ce9c54949b8a5045de55cff2d57"),
    FLOOR_ID("floorId", "key_cf1ae63870e34125af593d5de25bd0a0"),
    FLOOR_NAME("floorName", "key_062a1d825ef74c03924a314c40dd5bfa"),
    SPACE_ID("spaceId", "key_c0df977c12974033bba2e1d40833f5b2"),
    FLOOR_SEQUENCE_ID("floorSequenceID", "key_f750ffe85f384eab9f2b61070fe54acb"),
    OBJ_TYPE("objType", "key_554c6c01ffa74b7bbaeffcefee06a0c5"),
    CREATE_TIME("createTime", "key_bf62981a6f6c4b2782e0f1d1b2bcdc16"),
    USER_TYPE("userType", "key_882a05e8e5a7445e95c4ebd19513d298"),
    IC_USER_LEV("iCUserLev", "key_a876c9856253448c9af9b4dc89f367d0"),
    HU_DESIGN_CAP("hUDesignCap", "key_4f870aeef2af40238360a8b749faf660"),
    IS_HU_DUAL_POWER("isHUDualPower", "key_abad9b09c9c043bda3dc36fad195518d"),
    HU_DUAL_POWER_OPERATE_TYPE("hUDualPowerOperateType", "key_d65aecd864d94d749a91820ecb2b78dd"),
    HU_RATED_INU("hURatedInU", "key_aa3f4e6596f74a3eada68f2dfd28b6e4"),
    RATED_OUT_P("ratedOutP", "key_cda73895820742859a50306488563e3b"),
    DIESEL_GENERATOR_NUM("dieselGeneratorNum", "key_7872103df60b478fb1031b6ac781ab3d"),
    RATED_CAP("ratedCap", "key_dc6719a8f7604c639260350f66ad78fb"),
    BRAND("brand", "key_26f19a908d674c0fabd5636fed2d3298"),
    SPECIFICATION("specification", "key_382a2a0c6fac4d439bb6aff1d50e4d74"),
    START_DATE("startDate", "key_15bcbc34ec40472cbad9a44c1e71ea70"),
    MANUFACTURER("manufacturer", "key_f2efee73b8f6485c9b2d1230ff7518db"),
    EQUIP_STATUS("equipStatus", "key_64193f5e74f44eb3ab7e3bd8b5344ded"),
    SERVICE_LIFE("serviceLife", "key_2a6077f32387428782fc878d43c59f6b"),
    SPECIAL_PIPE_SECTION("specialPipeSection", "key_9a826f178e484f279e0a896fab137a86"),
    IS_PIPE_FITTING("isPipeFitting", "key_c9719250912c4dd9ada7f9371eee0f00"),
    PIPE_FITTING_TYPE("pipeFittingType", "key_1890a7194da84b869fa1be0db5775fb5"),
    SKELETON_LINE("skeletonLine", "key_62b76769013e4617b4245ed3852153b5"),
    NET_AREA("netArea", "key_3e4207be77424a60a48f6ce5c82717c1"),
    ISFOR_CUSTOMER("isforCustomer", "key_455c11a23a0b4206b668ea16e5f4ff71"),
    FORMAT("format", "key_4e3336e1223d4852b33d63930dc07a8b"),
    IS_LEASABLE_ZONE("isLeasableZone", "key_0746fc9e0e774ba6a001ca9e6f7050cf"),
    NATURAL_GAS_SUPPLY("naturalGasSupply", "key_470664a40c7c4617b89d26ae2937503c"),
    ROOM_HEIGHT("roomHeight", "key_56d9a8d895f843b4be9d78983593265a"),
    IS_OUTDOOR_SPACE("isOutdoorSpace", "key_56adc8792b58497cbf19e4d88e3636a2"),
    SPACE_BUSINESS_TYPE("spaceBusinessType", "key_3090174f84d34efca4e72dd7329a017f"),
    SHAFT_FUNC_TYPE("shaftFuncType", "key_42a88177f990488ea91d3e1553e17624"),
    PROJ_AREA("projArea", "key_83add3c72ae743a7b91ea04a10b6bd46"),
    PROJ_COVER_AREA("projCoverArea", "key_853ffc8e6dd14e6497fe3e721e1bba55"),
    PROJ_FLOOR_AREA("projFloorArea", "key_29ed2af9832341199f1b97fbe8209f4e"),
    PROJ_UNDER_AREA("projUnderArea", "key_eb20e7a139a04f0d85ec8c2f094226d4"),
    TO_HEIGHT("toHeight", "key_797cd0a3eb9d4556be0ee3c38c616803"),
    CLIMATE_ZONE("climateZone", "key_c86aeb134d3c435bb5b17d7e434a3c28"),
    BUSINESS_OPENING_TIME("businessOpeningTime", "key_c08d0ea8cd7748c1b1f92e1d43d0fc79"),
    CITY("city", "key_5a171758548146baac241dfdb90bde81"),
    GFA("gFA", "key_baf4a0f586204db0950cfba7a8bc404d"),
    FLOOR_AREA("floorArea", "key_9c1b0b740bbb4dd6be76707926d92d10"),
    UNDER_AREA("underArea", "key_df0b1c32ae364a118d52d03957ae6c58"),
    TOT_HEIGHT("totHeight", "key_89629ff8089648b388336342ddbaa65b"),
    FLOOR_LAYER("floorLayer", "key_46ab2c7623c34af1adbeed14063f5d70"),
    UNDER_LAYER("underLayer", "key_e1679631e8154fefba61363c2050bc7c"),
    BUILD_FUNC_TYPE("buildFuncType", "key_33af83e757694e1fbf26c86864fcdb9d"),
    BUILD_AREA("buildArea", "key_baf4a0f586204db0950cfba7a8bc404d"),
    MAJOR_FORMAT("majorFormat", "key_684928d966c449e5b002d4f767b45660"),
    HEIGHT("height", "key_26ed953e381e48048361f94309d11755"),
    NET_HEIGHT("netHeight", "key_722fb3cbb8bc46e1b9252228484b45d1"),
    BUILDING_ID("buildingId", "key_f5e21f1b64334ace8a5828a0892354b5"),
    BUILDING_NAME("buildingName", "key_97842d998e5f4158806c2e91a8c78f8a"),
    FLOOR_ELEVATION("floorElevation", "key_ddd503472eb048cab738cb476ecd0fd8"),
    HOLIDAY_SET("holidaySet", "key_42181384312f422b80f3d5b3212ada32"),
    SEASON_SET("seasonSet", "key_c15890ebc9a64149994845ee2d61e24b"),
    SYSTEM_ID("systemId", "key_f35c0b8c5c00457aa2148cb674cea9b6"),
    SYSTEM_LOCAL_NAME("systemLocalName", "key_b160e6fb41aa440dbfcfc1da0b9c5c13"),
    SUPPLIER("supplier", "key_fd843092a9884b07b98a584e16c5d134"),
    EQUIP_O2O_LOCAL_NAME("equipO2OLocalName", "key_ba0b1711a1ab465f8c6b1d6d65dd59c7");

    private final String code;
    private final String desc;

    private InfoProperty(final String code, final String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public String getDesc() {
        return LocaleMessageUtil.getMessage(this.desc);
    }
}

