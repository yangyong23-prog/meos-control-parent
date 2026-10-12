package com.persagy.apccontrol.basic.structure.demand.strategy;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.DemandInstanceGateway;
import com.persagy.apccontrol.basic.structure.demand.gateway.instance.param.DemandInstanceConfigQueryParam;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceConfigValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 根据实例查询需求空间参数
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(title = "根据实例查询需求空间参数")
public class DemandSpacesQueryParam extends SpaceQueryParam {

    @EqualsAndHashCode.Exclude
    private final DemandInstanceGateway demandInstanceGateway = SpringUtil.getBean(DemandInstanceGateway.class);

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "模版类型")
    private DemandTemplateTypeEnum templateType;

    @Schema(description = "模版类型")
    private List<DemandTemplateTypeEnum> templateTypes;

    @Schema(description = "是否过滤屏蔽空间, 默认不过滤")
    private Boolean filterInShieldedSpace;

    @Schema(description = "是否过滤黑名单空间, 默认不过滤")
    private Boolean filterInBlacklistedSpace;

    /**
     * 构造器
     *
     * @param templateType 模版类型
     * @since 7.250201.0
     */
    public DemandSpacesQueryParam(DemandTemplateTypeEnum templateType) {
        this.templateType = templateType;
    }

    @Override
    public List<DtpRwdSpaceDTO> queryObject(@NonNull DtpRwdObjectQueryParam queryParam) {
        return this.queryObject(queryParam, null);
    }

    @Override
    public List<DtpRwdSpaceDTO> queryObject(
            @NonNull DtpRwdObjectQueryParam originalQueryParam,
            @NonNull DtpRwdObjectQueryParam realQueryParam,
            @Nullable Function<DtpRwdObjectQueryParam, List<DtpRwdSpaceDTO>> customQuery) {
        boolean filterSpace = BooleanUtils.isTrue(this.filterInShieldedSpace)
                || BooleanUtils.isTrue(this.filterInBlacklistedSpace);
        DtpRwdObjectQueryParam finalQueryParam = realQueryParam;
        if (filterSpace) {
            finalQueryParam = dtpRwdConvert.copy(realQueryParam);
            finalQueryParam.setWithColumns(Lists.newArrayList(DtpRwdObjectDTO.Fields.id));
        }
        List<DtpRwdSpaceDTO> spaces = super.queryObject(originalQueryParam, finalQueryParam, customQuery);
        if (CollectionUtils.isEmpty(spaces)) {
            return Lists.newArrayList();
        }
        if (filterSpace) {
            Set<String> spaceIds = spaces.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
            Set<String> toBeDelSpaceIds = querySpaceIdsInInstanceConfig(this.date, templateType, spaceIds,
                    new DemandInstanceConfigValueObject()
                            .setShielded(BooleanUtils.isTrue(this.filterInShieldedSpace) ? true : null)
                            .setInBlacklisted(BooleanUtils.isTrue(this.filterInBlacklistedSpace) ? true : null));
            spaceIds.removeIf(toBeDelSpaceIds::contains);
            if (CollectionUtils.isEmpty(spaceIds)) {
                return Lists.newArrayList();
            }
            Collection<String> finalSpaceIds = spaceIds;
            if (CollectionUtils.isNotEmpty(realQueryParam.getId())) {
                finalSpaceIds = CollectionUtils.intersection(realQueryParam.getId(), spaceIds);
                if (CollectionUtils.isEmpty(finalSpaceIds)) {
                    return Lists.newArrayList();
                }
            }
            realQueryParam.setId(finalSpaceIds);
            spaces = super.queryObject(originalQueryParam, realQueryParam, customQuery);
        }
        return spaces;
    }

    /**
     * 仅查询管控空间
     *
     * @since 7.250201.0
     */
    public void queryOnlyControlled() {
        this.filterInShieldedSpace = true;
        this.filterInBlacklistedSpace = true;
    }

    /**
     * 转换运行监测空间查询参数
     *
     * @return 运行监测空间查询参数
     * @since 7.250201.0
     */
    public RunningMonitorSpaceQueryParam toRunningMonitorSpaceQueryParam() {
        RunningMonitorSpaceQueryParam runningMonitorSpaceQueryParam = new RunningMonitorSpaceQueryParam();
        BeanUtils.copyProperties(this, runningMonitorSpaceQueryParam);
        if (Objects.nonNull(this.templateType)) {
            runningMonitorSpaceQueryParam.setRunningMonitorSecondType(this.templateType.toRunningMonitorSecondTypeEnum());
        }
        if (CollectionUtils.isNotEmpty(this.templateTypes)) {
            runningMonitorSpaceQueryParam.setRunningMonitorSecondTypes(this.templateTypes.stream()
                    .map(DemandTemplateTypeEnum::toRunningMonitorSecondTypeEnum).collect(Collectors.toList()));
        }
        return runningMonitorSpaceQueryParam;
    }

    /**
     * 查询实例配置中的空间ID
     *
     * @param date         日期, 为空时查询最新
     * @param templateType 模版类型
     * @param spaceIds     空间ID, 为空时查询所有
     * @param filterConfig 过滤配置, 为空时查询所有
     * @return 实例配置中的空间ID
     * @since 7.250201.0
     */
    private Set<String> querySpaceIdsInInstanceConfig(
            @Nullable LocalDate date,
            @NonNull DemandTemplateTypeEnum templateType,
            @Nullable Set<String> spaceIds,
            @Nullable DemandInstanceConfigValueObject filterConfig) {
        DemandInstanceConfigQueryParam instanceConfigQueryParam = new DemandInstanceConfigQueryParam();
        instanceConfigQueryParam.setDate(date);
        instanceConfigQueryParam.setTemplateType(templateType);
        instanceConfigQueryParam.setInstanceType(DemandInstanceTypeEnum.SPACE);
        instanceConfigQueryParam.setInstanceIds(spaceIds);
        instanceConfigQueryParam.setFilterConfig(filterConfig);
        return demandInstanceGateway.listInstanceIds(instanceConfigQueryParam);
    }

    /**
     * 获取模版类型
     *
     * @return 模版类型
     * @since 7.250801.0
     */
    public @Nullable List<DemandTemplateTypeEnum> getTemplateTypes() {
        if (CollectionUtils.isNotEmpty(this.templateTypes)) {
            return this.templateTypes;
        }
        if (Objects.nonNull(this.templateType)) {
            return Lists.newArrayList(this.templateType);
        }
        return null;
    }
}
