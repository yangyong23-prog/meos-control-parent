package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 设备信息点-按对象查询配置参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class BuildingAndSpaceByProjectIdQueryParam {

    @Schema(description = "运行监测二级类型枚举")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "模版编码")
    @NotNull(message = "{key_46fb728a16c64e2b932840bd6b6c6c0d}")
    private InfoCodeTemplateTypeEnum templateCode;

    @Schema(description = "项目id")
    private String projectId;

}
