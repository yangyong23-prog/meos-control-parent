package com.persagy.apccontrol.basic.model.vo.demand.lighting;

import com.persagy.apccontrol.basic.model.entity.demand.DemandConfig;
import com.persagy.apccontrol.basic.model.entity.demand.DemandConfigGroup;
import com.persagy.apccontrol.basic.model.entity.demand.lighting.DemandConfigLighting;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 照明需求配置迁移信息VO
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
public class DemandLightingConfigMigrateMessageVO {

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "配置组")
    private List<DemandConfigGroup> configGroups;

    @Schema(description = "空间功能类型配置组")
    private List<DemandConfigGroup> roomFuncTypeConfigGroups;

    @Schema(description = "详细信息")
    private List<Detail> details;

    @Data
    public static class Detail {

        @Schema(description = "旧配置")
        private DemandConfigLighting oldConfig;

        @Schema(description = "新配置")
        private DemandConfig newConfig;

    }
}
