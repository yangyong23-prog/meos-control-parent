package com.persagy.apccontrol.basic.model.param.infocodeconfig;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeConfigQueryConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.Range;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.persagy.apccontrol.basic.structure.infocodeconfig.strategy.BaseInfoCodeAnalyzeStrategy.TIME_RANGE;

/**
 * 信息点-按对象查询配置参数
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeConfigByObjectQueryParam {

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

    @Schema(description = "是否过滤管控对象", defaultValue = "false")
    private Boolean filterControlObject;

    @Schema(description = "对象ID")
    @NotEmpty(message = "{key_db46294d82c44d7aa47bd996f27c6154}")
    private Set<String> objectIds;

    // ---------------------------- 分组筛选 ----------------------------

    @Schema(description = "分组编码")
    private Set<String> groupCodes;

    @Schema(description = "分组过滤参数")
    private List<InfoCodeConfigQueryConfigValueObject> filterGroups;

    @Schema(description = "分组排除参数")
    private List<InfoCodeConfigQueryConfigValueObject> excludeGroups;

    // ---------------------------- 分组筛选 ----------------------------

    @Deprecated
    @Schema(description = "老版本过滤SSE发布信息点", defaultValue = "false")
    private Boolean filterSsePublishInfoCodeOld;

    @Schema(description = "过滤SSE发布信息点", defaultValue = "false")
    private Boolean filterSsePublishInfoCode;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "查询字段, 为空时查询标准字段")
    private Set<String> withColumns;

    @Schema(description = "占位符参数, key为占位符名称, value为替换值, 会覆盖自动解析的数据源")
    private @Nullable Map<String, Object> placeholders;

    /**
     * 将参数中的时间存入条件中，查询iot持久化信息时使用
     *
     * @return 条件Map集合
     */
    public void paramTimeToConditions(Map<String, Object> paramAdditionalDataValueMap) {
        if (Objects.nonNull(this.startTime) && Objects.nonNull(this.endTime)) {
            paramAdditionalDataValueMap.put(TIME_RANGE, Range.between(this.startTime, this.endTime));
        }
    }
}
