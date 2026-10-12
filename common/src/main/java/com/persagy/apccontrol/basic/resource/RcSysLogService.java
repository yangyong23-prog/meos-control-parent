package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.model.dto.SystemLogDTO;
import com.persagy.apccontrol.basic.model.entity.RcSysLog;
import com.persagy.apccontrol.basic.model.enumtype.system.LogTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;
import com.persagy.framework.database.service.SuperService;

import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_sys_log(系统日志表)】的数据库操作Service
 * @createDate 2023-03-15 20:30:22
 */
public interface RcSysLogService extends SuperService<RcSysLog> {

    /**
     * 查询运行日志
     *
     * @param logDTO
     * @return
     */
    List<RcSysLog> queryRunningLogList(SystemLogDTO logDTO, boolean casPersonName);

    /**
     * 数据同步
     *
     * @param rcSysLogs
     */
    public void batchSyn(List<RcSysLog> rcSysLogs);

    /**
     * 获取最新一条日志记录
     *
     * @param projectId   项目id
     * @param type        业务类型
     * @param operateType 操作类型
     * @param memo        结果信息
     */
    RcSysLog queryLog(String projectId, LogTypeEnum type, OperateTypeEnum operateType, String memo);

}
