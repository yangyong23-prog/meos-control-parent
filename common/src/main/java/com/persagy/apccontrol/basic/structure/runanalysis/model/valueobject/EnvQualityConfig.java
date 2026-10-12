package com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

/**
 * 环境质量-配置
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EnvQualityConfig extends RunAnalysisReportConfig {

    /**
     * 空间ID
     */
    private Set<String> spaces;

}
