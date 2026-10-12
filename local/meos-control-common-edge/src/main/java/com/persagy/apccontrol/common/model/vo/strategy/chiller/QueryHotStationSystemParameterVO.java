package com.persagy.apccontrol.common.model.vo.strategy.chiller;

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
public class QueryHotStationSystemParameterVO {

    @Schema(description = "开关状态")
    private ColdSiteSystemContrastVO sysSwitchSet;


    @Schema(description = "出水温度")
    private ColdSiteSystemContrastVO waterOutTemp;

    @Schema(description = "供回水温差")
    private ColdSiteSystemContrastVO chillWaterDetaTemp;

    @Schema(description = "供回水压差")
    private ColdSiteSystemContrastVO waterDetaPress;




    public QueryHotStationSystemParameterVO() {
        init();
    }

    public void init() {
        this.sysSwitchSet = new ColdSiteSystemContrastVO();
        this.waterOutTemp = new ColdSiteSystemContrastVO();
        this.waterDetaPress = new ColdSiteSystemContrastVO();
        this.chillWaterDetaTemp = new ColdSiteSystemContrastVO();
    }

}
