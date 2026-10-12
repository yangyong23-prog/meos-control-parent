package com.persagy.apccontrol.basic.service.runninglog;


import com.persagy.apccontrol.basic.model.dto.runnningLog.CustomizedKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogCategoryDto;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.param.log.NowPageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.log.RunningLogVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;
import com.persagy.apccontrol.basic.resource.param.runningLog.NowRunningLogQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.QueryLogCategoryParam;
import com.persagy.framework.common.entity.R;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

/**
 * 运行日志处理类
 */
public interface NowRunningLogService {
    
    
    /**
     * 页面播报
     *
     * @param param
     */
    List<RunningLogVO> listPageBroadcastMsg(@NotNull NowPageBroadcastQueryParam param);
    
    
    /**
     * 分页查询运行日志列表
     *
     * @param param
     * @return
     */
    R<Collection<RunningLogVO>> queryRunningLogListByPage(@NotNull NowRunningLogQueryParam param);
    
    /**
     * 日志下载
     *
     * @param param
     * @return
     */
    String downloadLog(@NonNull NowRunningLogQueryParam param);
    
    /**
     * 通过一级分类查询数据库中有的二级日志分类
     *
     * @param param
     * @return
     */
    List<RunningLogCategoryDto> getSecondLevelRunningLogCategory(@NonNull QueryLogCategoryParam param);
    
    
    /**
     * 值班首页的日志统计
     */
    public TodayLogSummaryVo todayLogSummary();
    
    
    /**
     * 运行日志映射
     *
     * @param log
     * @param customizedKnowledgeInfoCode
     * @return
     */
    public RunningLogVO mappingRunningLogVO(RunningLog log, CustomizedKnowledgeInfoCodeDTO customizedKnowledgeInfoCode);
    
    public List<RunningLogVO> mappingRunningLogVO(List<RunningLog> log);
    
    
    
}
