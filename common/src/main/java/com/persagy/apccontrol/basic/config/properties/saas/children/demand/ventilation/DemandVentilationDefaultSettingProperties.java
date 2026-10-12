package com.persagy.apccontrol.basic.config.properties.saas.children.demand.ventilation;

import com.persagy.apccontrol.basic.config.properties.BaseProperties;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationDomeValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationLogisticsValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationParkingValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.ventilation.DemandConfigVentilationToiletValueObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 通风需求默认配置配置文件
 *
 * @author JiYinchuan
 * @since 5.241002.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = DemandVentilationDefaultSettingProperties.KEY)
@RefreshScope
public class DemandVentilationDefaultSettingProperties extends BaseProperties {

    public static final String KEY = DemandVentilationProperties.KEY + ".default-setting";

    /**
     * 机房后勤通风
     */
    private DemandConfigVentilationLogisticsValueObject logistics = new DemandConfigVentilationLogisticsValueObject();

    /**
     * 停车场通风
     */
    private DemandConfigVentilationParkingValueObject parking = new DemandConfigVentilationParkingValueObject();

    /**
     * 卫生间通风
     */
    private DemandConfigVentilationToiletValueObject toilet = new DemandConfigVentilationToiletValueObject();

    /**
     * 穹顶通风
     */
    private DemandConfigVentilationDomeValueObject dome = new DemandConfigVentilationDomeValueObject();

    /**
     * 构造器
     *
     * @since 5.241002.0
     */
    public DemandVentilationDefaultSettingProperties() {
        this.logistics.fillDefaultConfig(DemandInstanceTypeEnum.PROJECT);
        this.parking.fillDefaultConfig(DemandInstanceTypeEnum.PROJECT);
        this.toilet.fillDefaultConfig(DemandInstanceTypeEnum.PROJECT);
        this.dome.fillDefaultConfig(DemandInstanceTypeEnum.PROJECT);
    }
}
