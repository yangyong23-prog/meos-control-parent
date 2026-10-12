package com.persagy.apccontrol.basic.model.dto.demand.airconditioning;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.BaseDemandConfig;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningWorkConditionValueObject;
import com.persagy.apccontrol.basic.structure.intervention.model.InterventionConfigData;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 需求配置-空调需求-定制功能配置表
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "需求配置-空调需求-价值选择方案")
public class DemandConfigAirconditioningCustomDTO extends BaseDemandConfig implements BaseCheckParamProvider, InterventionConfigData {

    @Schema(description = "价值选择类型")
    private BasicsConfigTypeEnum valueSelectType;

    @Schema(description = "空间运行工况标准配置")
    private DemandConfigAirconditioningWorkConditionValueObject workCondition;

    @Schema(description = "价值选择配置")
    private List<ValueSelectDTO> valueSelectConfig;

    /**
     * 兼容价值选择需要此字段，20250102迭代后可删除
     */
    @Schema(description = "价值选择区间配置")
    @Deprecated
    private List<ValueSelectConfigDto> valueSelectRange;



    /**
     * 获取价值选择配置
     *
     * @return 价值选择配置
     * @since 7.250102.0
     */
    public List<ValueSelectConfigDto> parseValueSelectRange() {
        return parseValueSelectRange(null);
    }

    /**
     * 获取价值选择配置
     *
     * @param valueSelectType 筛选执行类型
     * @return 价值选择配置
     * @since 7.250102.0
     */
    public List<ValueSelectConfigDto> parseValueSelectRange(@Nullable BasicsConfigTypeEnum valueSelectType) {
        BasicsConfigTypeEnum finalValueSelectType = Objects.nonNull(valueSelectType)
                ? valueSelectType : this.valueSelectType;
        if (Objects.isNull(finalValueSelectType) || CollectionUtils.isEmpty(valueSelectConfig)) {
            return Lists.newArrayList();
        }
        return valueSelectConfig.stream()
                .filter(item -> item.getValueSelectType() == finalValueSelectType)
                .findFirst()
                .map(item -> Optional.ofNullable(item.getValueSelectRange()).orElseGet(ArrayList::new))
                .orElseGet(ArrayList::new);
    }

    /**
     * 填充价值选择配置
     *
     * @param valueSelectType 价值选择类型
     * @param typeConfigs     类型配置
     * @return 当前实例
     * @since 7.250102.0
     */
    public DemandConfigAirconditioningCustomDTO fillValueSelectConfig(
            @NonNull BasicsConfigTypeEnum valueSelectType,
            @NonNull List<ValueSelectConfigDto> typeConfigs) {
        List<ValueSelectDTO> valueSelectConfig = Optional.ofNullable(this.valueSelectConfig).orElseGet(ArrayList::new);
        Optional<ValueSelectDTO> configOpt = valueSelectConfig.stream()
                .filter(item -> item.getValueSelectType() == valueSelectType)
                .findFirst();
        if (configOpt.isPresent()) {
            configOpt.get().setValueSelectRange(typeConfigs);
        } else {
            valueSelectConfig.add(new ValueSelectDTO(valueSelectType, typeConfigs));
        }
        this.valueSelectConfig = valueSelectConfig;
        return this;
    }

    /**
     * 默认实例
     *
     * @return 默认实例
     * @since 7.250702.0
     */
    public static DemandConfigAirconditioningCustomDTO defaultInstance() {
        DemandConfigAirconditioningCustomDTO result = new DemandConfigAirconditioningCustomDTO();
        result.setValueSelectType(BasicsConfigTypeEnum.VALUE_SELECT_QUALITY_FIRST);
        return result;
    }
}
