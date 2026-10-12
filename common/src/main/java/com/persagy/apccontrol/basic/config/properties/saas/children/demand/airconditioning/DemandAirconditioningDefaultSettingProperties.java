package com.persagy.apccontrol.basic.config.properties.saas.children.demand.airconditioning;

import com.persagy.apccontrol.basic.config.properties.BaseProperties;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 空调需求默认配置配置文件
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = DemandAirconditioningDefaultSettingProperties.KEY)
@RefreshScope
public class DemandAirconditioningDefaultSettingProperties extends BaseProperties {

    public static final String KEY = DemandAirconditioningProperties.KEY + ".default-setting";

    /**
     * 机房后勤通风
     */
    private DemandConfigAirconditioningValueObject valueObject = new DemandConfigAirconditioningValueObject();

    /**
     * 构造器
     *
     * @since 5.241002.0
     */
    public DemandAirconditioningDefaultSettingProperties() {
        this.valueObject.fillDefaultConfig(DemandInstanceTypeEnum.PROJECT);
    }
}
