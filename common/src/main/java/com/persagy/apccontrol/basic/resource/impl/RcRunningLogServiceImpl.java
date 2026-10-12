package com.persagy.apccontrol.basic.resource.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.persagy.apccontrol.basic.mapper.RcRunningLogMapper;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.resource.RcRunningLogService;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogPageQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogQueryParam;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.database.service.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RcRunningLogServiceImpl extends SuperServiceImpl<RcRunningLogMapper, RcRunningLog> implements RcRunningLogService {

    @Override
    public List<RcRunningLog> queryRunningLogList(RunningLogQueryParam queryParam) {
        LambdaQueryWrapper<RcRunningLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RcRunningLog::getProjectId, queryParam.getProjectId())
                .eq(StringUtils.isNotEmpty(queryParam.getSystemId()), RcRunningLog::getSystemId, queryParam.getSystemId())
                .eq(queryParam.getType() != null, RcRunningLog::getType, queryParam.getType() == null ? null : queryParam.getType().value())
                .in(CollectionUtils.isNotEmpty(queryParam.getClassCodes()), RcRunningLog::getClassCode, queryParam.getClassCodes())
                .in(CollectionUtils.isNotEmpty(queryParam.getObjectIds()), RcRunningLog::getObjectId, queryParam.getObjectIds())
                .in(CollectionUtils.isNotEmpty(queryParam.getFirstLevelCategory()), RcRunningLog::getFirstLevelCategory, queryParam.getFirstLevelCategory())
                .in(CollectionUtils.isNotEmpty(queryParam.getSecondLevelCategory()), RcRunningLog::getSecondLevelCategory, queryParam.getSecondLevelCategory())
                .in(CollectionUtils.isNotEmpty(queryParam.getThirdLevelCategory()), RcRunningLog::getThirdLevelCategory, queryParam.getThirdLevelCategory())
                .gt(queryParam.getWorkDate() != null,RcRunningLog::getCreateTime, queryParam.getWorkDate().atStartOfDay())
                .le(queryParam.getWorkDate() != null, RcRunningLog::getCreateTime, queryParam.getWorkDate().plusDays(1).atStartOfDay());
        if ("DESC".equalsIgnoreCase(queryParam.getOrderBy())) {
            wrapper.orderByDesc(RcRunningLog::getCreateTime);
        } else {
            wrapper.orderByAsc(RcRunningLog::getCreateTime);
        }
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public IPage<RcRunningLog> queryRunningLogList(RunningLogPageQueryParam queryParam) {
        IPage<RcRunningLog> page = new Page<>(queryParam.getPageNumber(), queryParam.getPageSize(), true);
        LambdaQueryWrapper<RcRunningLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RcRunningLog::getProjectId, queryParam.getProjectId())
                .eq(StringUtils.isNotEmpty(queryParam.getSystemId()), RcRunningLog::getSystemId, queryParam.getSystemId())
                .eq(queryParam.getType() != null, RcRunningLog::getType, queryParam.getType() == null ? null : queryParam.getType().value())
                .in(CollectionUtils.isNotEmpty(queryParam.getObjectIds()), RcRunningLog::getObjectId, queryParam.getObjectIds())
                .in(CollectionUtils.isNotEmpty(queryParam.getFirstLevelCategory()), RcRunningLog::getFirstLevelCategory, queryParam.getFirstLevelCategory())
                .in(CollectionUtils.isNotEmpty(queryParam.getSecondLevelCategory()), RcRunningLog::getSecondLevelCategory, queryParam.getSecondLevelCategory())
                .in(CollectionUtils.isNotEmpty(queryParam.getThirdLevelCategory()), RcRunningLog::getThirdLevelCategory, queryParam.getThirdLevelCategory())
                .like(StringUtils.isNotEmpty(queryParam.getKeyWord()), RcRunningLog::getKeyWord, queryParam.getKeyWord())
                .ge(queryParam.getStartTime() != null,RcRunningLog::getCreateTime, queryParam.getStartTime())
                .le(queryParam.getEndTime() != null, RcRunningLog::getCreateTime, queryParam.getEndTime())
                .le(queryParam.getEffectTime() != null, RcRunningLog::getEffectStartTime, queryParam.getEffectTime())
                .ge(queryParam.getEffectTime() != null, RcRunningLog::getEffectEndTime, queryParam.getEffectTime())
                .ge(queryParam.getEffectTimeStart() != null, RcRunningLog::getEffectStartTime, queryParam.getEffectTimeStart())
                .le(queryParam.getEffectTimeEnd() != null, RcRunningLog::getEffectStartTime, queryParam.getEffectTimeEnd())
                .le(queryParam.getCreateTime() != null, RcRunningLog::getCreateTime, queryParam.getCreateTime())
                .orderBy(true, !StringUtils.isEmpty(queryParam.getOrderBy()) && queryParam.getOrderBy().equalsIgnoreCase("asc"), RcRunningLog::getCreateTime);
        IPage<RcRunningLog> logList = this.baseMapper.selectPage(page, wrapper);
        if ((long) (queryParam.getPageNumber() - 1) * queryParam.getPageSize() > logList.getTotal()) {
            logList.setRecords(new ArrayList<>());
        }
        return logList;
    }


    @Override
    public List<RcRunningLog> queryRcRunningLogHistory(LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        IPage<RcRunningLog> Ipage = new Page<>(page, pageSize, true);
        LambdaQueryWrapper<RcRunningLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RcRunningLog::getProjectId, ContextUtil.getProjectId())
                .ge(startTime != null, RcRunningLog::getCreateTime, startTime)
                .le(endTime != null, RcRunningLog::getCreateTime, endTime)
                .orderBy(true, true, RcRunningLog::getCreateTime);
        IPage<RcRunningLog> logList = this.baseMapper.selectPage(Ipage, wrapper);
        return logList.getRecords();
    }

    @Override
    public Map<RunningLogTypeEnum, Long> countWithType(String projectId) {
        List<Map<String, Object>> maps = this.baseMapper.countWithType(projectId);
        Map<RunningLogTypeEnum, Long> collect = maps.stream()
                .collect(Collectors.toMap(
                        map -> RunningLogTypeEnum.resolve((Integer) map.get("type")),
                        map -> (Long) map.get("count")
                ));
        return collect;
    }

}
