package com.persagy.apccontrol.basic.constant;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.CustomFunctionDTO;
import com.persagy.apccontrol.basic.model.dto.RcInfoCodeGroupDTO;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.BasePropertyDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 本地常用缓存，这里所有的缓存，server服务使用没问题，compute服务使用的时候，需要确认是否可行
 * <p>
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/17 19:30
 */
public class LocalCacheConstant {

    // 需要对象所属系统的设备类集合，与 OBJECT_DETAIL_MAP 联合使用，目前仅支持冷站、冷热站、热站
    public static final Set<String> OBJECT_SYSTEM_CLASS = Sets.newHashSet("ACCC", "ACCI", "ACCH", "ACCCCC", "ACCCSC", "ACCCPC", "ACCCCP", "ACCCOP", "ACOTU", "ACCCOF", "ACVVEO", "ACVVEJ",
            "ACGEHE", "ACCHHP", "ACCHGB", "ACAHC", "ACCHCH", "ACCCOT", "ACCCWT", "ACGEET", "ACGEFP", "ACGEST", "ACPPC", "WSDHHE", "WSDHHP");

    // 照明
    public static final Set<String> LIGHT_MONITORING_OBJECT_CLASS = Sets.newHashSet("LTLTCC");

    // 安防, SPVSCM--cameraType=1/2/3
    public static final Set<String> SECURITY_OBJECT_CLASS = Sets.newHashSet("SPEGCR", "CFCSDR", "CFCSFR", "SPEGFG", "SPVSCM", "SPIACU");

    // 末端，ACATAH--installType=1/2
    public static final Set<String> AIR_TERMINAL_OBJECT_CLASS = Sets.newHashSet(
            "ACCCCP", "ACGEHE", "ACATAH", "ACATFU", "ACATFC", "ACVRVI",
            "ACAHG", "ACVVER", "ACVVEA", "ACATHT", "ACCCWP", "ACATFA",
            "ACATVA", "ACATRA", "ACVTAC", "ACVTSF", "FFSCAF",
            "ACVVEJ", "ACATDH", "ACAHUF"
    );
    // 通风，ACVTSF--funcType=1/2
    public static final Set<String> VENTILATION_OBJECT_CLASS = Sets.newHashSet("FFSCAF", "ACVTSF", "FFSCMF", "CFCSWN", "CFCSEW");

    // 厨房排油烟，ACVTSF--funcType=5
    public static final Set<String> KITCHEN_RANGE_HOOD_OBJECT_CLASS = Sets.newHashSet("ACKLSF", "ACVTSF", "ACKLAP");

    // 本方业务判断需要的中台对象所交付的静态字段信息，写入后 2 小时过期的本地缓存，过期后回源重查中台，保证静态数据（如 localName）变更可自动收敛
    public static final Cache<String, RwdObjectBusinessFeildDTO> OBJECT_DETAIL_MAP = Caffeine.newBuilder().expireAfterWrite(2, TimeUnit.HOURS).build();

    /** 该项目要加载的系统类型，后续可以获取配置 */
    public static final List<String> SYSTEM_CLASS = Lists.newArrayList("ACCC", "ACCI", "ACCH", "ACAT");
    /** 边缘端独有 所有系统数据 key -> systemId，value -> localName or name */
    public static Map<String, BasePropertyDTO> SYSTEM_INFO = new HashMap<>();

    /** 云边共存 兼容旧版控制,key:systemClassCode, value: 图名 */
    public static Map<String, String> CLASS_CODE_IMAGE_NAME = new HashMap<>();

    /** 云边共存 仅业务使用-依旧放在本地，所有的分组ID，对应的顶级分组ID，key -> groupId, value -> topGroupId */
    public static Map<String, String> NODE_TOP_ID = new HashMap<>();

    /** 云边共存 仅业务使用-依旧放在本地，所有的顶级分组ID，对应的分组树，key -> topGroupId, value -> tree */
    public static Map<String, RcInfoCodeGroupDTO> TOP_GROUP_TREE = new HashMap<>();

    /** 云边共存 当前类下定制功能，key -> classCode graphTypeCode, value -> key: group, infoCodes */
    public static Map<String, List<RcInfoCodeGroupDTO>> SYSTEM_RUN_PARAM_MAP = new HashMap<>();

    /** 云边共存 当前类下定制功能，key -> classCode graphTypeCode, value -> customFunctions */
    public static Map<String, List<CustomFunctionDTO>> CUSTOM_FUNCTION = new HashMap<>();

    /** 边缘端独有 websocket 推送配置 */
    public static Map<String, Set<String>> WEBSOCKET_CONFIG = new ConcurrentHashMap<>();

}
