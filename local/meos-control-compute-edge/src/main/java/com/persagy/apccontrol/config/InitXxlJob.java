package com.persagy.apccontrol.config;

import cn.hutool.core.collection.ListUtil;
import com.persagy.apccontrol.basic.config.xxljob.XxlJobConfig;
import com.persagy.apccontrol.basic.config.xxljob.XxlJobService;
import com.persagy.apccontrol.basic.config.xxljob.XxlJobVo;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.apccontrol.common.config.SystemInitService;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
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
public class InitXxlJob implements SystemInitService {

    @Resource
    private XxlJobService xxlJobService;

    @Resource
    private XxlJobConfig xxlJobConfig;

    @Bean
    @ConditionalOnProperty(prefix = "xxl.job", value = "enabled", matchIfMissing = true, havingValue = "true")
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
    @Override
    public void init() {
        log.info("初始 XxlJob自动配置任务 开始........");

        //重新创建任务
        List<XxlJobVo> list = ListUtil.toList();
        list.add(new XxlJobVo(LocaleMessageUtil.getMessage("key_7050bf83c2e24116a819c1bc5795282e"), "0 5 0 * * ?", "historyStorageHandler"
                , ExecutorBlockStrategyEnum.DISCARD_LATER, ""));
        // rcRunStatus补偿任务，每30秒执行一次
        list.add(new XxlJobVo(LocaleMessageUtil.getMessage("key_rc_runstatus_compensation_task"), "0/30 * * * * ?", "rcRunStatusCompensationHandler"
                , ExecutorBlockStrategyEnum.DISCARD_LATER, ""));

        //清除错误任务
        xxlJobService.removeJob(list);

        for (XxlJobVo vo : list) {
            xxlJobService.addJob(vo);
        }
        log.info("初始化 XxlJob自动配置任务 结束........");


    }
    
    @Override
    public String getName() {
        return LocaleMessageUtil.getMessage("key_2f149612faff4f088ef040845e8187d3");
    }
}
