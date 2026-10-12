package com.persagy.apccontrol.basic.model.vo.demand.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 需求管理-日期配置VO
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Data
@Schema(title = "需求管理-日期配置VO")
public class DemandTemplateDateSettingVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "是否自定义配置")
    private Boolean isCustomizedSetting;

    @Schema(description = "关联模版")
    private DemandTemplateVO relTemplate;

}
