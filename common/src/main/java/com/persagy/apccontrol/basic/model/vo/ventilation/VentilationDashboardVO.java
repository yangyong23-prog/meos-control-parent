package com.persagy.apccontrol.basic.model.vo.ventilation;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 通风DashboardVO
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
@Schema(title = "通风DashboardVO")
public class VentilationDashboardVO extends DtpRwdSpaceVO {

    @Schema(description = "空气O2浓度是否存在规则")
    private Boolean existsIotRuleByO2;

    @Schema(description = "空气（干球）温度是否存在规则")
    private Boolean existsIotRuleByTdb;

    @Schema(description = "空气CO浓度是否存在规则")
    private Boolean existsIotRuleByCo;

    @Schema(description = "空气NH3浓度是否存在规则")
    private Boolean existsIotRuleByNH3;

    @Schema(description = "空气H2S浓度是否存在规则")
    private Boolean existsIotRuleByH2S;

}
