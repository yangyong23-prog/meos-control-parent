package com.persagy.apccontrol.basic.structure.demand.model.valueobject.lighting.children;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.dto.demand.lighting.DemandLightingCircuitFeedbackAdjustSettingDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.lighting.DemandLightingCircuitSettingModeEnum;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 照明需求照明回路配置值对象
 *
 * @author JiYinchuan
 * @since 7.250801.0
 */
@Data
@NoArgsConstructor
public class DemandConfigLightingCircuitValueObject implements ValueObject, BaseCheckParamProvider {

    /**
     * 照明回路ID
     */
    @NotBlank(message = "{key_d76ea9b42948435aabcf79f837dc128f}")
    private @NonNull String circuitId;

    /**
     * 照明回路配置模式
     *
     * <ul>
     *     <li><b>OPEN</b> - 开启</li>
     *     <li><b>CLOSE</b> - 关闭</li>
     *     <li><b>FEEDBACK_ADJUSTMENT</b> - 反馈调节</li>
     *     <li><b>NOT_CONFIGURED</b> - 未配置</li>
     * </ul>
     */
    @NotNull(message = "{key_e59ff1c22edb44158ece1b081c425861}")
    private @NonNull DemandLightingCircuitSettingModeEnum settingMode;

    /**
     * 回路级反馈调节条件配置
     *
     * <p>仅 {@link #settingMode} 为 {@link DemandLightingCircuitSettingModeEnum#FEEDBACK_ADJUSTMENT} 时存在</p>
     *
     * @since 7.260801.0
     */
    @Valid
    private DemandLightingCircuitFeedbackAdjustSettingDTO feedbackAdjustSetting;

    @Override
    public void checkAndSet() {
        switch (this.settingMode) {
            case FEEDBACK_ADJUSTMENT:
                if (Objects.isNull(this.feedbackAdjustSetting) || this.feedbackAdjustSetting.fieldEmpty()) {
                    throw new ValidationException(LocaleMessageUtil.getMessage("key_16295028e3b0459bbf801bc336a6fbb7"));
                }
                this.feedbackAdjustSetting.checkAndSet();
                break;
            case OPEN:
            case CLOSE:
            case NOT_CONFIGURED:
                this.feedbackAdjustSetting = null;
                break;
            default:
                break;
        }
    }

}
