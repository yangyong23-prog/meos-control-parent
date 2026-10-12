package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 信息点-模版类型查询参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeConfigTemplateTypeQueryParam {

    @Schema(description = "运行监测类型")
    @NotNull(message = "{key_a2aa6d0437464447b95289dc0cf17ac9}")
    private RunningMonitorTypeEnum runningMonitorType;

    @Nullable
    @Schema(description = "运行监测二级类型")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    @Schema(description = "模版编码")
    @NotNull(message = "{key_46fb728a16c64e2b932840bd6b6c6c0d}")
    @JsonAlias("templateCode")
    private InfoCodeTemplateTypeEnum templateType;

    @Schema(description = "模版ID")
    private Set<Long> templateIds;

    @Schema(description = "对象类型")
    private DtpRwdObjectTypeEnum objectType;

    @Schema(description = "对象类型列表")
    private Set<DtpRwdObjectTypeEnum> objectTypes;

    @Schema(description = "排除classCode")
    private Set<String> excludeClassCodes;

    @Schema(description = "过滤模版类型ID", defaultValue = "false")
    private Set<Long> filterTemplateTypeIds;

    @Schema(description = "过滤对象ID")
    private Set<String> filterObjectIds;

    @Schema(description = "过滤所在系统ID, 为空时不过滤")
    private Set<String> filterInSystemIds;

    @Schema(description = "是否过滤管控对象", defaultValue = "false")
    private Boolean filterControlObject;

    @Schema(description = "是否不查询对象数量", defaultValue = "false")
    private Boolean notQueryObjectCount;

}
