package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 空调需求配置--空间工况--实例对象
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningWorkInstanceValueObject implements ValueObject {

    @Schema(description = "系统配置")
    private DemandConfigAirconditioningWorkInstanceConfigValueObject systemConfig;

    @Schema(description = "系统下泵组对象数据")
    private List<DemandConfigAirconditioningWorkInstanceConfigValueObject> pumpConfig;

}
