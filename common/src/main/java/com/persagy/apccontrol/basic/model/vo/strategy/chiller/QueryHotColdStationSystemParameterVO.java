package com.persagy.apccontrol.basic.model.vo.strategy.chiller;

import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * @author yuyao
 */
@Data
@Schema(title = "冷热源系统参数")
@FieldNameConstants
public class QueryHotColdStationSystemParameterVO {

    @Schema(description = "开关状态")
    private ColdSiteSystemContrastVO sysSwitchSet;

    @Schema(description = "大机数量")
    private ColdSiteSystemContrastVO chillerLNum;

    @Schema(description = "小机数量")
    private ColdSiteSystemContrastVO chillerSNum;

    @Schema(description = "供冷季-主机出水温度")
    private ColdSiteSystemContrastVO chillWaterOutTemp;
    @Schema(description = "供热季-主机出水温度")
    private ColdSiteSystemContrastVO heatWaterSupplyTemp;


    @Schema(description = "供水温差设定")
    private ColdSiteSystemContrastVO chillWaterDetaTemp;

    @Schema(description = "供水压差设定")
    private ColdSiteSystemContrastVO chillWaterDetaPress;

    public QueryHotColdStationSystemParameterVO() {
        init();
    }

    public void init() {
        this.sysSwitchSet = new ColdSiteSystemContrastVO();
        this.chillerLNum = new ColdSiteSystemContrastVO();
        this.chillerSNum = new ColdSiteSystemContrastVO();
        this.chillWaterOutTemp = new ColdSiteSystemContrastVO();
        this.chillWaterDetaTemp = new ColdSiteSystemContrastVO();
        this.chillWaterDetaPress = new ColdSiteSystemContrastVO();
        this.heatWaterSupplyTemp = new ColdSiteSystemContrastVO();
    }

}
