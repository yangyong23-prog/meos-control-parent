package com.persagy.apccontrol.basic.model.dto.demand.lighting;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.ValidationException;
import java.math.BigDecimal;

/**
 * 需求管理-照明需求-回路级反馈调节条件配置DTO
 *
 * <p>当回路 settingMode = FEEDBACK_ADJUSTMENT 时携带，包含自动开启/关闭条件</p>
 *
 * @author JiYinchuan
 * @since 7.260801.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "需求管理-照明需求-回路级反馈调节条件配置DTO")
public class DemandLightingCircuitFeedbackAdjustSettingDTO implements BaseCheckParamProvider {

    @Schema(description = "自动开启条件·照度阈值（lx），0-10000正整数")
    private BigDecimal underOutdoorIlluminance;

    @Schema(description = "自动开启条件·持续时间（min），0-300整数，0=达到阈值即生效")
    private Integer underDuration;

    @Schema(description = "自动关闭条件·照度阈值（lx），0-10000正整数")
    private BigDecimal overtopOutdoorIlluminance;

    @Schema(description = "自动关闭条件·持续时间（min），0-300整数，0=达到阈值即生效")
    private Integer overtopDuration;

    /**
     * 判断所有条件字段是否为空
     *
     * @return true 如果所有条件字段都为空
     * @since 7.260801.0
     */
    public boolean fieldEmpty() {
        return ObjectUtils.allNull(this.underOutdoorIlluminance, this.underDuration,
                this.overtopOutdoorIlluminance, this.overtopDuration);
    }

    @Override
    public void checkAndSet() {
        // 自动开启和自动关闭条件最少配置一项
        if (ObjectUtils.allNull(this.underOutdoorIlluminance, this.underDuration,
                this.overtopOutdoorIlluminance, this.overtopDuration)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_16295028e3b0459bbf801bc336a6fbb7"));
        }
        // 自动开启条件成对校验：阈值和持续时间必须同时有值或同时为空
        if (ObjectUtils.anyNotNull(this.underOutdoorIlluminance, this.underDuration)
                && ObjectUtils.anyNull(this.underOutdoorIlluminance, this.underDuration)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_1eff7754d635403988906167afa0f5c3"));
        }
        // 自动关闭条件成对校验：阈值和持续时间必须同时有值或同时为空
        if (ObjectUtils.anyNotNull(this.overtopOutdoorIlluminance, this.overtopDuration)
                && ObjectUtils.anyNull(this.overtopOutdoorIlluminance, this.overtopDuration)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_015f1d8346c8449a9530743a2a3e6f01"));
        }
        // 两个条件都配置时，自动开启阈值不能大于自动关闭阈值
        if (this.underOutdoorIlluminance != null && this.overtopOutdoorIlluminance != null
                && this.underOutdoorIlluminance.compareTo(this.overtopOutdoorIlluminance) > 0) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_9c4e7a1f3b2d4e8a9f1c3d5e7a9b1c3d"));
        }
    }
}
