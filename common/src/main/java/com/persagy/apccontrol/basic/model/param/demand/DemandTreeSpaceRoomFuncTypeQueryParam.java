package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 需求管理空间功能类型树
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/2/28 10:56
 * @since 7.250202.0
 */
@Data
@Schema(title = "需求管理空间功能类型树查询配置参数")
public class DemandTreeSpaceRoomFuncTypeQueryParam {

    @Schema(description = "需求模版类型")
    @NotNull(message = "{key_67d92d2b08624257a2b1303cf198a074}")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "是否过滤黑名单空间", defaultValue = "true")
    private Boolean filterBlacklistSpace = true;

}
