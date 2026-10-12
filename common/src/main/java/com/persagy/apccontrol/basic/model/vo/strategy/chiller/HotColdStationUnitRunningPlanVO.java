package com.persagy.apccontrol.basic.model.vo.strategy.chiller;

import com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling.DtpRwdCentralCoolingInfoCodeEnum;
import com.persagy.apccontrol.basic.model.vo.strategy.RunningValueVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

/**
 * 冷热源运行计划VO
 *
 * @author JiYinchuan
 * @since v5.9.0
 */
@Data
@FieldNameConstants
@Schema(title = "冷热源运行计划VO")
public class HotColdStationUnitRunningPlanVO {

    @Schema(description = "主机/大机台数")
    private RunningValueVO<Integer> chillerLNum;
    /*
    @Schema(description = "小机台数")
    private RunningValueVO<Integer> chillerSNum;*/

    @Schema(description = "供冷季-出水温度设定值")
    private RunningValueVO<BigDecimal> chillWaterOutTempSet;

    @Schema(description = "供热季-出水温度设定值")
    private RunningValueVO<BigDecimal> heatWaterSupplyTempSet;


    @Schema(description = "供回水温差设定/供回水压差设定类型")
    private DtpRwdCentralCoolingInfoCodeEnum chillWaterDetaSetType;
    @Schema(description = "供水温差")
    private RunningValueVO<BigDecimal> chillWaterDetaTempSet;

    @Schema(description = "供水压差设定")
    private RunningValueVO<BigDecimal> chillWaterDetaPressSet;


    @Schema(description = "制冷供水温度设定-壳配置")
    private String policySiteColdHotColdTempSetCfg;

    @Schema(description = "制热供水温度设定-壳配置")
    private String policySiteColdHotHeatTempSetCfg;


}
