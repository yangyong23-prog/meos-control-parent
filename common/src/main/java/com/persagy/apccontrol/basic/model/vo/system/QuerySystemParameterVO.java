package com.persagy.apccontrol.basic.model.vo.system;

import com.persagy.apccontrol.basic.model.vo.ColdSiteSystemContrastVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * @author yuyao
 */
@Data
@Schema(title = "系统参数")
@FieldNameConstants
public class QuerySystemParameterVO {

    @Schema(description = "开关状态")
    private ColdSiteSystemContrastVO sysSwitchSet;

    @Schema(description = "大冷机数量")
    private ColdSiteSystemContrastVO chillerLNum;

    @Schema(description = "小冷机数量")
    private ColdSiteSystemContrastVO chillerSNum;

    @Schema(description = "冷机出水温度")
    private ColdSiteSystemContrastVO primaryChillWaterMainSupplyTemp;
    @Schema(description = "冷冻水供回水温差")
    private ColdSiteSystemContrastVO chillWaterDetaTemp;
    @Schema(description = "冷冻水供回水压差")
    private ColdSiteSystemContrastVO chillWaterDetaPress;
    @Schema(description = "冷却塔出水温度")
    private ColdSiteSystemContrastVO coolWaterMainReturnTemp;
    @Schema(description = "最不利末端压差")
    private ColdSiteSystemContrastVO worstTerminalDetaPress;

    public QuerySystemParameterVO() {
        init();
    }

    public void init() {
        this.sysSwitchSet = new ColdSiteSystemContrastVO();
        this.chillerLNum = new ColdSiteSystemContrastVO();
        this.chillerSNum = new ColdSiteSystemContrastVO();
        this.primaryChillWaterMainSupplyTemp = new ColdSiteSystemContrastVO();
        this.chillWaterDetaTemp = new ColdSiteSystemContrastVO();
        this.chillWaterDetaPress = new ColdSiteSystemContrastVO();
        this.coolWaterMainReturnTemp = new ColdSiteSystemContrastVO();
        this.worstTerminalDetaPress = new ColdSiteSystemContrastVO();
    }

}
