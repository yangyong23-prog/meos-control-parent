package com.persagy.apccontrol.basic.model.base;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeControlDeviationStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtpknowledge.DtpKnowledgeRunStatusEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 系统图固定显示信息点
 * @author: yancheng
 * @date: 2023/3/16
 * @version: V1.0
 */
@Getter
public enum InfoCodeEnum implements BaseEnum<String>, IEnum<String> {

    RUN_STATUS("runStatus", "key_61378bfff0cc4eafbe895a929c4f5351", DtpKnowledgeRunStatusEnum.class),
    SWITCH_STATUS("switchStatus", "key_7f31a0f778eb4745a5437993c9b760cc"),
    ON_OFF_STATUS("onOffStatus", "key_99ce6773bf45480d9ef948298ebbde0e"),
    POWER_STATUS("powerStatus", "key_dcc6e548ddbb46ee851243c1e041290a"),
    FAULT_STATUS("faultStatus", "key_7ec131a48115424282eff25ba919fb8a"),
    MANUAL_AUTO_SET("manualAutoSet", "key_56d178dc4bec47a082119904aacd00b0"),
    WORK_MODE_SET("workModeSet", "key_911a090577c846eca56819f6943dde35"),
    RUN_MODEL_SET("runModeSet", "key_6ada9bde0b9f4d4db480ca8c044cf913"),
    WORK_FREQ("workFreq", "key_367c234aaa5e475fbedcbb761405c510"),
    MANUAL_AUTO_STATUS("manualAutoStatus", "key_c53f9c63feb14766ab6f3a644ed78d57"),
    LOCAL_REMOTZE_STATUS("localRemoteStatus", "key_7a974456710f4c0b9b6c1c48083b39d7"),
    ONLINE_STATUS("onlineStatus", "key_e5d5a7cd3f174f289d61688a85ec8c6f"),
    ORDER_FAIL_ALARM("orderFailAlarm", "key_f0a572661bea44208463591020227964"),
    IS_FORCE("isForce", "key_ce965bd9306b49668ae9255dc00b9859"),
    EQUIP_SWITCH_SET("equipSwitchSet", "key_46f5d049b2624453865cdc94308e0d89"),
    ENABLE_CTRIL("enableCtrl", "key_f4700d8273924e908b9372f8a1324d2a"),
    ENABLE_COMMAND("enableCommand", "key_f4700d8273924e908b9372f8a1324d2b"),
    RATEDP("ratedP", "key_d0523ca989eb40bba136c049a811b681"),
    RATED_COOL("ratedCool", "key_687c27e08d28480bb58e95c2e78a1049"),
    RATED_HEAT("ratedHeat", "key_056e5e81d4104405a6b71713521a4c4e"),
    PANEL_NO("panelNo", "key_2bac4b84c1a94245977093275a6b7d7d"),
    I_TRANS_RATED_I_RATIO("iTransRatedIRatio", "key_f6b6ed8603f84bbda40b7abf76d5dc29"),
    ELEC_CONSUM_P("elecConsumP", "key_fd1ccfddf59a4ba29eb2a04a1b3491d4"),
    ELEC_PRICE("elecPrice", "key_19123e40b0734124b1b5875a26839da4"),
    RC_RUN_STATUS("rcRunStatus", "key_7a6a9883693b483fb3a6c89154463e35"),
    RC_FAULT_STATUS("rcFaultStatus", "key_9394bf706c8844a592727f9f2412240c"),
    INDOOR_MEDIUM_OUT_TEMP("indoorMediumOutTemp", "key_ff7301c954cf4875a4ddaabd578625e1"),
    SPACE_TDB("tdb", "key_7274b502eecc4af1ac679f90941dfcbe"),
    SPACE_CO2("cO2", "key_f4b2a3540e1543c9a24f9c15c564caaa"),
    ZONE_TEMP_MASK_SET("zoneTempMaskSet", "key_f4b2a3540e1543c9a24f9c15c564caab"),
    ROOM_FUNC_TYPE("roomFuncType", "key_c8e9a828419e4e2f81da655dd97ee277"),
    SUMMER_DESIGN_CHILL_WATER_FLOW("summerDesignChillWaterFlow", "key_0cc5c27b688849559e98c8725c4df589"),
    SUPPLY_CAP("supplyCap","key_a3b824223a2a4f0d982192841b4183a0"),
    RATED_FAN_FLOW("ratedFanFlow","key_1baf0bc83a04468babffdb2706af4bcf"),
    DESIGN_CHILL_WATER_FLOW("designChillWaterFlow","key_9afbcffd6dfc4be3b9e568e837af6b30"),
    IS_COOL_COIL_SECTION("isCoolCoilSection","key_ce99d7833862406cb6e104888d3840de"),
    IS_HEAT_RECOVERY_SECTION("isHeatRecoverySection","key_dcc026fc29674e8182b1641463118b1e"),
    RATED_COP("ratedCOP","key_f17a64acacd64b9683f7f10344a7fb6c"),
    DESIGN_EVAP_WATER_OUT_TEMP("designEvapWaterOutTemp","key_85f7ad7784a14de1ac11a2b91556be66"),
    DESIGN_EVAP_WATER_IN_TEMP("designEvapWaterInTemp","key_57f80f4077c548498baf71da310f5f19"),
    DESIGN_COND_WATER_IN_TEMP("designCondWaterInTemp","key_3bd561d3dd6b4612a0807a4ad41d2de2"),
    DESIGN_COND_WATER_OUT_TEMP("designCondWaterOutTemp","key_28465333e28546ffa9949803e65e8295"),
    DESIGN_COOL_WATER_FLOW("designCoolWaterFlow","key_723a86d8e39d4e38893131a96eb8ba96"),
    RATED_FLOW("ratedFlow","key_d6e44f1d98974fc58dd343a6b08cb512"),
    RATED_WATER_IN_TEMP("ratedWaterInTemp","key_8fa4a797b0c941279064a1a1b522f4d4"),
    RATED_WATER_OUT_TEMP("ratedWaterOutTemp","key_eeaaa1ebbdf54ded80f08f7888342edd"),
    RATED_FAN_AIR_FLOW("ratedFanAirFlow","key_4bcd0b418a6e42e485a5c51dc2832be8"),
    RATED_WATER_FLOW("ratedWaterFlow","key_c4eea2f2014e43039758c026af4e22f5"),
    FAN_NUM("fanNum","key_9c9bf9d1362e4ec093e4a7b34f452c95"),
    BUILD_AREA("buildArea","key_754697b6a2814e5ba7c8a9d71a3d3d1c"),
    FREQ("freq","key_754697b6a2814e5ba7c8a9d71a3d3d2c"),

    MONITORING_SURVEILLANCE_STATUS("monitoringSurveillanceStatus", "key_06cb6e6c89aa4a4881f417e0767d95fe"),

    DEVICE_ILLUM("illum", "key_93989ddc584b4ac7ae66cc97eaeed05a"),
    RH("rH", "key_c03fb9cdd9344d3293d66c6382095dba"),
    PM2D5("pM2d5", "PM2.5"),
    PM10("pM10", "PM10"),
    CO("cO", "key_b4ec68b6476c4def9282903a8b358f9b"),
    O2("o2", "key_3c87fe7ef2e14eecb0d5f2c8928576bb"),
    NH3("nH3", "key_3d179bb8ef034ce9b637c27b0a840e4a"),
    H2S("h2S", "key_b4f141153e3945fe84dab4fb141f9dc0"),
    NH3_CONTROL_AIM("nH3ControlAim", "key_1773dce1927c4d44aa6d6e2505c90f68"),
    O2_CONTROL_AIM("o2ControlAim", "key_a148c88f62c24fb1a0cd4508f5a0a5ff"),
    CO_CONTROL_AIM("cOControlAim", "key_b661e1723e654a309e03dfc995e337d5"),
    H2S_CONTROL_AIM("h2SControlAim", "key_ac6d7ff95b16495a97322ee6a7ba0f03"),
    TEMP_TWIN_CONTROL_AIM("tempTwinControlAim", "key_73fa2a863c18486bacb568a02fac3bad"),
    FEED_BACK_ADJUST("feedbackAdjust", "key_88703d62c0ff40919ed91e682477ff5a"),
    DELIVER_DEMAND("deliverDemand", "key_2ef23f41f6534176b69199a526294c9a"),
    EXHAUST_DEMAND("exhaustDemand", "key_0db368aa7f9e433ba5e7d9a4eb3c79f9"),
    IS_FRESH_AIR_SECTION("isFreshAirSection", "key_a48a81bca3d145cfbf584bdd96170858"),
    VOC("vOC", "key_6381ee9deefd43b2833662417e71388d"),
    TVOC("tVOC", "key_618a701a2e66457d8739009355b0e39e"),
    HCHO("hCHO", "key_f4cc76df3e7d4b63b5bd22c317e1df72"),
    MAJOR("major", "key_9ee7cc2e597649dfac720a416feaa43d"),

    /**
     * 控制偏差状态
     *
     * <ul>
     *     <li><b>1</b> - 多开</li>
     *     <li><b>2</b> - 少开</li>
     *     <li><b>3</b> - 无偏差</li>
     * </ul>
     */
    CONTROL_DEVIATION_STATUS("controlDeviationStatus", "key_8cbe3f5898754f6f931f1edaae09d8d9", DtpKnowledgeControlDeviationStatusEnum.class),

    /**
     * 项目室外照度值，即照度平均值
     */
    OUTDOOR_ILLUMINANCE("outdoorIlluminance", "key_5fdea567a3fd4122884aeba7da33dd35"),
    WORK_PLANE_LUX("workPlaneLux", "key_70b2e66010154211b223ff4688fa5359"),
    I("i", "key_8d013bbbe3ca45aa91390c85fb685a6c"),

    /**
     * 集水坑 隔油池 餐饮除油成套设备固定信息点
     */
    SUPER_HIGH_LEVEL_ALARM("superHighLevelAlarm", "key_ec40bdf0bb074ea5803580c29f24f412"),
    HIGH_LEVEL_ALARM("highLevelAlarm", "key_09a92bf266404c75a7f921451b561077"),

    COOL_WATER_RETURN_TEMP_SET("coolWaterReturnTempSet", "key_4e5427529d724164ae451f4c280d04ec"),

    /**
     * ACCCOT，ACCCWT,ACGEST，ACGEET,WSDWWT,WSDHWT
     */
    LOW_LEV_ALARM("lowLevAlarm", "key_74d553a880f9482ba6c0d448675c2b5a"),
    HIGH_LEV_ALARM("highLevAlarm", "key_e59a7772a8cf4fa7ac9fe23a2d793f0f"),

    /**
     * 安防
     */
    ARMING_DEMAND("armingDemand", "key_7a52a84139a14f2c8ff5e07ba29800bc"),
    DEPLOYMENT_STATUS("deploymentStatus", "key_5c9e187f4a0f419cb0832c715f993e11"),
    HAS_ARMING_FUNC("hasArmingFunc", "key_b9ec690da479421ea440e98fa48f2b1a"),
    ACCESS_DEMAND("accessDemand", "key_374f6c7eb5ff44dfb5aeb4754d74a86c"),
    CAMERA_TYPE("cameraType", "key_a920e28c7b59493c983e1568f542dac2"),
    DEPLOY_WITHDRAWALARM_STATUS("deployWithdrawAlarmStatus", "key_14ff90134ebe4c78bc4a9ac1593787eb"),
    ARMING_STATUS("armingStatus", "key_c54453ed724b42bf8587094630b59905"),
    REAL_TIME_STREAM_PROTOCOL("realTimeStreamProtocol", "key_e9a64eb7ace8417fa65c77f5a56c6dc9", null),

    /**
     * 客流
     */
    SP_EXSIT_PEOPLE_NUM("spExsitPeopleNum", "key_561dc647808b49cfa6d1a2f4d0488d6a"),
    ACCD_OUT_PEOPLE_FLOW("accdOutPeopleFlow", "key_dd19bbc32267492ba5885b8e6fc8cfdb"),
    ACCD_IN_PEOPLE_FLOW("accdInPeopleFlow", "key_ed67bd55db1748468630fbe014ed8ca0"),
    ACCM_OUT_PEOPLE_FLOW("accmOutPeopleFlow", "key_bdca46fb26c043d8b4abd1055d86add8"),
    ACCM_IN_PEOPLE_FLOW("accmInPeopleFlow", "key_d87d0213cab749ac969d57ced0512ddf"),
    EXSIT_PEOPLE_NUM("exsitPeopleNum", "key_fdba44d5d27040ccb5ac667661f1e42a"),


    /**
     * 停车场
     */
    PARKING_STATUS("parkingStatus", "key_de5e99d962f842868932ae1e5dfcf634"),

    /**
     * 剩余车位数量（项目对象）
     *
     * @since v7.260901.0
     */
    AVAILABLE_PARKING_SPACE_NUMBER("availableParkingSpaceNumber", "key_parking_available_space_number"),

    /**
     * 停车数量（项目对象）
     *
     * @since v7.260901.0
     */
    CAR_NUM("carNum", "key_parking_car_num"),

    // 热源
    COMBUSTION_STATE("combustionState", "key_0855a13b374a4ecfa48f8f32b1db962c"),

    /**
     * 高压强电
     */
    H_U_RATED_IN_U("hURatedInU", "key_aa3f4e6596f74a3eada68f2dfd28b6e4"),
    H_U_DESIGN_CAP("hUDesignCap", "key_4f870aeef2af40238360a8b749faf660"),
    A_PHASE_COIL_TEMP("aPhaseCoilTemp", "key_3f586d5b9aa9458cafbab10b75c317fa"),
    B_PHASE_COIL_TEMP("bPhaseCoilTemp", "key_a28ebbbccad348a3ae918596bd9c0a0a"),
    C_PHASE_COIL_TEMP("cPhaseCoilTemp", "key_5a14665da50a477fb53f24a43d3beffe"),
    L_U_LOAD_RATE("lULoadRate", "key_76e298ad080146e7a292be0706105bd0"),
    /**
     * 冷源
     */
    I_PLR("iPLR", "key_dc79f1ac329e44a5adfad74fbdb8f5d4"),
    ACC_ELEC_CONSUM("accElecConsum", "key_dc79f1ac329e44a5adfad74fbdb8f5d5"),
    SUBENTRY_ACC_CONSUM("subentryAccConsum", "key_subentryAccConsum"),
    ACC_COOL("accCool", "key_dc79f1ac329e44a5adfad74fbdb8f5d6"),
    /**
     * 给排水
     */
    SUPPLY_UNIT_OUT_PRESS("supplyUnitOutPress", "key_cdea293c126045a893be27456512e616"),
    WORST_TERMINAL_OUT_PRESS("worstTerminalOutPress", "key_426a03af0bd74b48a1309a05882079f5"),
    SUPPLY_PRESS("supplyPress", "key_8057ed49957d4d7eb0d1197dfc55462a"),
    RECLAIMED_WATER_BASIN_VOLUME("reclaimedWaterBasinVolume", "key_34fcc0a9162f45e585393b376ec970a2"),
    TANK_WATER_LEV("tankWaterLev", "key_d1cd5b6d2ce14c38ae9b7ad9d915ff90"),
    HEAT_WATER_MAIN_SUPPLY_TEMP_2("2heatWaterMainSupplyTemp", "key_807892977c4b453d80dc31f030bcefbf"),
    HEAT_WATER_MAIN_SUPPLY_TEMP("heatWaterMainSupplyTemp", "key_18240c9cf00b49d08c7d4956764ff3ed"),


    EQUIP_GEAR_SET("equipGearSet", "key_d7fd4277e1b7445b93dfe37666790b96"),
    FAN_GEAR("fanGear", "key_fan_gear"),
    EQUIP_FREQ_SET("equipFreqSet", "key_d04445392fdd4abd961d7b880f0999dc"),
    T_TARGET("tTarget", "key_6e0af960e23c46e2bc1f7a0d786ca848"),
    T_TARGET_SET("tTargetSet", "key_6e0af960e23c46e2bc1f7a0d786ca849"),
    SUPPLY_AIR_TEMP("supplyAirTemp", "key_6489c34168b745bf93ebabbfc57b48b5"),
    SUPPLY_AIR_TEMP_SET("supplyAirTempSet", "key_6489c34168b745bf93ebabbfc57b48b6"),
    EQUIP_ON_SET("equipOnSet", "key_3f0e2986d8fb467789365e599e3c3cc9"),


    /**
     * 系统大小机可用级
     */
    MAX_AVAI_LARGE_LEVEL("maxAvaiLargeLevel", "key_1937156859c3454bba61a2c826e656ee"),
    MAX_AVAI_SMALL_LEVEL("maxAvaiSmallLevel", "key_e0a36cbe730d4501a56510d2a58a0386"),
    MIN_AVAI_LARGE_LEVEL("minAvaiLargeLevel", "key_177e8e211cb64ce9b031b5ffa8a9acbb"),
    MIN_AVAI_SMALL_LEVEL("minAvaiSmallLevel", "key_50969d78ac9541efac0095f11ade8a61"),

    IS_COOL_STORE("isCoolStore", "key_ddac72d4073145b0bdd8600026c67122"),
    CURRENT_OPERATING_MODE("currentOperatingMode", "key_7a4430f58bc44f50ae049be1a6cde06b"),
    NET_USE_TYPE("netUseType", "key_4e7cb3d0a1fb40d9a55f65ed29c08c30"),
    IS_TERMINAL_PUMP("isTerminalPump", "key_e3bbd1a4867049118d84a173107b28fb"),
    IS_DUAL_CONDITION("isDualCondition", "key_9a8a0feba6ff4d2aaa901b5ef5e2d42d"),
    CHILL_PUMP_TYPE("chillPumpType", "key_dc72c2b9a7ba40229d390e1912d7fcd5"),

    /**
     * 冰蓄冷设备组相关信息点
     */
    GROUP_EQUIP_RUNING_NUM("groupEquipRuningNum", "key_26889c16eb454f98a40d5677e9f3d16c"),
    CONTROL_FAILURE_ALARM("controlFailureAlarm", "key_0f38d1063b664d9c9aab556c3b07affb"),
    COOL_WATER_VLV_CONTROL_FAILURE_ALARM("coolWaterVlvControlFailureAlarm", "key_2a81bb14192646959664eed51f74120a"),
    CHILL_WATER_VLV_CONTROL_FAILURE_ALARM("chillWaterVlvControlFailureAlarm", "key_1ac12bbdc7764f7abe537cdc3632cbbd"),
    SOURCE_SIDE_SWITCH_WATER_VALVE_CONTROL_FAILURE_ALARM("sourceSideSwitchWaterValveControlFailureAlarm", "key_744d513c623c432fbd736fa613b2692a"),
    TERMINAL_SIDE_SWITCH_WATER_VALVE_CONTROL_FAILURE_ALARM("terminalSideSwitchWaterValveControlFailureAlarm", "key_b399cc79641544859149c7a53bea8161"),
    INDOOR_MEDIUM_VALVE_CONTROL_FAILURE_ALARM("indoorMediumValveControlFailureAlarm", "key_215d0d51379d431aa6945cf6a16ce516"),

    /**
     * 冷源
     */
    CHILL_WATER_DETA_TEMP("chillWaterDetaTemp", "key_fd764bdffee34190a5dd3839483f79b8"),
    CHILL_WATER_DETA_PRESS("chillWaterDetaPress", "key_4ad868c4085443f78fcf941144038179"),
    WORST_TERMINAL_DETA_PRESS("worstTerminalDetaPress", "key_5613e5726ede46358cdda7be5d95138b"),
    PRIMARY_CHILL_WATER_MAIN_SUPPLY_TEMP("primaryChillWaterMainSupplyTemp", "key_7931bb5c6f7b4cdca0e539844364814b"),
    PRIMARY_CHILL_WATER_MAIN_RETURN_TEMP("primaryChillWaterMainReturnTemp", "key_7931bb5c6f7b4cdca0e539844364815b"),

    CHILL_WATER_OUT_TEMP_SET("chillWaterOutTempSet", "key_chill_water_out_temp_set"),
    CHILL_WATER_DETA_TEMP_SET("chillWaterDetaTempSet", "key_cef8eb4aa8d04f63ad90fdc6195fb09c"),
    CHILL_WATER_DETA_PRESS_SET("chillWaterDetaPressSet", "key_8f7243ab398f46b6b0380ffa7d043078"),
    WORST_TERMINAL_DETA_PRESS_SET("worstTerminalDetaPressSet", "key_83c37a7733284c83a1e23c0671d857e5"),
    DISTRICT_COOLING_PLATE_SWITCH_SET("districtCoolingPlateSwitchSet", "key_0b79b9de9f1c4c55be95cc452fd6ad3b"),

    /**
     * 泵组
     */
    BRANCH_DETA_TEMP_SET("branchDetaTempSet", "key_40ac6744dea44b1f8fac755b30e0c594"),
    BRANCH_DETA_PRESS_SET("branchDetaPressSet", "key_638d30800b2c470dbff0c81521cd6f0e"),

    AIR_CONDITION_DEMAND_ACZONE("airConditionDemand", "key_f7191173c94b4ab6bde6c77043cbe9e2"),

    COOLING_STORAGE_SWITCH_SET("coolingStorageSwitchSet", "key_bf21dbe74ba44ceca868742267d9039e"),
    COOLING_SUPPLY_SWITCH_SET("coolingSupplySwitchSet", "key_2bcc276acb6640699130bef61698ce0f"),
    DISCHARGE_COOLING_SWITCH_SET("dischargeCoolingSwitchSet", "key_21079122f3f647daa6defabdc410ffbb"),
    DISCHARGE_COOLING_SWITCH_STATUS("dischargeCoolingSwitchStatus", "key_e9d0f71861574837acfc03bf8f710799"),
    FREE_COOLING_SWITCH_SET("freeCoolingSwitchSet", "key_776cffdee898406a9fda8022eebb2876"),
    FREE_COOLING_SWITCH_STATUS("freeCoolingSwitchStatus", "key_368b9b5fd8f74fc194451da1b688ba89"),
    COOLING_STORAGE_SWITCH_SAFETY_STATUS("coolingStorageSwitchSafetyStatus", "key_0a7095cf58c84a559712d27274df10de"),
    DISCHARGE_COOLING_SWITCH_SAFETY_STATUS("dischargeCoolingSwitchSafetyStatus", "key_14b3cab740804e73849ab598a973cf33"),
    DUAL_MODE_CHILLER_COOLING_SAFETY_STATUS("dualModeChillerCoolingSafetyStatus", "key_05cee15f7f004471a7ff1a59531eb2c2"),
    DUAL_MODE_CHILLER_STORAGE_SAFETY_STATUS("dualModeChillerStorageSafetyStatus", "key_395c34901e954609bc92599c7275f622"),
    COOLING_TOWER_OUT_TEMP_SET("coolingTowerOutTempSet", "key_eb9326859c8746899aacb16f6207caa2"),
    COOL_WATER_MAIN_SUPPLY_TEMP("coolWaterMainSupplyTemp", "key_eb9326859c8746899aacb16f6207caa3"),
    COOL_WATER_MAIN_RETURN_TEMP("coolWaterMainReturnTemp", "key_eb9326859c8746899aacb16f6207caa4"),
    COOL_WATER_DETA_TEMP_SET("coolWaterDetaTempSet", "key_eb9326859c8746899aacb16f6207caa5"),
    COOL_WATER_DETA_TEMP("coolWaterDetaTemp", "key_eb9326859c8746899aacb16f6207caa6"),
    COOLING_TOWER_OUT_TEMP_COOLING_SET("coolingTowerOutTempCoolingSet", "key_c2b85db72663498282d833280f4270e0"),
    COOLING_TOWER_OUT_TEMP_STORAGE_SET("coolingTowerOutTempStorageSet", "key_2e1565b817bd4273b291c0586f3395d6"),
    COOLING_WATER_SUPPLY_RETURN_DIFF_COOLING_SET("coolingWaterSupplyReturnDiffCoolingSet", "key_4fbc2f02fec24be68de2951d6ad2f3f9"),
    COOLING_WATER_SUPPLY_RETURN_DIFF_STORAGE_SET("coolingWaterSupplyReturnDiffStorageSet", "key_baf62e229669448ea13971b234737c85"),
    SOURCE_COOLING_WATER_RETURN_TEMP_SET("sourceCoolingWaterReturnTempSet", "key_b5d9eb126b334fc7813453a900c8d69a"),
    STORAGE_MAIN_COOLING_TEMP_DIFF_SET("storageMainCoolingTempDiffSet", "key_dd538121ba464aed924043effa5cdde1"),
    STORAGE_MAIN_STORAGE_TEMP_DIFF_SET("storageMainStorageTempDiffSet", "key_b07a68f2e320486a86d66312cf63ac71"),
    DUAL_MODE_CHILLER_STORAGE_OUT_TEMP_SET("dualModeChillerStorageOutTempSet", "key_3bb49cfb3afc4007b595dd0101b64cce"),
    DUAL_MODE_CHILLER_COOLING_OUT_TEMP_SET("dualModeChillerCoolingOutTempSet", "key_778874e3725f41b1adc941528c84b258"),
    BALANCE_PIPE_BYPASS_FLOW_SET("balancePipeBypassFlowSet", "key_1211e9a6d52146e1a81ad40e4714e5e0"),
    SOURCE_COOLING_WATER_RETURN_MAIN_TEMP("sourceCoolingWaterReturnMainTemp", "key_f4c57ea4b0e34ab4b53515b369d46d78"),
    SOURCE_COOLING_WATER_SUPPLY_MAIN_TEMP("sourceCoolingWaterSupplyMainTemp", "key_a13795ec72ce446793b8331732230ffc"),
    ICE_STORAGE_MAIN_TEMP_DIFF("iceStorageMainTempDiff", "key_ffaacdae8b1e4d80bb62455575c11a17"),
    AVG_COOLING_STORAGE_RATE("avgCoolingStorageRate", "key_266c680368f34f4ba08ca56322c474bc"),
    DISCHATGE_COOLING_MAIN_TEMP_DIFF("dischargeCoolingMainTempDiff", "key_d2b0a9c0c47c4c049a59c7c34bd680a6"),
    DISCHATGE_COOLING_MAIN_SUPPLY_PRESS("dischargeCoolingMainSupplyPress", "key_382085ea4c2d48a8a58e6e53e6668074"),
    DISCHATGE_COOLING_MAIN_RETURN_PRESS("dischargeCoolingMainReturnPress", "key_ca4be4e499314941a1cc5b32073c03da"),
    DISCHATGE_COOLING_MAIN_PRESS_DIFF("dischargeCoolingMainPressDiff", "key_fc1c7c3000e2427da57c45b1c0c25271"),
    EVAP_WATER_OUT_TEMP("evapWaterOutTemp", "key_5bed4c1cadd54320a0ccc91e3bdaec89"),
    EVAP_WATER_IN_TEMP("evapWaterInTemp", "key_cb1906c24eb04eb6a9c3fbc0d671cff8"),

    COMPREHENSIVE_COOLING_PRICE("comprehensiveCoolingPrice", "key_5145ac9e67c64f0983d129e9a29f0a3c"),
    COMPREHENSIVE_COOLING_CARBON("comprehensiveCoolingCarbon", "key_e11770ee6dc64b15b98198d54846da5c"),

    OPERATING_MODE_SET("operatingModeSet", "key_064700094a034644b01c84678adcde41"),

    COOL_RATED_INDOOR_MEDUIM_FLOW("coolRatedIndoorMeduimFlow","制冷工况室内侧载冷剂额定流量"),
    HEAT_RATED_INDOOR_MEDUIM_FLOW("heatRatedIndoorMeduimFlow","制热工况室内侧载冷剂额定流量"),
    VFD_MIN_FREQ("vFDMinFreq","变频最低频率"),

    //-----------------------------气象监测站----------------------------------------------------
    AMBIENT_TEMP("ambientTemp", "key_6ae5a4fe99654a20ae8cdc9f7bc39435"),
    TWB("twb", "key_91c0ed838786402db5fc05e182a47f6a"),
    AMBIENT_RH("ambientRH", "key_245a1f1c05844e85ba4536d0c50325b1"),
    //--------------------------------厨房油烟机-------------------------------------------------
    FAN_POS("fanPos", "key_d7f3b346aee8431396eb57c807372794"),
    NEED_EXAUST_SMOKE("needExaustSmoke", "key_cff68fcab6fc4f6c8527e6fbb76407fd"),



    LOCK_STATUS("lockStatus", "key_47708411b55a4fdcbea3eaa356e346c8"),

    /**
     * 照明
     */
    FUNC_TYPE("funcType", "key_c84272a66bfa41a1b4b70b02642d8d0d"),
    SERVICE_AREA_TYPE("serviceAreaType", "key_6ebe268426ff486b8ddd01a5065ad68f"),
    LAMP_FUNC_TYPE("lampFuncType", "key_401f5c7ab84d4db9a2833943b88259c6"),
    ILLUM_FB_CTR_TYPE("illumFbCtrType", "key_fac952e5ffd6497cbf2fec77f4f28295"),
    OUTDOOR_ILLUMINATION_VALUE_TO_TURN_ON_GENERAL_LIGHTING("outdoorIlluminationValueToTurnOnGeneralLighting", "key_fac952e5ffd6497cbf2fec77f4f28296"),
    FEEDBACKOF_LIGHTING_TURNON_LAST_TIME("feedbackofLightingTurnonLastTime", "key_c3b90820324040caa7678adc683865b2"),
    OUTDOOR_ILLUMINATION_VALUE_TO_TURN_OFF_GENERAL_LIGHTING("outdoorIlluminationValueToTurnOffGeneralLighting", "key_e3fcfd00ae104ffb976ca877b240354e"),
    FEEDBACKOF_LIGHTING_TURNOFF_LAST_TIME("feedbackofLightingTurnoffLastTime", "key_fcd24eb2689f49839454f12230153736"),
    REPLACED_BY_NAT_LIGHTING("replacedByNatLighting", "key_4fa2fb83ff23478490c3d1d8887774a6"),

    /**
     * 空调热源
     */
    WATER_DETA_TEMP("waterDetaTemp", "key_fd764bdffee34190a5dd3839483f79a1"),
    WATER_DETA_PRESS_SET("waterDetaPressSet", "key_fd764bdffee34190a5dd3839483f79a2"),
    WATER_DETA_TEMP_SET("waterDetaTempSet", "key_fd764bdffee34190a5dd3839483f79a1"),
    AIR_CONDITIONING_CAPACITY_SET("airConditioningCapacitySet", "key_air_conditioning_capacity_set"),
    ;


    private final String code;
    private final String desc;
    private final Class<? extends BaseInfoCodeValueEnum<?>> type;

    InfoCodeEnum(String code, String desc) {
        this(code, desc, null);
    }

    InfoCodeEnum(String code, String desc, Class<? extends BaseInfoCodeValueEnum<?>> type) {
        this.code = code;
        this.desc = desc;
        this.type = type;
    }

    public static List<String> getInfoCodeList() {
        List<String> infoCodes = new ArrayList<>();
        for (InfoCodeEnum value : InfoCodeEnum.values()) {
            infoCodes.add(value.getCode());
        }
        return infoCodes;
    }

    @Override
    public String getValue() {
        return code;
    }

    @Override
    public String value() {
        return code;
    }

    @Override
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(desc);
    }
    public String getDesc() {
        return LocaleMessageUtil.getMessage(desc);
    }
}
