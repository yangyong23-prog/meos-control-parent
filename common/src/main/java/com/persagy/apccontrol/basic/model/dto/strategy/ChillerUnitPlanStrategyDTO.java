package com.persagy.apccontrol.basic.model.dto.strategy;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.enumtype.CoolerRuleTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.strategy.detail.ChillerUnitAdjustmentType;
import com.persagy.apccontrol.basic.validate.StrategyGroup;
import com.persagy.validate.utility.annotation.NumberToScaleString;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.ObjectUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 冷机策略计划DTO
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
@Schema(title = "冷机策略计划DTO")
public class ChillerUnitPlanStrategyDTO extends StrategyPlanDTO.Detail {

    @Schema(description = "大机台数")
    private Integer chillerLNum;

    @Schema(description = "小机台数")
    private Integer chillerSNum;

    @Schema(description = "冷机出水温度设定值")
    @NumberToScaleString
    private BigDecimal chillWaterOutTempSet;

    @Schema(description = "调整原因类型")
    @NotNull(message = "{key_b6a2f486ef074743a1d1b279a3803842}", groups = StrategyGroup.TempStrategy.class)
    private ChillerUnitAdjustmentType adjustmentReasonType;

    @Schema(description = "调整原因详情")
    private String adjustmentReasonDetail;

    @Schema(description = "持续时长")
    private Integer sustainTime;

    @Override
    public Set<String> controlFieldNames() {
        return Sets.newHashSet(
                StrategyPlanDTO.Detail.Fields.runStatus,
                Fields.chillerLNum,
                Fields.chillerSNum,
                Fields.chillWaterOutTempSet
        );
    }

    /**
     * 校验参数并设置
     *
     * @param coolerMap      冷机台数Map
     * @param isTempStrategy 是否为临时策略
     * @since v5.6.0
     */
    public void checkAndSet(Map<String, Integer> coolerMap, boolean isTempStrategy) {
        if (RunStatusEnum.CLOSE == runStatus) {
            whereCloseSetNull();
            return;
        }
        boolean chillerNumRequired = !isTempStrategy || ObjectUtils.anyNotNull(chillerLNum, chillerSNum);
        boolean chillWaterOutTempSetRequired = !isTempStrategy;
        boolean adjustmentReasonDetailRequired = isTempStrategy
                && adjustmentReasonType == ChillerUnitAdjustmentType.OTHER_REASON;

        boolean chillerNumUnValid = chillerNumRequired && (Objects.isNull(chillerLNum)) && (Objects.isNull(chillerSNum));
        if (chillerNumUnValid) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_6a04d1612eec47848861494828370488"));
        }
        int coolerL = coolerMap.getOrDefault(CoolerRuleTypeEnum.LARGE_COOLER.getCode(), 0);
        int coolerS = coolerMap.getOrDefault(CoolerRuleTypeEnum.SMALL_COOLER.getCode(), 0);
        if (chillerNumRequired && this.chillerLNum != null && this.chillerLNum > coolerL) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_3367fb8fcdc54ad599be47fab287ebf0"));
        }
        if (chillerNumRequired && this.chillerSNum != null && this.chillerSNum > coolerS) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_813be38f211c4b5f845ec4d1a3e9645c"));
        }
        if (chillWaterOutTempSetRequired && Objects.isNull(chillWaterOutTempSet)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_4dac5bdb3aa5478ba95855a7e8e65fbf"));
        }
        if (adjustmentReasonDetailRequired && Objects.isNull(adjustmentReasonDetail)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_a46c30650f1c4efaa235bc1bb4723d87"));
        }
    }

    /**
     * 当为关闭时设置控制值为 {@code null}
     *
     * @since v5.6.0
     */
    public ChillerUnitPlanStrategyDTO whereCloseSetNull() {
        if (RunStatusEnum.CLOSE == runStatus) {
            this.chillerLNum = null;
            this.chillerSNum = null;
            this.chillWaterOutTempSet = null;
        }
        return this;
    }
}
