package com.persagy.apccontrol.basic.model.param.common;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 查询服务空间的传感器信息（设备服务空间关系）
 *
 * @author 薛涛
 * @since 5.240602.0
 */
@Data
public class EquipmentServiceSpaceQueryParam {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "设备ID")
    @NotEmpty(message = "{key_cb0bd77252b64497b89675d0ee1d97ed}")
    private String equipmentId;

    @Schema(description = "运行监测二级类型枚举")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "模版编码")
    @NotNull(message = "{key_46fb728a16c64e2b932840bd6b6c6c0d}")
    private InfoCodeTemplateTypeEnum templateCode;


}
