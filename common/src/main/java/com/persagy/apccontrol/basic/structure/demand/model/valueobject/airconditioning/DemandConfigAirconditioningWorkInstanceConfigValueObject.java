package com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning;

import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.centralcooling.DtpRwdCentralCoolingInfoCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 空调需求配置--空间工况--对象配置
 *
 * @author JiYinchuan
 * @since 5.241102.0
 */
@Data
public class DemandConfigAirconditioningWorkInstanceConfigValueObject implements ValueObject {

    public static final String FIELD_NAME_SUPPLY_TEMP = "supplyTemp";
    public static final String FIELD_NAME_SUPPLY_TEMP_TIME = "supplyTempTime";
    public static final String FIELD_NAME_LOWER_CHILL_WATER_DETA_SERT = "lowerChillWaterDetaSet";
    public static final String FIELD_NAME_UPPER_CHILL_WATER_DETA_SERT = "upperChillWaterDetaSet";

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象本地编码")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "设备类编码")
    private String classCode;

    @Schema(description = "供水温度")
    private BigDecimal supplyTemp;

    @Schema(description = "供水温度文本名称")
    private String supplyTempName;

    @Schema(description = "供水温度持续时长")
    private Integer supplyTempTime;

    @Schema(description = "回水温度文本名称")
    private String primaryChillWaterMainReturnTempName;

    @Schema(description = "回水温度下限")
    private BigDecimal lowerPrimaryChillWaterMainReturnTemp;

    @Schema(description = "回水温度上限")
    private BigDecimal upperPrimaryChillWaterMainReturnTemp;

    @Schema(description = "供回水温/压差设定类型")
    private DtpRwdCentralCoolingInfoCodeEnum chillWaterDetaSetType;

    @Schema(description = "供回水温/压差单位")
    private String chillWaterDetaUnit;

    @Schema(description = "供回水温/压差文本名称")
    private String chillWaterDetaSetName;

    @Schema(description = "供回水温度/压差设定列表")
    private List<TempAndPressureSetDTO> tempAndPressureSetList;

    @Schema(description = "供回水温/压差下限")
    private BigDecimal lowerChillWaterDetaSet;

    @Schema(description = "供回水温/压差上限")
    private BigDecimal upperChillWaterDetaSet;

    /**
     * 温差和压差设置列表
     *
     * @author MuAnFu
     * @date 2025-09-03
     */
    @Data
    public static class TempAndPressureSetDTO {

        public static final String FIELD_NAME_LOWER_CHILL_WATER_DETA_SERT = "lowerChillWaterDetaSet";
        public static final String FIELD_NAME_UPPER_CHILL_WATER_DETA_SERT = "upperChillWaterDetaSet";

        @Schema(description = "供回水温/压差设定类型")
        private DtpRwdCentralCoolingInfoCodeEnum chillWaterDetaSetType;

        @Schema(description = "供回水温/压差单位")
        private String chillWaterDetaUnit;

        @Schema(description = "供回水温/压差文本名称")
        private String chillWaterDetaSetName;

        @Schema(description = "供回水温/压差下限")
        private BigDecimal lowerChillWaterDetaSet;

        @Schema(description = "供回水温/压差上限")
        private BigDecimal upperChillWaterDetaSet;
    }

}
