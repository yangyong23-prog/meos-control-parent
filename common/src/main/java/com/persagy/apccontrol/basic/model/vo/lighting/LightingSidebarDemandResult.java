package com.persagy.apccontrol.basic.model.vo.lighting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSceneDTO;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLightingResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 需求信息
 *
 * @author yang
 * @version 1.0.0
 * @date 2024/4/29 15:33
 * @since 5.240402.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(title = "需求信息")
public class LightingSidebarDemandResult extends LightingSceneDTO {

    /**
     * 关闭回路数
     */
    private Long offLoopNumber;

    /**
     * 新开启回路数
     */
    private Long openLoopNumber;

    /**
     * 当前场景的所有数据
     */
    @JsonIgnore
    private List<DemandConfigLightingResult> demandConfigLightingResultList;

}
