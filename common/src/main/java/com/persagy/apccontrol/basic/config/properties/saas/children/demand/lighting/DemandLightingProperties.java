package com.persagy.apccontrol.basic.config.properties.saas.children.demand.lighting;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.properties.BaseProperties;
import com.persagy.apccontrol.basic.config.properties.saas.children.DemandProperties;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 需求管理-照明需求配置文件
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = DemandLightingProperties.KEY)
@RefreshScope
public class DemandLightingProperties extends BaseProperties {

    public static final String KEY = DemandProperties.KEY + ".lighting";

    /**
     * 服务区域类型
     */
    private Map<DemandTemplateTypeEnum, Set<String>> serviceAreaType = new LinkedHashMap<>(16);

    /**
     * 构造器
     *
     * @since 24Q1 T3S1-5.17.0
     */
    public DemandLightingProperties() {
        serviceAreaType.put(DemandTemplateTypeEnum.PUBLIC_AREA_LIGHTING, Sets.newHashSet("1"));
        serviceAreaType.put(DemandTemplateTypeEnum.PARKING_LOT_LIGHTING, Sets.newHashSet("2"));
        serviceAreaType.put(DemandTemplateTypeEnum.BUILDING_LIGHTING, Sets.newHashSet("3"));
        serviceAreaType.put(DemandTemplateTypeEnum.LANDSCAPE_LIGHTING, Sets.newHashSet("4"));
        serviceAreaType.put(DemandTemplateTypeEnum.SIGN_LIGHTING, Sets.newHashSet("5"));
    }
}
