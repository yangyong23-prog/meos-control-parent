package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;

/**
 * 原始信息点配置查询参数
 *
 * @author JiYinchuan
 * @since 7.260401.0
 */
@Data
public class InfoCodeConfigsOriginQueryParam {

    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;

    @Nullable
    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "模版编码列表")
    private Set<InfoCodeTemplateTypeEnum> templateTypes;

    @Schema(description = "设备类编码列表")
    private Set<String> classCodes;

    @Nullable
    @Schema(description = "信息点类型列表")
    private Collection<InfoCodeTypeEnum> infoCodeTypes;

    @Nullable
    @Schema(description = "分组过滤参数")
    private Collection<InfoCodeConfigQueryConfigValueObject> filterGroups;

}
