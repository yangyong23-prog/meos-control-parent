package com.persagy.apccontrol.basic.model.vo.demand.scene;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandTimeValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 需求配置-照明场景表
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "场景VO")
public class DemandSceneVO implements Serializable {

    private static final long serialVersionUID = 2291508432143794911L;

    @Schema(description = "id")
    private Long id;

    @Schema(description = "需求类型")
    private DemandTypeEnum demandType;

    @Schema(description = "需求模版类型")
    private DemandTemplateTypeEnum demandTemplateType;

    @Schema(description = "模版标签")
    private String demandTemplateTag;

    @Schema(description = "开始时间")
    private DemandTimeValueObject beginTime;

    @Schema(description = "结束时间")
    private DemandTimeValueObject endTime;

    @Schema(description = "场景名称")
    private String sceneName;

    @Schema(description = "排序")
    private Integer sequence;

    @Schema(description = "关联需求模版ID")
    private Long relTemplateId;
}