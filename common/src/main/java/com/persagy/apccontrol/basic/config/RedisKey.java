package com.persagy.apccontrol.basic.config;

import cn.hutool.core.date.DatePattern;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一缓存key
 *
 * @author yuyao
 * @date 2022年11月23日 10:17
 */
public final class RedisKey {

    public final static String APC = "apc";

    public final static String MEOS = "meos";

    /**
     * hash 类型，设备类名称映射
     **/
    public final static String CLASS_NAME = APC + CommonConst.COLON + "class_name";

    /**
     * 信息点配置前缀
     */
    public final static String INFO_CODE_CONFIG_PREFIX =
            APC + CommonConst.COLON + "info_code_config_new" + CommonConst.COLON;
    public final static String INFO_CODE_CONFIG_DEMAND_PREFIX =
            APC + CommonConst.COLON + "info_code_config_demand" + CommonConst.COLON;
    public final static String INFO_CODE_CONFIG_GROUP_PREFIX = APC + CommonConst.COLON + "info_code_config_group";
    public final static String INFO_CODE_CONFIG_TRIGGER_PREFIX = APC + CommonConst.COLON + "info_code_config_trigger" + CommonConst.COLON;
    public final static String INFO_CODE_CONFIG_CONTROL_PREFIX =
            APC + CommonConst.COLON + "info_code_config_control" + CommonConst.COLON;

    /**
     * 计算不存在的集团编码
     */
    public final static String NON_GROUP_CODE =
            APC + CommonConst.COLON + "xx_job_task_non_group_code" + CommonConst.COLON;

    /**
     * 任务加锁标识
     */
    public static final String LOCK_KEY_TASK = APC + CommonConst.COLON + "lock_xx_job_task" + CommonConst.COLON;

    /**
     * 需求黑名单保存加锁标识
     */
    public static final String DEMAND_BLACKLIST_ADD_LOCK = APC + CommonConst.COLON + "DEMAND_BLACKLIST_ADD_LOCK";

    /**
     * IOT推送给前端的sock 数据缓存 反馈值
     */
    public static final String APC_IOT_REAL_TIME_DATA_KEY =
            APC + CommonConst.COLON + "iot_real_time_data" + CommonConst.COLON;

    /**
     * IOT推送给前端的sock 数据缓存 控制值
     */
    public static final String APC_IOT_CTRL_DATA_KEY = APC + CommonConst.COLON + "iot_ctrl_data" + CommonConst.COLON;

    /**
     * 强制状态信息点
     */
    public static final String APC_OBJ_INFOCODE_FORCED_KEY =
            APC + CommonConst.COLON + "obj_infocode_forced" + CommonConst.COLON;

    /**
     * 需求 人工干预
     */
    public static final String APC_SYSTEM_INTERVENE_KEY = APC + CommonConst.COLON + "intervene" + CommonConst.COLON;
    /**
     * 手自动状态
     */
    public static final String APC_AUTOMATIC_KEY = APC + CommonConst.COLON + "automatic" + CommonConst.COLON;

    /**
     * 空间异常
     */
    public static final String APC_SPATIAL_ANOMALY_KEY = APC + CommonConst.COLON + "anomaly" + CommonConst.COLON;

    /**
     * 运行日志缓存key
     */
    public static final String MEOS_RC_RUNNING_LOG = MEOS + CommonConst.COLON + "rc_running_log" + CommonConst.COLON;
    // 自控运行日志
    public static final String MEOS_RC_RUNNING_LOG_AUTO = MEOS + CommonConst.COLON + "rc_running_log_auto";
    // 设备报警运行日志
    public static final String MEOS_RC_RUNNING_LOG_ALARM = MEOS + CommonConst.COLON + "rc_running_log_alarm";
    // 当日运行日志占比  数据格式 人工,自控
    public static final String MEOS_RC_RUNNING_LOG_BY_DAY_AUTO_SCALE = MEOS + CommonConst.COLON + "rc_running_log_by_day_auto_scale";

    /**
     * 孪生运行日志建档锁前缀。
     *
     * <p>按控制指令 traceId 串行化"查重 + 建档"，防止设备多帧报文(开始执行 dataStatus=0 /
     * 执行中 dataStatus=1，result 均为 0 未完成)在 @Async 并发下对同一指令重复建档。</p>
     */
    public static final String APC_RC_RUNNING_LOG_TWIN_LOCK_KEY =
            APC + CommonConst.COLON + "rc_running_log_twin_lock" + CommonConst.COLON;

    /**
     * 运行分析报告
     */
    public static final String APC_RUN_ANALYSIS_REPORT_KEY = MEOS + CommonConst.COLON + "runAnalysisReport" + CommonConst.COLON;

    /**
     * 生成反馈值key
     *
     * @param objId
     * @param infoCode
     * @return java.lang.String
     * @author shaohongbo
     * @date 2023/4/14 16:15
     */
    public static String buildIotRealKey(String objId, String infoCode) {
        return RedisKey.APC_IOT_REAL_TIME_DATA_KEY + objId + CommonConst.COLON + infoCode;
    }

    /**
     * 生成控制值key
     *
     * @param objectId
     * @param infoCode
     * @return java.lang.String
     * @author shaohongbo
     * @date 2023/4/14 16:15
     */
    public static String buildIotCtrlKey(String objectId, String infoCode) {
        return RedisKey.APC_IOT_CTRL_DATA_KEY + objectId + CommonConst.COLON + infoCode;

    }

    /**
     * 信息点强制状态位判断，和IOT中得不太一样，业务定义，这里指的永远都是主设备对应的信息点的强控标识
     */
    public static String buildObjectInfoCodeForcedKey(String objectId, String infoCode) {
        return RedisKey.APC_OBJ_INFOCODE_FORCED_KEY + objectId + CommonConst.COLON + infoCode;
    }

    /**
     * 系统运行模式
     *
     * @param projectId
     * @param systemId
     * @return
     */
    public static String buildSystemRunnningModel(String projectId, String systemId) {
        return APC + CommonConst.COLON + "system_running_model" + CommonConst.COLON + projectId + CommonConst.COLON + systemId;
    }

    /**
     * 当前项目所配置的所有信息点配置信息缓存， string 类型
     *
     * @param classCode
     * @param infoCode
     * @return
     */
    public static String buildInfoCodeConfigStringKey(String classCode, String infoCode) {
        return INFO_CODE_CONFIG_PREFIX + classCode + CommonConst.COLON + infoCode;
    }

    /**
     * 控制点位
     *
     * @param classCode
     * @param infoCode
     * @return
     */
    public static String buildControlInfoCodeConfigStringKey(String classCode, String infoCode) {
        return INFO_CODE_CONFIG_CONTROL_PREFIX + classCode + CommonConst.COLON + infoCode;
    }

    /**
     * 需求类型下所有的信息点
     *
     * @param demandType
     * @return
     */
    public static String buildDemandTypeInfoCodeConfigStringKey(String demandType) {
        return INFO_CODE_CONFIG_DEMAND_PREFIX + demandType;
    }

    /**
     * 信息点配置分组信息
     *
     * @return
     */
    public static String buildInfoCodeConfigGroupHashKey() {
        return INFO_CODE_CONFIG_GROUP_PREFIX;
    }

    /**
     * 信息点配置触发表达式的信息
     *
     * @return
     */
    public static String buildInfoCodeConfigTriggerHashKey(String classCode) {
        return INFO_CODE_CONFIG_TRIGGER_PREFIX + classCode;
    }

    /**
     * 手自动
     * key: classCode，value: objectIds
     *
     * @return
     */
    public static String getAutomatic(String objId, String infoCode) {
        return RedisKey.APC_AUTOMATIC_KEY + objId + CommonConst.COLON + infoCode;
    }

    /**
     * 空间异常
     * key: classCode，value: objectIds
     *
     * @return
     */
    public static String getSpatialAnomaly(String objId, String infoCode, String mark) {
        return RedisKey.APC_SPATIAL_ANOMALY_KEY + objId + CommonConst.COLON + infoCode + CommonConst.COLON + mark;
    }

    /**
     * 系统泵组 人工干预
     */
    public static String systemIntervene(String objectId, String infoCode) {
        return APC_SYSTEM_INTERVENE_KEY + CommonConst.COLON + objectId + CommonConst.COLON + infoCode;
    }

    /**
     * 空调需求开关
     *
     * @param projectId
     * @return
     */
    public static String getAirConditionDemandKey(String projectId) {
        return APC + CommonConst.COLON + projectId + CommonConst.COLON + "AIR_CONDITION_DEMAND";
    }

    //获取运行分析报告生成key
    public static String getRunAnalysisReportKey(LocalDate reportDate, String type, String projectId) {
        return APC_RUN_ANALYSIS_REPORT_KEY + projectId + CommonConst.COLON + reportDate + CommonConst.COLON + type;
    }

    /**
     * 物理世界缓存构建锁key
     */
    public static final String APC_CACHE_RWD_BUILD_KEY = MEOS + CommonConst.COLON + "cacheRwdBuild";

    /**
     * 缓存对象模型状态 Redis Key 前缀。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String APC_CACHE_OBJECT_MODEL_STATUS_KEY =
            APC + CommonConst.COLON + "cache_object_model_status" + CommonConst.COLON;

    /**
     * 缓存规则业务键分配锁前缀。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String APC_CACHE_RULE_WRITE_LOCK_KEY =
            APC + CommonConst.COLON + "cache_rule_write_lock" + CommonConst.COLON;

    //获取物理世界缓存构建锁key
    public static String getCacheRwdBuildKey(String customIndexKey, String projectId) {
        return APC_CACHE_RWD_BUILD_KEY + CommonConst.COLON + projectId + CommonConst.COLON + customIndexKey;
    }

    /**
     * 构造当前项目缓存规则业务键分配锁。
     *
     * @param projectId 项目 ID
     * @return 项目级规则写入锁 Key
     * @since v7.260701.0
     */
    public static @NonNull String getCacheRuleWriteLockKey(@NonNull String projectId) {
        return RedisKey.APC_CACHE_RULE_WRITE_LOCK_KEY + projectId;
    }

    /**
     * 构造缓存对象模型状态 Key。
     *
     * <p>集团编码和项目 ID 同时参与 Key，避免多租户共享 Redis 时跨集团/项目串值；状态 Key
     * 不设置过期时间，由状态生命周期显式维护。</p>
     *
     * @param groupCode           集团编码
     * @param projectId           项目 ID
     * @param runningMonitorType 一级运行监测类型
     * @return 状态 Redis Key
     * @since v7.260701.0
     */
    public static @NonNull String getCacheObjectModelStatusKey(
            @NonNull String groupCode,
            @NonNull String projectId,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        return RedisKey.APC_CACHE_OBJECT_MODEL_STATUS_KEY
                + groupCode + CommonConst.COLON
                + projectId + CommonConst.COLON
                + BasicsConfigTypeEnum.CACHE_OBJECT_MODEL_STATUS.getCode() + CommonConst.COLON
                + runningMonitorType.value();
    }

    //业务性检查
    public static String getBizCheckKey(String projectId, String bizCode) {
        return APC + CommonConst.COLON + "BIZ_CHECK" + CommonConst.COLON + projectId + CommonConst.COLON + bizCode;
    }

    /**
     * 运行日志缓存key
     * 只保留一天的 加日期区分
     *
     * @param systemId 系统 ID
     * @return key
     */
    public static String MEOS_RC_RUNNING_LOG(String systemId) {
        return MEOS_RC_RUNNING_LOG + CommonConst.COLON + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATE_PATTERN)) + CommonConst.COLON + systemId;
    }

    public static String MEOS_RC_RUNNING_LOG_AUTO() {
        return MEOS_RC_RUNNING_LOG_AUTO + CommonConst.COLON + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATE_PATTERN));
    }

    public static String MEOS_RC_RUNNING_LOG_BY_DAY_AUTO_SCALE() {
        return MEOS_RC_RUNNING_LOG_BY_DAY_AUTO_SCALE + CommonConst.COLON + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DatePattern.PURE_DATE_PATTERN));
    }

    /**
     * {@code rcRunStatus}补偿跟踪Hash前缀。
     *
     * <p>完整key按项目和设备类型分组：
     * {@code apc:rc_runstatus_tracking_hash:{projectId}:{classCode}}。
     * Hash字段为{@code objectId}，Hash值为进入补偿时的过渡状态：
     * {@code 2}-开启中，{@code 3}-关闭中。
     */
    public static final String APC_RC_RUNSTATUS_TRACKING_HASH_KEY = APC + CommonConst.COLON + "rc_runstatus_tracking_hash";

    /**
     * 获取{@code rcRunStatus}补偿跟踪Hash key。
     *
     * @param projectId 项目ID
     * @param classCode 设备类型编码
     * @return {@code rcRunStatus}补偿跟踪Hash key
     * @since v7.260402.0
     */
    public static String buildRcRunStatusTrackingHashKey(String projectId, String classCode) {
        return APC_RC_RUNSTATUS_TRACKING_HASH_KEY + CommonConst.COLON + projectId + CommonConst.COLON + classCode;
    }

    /**
     * 构造孪生运行日志建档锁 key。
     *
     * @param traceId  控制指令追踪 ID
     * @param objectId 对象 ID
     * @param infoCode 信息点编码
     * @return 建档锁 key
     */
    public static String buildRunningLogTwinLockKey(String traceId, String objectId, String infoCode) {
        return APC_RC_RUNNING_LOG_TWIN_LOCK_KEY + traceId + CommonConst.COLON + objectId + CommonConst.COLON + infoCode;
    }
}
