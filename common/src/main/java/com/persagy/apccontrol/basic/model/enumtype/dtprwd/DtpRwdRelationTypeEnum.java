package com.persagy.apccontrol.basic.model.enumtype.dtprwd;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;

/**
 * 实例关联类型枚举
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@AllArgsConstructor
public enum DtpRwdRelationTypeEnum implements BaseEnum<Pair<String, String>> {

    // ------------------------- ArchSubset -------------------------

    /**
     * 项目下的建筑
     */
    BUILDING_IN_PROJECT(Pair.of("ArchSubset", "Pj2Bd"), "key_1a1d0f99a31a407a8041c7275e207867"),

    /**
     * 建筑下的楼层
     */
    FLOOR_IN_BUILDING(Pair.of("ArchSubset", "Bd2Fl"), "key_ac922b9c688b4286a8a7739ea7d4ba0b"),

    /**
     * 建筑下的空间
     */
    SPACE_IN_BUILDING(Pair.of("ArchSubset", "Bd2Sp"), "key_12077cbacb5742518ebbfd6bedb0fb04"),

    /**
     * 空间服务于楼层
     */
    SPACE_IN_FLOOR(Pair.of("ArchSubset", "Fl2Sp"), "key_65794414d0264c2f9824054abafcbe30"),

    // ------------------------- MechForArch -------------------------

    /**
     * 系统服务于空间
     */
    SYSTEM_SERVICE_SPACE(Pair.of("MechForArch", "Sy2Sp"), "key_23073375d7a440c38e93f6de6684d7e5"),

    /**
     * 设备服务于空间
     */
    EQUIPMENT_SERVICE_SPACE(Pair.of("MechForArch", "Eq2Sp"), "key_93300b82ebaf4a389a98a65b2db75bb9"),

    /**
     * 设备服务于楼层
     */
    EQUIPMENT_SERVICE_FLOOR(Pair.of("MechForArch", "Eq2Fl"), "key_91bf36f39e4e45a9be431ebb30c9550e"),

    // ------------------------- MechCtrl -------------------------

    /**
     * 设备通讯控制关系
     */
    EQUIPMENT_CONTROLLER(Pair.of("MechCtrl", "EqCtrl"), "key_140b55325d2e471f8bcbddf1463ed761"),

    // ------------------------- MechInArch -------------------------

    /**
     * 设备所在建筑
     */
    EQUIPMENT_IN_BUILDING(Pair.of("MechInArch", "Eq2Bd"), "key_8f66eb5fbb584a2a92b5040424a847e4"),

    /**
     * 设备所在楼层
     */
    EQUIPMENT_IN_FLOOR(Pair.of("MechInArch", "Eq2Fl"), "key_da4360d042624431864e6061296362f9"),

    /**
     * 设备所在空间
     */
    EQUIPMENT_IN_SPACE(Pair.of("MechInArch", "Eq2Sp"), "key_b4a1ff87b72d4729a2c75ede0d1d5457"),

    // ------------------------- EquipServing -------------------------

    /**
     * 系统服务于系统
     */
    SYSTEM_SERVICE_SYSTEM(Pair.of("EquipServing", "Sy4Sy"), "key_bc7b3566e11e48fab6b7029fa4f5f2ff"),


    /**
     * 设备服务于设备
     */
    EQ_SERVICE_EQ(Pair.of("EquipServing", "Eq4Eq"), "key_12bb83fff9e3456681881a26e00adec1"),

    // ------------------------- MechSubset -------------------------

    /**
     * 系统下的设备
     */
    EQ_IN_SYSTEM(Pair.of("MechSubset", "Sy2Eq"), "key_2d2cccc0c9ee4dbeb98111a24917f195"),

    /**
     * 设备下的设备
     */
    EQ_IN_EQ(Pair.of("MechSubset", "Eq2Eq"), "key_b609bee03a4d4e67abca105d47246351"),

    // ------------------------- CombineSubset -------------------------

    /**
     * 系统下的设备组
     */
    EQUIP_GROUP_IN_SYSTEM(Pair.of("CombineSubset", "Sy2Eg"), "key_852d2efda153405c9929aecc3137ffa2"),

    /**
     * 设备组下的设备
     */
    EQUIP_IN_GROUP(Pair.of("CombineSubset", "Eg2Eq"), "key_688075330d3147879e9c7031bdbf6cb0"),

    /**
     * 设备位置关系
     */
    EQ_LOCATION_IN_EQ(Pair.of("MechLocation", "Inside"), "key_b1d35c5a3c5943bcad23a878589a8d9a"),

    /**
     * 设备组下的设备
     */
    EQUIP_MONITOR_IN_SPACE(Pair.of("SensorRelationship", "Ss2Sp"), "key_b4db2ff2e8de44aeb4674dfde349ac8c"),

    // ------------------------- WENetwork -------------------------

    /**
     * 通信关系
     */
    WE_NETWORK(Pair.of("WENetwork", "Normal"), "key_7aa88010aba542b081c167a3fbb16d9a"),

    // ------------------------- FreshAirNetwork -------------------------

    /**
     * 新风管网关系
     */
    FRESH_AIR_NETWORK(Pair.of("FreshAirNetwork", "Fresh"), "key_d196555b01a046c19b8c603d638a8a23"),

    // ------------------------- ACAirNetwork -------------------------

    /**
     * 空调风管网空调送风关系
     *
     * @since v7.260502.0
     */
    AC_AIR_NETWORK_SUPPLY(Pair.of("ACAirNetwork", "Supply"), "key_5a8c3e7b9d1f4e2a8c6b5d7e3f9a0c1d"),

    /**
     * 空调风管网空调回风关系
     *
     * @since v7.260502.0
     */
    AC_AIR_NETWORK_RETURN(Pair.of("ACAirNetwork", "Return"), "key_7b2d4f9c8e1a3d5f6c0b9e2a4d7f1c3b"),

    /**
     * 空调风管网空调回风兼排风关系
     *
     * @since v7.260502.0
     */
    AC_AIR_NETWORK_RE_AND_EX(Pair.of("ACAirNetwork", "ReandEx"), "key_9c1e3d5a7f2b4c8e0d6a3f5b8e1c4d7a"),

    // ------------------------- VentNetwork -------------------------

    /**
     * 通风管网排风关系
     *
     * @since v7.260502.0
     */
    VENT_NETWORK_DISCHARGE(Pair.of("VentNetwork", "Discharge"), "key_2d4f6a8c0e1b3d5f7a9c1e3f5b7d9a2c"),

    /**
     * 空调冷冻水补水关系
     */
    CHILL_WATER_LOOP(Pair.of("ChillWaterLoop", "Fill"), "key_1fa218b0255c4b8093be131859407cc6"),
    /**
     *空调冷却水补水关系
     */
    COOLING_WATER_LOOP(Pair.of("CoolingWaterLoop", "Fill"), "key_3b121f6226f848e1bd552e5a9ff8d736"),

    ;

    /**
     * 枚举值
     */
    private final Pair<String, String> value;

    /**
     * 枚举信息
     */
    private final String reasonPhrase;


    @Override
    @NonNull
    public Pair<String, String> value() {
        return this.value;
    }

    @Override
    @NonNull
    public String getReasonPhrase() {
        return LocaleMessageUtil.getMessage(reasonPhrase);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
