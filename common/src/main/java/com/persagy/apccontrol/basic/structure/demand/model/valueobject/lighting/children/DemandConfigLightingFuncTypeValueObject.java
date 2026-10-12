package com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitGroupSettingDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingFeedbackAdjustTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingTypeSettingModeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 照明需求功能类型配置值对象
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
@NoArgsConstructor
public class DemandConfigLightingFuncTypeValueObject implements ValueObject, BaseCheckParamProvider {

    /**
     * 照明回路功能类型
     */
    @NotBlank(message = "{key_0b89340dffc8430a9cc7967116246d55}")
    private @NonNull String funcType;

    /**
     * 配置模式
     *
     * <ul>
     *     <li><b>ALL_OPEN</b> - 全开</li>
     *     <li><b>ALL_CLOSE</b> - 全关</li>
     *     <li><b>PARTIAL_OPEN</b> - 部分开启</li>
     *     <li><b>INSTANCE_GROUP</b> - 编组配置</li>
     *     <li><b>FEEDBACK_ADJUSTMENT</b> - 反馈调节配置</li>
     *     <li><b>NOT_CONFIGURED</b> - 未配置</li>
     * </ul>
     */
    @NotNull(message = "{key_7288bbec93cc469f84df47060e635e83}")
    private @NonNull DemandLightingTypeSettingModeEnum settingMode;

    /**
     * 照明回路配置
     *
     * <p>存储时仅 {@link #settingMode} 为 {@link DemandLightingTypeSettingModeEnum#PARTIAL_OPEN}
     * 或 {@link DemandLightingTypeSettingModeEnum#FEEDBACK_ADJUSTMENT} 时存在</p>
     * <p>查询时该对象赋值该对象下所有照明回路配置</p>
     */
    @Valid
    private List<DemandConfigLightingCircuitValueObject> circuitSettings;

    /**
     * 照明回路编组配置
     *
     * <p>仅 {@link #settingMode} 为 {@link DemandLightingTypeSettingModeEnum#INSTANCE_GROUP} 时存在</p>
     */
    @Valid
    private List<DemandLightingCircuitGroupSettingDTO> circuitGroupSettings;

    /**
     * 空间反馈调节类型
     *
     * <p>仅配置对象为空间且 {@link #settingMode} 为 {@link DemandLightingTypeSettingModeEnum#FEEDBACK_ADJUSTMENT} 时存在</p>
     */
    private DemandLightingFeedbackAdjustTypeEnum spaceFeedbackAdjustType;

    @Override
    public void checkAndSet() {
        switch (this.settingMode) {
            case ALL_OPEN:
            case ALL_CLOSE:
                this.circuitSettings = null;
                this.circuitGroupSettings = null;
                break;
            case PARTIAL_OPEN:
                if (CollectionUtils.isEmpty(this.circuitSettings)) {
                    throw new ValidationException(LocaleMessageUtil.getMessage("key_08b327e8ce0b4992b3c36c8c45c095d4"));
                }
                this.circuitSettings.removeIf(item ->
                        item.getSettingMode() == DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED);
                if (CollectionUtils.isEmpty(this.circuitSettings)) {
                    throw new ValidationException(LocaleMessageUtil.getMessage("key_246dfcfdf74a4d089aa3d149136e8517"));
                }
                this.circuitGroupSettings = null;
                break;
            case INSTANCE_GROUP:
                if (CollectionUtils.isEmpty(this.circuitGroupSettings)) {
                    throw new ValidationException(LocaleMessageUtil.getMessage("key_b5082c2108844d80b514e0157c247e12"));
                }
                this.circuitSettings = null;
                break;
            case FEEDBACK_ADJUSTMENT:
                this.circuitGroupSettings = null;
                if (CollectionUtils.isNotEmpty(this.circuitSettings)) {
                    this.circuitSettings.removeIf(item ->
                            item.getSettingMode() == DemandLightingCircuitSettingModeEnum.NOT_CONFIGURED);
                    for (DemandConfigLightingCircuitValueObject circuit : this.circuitSettings) {
                        circuit.checkAndSet();
                    }
                }
                break;
            default:
                throw new ValidationException(LocaleMessageUtil.getMessage("key_94d2cb327aef46b59dbe3db880e5eaea"));
        }
    }
}
