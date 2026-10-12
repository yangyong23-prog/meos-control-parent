package com.persagy.apccontrol.config;

import cn.hutool.core.collection.ListUtil;
import com.persagy.apccontrol.basic.config.xxljob.XxlJobConfig;
import com.persagy.apccontrol.basic.config.xxljob.XxlJobService;
import com.persagy.apccontrol.basic.config.xxljob.XxlJobVo;
import com.persagy.apccontrol.task.BizCheckHandler;
import com.persagy.apccontrol.task.cacheobjectmodel.CacheObjectModelInitTaskHandler;
import com.persagy.apccontrol.task.DelRcRunningLog;
import com.persagy.apccontrol.task.DemandConfigResultInitTaskHandler;
import com.persagy.apccontrol.task.RunAnalysisReportHandler;
import com.persagy.apccontrol.task.SpaceAnalyseTaskHandlerV2;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author : yuyao
 * @Date : 2022/9/11 16:17
 * @Description ：初始化定时任务
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "xxl.job", value = "enabled", matchIfMissing = true, havingValue = "true")
public class InitXxlJob implements CommandLineRunner {

    @Resource
    XxlJobService xxlJobService;

    @Resource
    private XxlJobConfig xxlJobConfig;
    
    @Override
    public void run(String... args) {
        init();
    }

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(xxlJobConfig.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(xxlJobConfig.getAppname());
        xxlJobSpringExecutor.setPort(xxlJobConfig.getPort());
        xxlJobSpringExecutor.setLogPath(xxlJobConfig.getLogPath());
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobConfig.getLogRetentionDays());
        xxlJobSpringExecutor.setAccessToken(xxlJobConfig.getAccessToken());
        return xxlJobSpringExecutor;
    }

    /**
     * XxlJob自动配置任务
     *
     * @author yuyao
     * @date 2022/4/25 17:29
     * @Return void
     */
    public void init() {
        log.info("初始 XxlJob自动配置任务 开始........");
        List<XxlJobVo> list = ListUtil.toList();
        //国际化策略 新增定时任务都配置 每小时一次 具体执行的时间配置在 executorParam 里面
        list.add(new XxlJobVo(SpaceAnalyseTaskHandlerV2.LOG_TAG, "0 0 * * * ?", "spaceAnalyseTaskHandler",
                ExecutorBlockStrategyEnum.DISCARD_LATER, "* * 2 * * ?"));
        list.add(new XxlJobVo(DelRcRunningLog.LOG_TAG, "0 0 * * * ?", "delRcRunningLogHandler",
                ExecutorBlockStrategyEnum.DISCARD_LATER, "* * 2 * * ?"));
        list.add(new XxlJobVo(RunAnalysisReportHandler.LOG_TAG, "0 * * * * ?", "runAnalysisReportHandler",
                ExecutorBlockStrategyEnum.DISCARD_LATER,"* * * * * ?"));
        list.add(new XxlJobVo(BizCheckHandler.LOG_TAG, "0 * * * * ?", "bizCheckHandler",
                ExecutorBlockStrategyEnum.DISCARD_LATER,"* * * * * ?"));
        XxlJobVo demandConfigResultInitTask = new XxlJobVo(DemandConfigResultInitTaskHandler.LOG_TAG,
                "", "demandConfigResultInitTaskHandler", ExecutorBlockStrategyEnum.DISCARD_LATER, "");
        list.add(demandConfigResultInitTask);
        // 缓存对象模型初始化由人工触发，只注册 XXL-JOB 入口，不配置自动调度表达式。
        XxlJobVo cacheObjectModelInitTask = new XxlJobVo(CacheObjectModelInitTaskHandler.LOG_TAG,
                "", CacheObjectModelInitTaskHandler.BEAN_NAME, ExecutorBlockStrategyEnum.DISCARD_LATER, "");
        list.add(cacheObjectModelInitTask);

        //清除错误任务
        xxlJobService.removeJob(list);

        for (XxlJobVo vo : list) {
            // 两个数据初始化任务都必须由运维手动执行，不能被启动流程自动调度。
            if (StringUtils.equals(
                    DemandConfigResultInitTaskHandler.LOG_TAG, vo.getJobDesc())
                    || StringUtils.equals(
                            CacheObjectModelInitTaskHandler.LOG_TAG, vo.getJobDesc())) {
                this.xxlJobService.addNoneScheduleJob(vo);
                continue;
            }
            this.xxlJobService.addJob(vo);
        }
        log.info("初始化 XxlJob自动配置任务 结束........");
    }

}
