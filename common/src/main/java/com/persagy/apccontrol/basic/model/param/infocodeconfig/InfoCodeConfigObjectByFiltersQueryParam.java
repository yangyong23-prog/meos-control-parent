package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectOrderParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
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
public class InfoCodeConfigObjectByFiltersQueryParam {

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

    @Schema(description = "是否查询IOT信息点规则", defaultValue = "true")
    private Boolean queryIotInfoCodeRule;

    @Schema(description = "是否不过滤IOT规则", defaultValue = "false")
    private Boolean notFilterIotInfoCodeRule;

    @Schema(description = "是否过滤结果对象都不存在规则的IOT信息点, 当 [notFilterIotInfoCodeRule] 不为true时生效", defaultValue = "false")
    private Boolean filterBothNotExistIotInfoCodeRule;

    @Schema(description = "服务空间ID, 为空时查询所有")
    private Set<String> serviceSpaceIds;

    @Schema(description = "所在空间ID, 为空时查询所有")
    private Set<String> inSpaceIds;

    @Schema(description = "对象名称, 为空时查询所有")
    private String localName;

    @Schema(description = "对象类型, 为空时查询所有")
    private DtpRwdObjectTypeEnum objectType;

    // ---------------------------- 分组筛选 ----------------------------

    @Schema(description = "分组编码")
    private Set<String> groupCodes;

    @Schema(description = "分组过滤参数")
    private List<InfoCodeConfigQueryConfigValueObject> filterGroups;

    @Schema(description = "分组排除参数")
    private List<InfoCodeConfigQueryConfigValueObject> excludeGroups;

    // ---------------------------- 分组筛选 ----------------------------

    @Schema(description = "对象类")
    private Set<String> classCodes;

    @Schema(description = "是否过滤管控对象")
    private Boolean filterControlObject;

    @Valid
    @Schema(description = "排序参数, 为空时不排序")
    private List<DtpRwdObjectOrderParam> orders;

}
