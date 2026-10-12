package com.persagy.apccontrol.basic.model.vo;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Description: 算法注册参数
 */
@Data
@Schema(title = "算法注册参数")
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmStartCalculationDTO {

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "系统编码")
    private String systemId;

    @Schema(description = "集团")
    private String groupCode;

    @Schema(description = "城市")
    private String cityCode;

    @Schema(description = "模式")
    private String mode;

    @Schema(description = "冷热设备类型")
    private String heating_source;

    @Schema(description = "冷机设定出水温度范围")
    private SupplyRangMap supplyRangMap;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplyRangMap{

        @Schema(description = "冷机设定出水温度上限")
        private Integer supplyRangMapMin;

        @Schema(description = "冷机设定出水温度下限")
        private Integer supplyRangMapMax;

    }

    @Schema(description = "供水温度设定值改变后模式维持时间（min）")
    @JsonAlias({"alg_cooling_water_temp_timing"})
    private Integer waterSetpointChangeLimitTime;

    @Schema(description = "冷机加减机动作后模式维持时间（min）")
    @JsonAlias({"alg_cooling_machine_change_timing"})
    private Integer chillerModeChangeLimitTime;

    @Schema(description = "最大加减冷机挡位限制")
    @JsonAlias({"alg_cooling_machine_change_range"})
    private String gearLimit;

    public void setGearLimit(String gearLimit) {
        String[] g = gearLimit == null ? null : gearLimit.split(",");
        this.gearLimit = g == null ? null : g[0] + "L" + g[1] + "S";
    }

    @Schema(description = "是否支持同时开大小机")
    @JsonAlias({"alg_cooling_machine_mix_enabled"})
    private Boolean sameTimeOpenLSChiller;

    public void setSameTimeOpenLSChiller(String sameTimeOpenLSChiller) {
        if (LocaleMessageUtil.getMessage("key_42bb8d323c484d7fa6be700b8d5f55fd").equals(sameTimeOpenLSChiller)) {
            this.sameTimeOpenLSChiller = true;
        }else {
            this.sameTimeOpenLSChiller = false;
        }
    }

    @Schema(description = "大冷机开启台数上限")
    @JsonAlias({"alg_cooling_machine_large_max"})
    private Integer chillerNumSetLUpper;

    @Schema(description = "小冷机开启台数上限")
    @JsonAlias({"alg_cooling_machine_small_max"})
    private Integer chillerNumSetSUpper;

    @Schema(description = "过冷过热判定持续时间")
    @JsonAlias({"alg_cooling_machine_temp_timing"})
    private Integer tl_of_cold_hot;

    @Schema(description = "自动加机IPLR阈值")
    @JsonAlias({"alg_cooling_machine_add_ratio"})
    private BigDecimal iPLR_upper_limit;

    @Schema(description = "自动减机IPLR阈值")
    @JsonAlias({"alg_cooling_machine_minus_ratio"})
    private BigDecimal iPLR_lower_limit;

    @Schema(description = "开机温度设定值")
    @JsonAlias({"alg_cooling_water_temp_init"})
    private BigDecimal T_ch_s_set_startup;

    @Schema(description = "最多提前开机时长")
    @JsonAlias({"alg_project_power_on"})
    private Integer tl_earlyStartup;

    @Schema(description = "最多提前关机时长")
    @JsonAlias({"alg_project_power_off"})
    private Integer tl_earlyShutdown;
}
