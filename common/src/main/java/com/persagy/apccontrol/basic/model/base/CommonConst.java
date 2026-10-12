package com.persagy.apccontrol.basic.model.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.enumtype.EnumTimeType;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;

import java.time.LocalDateTime;
import java.util.*;

import static com.persagy.apccontrol.basic.utils.dateUtil.DateUtils.DTF_SDF_SECOND;

/**
 * @author yuyao
 * @Date 2022/3/2 2:46 PM
 * @Description 常量类 字符串常量有通用类 com.persagy.framework.common.util.string.StrPool
 */
public final class CommonConst {

    /**
     * 应用名称
     */
    public static final String APPLICATION_NAME = "meos-control";

    /**
     * 锁前缀
     */
    public static final String LOCK_PREFIX = "meos:lock:";

    // custom header name
    public static final String MODEL_TYPE_HEADER = "modelType";

    // 非数值类的信息点
    public static final Set<String> STRING_VALUE_INFO_CODES = Sets.newHashSet("stopFloor");

    public static final String FROM_RELATION = "from";

    public static final String TO_RELATION = "to";

    /**
     * 强控点位释放的 infoValue 传参值
     */
    public static final String FORCE_VALUE = "release";

    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";

    /**
     * 减号
     */
    public static final String MINUS = "-";

    /**
     * 冒号
     */
    public static final String COLON = ":";

    /**
     * 空字符串视图
     */
    public static final String NULL_STRING_VO = "--";

    /**
     * SYSTEM
     */
    public static final String SYSTEM = "system";
    /**
     * SYSTEM
     */
    public static final String SYSTEM_NAME = LocaleMessageUtil.getMessage("key_97300a9731294466884c1933c2a6cb49");
    /**
     * projectId
     */
    public static final String PROJECT_ID = "projectId";
    /**
     * SYS
     */
    public static final String GROUP_CODE = "groupCode";
    /**
     * LIMIT_ONE
     */
    public static final String LIMIT_ONE = "limit 1";
    /**
     * LIMIT_ONE
     */
    public static final String LIMIT_FIVE = "limit 5";

    public static final String USER_ID = "systemId";
    public static final String LOGINDEVICE = "PC";

    //冷暂 系统设备编码
    public static final String ACCC = "ACCC";
    //冷热站 系统设备编码
    public static final String ACCI = "ACCI";

    public static final String building = "building";// 建筑
    public static final String floor = "floor";// 楼层
    public static final String space = "space";//空间

    public static final String RUNNING_MONITOR_TYPE = "runningMonitorType";// 建筑
    public static final String IPDATA_START_TIME = "IPDataStartTime";// 楼层
    public static final String IPDATA_END_TIME = "IPDataEndTime";//空间
    public static final String IPTYPE = "IPType";//空间
    // 需要替换时间和分精度的系统
    public static final List<String> SYSTEMS_THAT_NEED_TO_BE_REPLACED = Arrays.asList(
            RunningMonitorTypeEnum.COLD_HEAT_SOURCE.value(),
            RunningMonitorTypeEnum.COLD_SOURCE.value(),
            RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE.value(),
            RunningMonitorTypeEnum.AIR_CONDITIONING_HEAT_SOURCE.value(),
            RunningMonitorTypeEnum.AIR_CONDITIONING_C_H_SOURCE.value(),
            RunningMonitorTypeEnum.PHOTOVOLTAIC.value()
//            RunningMonitorTypeEnum.HEAT_SOURCE.value()
    );

    // 需要替换时间和分精度的系统
    public static final List<RunningMonitorTypeEnum> RUNNING_MONITOR_TYPE_NEED_REPLACED = Arrays.asList(
            RunningMonitorTypeEnum.COLD_HEAT_SOURCE,
            RunningMonitorTypeEnum.COLD_SOURCE,
            RunningMonitorTypeEnum.AIR_CONDITIONING_COLD_SOURCE,
            RunningMonitorTypeEnum.AIR_CONDITIONING_HEAT_SOURCE,
            RunningMonitorTypeEnum.AIR_CONDITIONING_C_H_SOURCE,
            RunningMonitorTypeEnum.PHOTOVOLTAIC
//            RunningMonitorTypeEnum.HEAT_SOURCE.value()
    );

    /**
     * 系统jsonMapper
     */
    public static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static class DtpRwdConst {
        public static final String GeneralZone = "GeneralZone";// 物业分区
        public static final String WEFCDC = "WEFCDC";// 采集器
        public static final String BUILDING = "building";// 建筑
        public static List<String> MeterClassCodeList = new ArrayList<>();

        static {
            MeterClassCodeList.add("WESSEE");
            MeterClassCodeList.add("WESSFL");
        }
    }

    /**
     * 可监测 信息点
     */
    public static final String MONITORING_SURVEILLANCE_STATUS = "monitoringSurveillanceStatus";

    /**
     * 可检测的信息点的值
     */
    public static final ArrayList<String> MONITORING_SURVEILLANCE_STATUS_LIST = Lists.newArrayList("2", "3");

    public static Boolean isNeedReplacedSystem(Map<String, Object> extra) {
        return extra.containsKey(CommonConst.RUNNING_MONITOR_TYPE) && Objects.nonNull(extra.get(CommonConst.RUNNING_MONITOR_TYPE)) &&
                SYSTEMS_THAT_NEED_TO_BE_REPLACED.contains(extra.get(CommonConst.RUNNING_MONITOR_TYPE).toString());
    }

    /**
     * 冷源、冷热源需要在context中设置时间、精度、系统类，统一数据时间
     *
     * @param runningMonitorType 运行监测类型
     */
    public static void checkSystemReplacementTime(RunningMonitorTypeEnum runningMonitorType) {
        if (RUNNING_MONITOR_TYPE_NEED_REPLACED.contains(runningMonitorType)) {
            LocalDateTime fiveMinutePrecisionTime = DateUtils.getTheLastPrecisionMinutes(5);
            String tenMinutesAgo = fiveMinutePrecisionTime.minusMinutes(5).format(DTF_SDF_SECOND);
            HashMap<String, Object> extra = new HashMap<>();
            extra.put(RUNNING_MONITOR_TYPE, runningMonitorType);
            extra.put(IPDATA_START_TIME, tenMinutesAgo);
            extra.put(IPDATA_END_TIME, fiveMinutePrecisionTime.format(DTF_SDF_SECOND));
            extra.put(IPTYPE, EnumTimeType.MINUTE5.getName());
            ContextUtil.getContext().setExtra(extra);
        }
    }

}
