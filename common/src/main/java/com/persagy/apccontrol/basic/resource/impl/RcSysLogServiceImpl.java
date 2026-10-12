package com.persagy.apccontrol.basic.resource.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.mapper.RcSysLogMapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.SystemLogDTO;
import com.persagy.apccontrol.basic.model.entity.RcSysLog;
import com.persagy.apccontrol.basic.model.enumtype.system.LogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.SaasOrgPersonService;
import com.persagy.apccontrol.basic.resource.RcSysLogService;
import com.persagy.framework.database.service.SuperServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yuyao
 * @description 针对表【rc_sys_log(系统日志表)】的数据库操作Service实现
 * @createDate 2023-03-15 20:30:22
 */
@Slf4j
@Service
public class RcSysLogServiceImpl extends SuperServiceImpl<RcSysLogMapper, RcSysLog> implements RcSysLogService {

    @Autowired
    private SaasOrgPersonService saasOrgPersonService;

    @Override
    public List<RcSysLog> queryRunningLogList(SystemLogDTO logDTO, boolean casPersonName) {
        LambdaQueryChainWrapper<RcSysLog> queryChainWrapper = this.lambdaQuery().eq(StrUtil.isNotBlank(logDTO.getGroupCode()), RcSysLog::getGroupCode, logDTO.getGroupCode()).eq(RcSysLog::getProjectId, logDTO.getProjectId())
                .eq(StrUtil.isNotBlank(logDTO.getSystemId()), RcSysLog::getSystemId, logDTO.getSystemId()).eq(StrUtil.isNotBlank(logDTO.getClassCode()), RcSysLog::getClassCode, logDTO.getClassCode())
                .eq(StrUtil.isNotBlank(logDTO.getObjectId()), RcSysLog::getObjectId, logDTO.getObjectId()).eq(logDTO.getLogSource() != null, RcSysLog::getLogSource, logDTO.getLogSource())
                .eq(logDTO.getLogType() != null, RcSysLog::getLogType, logDTO.getLogType()).ne(BooleanUtil.isTrue(logDTO.getExcludePageBroadcast()), RcSysLog::getLogType, LogTypeEnum.PAGE_BROADCASR)
                .ge(logDTO.getStartTime() != null, RcSysLog::getCreateTime, logDTO.getStartTime()).le(logDTO.getEndTime() != null, RcSysLog::getCreateTime, logDTO.getEndTime())
                .orderByDesc(RcSysLog::getCreateTime);
        List<RcSysLog> rcSysLogs = queryChainWrapper.list();
        // 填充人员名称
        if (casPersonName && CollectionUtil.isNotEmpty(rcSysLogs)) {
            Set<String> personUserIds = rcSysLogs.stream().filter(item -> StrUtil.isNotBlank(item.getCreateUser())).map(RcSysLog::getCreateUser).collect(Collectors.toSet());
            Map<String, String> personNameMap = this.saasOrgPersonService.queryPersonNameByUserIds(logDTO.getGroupCode(), logDTO.getProjectId(), personUserIds);
            for (RcSysLog rcSysLog : rcSysLogs) {
                if (personNameMap.get(rcSysLog.getCreateUser()) != null) {
                    rcSysLog.setCreateUser(personNameMap.get(rcSysLog.getCreateUser()));
                }
            }
        }
        return rcSysLogs;
    }

    @Override
    public RcSysLog queryLog(String projectId, LogTypeEnum type, OperateTypeEnum operateType, String memo) {
        return lambdaQuery()
                .eq(RcSysLog::getProjectId, projectId)
                .eq(RcSysLog::getLogType, type)
                .eq(RcSysLog::getOperateType, operateType)
                .eq(StringUtils.isNotBlank(memo), RcSysLog::getResultInfo, memo)
                .orderByDesc(RcSysLog::getCreateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
    }

    @Async
    @Override
    public void batchSyn(List<RcSysLog> rcSysLogs) {
        Date now = new Date();
        for (RcSysLog rcSysLog : rcSysLogs) {
            rcSysLog.setSynTime(now);
        }
        List<List<RcSysLog>> partition = Lists.partition(rcSysLogs, 1000);
        for (List<RcSysLog> sysLogs : partition) {
            this.saveOrUpdateBatch(sysLogs);
        }
    }

}




