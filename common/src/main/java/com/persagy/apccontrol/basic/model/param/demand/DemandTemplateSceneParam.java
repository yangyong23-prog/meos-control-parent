package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 需求管理基查询参数
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "需求管理基查询参数")
public class DemandTemplateSceneParam extends BaseDemandTemplateParam implements BaseCheckParamProvider {

    @Schema(description = "场景ID, 存在时必填")
    private Long sceneId;

}
