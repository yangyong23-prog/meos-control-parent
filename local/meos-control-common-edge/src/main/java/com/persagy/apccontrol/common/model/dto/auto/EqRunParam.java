package com.persagy.apccontrol.common.model.dto.auto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhangyanchang
 * @version 1.0
 * @title: EqRunParam
 * @description: 自动加减机运行所需的参数
 * @date: 2023/12/26 09:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EqRunParam {
    @Schema(title = "设备ID",required = true)
    private String eqId;
    @Schema(title = "运行状态",required = true)
    private String runStatus;
    @Schema(title = "设备上线状态",required = true)
    private String onlineStatus;
    @Schema(title = "负载率最大值设定",required = true)
    private String maxPLRSet;
    @Schema(title = "蒸发器出口水温，冷站专用",required = true)
    private String evapWaterOutTemp;
    @Schema(title = "冷冻水出水温度",required = true)
    private String chillWaterOutTempSet;
    @Schema(title = "额定功率",required = true)
    private String ratedP;
    @Schema(title = "耗电功率",required = true)
    private String elecConsumP;
    @JsonAlias("iPLR")
    @Schema(title = "电流负载率",required = true)
    private String iPLR;
    @Schema(title = "温室内侧机组出口载冷剂温度，冷热源专用",required = true)
    private String indoorMediumOutTemp;
    @Schema(title = "室内侧制热供水温度设定，冷热源-供热季-专用", required = true)
    private String heatWaterSupplyTempSet;

    @Schema(title = "本地远程状态 0-本地 1-远程", required = true)
    private String localRemoteStatus;

    @Schema(title = "额定制冷量", required = true)
    private String ratedCool;

    @Schema(title = "额定制热量", required = true)
    private String ratedHeat;
}
