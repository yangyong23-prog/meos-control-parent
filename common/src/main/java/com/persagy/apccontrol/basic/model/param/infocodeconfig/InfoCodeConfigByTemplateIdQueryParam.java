package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdCriteriaSignEnum;
import com.persagy.apccontrol.basic.model.page.query.PageQuery;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectOrderParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 信息点-按模版ID查询配置参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@Accessors(chain = true)
public class InfoCodeConfigByTemplateIdQueryParam implements BaseCheckParamProvider {

    @Schema(description = "分页参数, 为空时不分页")
    private PageQuery page;

    @Schema(description = "模版类型ID")
    @NotNull(message = "{key_02761da21e69441c86b58c7195dc5b83}")
    private Long templateTypeId;

    @Schema(description = "是否查询IOT信息点规则", defaultValue = "true")
    private Boolean queryIotInfoCodeRule;

    @Schema(description = "是否不过滤IOT规则", defaultValue = "false")
    private Boolean notFilterIotInfoCodeRule;

    @Schema(description = "是否过滤结果对象都不存在规则的IOT信息点, 当 [notFilterIotInfoCodeRule] 不为true时生效", defaultValue = "false")
    private Boolean filterBothNotExistIotInfoCodeRule;

    @Schema(description = "是否过滤管控对象", defaultValue = "false")
    private Boolean filterControlObject;

    @Schema(description = "过滤对象ID, 为空时查询所有")
    private Set<String> filterObjectIds;

    @Schema(description = "过滤所在系统ID, 为空时不过滤")
    private Set<String> filterInSystemIds;

    @Schema(description = "对象名称, 为空时查询所有")
    private String localName;

    @Schema(description = "动态信息点过滤, 为空时查询所有")
    private List<DynamicInfoCodeFilter> dynamicInfoCodeFilters;

    @Valid
    @Schema(description = "静态信息点过滤, 为空时查询所有")
    private List<InfoCodeStaticFilter> staticInfoCodeFilters;

    @Schema(description = "标识和静态信息点过滤, 为空时查询所有")
    private Map<DtpRwdCriteriaSignEnum, List<InfoCodeStaticFilter>> signAndStaticInfoCodeFilters;

    @Schema(description = "服务空间ID, 为空时查询所有")
    private Set<String> serviceSpaceIds;

    @Schema(description = "所在空间ID, 为空时查询所有")
    private Set<String> inSpaceIds;

    @Valid
    @Schema(description = "排序参数, 为空时不排序")
    private List<DtpRwdObjectOrderParam> orders;

    // ---------------------------- 分组筛选 ----------------------------

    @Schema(description = "分组编码")
    private Set<String> groupCodes;

    @Schema(description = "分组过滤参数")
    private List<InfoCodeConfigQueryConfigValueObject> filterGroups;

    @Schema(description = "分组排除参数")
    private List<InfoCodeConfigQueryConfigValueObject> excludeGroups;

    // ---------------------------- 分组筛选 ----------------------------

}
