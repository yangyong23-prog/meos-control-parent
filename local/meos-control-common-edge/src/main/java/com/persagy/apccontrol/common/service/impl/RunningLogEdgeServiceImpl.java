package com.persagy.apccontrol.common.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogCategoryEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.model.param.log.PageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogInsertParam;
import com.persagy.apccontrol.basic.service.runninglog.RunningLogService;
import com.persagy.apccontrol.basic.utils.SnowflakeIdWorker;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.common.server.PaasDatatransferServer;
import com.persagy.apccontrol.common.service.PageBroadcastMsgService;
import com.persagy.apccontrol.common.service.RunningLogEdgeService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class RunningLogEdgeServiceImpl implements RunningLogEdgeService {

    @Autowired
    private PaasDatatransferServer paasDatatransferServer;
    @Autowired
    private PageBroadcastMsgService pageBroadcastMsgService;


    // 每秒允许 1 个请求
    private final RateLimiter rateLimiter = RateLimiter.create(1.0);

    //需要记录的日志类型
    private final List<RunningLogCategoryEnum> runningLogCategoryList = Arrays.asList(
            RunningLogCategoryEnum.STRATEGY_ADJUST,
            RunningLogCategoryEnum.HOT_STRATEGY_ADJUST,
            RunningLogCategoryEnum.SYSTEM_PARAM_MODIFY,
            RunningLogCategoryEnum.HOT_SYSTEM_PARAM_MODIFY,
            RunningLogCategoryEnum.EQUIPMENT_MENUAL_OPERATION,
            RunningLogCategoryEnum.LIGHT_MONITORING_MANUAL_CONTROL,
            RunningLogCategoryEnum.HOT_EQUIPMENT_MENUAL_OPERATION,
            RunningLogCategoryEnum.KITCHEN_RANGE_HOOD_MANUAL_CONTROL,
            RunningLogCategoryEnum.VENTILATION_MANUAL_CONTROL,
            RunningLogCategoryEnum.SECURITY_EQUIPMENT_MANUAL_FORCE_OPERATE,
            RunningLogCategoryEnum.LIGHT_MONITORING_INTERVENTION
    );
    @Autowired
    private RedisCacheTemplate redisCacheTemplate;
    @Autowired
    private MidPermissionClientService midPermissionClientService;

    public TodayLogSummaryVo todayLogSummary() {
        TodayLogSummaryVo todayLogSummaryVo = new TodayLogSummaryVo(0L, 0L, null);
        String statisticsKey = RedisKey.MEOS_RC_RUNNING_LOG_BY_DAY_AUTO_SCALE();
        Map<String, Integer> map = redisCacheTemplate.object().get(statisticsKey);
        if (CollectionUtil.isNotEmpty(map)) {
            // 日志统计汇总  总数只记录手动和自动的
            Integer auto = map.get(RunningLogTypeEnum.AUTO.getValue().toString());
            Integer manual = map.get(RunningLogTypeEnum.MANUAL.getValue().toString());
            auto = auto == null ? 0 : auto;
            manual = manual == null ? 0 : manual;
            todayLogSummaryVo.setAutoCount((long) auto);
            todayLogSummaryVo.setTotal((long) (auto + manual));
            // 查询项目今日最新三条自动日志
            String key = RedisKey.MEOS_RC_RUNNING_LOG_AUTO();
            List<RcRunningLog> rcRunningLogs = redisCacheTemplate.getLatestDate(key, 3);
            if (CollectionUtil.isEmpty(rcRunningLogs)) {
                return todayLogSummaryVo;
            }
            //按照时间排序 只取前三条
            rcRunningLogs.sort(Comparator.comparing(RcRunningLog::getCreateTime).reversed());
            
            todayLogSummaryVo.setLatestContent(RunningLogService.getRunningLogHomePageVO(rcRunningLogs));
        }
        return todayLogSummaryVo;
    }

    public List<FeedbackInfoMsgMqttVO.PageBroadcastContent> listPageBroadcastMsg(PageBroadcastQueryParam param) {
        //查询最新的五条页面播报数据
        String key = RedisKey.MEOS_RC_RUNNING_LOG(param.getSystemId());
        List<RcRunningLog> rcRunningLogs = redisCacheTemplate.getLatestDate(key, 4);

        if (CollectionUtil.isEmpty(rcRunningLogs)) {
            return new ArrayList<>();
        }

        List<FeedbackInfoMsgMqttVO.PageBroadcastContent> contents = new ArrayList<>();
        for (RcRunningLog runningLog : rcRunningLogs) {
            FeedbackInfoMsgMqttVO.PageBroadcastContent broadcastContent = new FeedbackInfoMsgMqttVO.PageBroadcastContent();
            broadcastContent.setContent(runningLog.getPageBroadcastContent(runningLog.getCreateUser()));
            broadcastContent.setOperateType(RunningLogTypeEnum.AUTO.value().equals(runningLog.getType()) ? OperateTypeEnum.PAGE_BROADCAST_AUTO.value()
                    : RunningLogTypeEnum.MANUAL.value().equals(runningLog.getType()) ? OperateTypeEnum.PAGE_BROADCAST_MANUAL.value()
                    : null);
            contents.add(broadcastContent);
        }
        return contents;
    }

    @Override
    public RcRunningLog getNoEndRunningLogByAlarm(String objectId, RunningLogCategoryEnum firstLevelCategory,
                                                  RunningLogCategoryEnum secondLevelCategory, RunningLogCategoryEnum thirdLevelCategory) {
        String key = getLogAlarmKey(objectId, firstLevelCategory, secondLevelCategory, thirdLevelCategory);
        return redisCacheTemplate.object().get(key);
    }

    /**
     * 获取报警的 key
     *
     * @param objectId
     * @param firstLevelCategory
     * @param secondLevelCategory
     * @param thirdLevelCategory
     * @return
     */
    private String getLogAlarmKey(String objectId, RunningLogCategoryEnum firstLevelCategory,
                                  RunningLogCategoryEnum secondLevelCategory, RunningLogCategoryEnum thirdLevelCategory) {
        String key = RedisKey.MEOS_RC_RUNNING_LOG_ALARM;
        String objKey = objectId + firstLevelCategory.getValue() + secondLevelCategory.getValue() + thirdLevelCategory.getValue();
        return key + CommonConst.COLON + objKey;
    }


    /**
     * 本地端保存日志调用此方法，实时同步日志到云端
     * @param groupCode
     * @param param
     */
    @Override
    public void saveLog(String groupCode, RunningLogInsertParam param) {
        if (param.getThirdLevelCategory() == null || param.getObjectCategory() == null) {
            log.warn("三级菜单或对象类型为空，不进行日志记录：{}", JsonUtils.toJson(param));
            return;
        }
        try {
            RcRunningLog runningLog = getLog(param, true);
            saveLog(runningLog);
        } catch (Exception e) {
            log.error("边缘端运行日志保存失败！error:{}", e);
            log.error("边缘端运行日志保存失败！param:{}", param);
        }
    }

    @Override
    public synchronized void saveLog(RcRunningLog runningLog) {
        if (runningLog == null || runningLog.getFirstLevelCategory() == null) {
            log.info("运行日志为空，不进行日志记录：{}", JsonUtils.toJson(runningLog));
            return;
        }
        save(runningLog);
        syncLog(runningLog);
    }
    
    private void save(RcRunningLog runningLog) {

        if (runningLog != null) {
            if (StringUtils.isBlank(runningLog.getId())) {
                runningLog.setId(SnowflakeIdWorker.getWorkerId());
            }
            //1.运行播报相关日志记录
            pageBroadcastSaveLog(runningLog);

            //2.值班首页自控日志
            //只记录人工和自动指令
            if (Objects.equals(RunningLogTypeEnum.AUTO.value(), runningLog.getType()) ||
                    Objects.equals(RunningLogTypeEnum.MANUAL.value(), runningLog.getType())) {
                homePageSaveLog(runningLog);
            }
            
        }

    }

    /**
     * 值班首页日志保存
     *
     * @param runningLog
     */

    private void homePageSaveLog(RcRunningLog runningLog) {
        //单独保存自控的日志 用于值班首页
        if (Objects.equals(RunningLogTypeEnum.AUTO.value(), runningLog.getType())) {
            String key = RedisKey.MEOS_RC_RUNNING_LOG_AUTO();
            //保存数据 只保留 10 条
            redisCacheTemplate.saveLatestDate(key, runningLog, 9, DateUtils.getTodayEndTime());
        }
        //计算当日手自动总条数
        String scaleKey = RedisKey.MEOS_RC_RUNNING_LOG_BY_DAY_AUTO_SCALE();
        Map<String, Integer> map = redisCacheTemplate.object().get(scaleKey);
        if (MapUtils.isEmpty(map)) {
            map = new HashMap<>();
        }
        map.compute(runningLog.getType().toString(), (k, num) -> num == null ? 1 : num + 1);
        log.info("运行日志-日志记录：{}", JsonUtils.toJson(map));
        redisCacheTemplate.object().set(scaleKey, map, DateUtils.getTimeUntilEndOfDay());
    }

    /**
     * 播报日志保存
     *
     * @param runningLog
     */
    private void pageBroadcastSaveLog(RcRunningLog runningLog) {
        RunningLogCategoryEnum secondLevelCategory = runningLog.getSecondLevelCategory();
        //只保存 “日志分类 = 控制需求调整、系统控制参数修改、设备手动/强制操作、需求干预”的日志
        if (runningLogCategoryList.contains(secondLevelCategory)) {
            //保存数据到redis
            String key = RedisKey.MEOS_RC_RUNNING_LOG(runningLog.getSystemId());
            //保存数据 只保留 10 条
            redisCacheTemplate.saveLatestDate(key, runningLog, 9, DateUtils.getTodayEndTime());
        }
    }


    /**
     * 同步和播报
     *
     * @param runningLog
     */
    private void syncLog(RcRunningLog runningLog) {
        if (runningLog != null) {
            //同步到云端
            paasDatatransferServer.sendLogDataToKafka(ContextUtil.getGroupCode(), runningLog);

            if (rateLimiter.tryAcquire()) {
                // 推送页面播报数据给前端
                pageBroadcastMsgService.pushMessageToWeb(runningLog.getProjectId(), runningLog.getSystemId(), runningLog.getFirstLevelCategory().getCode());
            }
        }
    }

    public RcRunningLog getLog(@NotNull RunningLogInsertParam param, boolean updateUser) {
        if (updateUser && StrUtil.isNotBlank(param.getCreateUser()) && !CommonConst.SYSTEM_NAME.equals(param.getCreateUser())) {
            Map<String, String> userNameByUserIds = this.midPermissionClientService.getUserNameByUserIds(ContextUtil.getGroupCode(), Sets.newHashSet(param.getCreateUser()));
            if (userNameByUserIds.containsKey(param.getCreateUser())) {
                param.setCreateUser(userNameByUserIds.get(param.getCreateUser()));
            }
        }

        RcRunningLog runningLog = new RcRunningLog();
        BeanUtils.copyProperties(param, runningLog);
        runningLog.setType(param.getType().value());
        runningLog.setSecondLevelCategory(RunningLogCategoryEnum.getEnumByCode(runningLog.getThirdLevelCategory().getParentCode()));
        runningLog.setFirstLevelCategory(RunningLogCategoryEnum.getEnumByCode(runningLog.getSecondLevelCategory().getParentCode()));
        runningLog.setDetail(JSONArray.parseArray(JSON.toJSONString(param.getDetail())));
        runningLog.setKeyWord(param.getCreateUser() + " " + param.getObjectName() + " " + (com.alibaba.nacos.common.utils.StringUtils.isEmpty(param.getKeyWord()) ? "" : param.getKeyWord()));
        runningLog.setCreateTime(param.getCreateTime() == null ? LocalDateTime.now() : param.getCreateTime());

        return runningLog;
    }

}

