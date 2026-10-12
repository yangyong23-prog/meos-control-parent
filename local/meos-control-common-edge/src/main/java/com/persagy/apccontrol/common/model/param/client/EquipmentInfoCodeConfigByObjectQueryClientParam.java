package com.persagy.apccontrol.common.model.param.client;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * 信息点-按对象查询配置参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class EquipmentInfoCodeConfigByObjectQueryClientParam {

    @Schema(description = "项目ID")
    @NotBlank(message = "{key_a042ffe47b0046cc955f7d00bd4bde24}")
    private String projectId;

    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;

    @Schema(description = "模版编码")
    @NotNull(message = "{key_46fb728a16c64e2b932840bd6b6c6c0d}")
    private InfoCodeTemplateTypeEnum templateCode;

    @Schema(description = "是否不过滤IOT规则", defaultValue = "false")
    private Boolean notFilterIotInfoCodeRule;

    @Schema(description = "对象ID")
    @NotEmpty(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private List<String> objectIds;

    @Schema(description = "需求实例类型枚举")
    private DemandInstanceTypeEnum instanceType;

    @Schema(description = "分组编码")
    private Set<String> groupCodes;

}
