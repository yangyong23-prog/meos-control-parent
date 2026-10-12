package com.persagy.apccontrol.basic.resource.demand.param.lighting;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandConfigLightingDataDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 需求配置-照明需求-保存参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingSaveDaoParam {

    /**
     * 需求实例类型
     */
    @NotNull(message = "{key_bef82d36a29642ba8174459b23bd0ccd}")
    private DemandInstanceTypeEnum demandInstanceType;

    /**
     * 需求实例ID
     */
    @NotBlank(message = "{key_b6a999341d384e8d8e3c837821c85508}")
    private String demandInstanceId;

    /**
     * 配置数据
     */
    @NotNull(message = "{key_198da2c118da4426a857e4a52e9957cb}")
    private DemandConfigLightingDataDTO configData;

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

    /**
     * 状态
     */
    @NotNull(message = "{key_31b1c9f4e49941dd933a4f97efcd1a5a}")
    private StateTypeEnum st = StateTypeEnum.VALID;

}
