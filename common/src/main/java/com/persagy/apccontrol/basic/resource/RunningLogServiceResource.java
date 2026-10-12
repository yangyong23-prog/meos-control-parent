package com.persagy.apccontrol.basic.resource;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceStateEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.param.log.NowPageBroadcastQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.NowRunningLogQueryParam;
import com.persagy.apccontrol.basic.resource.param.runningLog.QueryLogCategoryParam;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author yuyao
 * @description 针对表【running_log(感知运行日志表)】的数据库操作Service
 * @createDate 2025-04-22 16:47:52
 */
public interface RunningLogServiceResource extends IService<RunningLog> {
    
    /**
     * 查询日志 ID
     * 只查询状态未知的数据
     * @param traceId
     * @param objectId
     * @param infoCode
     * @return
     */
    public Long getIdByUnknown(@NotNull String traceId, @NotNull String objectId, @NotNull String infoCode);
    
    
    /**
     * 判断同一控制指令是否已建过运行日志（不区分状态）。
     *
     * <p>孪生自动日志建档去重用：一个控制指令(traceId)只应产生一条运行日志，设备陆续推送的
     * 多帧报文(开始执行/执行中，result 均为未完成)不应重复建档。与 {@link #getIdByUnknown} 不同，
     * 本方法不限定 state，避免终态已回写后迟到的中间帧再次建档。</p>
     *
     * @param traceId  追踪 ID
     * @param objectId 对象 ID
     * @param infoCode 信息点编码
     * @return true-已存在同指令日志
     */
    boolean existsByTraceId(@NotNull String traceId, @NotNull String objectId, @NotNull String infoCode);
    
    
    /**
     * 通过 ID 更新状态（状态单调守卫）。
     *
     * <p>仅当当前 state 为非终态(UNKNOWN / UNFINISHED)时才更新，保证 -1/0 → 终态 单向流转；
     * 已终态行不会被覆盖，避免终态被迟到帧改回"未完成"。</p>
     *
     * @param id 日志 ID
     * @param st 目标状态
     * @return true-实际更新了一行；false-该行已终态或不存在，未更新
     */
    boolean updateStateById(@NotNull Long id, @NotNull RunningLogSourceStateEnum st);
    
    
    /**
     * 获取日志播报的数据
     *
     * @param param
     * @return
     */
    public List<RunningLog> listPageBroadcastMsg(@NotNull NowPageBroadcastQueryParam param);
    
    
    /**
     * 查询日志列表
     *
     * @param queryParam
     * @return
     */
    IPage<RunningLog> queryRunningLogListByPage(@NonNull NowRunningLogQueryParam queryParam);
    
    
    /**
     * 定时创建分区和删除历史分区
     */
    public void createPartitionAndDeleteHistoryPartition(String projectId);
    
    
    /**
     * 获取项目下运行日志类型统计 当日的
     *
     * @return
     */
    public Map<RunningLogTypeEnum, Long> countWithTypeByToday();
    
    
    /**
     * 查询二级分组的数据
     *
     * @param param
     * @return
     */
    public List<RunningLog> getSecondLevelRunningLogCategory(@NonNull QueryLogCategoryParam param);
    
}
