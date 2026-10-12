package com.persagy.apccontrol.basic.structure.cacherwd.key;

import com.google.common.collect.ImmutableMap;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import org.jspecify.annotations.NonNull;

import java.util.Map;

/**
 * 新缓存对象模型业务 Key 配置。
 *
 * <p>这里只维护新七表使用的稳定业务 Key。旧 {@code cache_rwd_object} 仍使用原有策略生成的
 * 历史 Key，避免新版读取约定反向改变旧缓存。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public final class CacheObjectModelKeyConfig {

    /**
     * 空调末端项目全量空间 Key。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String AIR_TERMINAL_LIST_SPACE =
            "AIR_TERMINAL_listSpace";

    /**
     * 空调末端项目全量设备 Key。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String AIR_TERMINAL_LIST_EQUIPMENT =
            "AIR_TERMINAL_listEquipment";

    /**
     * 空调末端用户白名单空间 Key。
     *
     * <p>固定值与历史白名单 Key 保持一致，首版初始化才能只迁移配置 ID；后续读写统一从
     * 本配置引用，不能在业务代码中再次拼接。</p>
     *
     * @since v7.260701.0
     */
    public static final @NonNull String AIR_TERMINAL_WHITELIST_SPACE =
            "WHITELIST_AIR_TERMINAL_space";

    /**
     * 照明二级类型对应的项目全量设备 Key。
     *
     * @since v7.260701.0
     */
    public static final @NonNull Map<RunningMonitorSecondTypeEnum, String>
            LIGHTING_LIST_EQUIPMENT_BY_SECOND_TYPE = ImmutableMap.of(
                    RunningMonitorSecondTypeEnum.PUBLIC_AREA_LIGHTING,
                    "LIGHTING_PUBLIC_AREA_LIGHTING_listEquipment",
                    RunningMonitorSecondTypeEnum.PARKING_LOT_LIGHTING,
                    "LIGHTING_PARKING_LOT_LIGHTING_listEquipment",
                    RunningMonitorSecondTypeEnum.BUILDING_LIGHTING,
                    "LIGHTING_BUILDING_LIGHTING_listEquipment",
                    RunningMonitorSecondTypeEnum.LANDSCAPE_LIGHTING,
                    "LIGHTING_LANDSCAPE_LIGHTING_listEquipment",
                    RunningMonitorSecondTypeEnum.SIGN_LIGHTING,
                    "LIGHTING_SIGN_LIGHTING_listEquipment");

    /**
     * 通风项目全量空间 Key。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String VENTILATION_LIST_SPACE =
            "VENTILATION_listSpace";

    /**
     * 通风项目全量设备 Key。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String VENTILATION_LIST_EQUIPMENT =
            "VENTILATION_listEquipment";
    /**
     * 安防项目全量空间 Key。
     *
     * @since v7.260802.0
     */
    public static final @NonNull String SECURITY_LIST_SPACE =
            "SECURITY_listSpace";

    /**
     * 安防项目全量设备 Key。
     *
     * @since v7.260802.0
     */
    public static final @NonNull String SECURITY_LIST_EQUIPMENT =
            "SECURITY_listEquipment";

    /**
     * 机房安全楼层全量空间 Key。
     *
     * @since v7.260802.0
     */
    public static final @NonNull String COMPUTER_ROOM_SECURITY_LIST_SPACE =
            "COMPUTER_ROOM_SECURITY_listSpace";

    /**
     * 机房安全楼层全量设备 Key。
     *
     * @since v7.260802.0
     */
    public static final @NonNull String COMPUTER_ROOM_SECURITY_LIST_EQUIPMENT =
            "COMPUTER_ROOM_SECURITY_listEquipment";


    /**
     * 构建新版对象模型使用的白名单 Key。
     *
     * <p>所有新版白名单场景统一使用该稳定规则，业务代码不得调用旧缓存领域服务生成 Key。
     * 末端空间生成结果与历史白名单 Key 保持一致，仅用于首版配置 ID 迁移。</p>
     *
     * @param runningMonitorType 一级运行监测类型
     * @param objectType 对象类型
     * @return 新版白名单 Key
     * @since v7.260701.0
     */
    public static @NonNull String buildWhitelistKey(
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            @NonNull DtpRwdObjectTypeEnum objectType) {
        return "WHITELIST_" + runningMonitorType.name() + "_" + objectType.value();
    }

    /**
     * 工具类不允许实例化。
     *
     * @since v7.260701.0
     */
    private CacheObjectModelKeyConfig() {
        super();
    }
}
