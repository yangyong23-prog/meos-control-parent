package com.persagy.apccontrol.basic.model.vo.demand;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.domain.DemandConfigAggregate;
import com.persagy.apccontrol.basic.structure.demand.model.instance.domain.DemandInstanceConfigEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@Schema(description = "需求管理-实例标签VO")
public class DemandInstanceTagVO {

    // ------------------------- 需求标签 -------------------------

    @Schema(description = "是否是自定义配置")
    private Boolean customConfig;

    @Schema(description = "是否存在配置")
    private Boolean existCfg;

    // ------------------------- 实例标签 -------------------------

    @Schema(description = "是否被屏蔽")
    private Boolean shielded;

    @Schema(description = "是否在黑名单中")
    private Boolean inBlacklisted;

    @JsonIgnore
    @Schema(description = "自定义标签")
    private Map<String, Object> customizedTag;

    // ------------------------- 特殊标签 -------------------------

    @Schema(description = "当前以及下级对象的总配置数据")
    private Integer totalCustomizedSetting;

    @Schema(description = "下级对象是否存在自定义配置")
    private Boolean childCustomConfig;

    /**
     * 填充需求标签
     *
     * @param demandConfig 需求配置
     * @since 5.241102.0
     */
    public void fillDemandTag(@NonNull DemandConfigAggregate<BaseDemandConfig> demandConfig) {
        this.customConfig = Objects.nonNull(demandConfig.getConfig());
        this.existCfg = Objects.nonNull(demandConfig.getFinalConfig());
    }

    /**
     * 填充实例配置
     *
     * @param instanceConfig 实例配置
     * @param customizedTag  自定义标签, 可以为空
     * @since 5.241102.0
     */
    public void fillInstanceTag(
            @Nullable DemandInstanceConfigEntity instanceConfig,
            @Nullable Map<String, Object> customizedTag) {
        this.shielded = instanceConfig != null && instanceConfig.getConfig() != null
                && BooleanUtils.isTrue(instanceConfig.getConfig().getShielded());
        this.inBlacklisted = instanceConfig != null && instanceConfig.getConfig() != null
                && BooleanUtils.isTrue(instanceConfig.getConfig().getInBlacklisted());
        if (MapUtils.isEmpty(customizedTag)) {
            return;
        }
        this.customizedTag = customizedTag;
    }

    /**
     * 获取当前 以及下级对象的总配置数据
     * 只统计空间节点
     *
     * @return 配置总数
     */
    public CalculateVO buildTotalCustomizedSetting(DemandTreeObjectVO vo) {
        int total = 0;
        boolean childCustomConfig = false;

        if (vo.getChildren() != null) {
            for (DemandTreeObjectVO child : vo.getChildren()) {
                if (child != null) {
                    CalculateVO calculateVO = vo.getInstanceTag().buildTotalCustomizedSetting(child);
                    total += calculateVO.getTotal();
                    if (calculateVO.childCustomConfig) {
                        childCustomConfig = true;
                    }
                }
            }
        }
        if (vo.getInstanceTag() == null) {
            vo.setInstanceTag(new DemandInstanceTagVO());
        }
        if (vo.getInstanceTag().getCustomConfig() != null
                && vo.getInstanceTag().getCustomConfig()) {
            if (DemandInstanceTypeEnum.SPACE.equals(vo.getInstanceType())) {
                total++;
            }
            childCustomConfig = true;
        }
        vo.getInstanceTag().setTotalCustomizedSetting(total);
        vo.getInstanceTag().setChildCustomConfig(childCustomConfig);

        return new CalculateVO(total, childCustomConfig);
    }

    @Data
    public static class CalculateVO {
        int total = 0;
        boolean childCustomConfig;

        public CalculateVO(int total, boolean childCustomConfig) {
            this.total = total;
            this.childCustomConfig = childCustomConfig;
        }
    }

    @JsonAnyGetter
    public Map<String, Object> getUndefinedProperties() {
        if (MapUtils.isEmpty(this.customizedTag)) {
            return this.customizedTag;
        }
        Map<String, Object> result = new HashMap<>(this.customizedTag);
        Arrays.stream(this.getClass().getDeclaredFields()).map(Field::getName).forEach(result::remove);
        return result;
    }
}
