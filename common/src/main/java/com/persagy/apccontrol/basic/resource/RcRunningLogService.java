package com.persagy.apccontrol.basic.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogPageQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.RunningLogQueryParam;
import com.persagy.framework.database.service.SuperService;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RcRunningLogService extends SuperService<RcRunningLog> {

    /**
     * 查询日志列表--不分页
     *
     * @param queryParam
     * @return
     */
    List<RcRunningLog> queryRunningLogList(RunningLogQueryParam queryParam);

    /**
     * 查询日志列表
     * @param queryParam
     * @return
     */
    IPage<RcRunningLog> queryRunningLogList(@NonNull RunningLogPageQueryParam queryParam);



    List<RcRunningLog> queryRcRunningLogHistory(LocalDateTime startTime, LocalDateTime endTime, Integer page , Integer pageSize);


    /**
     * 统计不同类型日志数量
     * @param projectId
     * @return
     */
    Map<RunningLogTypeEnum, Long> countWithType(String projectId);

}

