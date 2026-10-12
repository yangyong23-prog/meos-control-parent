package com.persagy.apccontrol.basic.config.properties.edge.children;

import com.persagy.apccontrol.basic.config.properties.edge.EdgeProperties;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.freezepumpgroup.DtpRwdFreezePumpGroupInfoCodeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * AI配置文件
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ConfigurationProperties(prefix = AiProperties.KEY)
@RefreshScope
public class AiProperties {

    public static final String KEY = EdgeProperties.KEY + ".ai";

    /**
     * 构造器
     *
     * @since v5.7.0
     */
    public AiProperties() {
        this.defaultFreezePumpGroupInfoCodeValue.put(
                DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_TEMP_SET.value(), BigDecimal.valueOf(5));
        this.defaultFreezePumpGroupInfoCodeValue.put(
                DtpRwdFreezePumpGroupInfoCodeEnum.BRANCH_DETA_PRESS_SET.value(), BigDecimal.valueOf(5));
    }

    /**
     * 默认信息点值
     */
    private Map<String, BigDecimal> defaultFreezePumpGroupInfoCodeValue = new HashMap<>(16);

}
