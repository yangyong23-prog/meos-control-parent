package com.persagy.apccontrol.basic.model.base;

import com.persagy.apccontrol.basic.config.cache.RedisCacheConfigurationImpl;
import org.springframework.data.redis.cache.CacheKeyPrefix;

/**
 * 动态缓存常量
 *
 * <p>{@value DYNAMIC_KEY_PREFIX} 开头的变量名将在启动时初始化到内存缓存中根据 {@value DYNAMIC_KEY_TIMEOUT_SPLIT} 来分割过期时间
 * <p>过期时间通过 {@link java.time.Duration} 来解析, 支持的单位如下
 * <ul>
 *     <li>秒: {@code s}</li>
 *     <li>分钟: {@code m}</li>
 *     <li>小时: {@code h}</li>
 *     <li>天: {@code d}</li>
 * </ul>
 * <p>定义变量规范示例如下:
 * <pre>
 *     // 变量名称可自定义, 推荐统一加上 [DYNAMIC_] 前缀便于区分哪些是动态缓存
 *     // 变量值的定义统一使用 [DYNAMIC_KEY_PREFIX] + [缓存名称] + [DYNAMIC_KEY_SPLIT] + "[过期时间]"
 *     // 也可以直接使用 [DYNAMIC_KEY_PREFIX] + [缓存名称] 不进行设置过期时间 (不推荐)
 *     // 当未进行配置过期时间时将使用默认过期时间 {@link CacheKey#DEFAULT_TTL}
 *     String DYNAMIC_USER_CACHE = DYNAMIC_KEY_PREFIX + "USER_CACHE" + DYNAMIC_KEY_SPLIT + "60s";
 * </pre>
 * <p><b>Tips - </b>当缓存名称需要分割时建议分隔符使用 {@link CacheKeyPrefix#SEPARATOR}
 *
 * @author JiYinchuan
 * @see RedisCacheConfigurationImpl
 * @since v6.0.0
 */
public interface CacheDynamicKey {

    /**
     * Key统一前缀
     */
    String DYNAMIC_KEY_PREFIX = "CACHE_";

    /**
     * Key时间单位分隔符
     */
    String DYNAMIC_KEY_TIMEOUT_SPLIT = "_";

    /**
     * 运维平台-有效集团
     */
    String SAAS_GROUP_VALID = DYNAMIC_KEY_PREFIX
            + "saas" + CacheKeyPrefix.SEPARATOR + "group_valid"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 运维平台-项目
     */
    String SAAS_PROJECT = DYNAMIC_KEY_PREFIX
            + "saas" + CacheKeyPrefix.SEPARATOR + "project"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 运维平台-项目
     */
    String SAAS_PROJECT_VALID = DYNAMIC_KEY_PREFIX
            + "saas" + CacheKeyPrefix.SEPARATOR + "project_valid"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    // ------------------------- 物理世界 -------------------------

    /**
     * 物理世界-对象树
     */
    String RWD_OBJECT_INSTANCE_TREE = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_instance_tree"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "5m";

    /**
     * 物理世界-对象
     */
    String RWD_OBJECT = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-关联对象
     */
    String RWD_RELATION_OBJECT = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "relation_object"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-建筑
     */
    String RWD_OBJECT_BUILDING = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_building"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-建筑
     */
    String RWD_OBJECT_BUILDING_BY_FLOOR_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_building_by_floor_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-楼层
     */
    String RWD_OBJECT_FLOOR = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_floor"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-建筑下楼层
     */
    String RWD_OBJECT_FLOOR_BY_BUILDING_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_floor_by_building_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-空间所在楼层
     */
    String RWD_OBJECT_FLOOR_BY_SPACE_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_floor_by_space_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-设备服务楼层
     */
    String RWD_OBJECT_FLOOR_SERVICE_BY_EQUIPMENT_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_floor_service_by_equipment_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-空间
     */
    String RWD_OBJECT_SPACE = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_space"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-设备所在空间
     */
    String RWD_OBJECT_SPACE_IN_BY_EQUIPMENT_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_space_in_by_equipment_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-设备所在空间
     */
    String RWD_OBJECT_SPACE_SERVICE_BY_EQUIPMENT_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_space_service_by_equipment_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-中央供冷系统
     */
    String RWD_OBJECT_CENTRAL_COOLING_SYSTEM = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_central_cooling_system"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    /**
     * 物理世界-中央供冷系统
     */
    String RWD_OBJECT_CENTRAL_COOLING_HEATING_SYSTEM = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_central_cooling_heating_system"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    /**
     * 物理世界-中央系统
     */
    String RWD_OBJECT_CENTRAL_SYSTEM = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_central_system"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    /**
     * 物理世界-冷水机组
     */
    String RWD_OBJECT_CHILLER_UNIT = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_chiller_unit"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    /**
     * 物理世界-冷冻泵组
     */
    String RWD_OBJECT_FREEZE_PUMP_GROUP = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_freeze_pump_group"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    /**
     * 物理世界-照明回路
     */
    String RWD_OBJECT_CIRCUIT = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_circuit"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-照明回路
     */
    String RWD_OBJECT_CIRCUIT_BY_SPACE_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_circuit_by_space_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-照明控制模块关联照明回路
     */
    String RWD_OBJECT_CIRCUIT_BY_CONTROLLER_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_circuit_by_controller_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-照明控制模块
     */
    String RWD_OBJECT_CIRCUIT_CONTROLLER = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_circuit_controller"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-空间下照明控制模块
     */
    String RWD_OBJECT_CIRCUIT_CONTROLLER_IN_BY_SPACE_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_circuit_controller_in_by_space_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-照明回路关联照明控制模块
     */
    String RWD_OBJECT_CIRCUIT_CONTROLLER_BY_CIRCUIT_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_circuit_controller_by_circuit_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 物理世界-低压末端配电柜
     */
    String RWD_OBJECT_DISTRIBUTION_CABINET = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_distribution_cabinet"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    /**
     * 物理世界-照明回路关联低压末端配电柜
     */
    String RWD_OBJECT_DISTRIBUTION_CABINET_BY_EQUIPMENT_ID = DYNAMIC_KEY_PREFIX
            + "rwd" + CacheKeyPrefix.SEPARATOR + "object_distribution_cabinet_by_equipment_id"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    // ------------------------- 数据字典 -------------------------

    /**
     * 数据字典-信息点定义
     */
    String KNOWLEDGE_INFO_CODE = DYNAMIC_KEY_PREFIX
            + "knowledge" + CacheKeyPrefix.SEPARATOR + "info_code"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 数据字典-信息点定义
     */
    String KNOWLEDGE_INFO_CODE_LIST = DYNAMIC_KEY_PREFIX
            + "knowledge" + CacheKeyPrefix.SEPARATOR + "info_code_list"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 数据字典-信息点定义
     */
    String KNOWLEDGE_INFO_CODE_MAP = DYNAMIC_KEY_PREFIX
            + "knowledge" + CacheKeyPrefix.SEPARATOR + "info_code_map"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 数据字典-设备类定义
     */
    String KNOWLEDGE_CLASS_CODE = DYNAMIC_KEY_PREFIX
            + "knowledge" + CacheKeyPrefix.SEPARATOR + "class_code"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 数据字典-设备类编码名称
     */
    String KNOWLEDGE_CLASS_CODE_NAME_MAP = DYNAMIC_KEY_PREFIX
            + "knowledge" + CacheKeyPrefix.SEPARATOR + "class_code_name_map"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    // ------------------------- 通用配置 -------------------------

    /**
     * 信息点配置
     */
    String INFO_CODE_CFG = DYNAMIC_KEY_PREFIX
            + "rc:info_code_cfg"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "1d";

    /**
     * 运行分析_存在规则空间
     */
    String RUN_ANALYSIS_EXISTS_RULE_SPACE = DYNAMIC_KEY_PREFIX
            + "RUN_ANALYSIS" + CacheKeyPrefix.SEPARATOR + "EXISTS_RULE_SPACE"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

    /**
     * 安防_通行空间
     */
    String SECURITY_ACCESS_SPACE = DYNAMIC_KEY_PREFIX
            + "SECURITY" + CacheKeyPrefix.SEPARATOR + "ACCESS_SPACE"
            + DYNAMIC_KEY_TIMEOUT_SPLIT
            + "30m";

}
