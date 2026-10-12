package com.persagy.apccontrol.basic.structure.demand.model.dto;

import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 需求实例自定义标签DTO
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
@Accessors(chain = true)
public class DemandInstanceCustomizedTagDTO {

    /**
     * 需求实例
     */
    private DemandInstanceValueObject instance;

    /**
     * 自定义标签
     */
    private Map<String, Object> customizedTag;

    /**
     * 服务冷热源系统ID
     */
    public static final String SERVICE_COLD_HEAT_SYSTEM_IDS = "serviceColdHeatSystemIds";

    /**
     * 是否有 [tdb] 信息点规则
     */
    public static final String EXIST_TAB_IOT_INFO_CODE_RULE = "existTabIotInfoCodeRule";

}
