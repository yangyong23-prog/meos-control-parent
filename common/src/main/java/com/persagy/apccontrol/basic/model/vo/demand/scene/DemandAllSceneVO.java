package com.persagy.apccontrol.basic.model.vo.demand.scene;

import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 需求管理-所有场景VO
 *
 * @author wxf
 */
@Data
@Schema(description = "需求管理-所有场景VO")
public class DemandAllSceneVO {

    @Schema(description = "模版")
    private DemandTemplateVO template;

    @Schema(description = "场景")
    private List<DemandSceneVO> scenes;
}
