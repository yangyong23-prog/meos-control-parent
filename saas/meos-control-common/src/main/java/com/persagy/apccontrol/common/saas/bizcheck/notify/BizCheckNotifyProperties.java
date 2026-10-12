package com.persagy.apccontrol.common.saas.bizcheck.notify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 业务可用性检查异常通知配置
 *
 * @since 7.260601.0
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "meos.bizcheck.notify")
public class BizCheckNotifyProperties {

    /**
     * 是否启用通知
     */
    private boolean enabled = true;

    /**
     * 组织编码
     */
    private String group = "YuShu";

    /**
     * 岗位名称列表
     */
    private List<String> positionNames = Collections.singletonList("业务顾问");

    /**
     * 截断长度
     */
    private int truncateLength = 2000;
}
