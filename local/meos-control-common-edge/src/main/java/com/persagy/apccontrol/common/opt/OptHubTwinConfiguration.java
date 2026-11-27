package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.common.outside.feign.OptMeosHubTwinClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.time.ZoneId;

/**
 * 孪生数据源切换（switch.opt.twinSource）的 Bean。
 *
 * <p>{@code opt.hub-twin.zone}（默认 Asia/Shanghai）：meos-hub 时间为 UTC，转成该时区的 yyyyMMddHHmmss；
 * {@code opt.hub-twin.data-indate}（默认 0）：返回记录的时间常数（分钟），0 为不过期。均为启动时读取。
 * Feign 客户端懒调用：开关为 twin 时从不访问 meos-hub。
 */
@Configuration
public class OptHubTwinConfiguration {

    @Bean
    public OptHubTwinSettings optHubTwinSettings(@Value("${opt.hub-twin.zone:Asia/Shanghai}") String zone,
                                                 @Value("${opt.hub-twin.data-indate:0}") int dataIndate) {
        return new OptHubTwinSettings(ZoneId.of(zone), dataIndate);
    }

    @Bean
    public OptHubTwinReader optHubTwinReader(@Lazy OptMeosHubTwinClient client, ProjectCfg projectCfg,
                                             OptHubTwinSettings settings) {
        String projectId = projectCfg.getProjectId() == null ? null : projectCfg.getProjectId().trim();
        return new OptHubTwinReader(client::getPointTwin, projectId, settings.getZone(), settings.getDataIndate());
    }
}
