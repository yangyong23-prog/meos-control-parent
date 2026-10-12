package com.persagy.apccontrol.task;

import com.persagy.apccontrol.task.cacheobjectmodel.CacheObjectModelInitTaskHandler;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author yuyao
 * @date 2022/4/13 16:26
 * xxjob 统一消息配置
 */
@Component
@Slf4j
public class JobTask extends BaseXXJob {
    
    
    /**
     * 空间分析定时任务
     */
    @Resource(name = "spaceAnalyseTaskHandlerV2")
    private TecXxJobHandler spaceAnalyseTaskHandler;

    /**
     * 业务可用性检查
     */
    @Resource(name = "bizCheckHandler")
    private TecXxJobHandler bizCheckHandler;

    /**
     * 删除历史运行日志
     */
    @Resource(name = "delRcRunningLogHandler")
    private TecXxJobHandler delRcRunningLogHandler;


    /**
     * 运行分析报告生成
     */
    @Resource(name = "runAnalysisReportHandler")
    private TecXxJobHandler runAnalysisReportHandler;

    /**
     * 照明配置计算定时任务
     */
    @Resource(name = "demandConfigLightingComputeTaskHandler")
    private DemandConfigLightingComputeTaskHandler demandConfigLightingComputeTaskHandler;

    /**
     * 需求配置结果初始化任务。
     */
    @Resource(name = "demandConfigResultInitTaskHandler")
    private @NonNull DemandConfigResultInitTaskHandler demandConfigResultInitTaskHandler;

    /**
     * 缓存对象模型手动初始化任务。
     *
     * @since v7.260701.0
     */
    @Resource(name = CacheObjectModelInitTaskHandler.BEAN_NAME)
    private @NonNull CacheObjectModelInitTaskHandler cacheObjectModelInitTaskHandler;

    
    /**
     * 空间分析定时任务
     * @author wangxiaofeng
     * @date 2024/11/29 14:33
     * @return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     */
    @XxlJob("spaceAnalyseTaskHandler")
    public ReturnT<String> spaceAnalyseTaskHandler() {
        return processHandle(spaceAnalyseTaskHandler);
    }

    /**
     * 业务可用性检查
     */
    @XxlJob("bizCheckHandler")
    public ReturnT<String> bizCheckHandler() {
        return processHandle(bizCheckHandler);
    }

    /**
     * 删除历史运行日志
     * 保留一年的数据
     */
    @XxlJob("delRcRunningLogHandler")
    public ReturnT<String> delRcRunningLogHandler() {
        return processHandle(delRcRunningLogHandler);
    }


    /**
     * 运行分析报告生成
     */
    @XxlJob("runAnalysisReportHandler")
    public ReturnT<String> runAnalysisReportHandler() {
        return processHandle(runAnalysisReportHandler);
    }
    
    /**
     * 照明配置计算定时任务
     */
    @XxlJob("demandConfigLightingComputeTaskHandler")
    public ReturnT<String> demandConfigLightingComputeTaskHandler() {
        return processHandle(demandConfigLightingComputeTaskHandler);
    }

    /**
     * 需求配置结果初始化任务。
     *
     * @return 执行结果
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @XxlJob("demandConfigResultInitTaskHandler")
    public @NonNull ReturnT<String> demandConfigResultInitTaskHandler() {
        return this.demandConfigResultInitTaskHandler.execute();
    }

    /**
     * 缓存对象模型手动初始化任务。
     *
     * @return 执行结果
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @XxlJob(CacheObjectModelInitTaskHandler.BEAN_NAME)
    public @NonNull ReturnT<String> cacheObjectModelInitTaskHandler() {
        return this.cacheObjectModelInitTaskHandler.execute();
    }

}


