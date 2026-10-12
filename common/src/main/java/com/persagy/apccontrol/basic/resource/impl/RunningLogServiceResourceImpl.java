package com.persagy.apccontrol.basic.resource.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.mapper.RunningLogMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.param.log.NowPageBroadcastQueryParam;
import com.persagy.apccontrol.basic.resource.RunningLogServiceResource;
import com.persagy.apccontrol.basic.resource.param.runningLog.NowRunningLogQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.QueryLogCategoryParam;
import com.persagy.apccontrol.basic.service.cache.RedissonDistributedLockerService;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yuyao
 * @description 针对表【running_log(感知运行日志表)】的数据库操作Resource实现
 * @createDate 2025-04-22 16:47:52
 */
@Service
public class RunningLogServiceResourceImpl extends ServiceImpl<RunningLogMapper, RunningLog>
        implements RunningLogServiceResource {
    
    @Resource
    private NacosSwitchConfig nacosSwitchConfig;
    @Resource
    RedissonDistributedLockerService redissonDistributedLockerService;
    
    @Override
    public Long getIdByUnknown(String traceId, String objectId, String infoCode) {
        //0 和 -1 都属于状态未知
        List<RunningLogSourceStateEnum> stList = new ArrayList<>();
        stList.add(RunningLogSourceStateEnum.UNKNOWN);
        stList.add(RunningLogSourceStateEnum.UNFINISHED);
        
        RunningLog runLog = this.lambdaQuery()
                                .select(RunningLog::getId)
                                .eq(RunningLog::getTraceId, traceId)
                                .eq(RunningLog::getObjectId, objectId)
                                .eq(RunningLog::getInfoCode, infoCode)
                                .in(RunningLog::getState, stList)
                                .last(CommonConst.LIMIT_ONE).one();
        if (runLog == null) {
            return null;
        }
        return runLog.getId();
    }
    
    @Override
    public boolean existsByTraceId(String traceId, String objectId, String infoCode) {
        RunningLog runLog = this.lambdaQuery()
                                .select(RunningLog::getId)
                                .eq(RunningLog::getTraceId, traceId)
                                .eq(RunningLog::getObjectId, objectId)
                                .eq(RunningLog::getInfoCode, infoCode)
                                .last(CommonConst.LIMIT_ONE).one();
        return runLog != null;
    }
    
    @Override
    public boolean updateStateById(Long id, RunningLogSourceStateEnum st) {
        //0 和 -1 都属于非终态，只允许非终态 → 终态单向更新，已终态行不覆盖（防状态回退）
        List<RunningLogSourceStateEnum> nonTerminalStates = new ArrayList<>();
        nonTerminalStates.add(RunningLogSourceStateEnum.UNKNOWN);
        nonTerminalStates.add(RunningLogSourceStateEnum.UNFINISHED);
        return this.lambdaUpdate().eq(RunningLog::getId, id)
            .in(RunningLog::getState, nonTerminalStates)
            .set(RunningLog::getState, st)
            .update();
    }
    
    @Override
    public List<RunningLog> listPageBroadcastMsg(NowPageBroadcastQueryParam param) {
        return this.lambdaQuery().eq(RunningLog::getProjectId, ContextUtil.getProjectId())
                   .eq(param.getFirstLevelCategory() != null, RunningLog::getFirstLevelCategory,
                           param.getFirstLevelCategory())
                   .eq(StringUtils.isNotEmpty(param.getSecondLevelCategory()), RunningLog::getSecondLevelCategory,
                           param.getSecondLevelCategory())
                   .orderByDesc(RunningLog::getDataTime)
                   .last(CommonConst.LIMIT_FIVE)
                   .list();
    }
    
    @Override
    public IPage<RunningLog> queryRunningLogListByPage(@NotNull NowRunningLogQueryParam queryParam) {
        IPage<RunningLog> page = new Page<>(queryParam.getPageNumber(), queryParam.getPageSize(), true);
        
        LambdaQueryWrapper<RunningLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RunningLog::getProjectId, ContextUtil.getProjectId())
               .ge(RunningLog::getDataTime, queryParam.getDataStartTime())
               .le(queryParam.getDataEndTime() != null, RunningLog::getDataTime, queryParam.getDataEndTime())
               .eq(queryParam.getType() != null, RunningLog::getType, queryParam.getType())
               .eq(queryParam.getFirstLevelCategory() != null, RunningLog::getFirstLevelCategory,
                       queryParam.getFirstLevelCategory())
               .eq(StringUtils.isNotEmpty(queryParam.getSecondLevelCategory()), RunningLog::getSecondLevelCategory,
                       queryParam.getSecondLevelCategory())
               .in(CollectionUtils.isNotEmpty(queryParam.getObjectIds()), RunningLog::getObjectId,
                       queryParam.getObjectIds())
               .eq(StringUtils.isNotEmpty(queryParam.getSystemId()), RunningLog::getSystemId, queryParam.getSystemId())
               .le(queryParam.getEffectStartTime() != null, RunningLog::getEffectStartTime,
                       queryParam.getEffectStartTime())
               .ge(queryParam.getEffectStartTime() != null, RunningLog::getEffectEndTime,
                       queryParam.getEffectStartTime())
               .le(queryParam.getEffectEndTime() != null, RunningLog::getEffectStartTime, queryParam.getEffectEndTime())
               .ge(queryParam.getEffectEndTime() != null, RunningLog::getEffectEndTime, queryParam.getEffectEndTime())
               .le(queryParam.getWorkDate() != null, RunningLog::getCreateTime, queryParam.getWorkDate())
               .and(StringUtils.isNotEmpty(queryParam.getKeyWord()), query -> {
                   query.like(RunningLog::getKeyWord, queryParam.getKeyWord()).or();
                   query.like(RunningLog::getObjectName, queryParam.getKeyWord()).or();
                   query.like(RunningLog::getCreateUser, queryParam.getKeyWord());
               });
        if (queryParam.isDesc()) {
            wrapper.orderByDesc(RunningLog::getDataTime);
        } else {
            wrapper.orderByAsc(RunningLog::getDataTime);
        }
        
        IPage<RunningLog> logList = this.baseMapper.selectPage(page, wrapper);
        if ((long) (queryParam.getPageNumber() - 1) * queryParam.getPageSize() > logList.getTotal()) {
            logList.setRecords(new ArrayList<>());
        }
        return logList;
    }
    
    @Override
    public void createPartitionAndDeleteHistoryPartition(String projectId) {
        
        //删除一个月前的数据 当前时间月份-1
        LocalDateTime localDate = LocalDateTime.now().minusMonths(nacosSwitchConfig.getRunningDeleteMonthsAgoData());
        LambdaQueryChainWrapper<RunningLog> chainWrapper = this.lambdaQuery()
                                                               .eq(RunningLog::getProjectId, projectId)
                                                               .lt(RunningLog::getDataTime, localDate);
        this.remove(chainWrapper.getWrapper());
        
        //删除分区 分区在原分区基础上加 1 个分区 分区表是集团纬度 按照集团编码加锁防止并发执行
        String lockKey = RedisKey.LOCK_KEY_TASK + ContextUtil.getGroupCode() +
                "createPartitionAndDeleteHistoryPartition";
        boolean lock = redissonDistributedLockerService.tryLock(lockKey, TimeUnit.SECONDS, 1, 60);
        //没取到锁跳过
        if (!lock) {
            return;
        }
        try {
            this.baseMapper.createPartitionAndDeleteHistoryPartition(nacosSwitchConfig.getRunningDeleteMonthsAgoData() + 1);
        } finally {
            redissonDistributedLockerService.unlock(lockKey);
        }
    }
    
    @Override
    public Map<RunningLogTypeEnum, Long> countWithTypeByToday() {
        List<Map<String, Object>> maps = this.baseMapper.countWithTypeByToday(ContextUtil.getProjectId());
        return maps.stream().collect(Collectors.toMap(map -> RunningLogTypeEnum.resolve((Integer) map.get("type")),
                map -> (Long) map.get("count")));
    }
    
    @Override
    public List<RunningLog> getSecondLevelRunningLogCategory(@NonNull QueryLogCategoryParam param) {
        LambdaQueryWrapper<RunningLog> wrapper = new LambdaQueryWrapper<>();
        wrapper
                //.select(RunningLog::getSecondLevelCategory, RunningLog::getSystemName, RunningLog::getSystemId)
                .eq(RunningLog::getProjectId, ContextUtil.getProjectId())
                .ge(RunningLog::getDataTime, param.getDataStartTime())
                .le(param.getDataEndTime() != null, RunningLog::getDataTime, param.getDataEndTime())
                .eq(param.getType() != null, RunningLog::getType, param.getType())
                .eq(param.getFirstLevelCategory() != null, RunningLog::getFirstLevelCategory,
                        param.getFirstLevelCategory())
                .groupBy(RunningLog::getSecondLevelCategory);
        return this.baseMapper.selectList(wrapper);
    }
}




