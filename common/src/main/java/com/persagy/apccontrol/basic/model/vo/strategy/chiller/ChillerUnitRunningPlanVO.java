package com.persagy.apccontrol.basic.model.vo.strategy.chiller;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling.DtpRwdCentralCoolingInfoCodeEnum;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 冷机运行计划VO
 *
 * @author JiYinchuan
 * @since v5.9.0
 */
@Data
@FieldNameConstants
@Schema(title = "冷机运行计划VO")
public class ChillerUnitRunningPlanVO {

    @Schema(description = "大机台数")
    private RunningValueVO<Integer> chillerLNum;

    @Schema(description = "小机台数")
    private RunningValueVO<Integer> chillerSNum;

    @Schema(description = "大小机自动设定值")
    private Map<String, Integer> autoSetChillerNum;

    @Schema(description = "大小自动反馈值")
    private Map<String, Integer> feedbackChillerNum;

    @Schema(description = "冷机出水温度设定值")
    private RunningValueVO<BigDecimal> chillWaterOutTempSet;

    @Schema(description = "优先级按先后顺序最不利末端压差/冷冻一次侧供回水温差设定/冷冻一次一级泵供回水压差设定类型")
    private DtpRwdCentralCoolingInfoCodeEnum chillWaterDetaSetType;

    @Schema(description = "优先级按先后顺序最不利末端压差/冷冻一次侧供回水温差设定/冷冻一次一级泵供回水压差设定值")
    private RunningValueVO<BigDecimal> chillWaterDetaSet;

    @Schema(description = "冷冻一次侧供回水温差设定/冷冻一次一级泵供回水压差设定单位")
    private String chillWaterDetaSetUnit;

}
