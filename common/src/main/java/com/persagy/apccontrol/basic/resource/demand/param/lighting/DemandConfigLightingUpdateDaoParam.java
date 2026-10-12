package com.persagy.apccontrol.basic.resource.demand.param.lighting;

import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingFuncTypeSettingDTO;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingSpaceFuncTypeSettingDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 需求配置-照明需求-保存参数
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Accessors(chain = true)
public class DemandConfigLightingUpdateDaoParam {

    /**
     * ID
     */
    @NotNull(message = "{key_3bbfbcb78c1744b5b14eac0557d4c399}")
    private Long id;

    /**
     * 全局配置, 非 {@code null} 时则更新
     */
    private List<DemandLightingFuncTypeSettingDTO> globalSettings;

    /**
     * 空间类型配置, 非 {@code null} 时则更新
     */
    private List<DemandLightingSpaceFuncTypeSettingDTO> spaceSettings;

    /**
     * 状态
     */
    private StateTypeEnum st;

}
