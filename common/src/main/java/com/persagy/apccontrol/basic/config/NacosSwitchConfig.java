package com.persagy.apccontrol.basic.config;

import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * nacos 配置的开关 支持热刷新
 */
@Component
@Data
@RefreshScope
public class NacosSwitchConfig {

    //是否启用缓存的开关 默认开启
    @Value("${switch.asyncRefreshTemplate:true}")
    private Boolean asyncRefreshTemplateSwitch;

    /**
     * 默认为开启状态，代表有yiheng-server-edge
     */
    @Value("${switch.yihengServer:true}")
    private Boolean yihengServerSwitch;


    @Value("${update.system.graph:true}")
    private Boolean updateSystemGraph;

    /**
     * iot 查询时间偏移量 默认 10 分钟
     * 单位分钟
     */
    @Value("${meos.cfg.iot.timeOffset:10}")
    private int IOT_TIME_OFFSET;

    /**
     * 默认为开启状态，统计SSE/MQTT 的推数耗时和总数
     */
    @Value("${switch.dailyStringCounter:true}")
    private Boolean dailyStringCounter;

    /**
     * 统计SSE/MQTT 的推数耗时 超过几秒记录 默认1 秒
     */
    @Value("${switch.dailyStringCounterTimeout:1}")
    private long dailyStringCounterTimeout;

    /**
     * 末端是否继续加载空间数量开关配置 默认 500
     */
    @Value("${switch.airDialogContinueLoad.spaceNumber: 500}")
    private int airDialogContinueLoadSpaceNumber;
    
    
    /**
     * 开启中持续时间 默认  40 分钟
     */
    @Value("${switch.open.timeOfDuration: 40}")
    private int openTimeOfDuration;
    
    
    /**
     * 效率的精度配置 默认 1d
     */
    @Value("${switch.bdtp.timeType.efficiency: 1d}")
    private String bdtpTimeTypeEfficiency;
    
    
    /**
     * runningLog 保留几个月的数据 默认 1 个月
     */
    @Value("${meos.cfg.running.deleteMonthsAgoData:12}")
    private int runningDeleteMonthsAgoData;

    /**
     * 运行分析 保留多少天的数据 默认 1 个月
     */
    @Value("${meos.cfg.analysis.deleteDaysAgoData:180}")
    private int analysisDeleteDaysAgoData;

    /**
     * 传递时间 获取IOT 偏移后的时间
     *
     * @param time
     * @return
     */
    public LocalDateTime getIotTimeOffset(LocalDateTime time) {
        return time.minusMinutes(IOT_TIME_OFFSET);
    }


    /**
     * 当配置刷新时会调用此方法
     * 配置方式
     * # 开启 Spring Boot Actuator
     * management.endpoints.enabled-by-default: true
     * # 暴露 refresh 端点
     * management.endpoints.web.exposure.include: refresh
     * switch.dailyStringCounterTimeout: 0
     */
    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onConfigRefresh() {
        DailyStringCounter.SWITCH = asyncRefreshTemplateSwitch;
        DailyStringCounter.timeout = dailyStringCounterTimeout;
    }

}
