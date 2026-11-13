package com.persagy.apccontrol.common.server;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.common.model.vo.PaasDataCacheVo;
import com.persagy.apccontrol.common.opt.OptRunningLogUploader;
import com.persagy.apccontrol.common.outside.feign.PaasDatatransferClient;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 边到云数据同步
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/7/10 17:40
 */
@Slf4j
@Component
public class PaasDatatransferServer {

    @Value("${mysql.database.tenant-case-sensitive:true}")
    private Boolean tenantUpper;

    @Autowired
    private ProjectCfg projectCfg;

    @Autowired
    private PaasDatatransferClient paasDatatransferClient;

    /** opt 分支 B1：switch.opt.runningLogQueue 打开时运行日志改为入本地队列异步合批发送 */
    @Lazy
    @Autowired(required = false)
    private OptRunningLogUploader optRunningLogUploader;

    /**
     * 价值计算结果同步
     *
     * @param groupCode
     * @param rcCalculateResults
     * @return
     */
    public boolean sendCalculateDataListToKafka(String groupCode, List<RcCalculateResult> rcCalculateResults) {
        boolean result = true;
        groupCode = StrUtil.isBlank(groupCode) ? projectCfg.getGroupCode() : groupCode;
        try {
            List<PaasDataCacheVo> paasDataCacheVos = new ArrayList<>();
            for (RcCalculateResult rcCalculateResult : rcCalculateResults) {
                paasDataCacheVos.add(getKafkaMessage(groupCode, rcCalculateResult, tenantUpper));
            }
            List<List<PaasDataCacheVo>> partition = Lists.partition(paasDataCacheVos, 100);
            for (List<PaasDataCacheVo> list : partition) {
                //log.info("价值计算-同步数据: {}", JsonUtils.toJson(list));
                this.paasDatatransferClient.sendDataToKafka(list);
            }
        } catch (Exception e) {
            result = false;
            log.error("运行日志云边同步异常", e);
        }
        return result;
    }

    /**
     * 同步运行日志到kafka，进而写入数据到云边数据库
     *
     * @param groupCode
     * @param runningLogs
     */
    public boolean sendLogDataToKafka(String groupCode, List<RcRunningLog> runningLogs) {
        boolean result = true;
        groupCode = StrUtil.isBlank(groupCode) ? projectCfg.getGroupCode() : groupCode;

        try {
            LocalDateTime localDateTime = LocalDateTime.now();
            List<PaasDataCacheVo> paasDataCacheVos = new ArrayList<>();
            for (RcRunningLog runningLog : runningLogs) {
                runningLog.setSynTime(localDateTime);
                paasDataCacheVos.add(getKafkaMessage(groupCode, runningLog, tenantUpper));
            }
            //log.info("运行日志-同步数据: {}", JsonUtils.toJson(paasDataCacheVos));
            List<List<PaasDataCacheVo>> partition = Lists.partition(paasDataCacheVos, 100);
            for (List<PaasDataCacheVo> list : partition) {
                this.paasDatatransferClient.sendDataToKafka(list);
            }
        } catch (Exception e) {
            result = false;
            log.error("运行日志云边同步异常", e);
        }
        return result;
    }

    /**
     * 同步运行日志到kafka，进而写入数据到云边数据库
     *
     * @param groupCode
     * @param runningLog
     */
    public boolean sendLogDataToKafka(String groupCode, RcRunningLog runningLog) {
        boolean result = true;
        groupCode = StrUtil.isBlank(groupCode) ? projectCfg.getGroupCode() : groupCode;

        if (runningLog != null) {
            try {
                PaasDataCacheVo kafkaMessage = getKafkaMessage(groupCode, runningLog, tenantUpper);
                this.paasDatatransferClient.sendDataToKafka(Lists.newArrayList(kafkaMessage));
            } catch (Exception e) {
                result = false;
                log.error("运行日志云边同步异常，异常数据: {}", e);
                log.error("运行日志云边同步异常，异常数据: {}", JsonUtils.toJson(runningLog));
            }
        }

        return result;
    }
    
    /**
     * 转换为kafka所需要的实体类
     *
     * @param groupCode
     * @param runningLog
     */
    public static PaasDataCacheVo getKafkaMessage(String groupCode, RunningLog runningLog, Boolean tenantUpper) {
        PaasDataCacheVo paasDataCacheVo = new PaasDataCacheVo();
        
        // key
        String tenant = BooleanUtil.isTrue(tenantUpper) ? groupCode : groupCode.toLowerCase();
        PaasDataCacheVo.PaasDataCacheKey paasDataCacheKey = new PaasDataCacheVo.PaasDataCacheKey();
        paasDataCacheKey.setId(runningLog.getId().toString());
        paasDataCacheKey.setPaas_table("running_log");
        paasDataCacheKey.setPaas_schema(tenant + "_meos_control");
        paasDataCacheVo.setKey(JsonUtils.toJson(paasDataCacheKey));
        
        // value
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", runningLog.getId());
        valueMap.put("project_id", runningLog.getProjectId());
        valueMap.put("type", runningLog.getType().value());
        valueMap.put("object_id", runningLog.getObjectId());
        valueMap.put("object_name", runningLog.getObjectName());
        valueMap.put("state", runningLog.getState().value());
        valueMap.put("key_word", runningLog.getKeyWord());
        valueMap.put("create_user", runningLog.getCreateUser());
        valueMap.put("data_time", runningLog.getDataTime());
        valueMap.put("create_time", runningLog.getCreateTime());
        valueMap.put("content", runningLog.getContent() == null ? null : JsonUtils.toJson(runningLog.getContent()));
        valueMap.put("class_code", runningLog.getClassCode());
        valueMap.put("system_id", runningLog.getSystemId());
        valueMap.put("system_name", runningLog.getSystemName());
        valueMap.put("system_class_code", runningLog.getSystemClassCode());
        valueMap.put("second_level_category", runningLog.getSecondLevelCategory());
        valueMap.put("first_level_category", runningLog.getFirstLevelCategory());
        valueMap.put("effect_start_time", runningLog.getEffectStartTime());
        valueMap.put("effect_end_time", runningLog.getEffectEndTime());
        valueMap.put("trace_id", runningLog.getTraceId());
        valueMap.put("info_code", runningLog.getInfoCode());
        valueMap.put("info_value", runningLog.getInfoValue());
        valueMap.put("source_type", runningLog.getSourceType() == null ? null : runningLog.getSourceType().value());
        
        paasDataCacheVo.setValue(JsonUtils.toJson(valueMap));
        
        return paasDataCacheVo;
    }

    public static PaasDataCacheVo getKafkaMessageDelete(String groupCode, RunningLog runningLog, Boolean tenantUpper) {
        PaasDataCacheVo paasDataCacheVo = new PaasDataCacheVo();
        // key
        String tenant = BooleanUtil.isTrue(tenantUpper) ? groupCode : groupCode.toLowerCase();
        PaasDataCacheVo.PaasDataCacheKey paasDataCacheKey = new PaasDataCacheVo.PaasDataCacheKey();
        paasDataCacheKey.setId(runningLog.getId().toString());
        paasDataCacheKey.setPaas_table("running_log");
        paasDataCacheKey.setPaas_schema(tenant + "_meos_control");
        paasDataCacheVo.setKey(JsonUtils.toJson(paasDataCacheKey));
        return paasDataCacheVo;
    }

    /**
     * 价值计算结果同步
     *
     * @param groupCode
     * @param rcCalculateResult
     * @return
     */
    public static PaasDataCacheVo getKafkaMessage(String groupCode, RcCalculateResult rcCalculateResult, Boolean tenantUpper) {
        PaasDataCacheVo paasDataCacheVo = new PaasDataCacheVo();

        // key
        String tenant = BooleanUtil.isTrue(tenantUpper) ? groupCode : groupCode.toLowerCase();
        PaasDataCacheVo.PaasDataCacheKey paasDataCacheKey = new PaasDataCacheVo.PaasDataCacheKey();
        paasDataCacheKey.setId(rcCalculateResult.getId());
        paasDataCacheKey.setPaas_table("rc_calculate_result");
        paasDataCacheKey.setPaas_schema(tenant + "_meos_control");
        paasDataCacheKey.setJson_columns("work_time,calculate_result,extend_info");
        paasDataCacheVo.setKey(JsonUtils.toJson(paasDataCacheKey));

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", rcCalculateResult.getId());
        valueMap.put("group_code", rcCalculateResult.getGroupCode());
        valueMap.put("project_id", rcCalculateResult.getProjectId());
        valueMap.put("system_id", rcCalculateResult.getSystemId());
        valueMap.put("space_id", rcCalculateResult.getSpaceId());
        valueMap.put("space_local_id", rcCalculateResult.getSpaceLocalId());
        valueMap.put("space_local_name", rcCalculateResult.getSpaceLocalName());
        valueMap.put("work_date", rcCalculateResult.getWorkDate());
        valueMap.put("work_time", rcCalculateResult.getWorkTime());
        valueMap.put("season", rcCalculateResult.getSeason());
        valueMap.put("calculate_type", rcCalculateResult.getCalculateType());
        valueMap.put("calculate_result", rcCalculateResult.getCalculateResult());
        valueMap.put("extend_info", rcCalculateResult.getExtendInfo());
        valueMap.put("create_time", rcCalculateResult.getCreateTime());
        valueMap.put("update_time", rcCalculateResult.getUpdateTime());
        paasDataCacheVo.setValue(JsonUtils.toJson(valueMap));

        return paasDataCacheVo;
    }

    /**
     * 转换为kafka所需要的实体类
     *
     * @param groupCode
     * @param runningLog
     */
    public static PaasDataCacheVo getKafkaMessage(String groupCode, RcRunningLog runningLog, Boolean tenantUpper) {
        PaasDataCacheVo paasDataCacheVo = new PaasDataCacheVo();

        // key
        String tenant = BooleanUtil.isTrue(tenantUpper) ? groupCode : groupCode.toLowerCase();
        PaasDataCacheVo.PaasDataCacheKey paasDataCacheKey = new PaasDataCacheVo.PaasDataCacheKey();
        paasDataCacheKey.setId(runningLog.getId());
        paasDataCacheKey.setPaas_table("rc_running_log");
        paasDataCacheKey.setPaas_schema(tenant + "_meos_control");
        paasDataCacheVo.setKey(JsonUtils.toJson(paasDataCacheKey));

        // value
        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("id", runningLog.getId());
        valueMap.put("project_id", runningLog.getProjectId());
        valueMap.put("system_id", runningLog.getSystemId());
        valueMap.put("object_id", runningLog.getObjectId());
        valueMap.put("object_name", runningLog.getObjectName());
        valueMap.put("class_code", runningLog.getClassCode());
        valueMap.put("object_category", runningLog.getObjectCategory() == null ? null : runningLog.getObjectCategory().getCode());
        valueMap.put("first_level_category", runningLog.getFirstLevelCategory() == null ? null : runningLog.getFirstLevelCategory().getCode());
        valueMap.put("second_level_category", runningLog.getSecondLevelCategory() == null ? null : runningLog.getSecondLevelCategory().getCode());
        valueMap.put("third_level_category", runningLog.getThirdLevelCategory() == null ? null : runningLog.getThirdLevelCategory().getCode());
        valueMap.put("type", runningLog.getType());
        valueMap.put("key_word", runningLog.getKeyWord());
        valueMap.put("content", runningLog.getContent() == null ? null : JsonUtils.toJson(runningLog.getContent()));
        valueMap.put("detail", runningLog.getDetail() == null ? null : runningLog.getDetail().toJSONString());
        valueMap.put("effect_start_time", runningLog.getEffectStartTime());
        valueMap.put("effect_end_time", runningLog.getEffectEndTime());
        valueMap.put("reason", runningLog.getReason());
        valueMap.put("create_user", runningLog.getCreateUser());
        valueMap.put("create_time", runningLog.getCreateTime());
        valueMap.put("syn_time", runningLog.getSynTime());
        paasDataCacheVo.setValue(JsonUtils.toJson(valueMap));

        return paasDataCacheVo;
    }
    
    /**
     * 同步运行日志到kafka，进而写入数据到云边数据库
     *
     * @param runningLog
     */
    public void sendLogDataToKafka(RunningLog runningLog) {
        if (runningLog != null) {
            try {
                PaasDataCacheVo kafkaMessage = getKafkaMessage(ContextUtil.getGroupCode(), runningLog, tenantUpper);
                if (optRunningLogUploader != null && optRunningLogUploader.enabled()) {
                    optRunningLogUploader.offer(Lists.newArrayList(kafkaMessage));
                    return;
                }
                this.paasDatatransferClient.sendDataToKafka(Lists.newArrayList(kafkaMessage));
            } catch (Exception e) {
                log.error("运行日志云边同步异常，异常数据: {}", JsonUtils.toJson(runningLog), e);
            }
        }
    }

    public void sendLogDataToKafkaUpdate(RunningLog runningLog) {
        if (runningLog != null) {
            try {
                //先删除
                PaasDataCacheVo kafkaMessageDelete = getKafkaMessageDelete(ContextUtil.getGroupCode(), runningLog, tenantUpper);
                if (optRunningLogUploader != null && optRunningLogUploader.enabled()) {
                    // 先删后增两条按顺序入队，由单线程按序发送
                    optRunningLogUploader.offer(Lists.newArrayList(kafkaMessageDelete,
                            getKafkaMessage(ContextUtil.getGroupCode(), runningLog, tenantUpper)));
                    return;
                }
                this.paasDatatransferClient.sendDataToKafka(Lists.newArrayList(kafkaMessageDelete));
                //新增
                PaasDataCacheVo kafkaMessage = getKafkaMessage(ContextUtil.getGroupCode(), runningLog, tenantUpper);
                this.paasDatatransferClient.sendDataToKafka(Lists.newArrayList(kafkaMessage));
            } catch (Exception e) {
                log.error("运行日志云边同步异常，异常数据: {}", JsonUtils.toJson(runningLog), e);
            }
        }
    }

}
