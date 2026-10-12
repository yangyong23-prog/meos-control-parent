package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.vo.BaseEnumVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 需求管理-照明需求-空间类型配置-功能类型配置VO
 *
 * @author zhangqiankun
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@Schema(title = "需求管理-照明需求-空间类型配置-功能类型配置DTO")
public class DemandLightingSpaceFuncTypeSettingVO extends DemandConfigLightingGlobalConfigVO{

    @Schema(description = "当前配置ID值")
    private String configId;

    @Schema(description = "空间功能类型")
    private List<BaseEnumVO> spaceFuncTypes;

}
