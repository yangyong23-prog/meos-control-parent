package com.persagy.apccontrol.basic.model.vo;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.vo.cfg.ExpertRulesVO;
import com.persagy.apccontrol.basic.model.vo.rules.CoolerStand;
import com.persagy.apccontrol.basic.model.vo.rules.CoolerSwitch;
import com.persagy.apccontrol.basic.model.vo.rules.CoolerTemperature;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @Description: 专家经验调研
 * @author: yancheng
 * @date: 2023/3/25
 * @version: V1.0
 */
@Data
@Schema(title = "专家经验调研")
@NoArgsConstructor
@AllArgsConstructor
public class ExpertExperienceDTO {

    @Schema(description = "开始时间")
    private String continueStartTime;

    @Schema(description = "结束时间")
    private String continueStopTime;

    @Schema(description = "冷机出水温度上限")
    private Integer chillerWaterOutTempSetUpper;

    @Schema(description = "冷机出水温度下限")
    private Integer chillerWaterOutTempSetLower;

    @Schema(description = "大冷机台数上限")
    private Integer chillerNumSetLUpper;

    @Schema(description = "大冷机台数下限")
    private Integer chillerNumSetLLower;

    @Schema(description = "小冷机台数上限")
    private Integer chillerNumSetSUpper;

    @Schema(description = "小冷机台数下限")
    private Integer chillerNumSetSLower;

    @Schema(description = "最多提前开机时间(min)")
    private Integer earlyStartupTimeLimitUpper;

    @Schema(description = "最少提前开机时间(min)")
    private Integer earlyStartupTimeLimitLower;

    @Schema(description = "最多提前关机时间(min)")
    private Integer earlyShutdownTimeLimitUpper;

    @Schema(description = "最少提前关机时间(min)")
    private Integer earlyShutdownTimeLimitLower;

    public ExpertExperienceDTO convertDto(RcBasicsConfig basicsConfig) {
        ExpertRulesVO expertRulesVO = JSONObject.parseObject(basicsConfig.getResult(), ExpertRulesVO.class);
        // 冷机台数约束
        CoolerStand coolerStand = expertRulesVO.getCoolerStand();
        if (Objects.nonNull(coolerStand.getLargeCooler()) && Objects.nonNull(coolerStand.getLargeCooler().getMax()) && Objects.nonNull(coolerStand.getLargeCooler().getMin())) {
            this.chillerNumSetLUpper = coolerStand.getLargeCooler().getMax().intValue();
            this.chillerNumSetLLower = coolerStand.getLargeCooler().getMin().intValue();
        } else {
            this.chillerNumSetLUpper = 0;
            this.chillerNumSetLLower = 0;
        }
        if (Objects.nonNull(coolerStand.getSmallCooler()) && Objects.nonNull(coolerStand.getSmallCooler().getMax()) && Objects.nonNull(coolerStand.getSmallCooler().getMin())) {
            this.chillerNumSetSUpper = coolerStand.getSmallCooler().getMax().intValue();
            this.chillerNumSetSLower = coolerStand.getSmallCooler().getMin().intValue();
        } else {
            this.chillerNumSetSUpper = 0;
            this.chillerNumSetSLower = 0;
        }
        // 冷机开关机约束
        CoolerSwitch coolerSwitch = expertRulesVO.getCoolerSwitch();
        this.earlyStartupTimeLimitUpper = coolerSwitch.getPowerUp().getMax().intValue();
        this.earlyStartupTimeLimitLower = coolerSwitch.getPowerUp().getMin().intValue();
        this.earlyShutdownTimeLimitUpper = coolerSwitch.getShutDown().getMax().intValue();
        this.earlyShutdownTimeLimitLower = coolerSwitch.getShutDown().getMin().intValue();
        // 冷机出水温度
        CoolerTemperature coolerTemperature = expertRulesVO.getCoolerTemperature();
        this.chillerWaterOutTempSetUpper = coolerTemperature.getWaterTemperature().getMax().intValue();
        this.chillerWaterOutTempSetLower = coolerTemperature.getWaterTemperature().getMin().intValue();

        this.continueStartTime = DateUtils.date2Str(expertRulesVO.getStartTime(), DateUtils.SDFDAY);
        this.continueStopTime = DateUtils.date2Str(expertRulesVO.getEndTime(), DateUtils.SDFDAY);
        return this;
    }
}
