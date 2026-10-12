package com.persagy.apccontrol.basic.resource.demand.param.lighting;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * 需求配置-照明需求-清空配置参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingClearSettingDaoParam {

    /**
     * 需求实例ID
     */
    private String demandInstanceId;

    /**
     * 需求实例ID
     */
    private Collection<String> demandInstanceIds;

    /**
     * 关联需求模版ID
     */
    @NotNull(message = "{key_8c0f8d5f23034cb09375574633685fb9}")
    private Long relDemandTemplateId;

    /**
     * 关联照明场景ID
     */
    @NotNull(message = "{key_cde999ede28348c5abd66e7e2b812f2a}")
    private Long relDemandLightingSceneId;

}
