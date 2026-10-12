package com.persagy.apccontrol.basic.model.terminal;

import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.EnumCalculateValueType;
import com.persagy.apccontrol.basic.model.enumtype.EnumSpaceBusinessModel;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 云端--异常空间列表--异常数据
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/6 16:38
 */
@Data
public class AbnormalResultDetail {

    @Schema(description = "是否有异常")
    private Boolean enable;

    @Schema(description = "当前空间名称")
    private String localName;

    /**
     * 季节 ACSeason-1：供冷季   ACSeason-2：供暖季    ACSeason-3：过渡季
     */
    private MidCalendarAcSeasonTypeEnum season;

    /**
     * 当前时间点对应的营业时间的时间模式
     */
    private EnumSpaceBusinessModel businessModel;

    /**
     * 价值计算方向
     *
     *     VALUE_SELECT_LOWEST_CAST("value_select_lowest_cast", "费用最低", "项目纬度"),
     *     VALUE_SELECT_LOWEST_CARBON_EMISSIONS("value_select_Lowest_carbon_emissions", "碳排放最低", "项目纬度"),
     *     VALUE_SELECT_LOWEST_ENERGY("value_select_lowest_energy", "能耗最低", "项目纬度"),
     *     VALUE_SELECT_QUALITY_FIRST("value_select_quality_first", "品质优先", "项目纬度"),
     */
    private BasicsConfigTypeEnum calculateType;

    @Schema(description = "是否超上限")
    private Boolean upperLimit;

    @Schema(description = "是否超下限")
    private Boolean lowerLimit;

    @Schema(description = "是否偏离目标时长")
    private Boolean offsetTarget;

    @Schema(description = "偏离目标持续时长")
    private Integer offsetTargetTime;

    @Schema(description = "是否被屏蔽")
    private Boolean isShielded;

    /****************  配置数据 **********************/
    @Schema(description = "需求配置的可持续调度时长")
    private BigDecimal continuousScheduledHour;

    @Schema(description = "当前时间点的IOT数据")
    private BigDecimal currentData;

    @Schema(description = "当前时间点的目标数据")
    private BigDecimal targetData;

    @Schema(description = "当前时间点的上限数据")
    private BigDecimal upperData;

    @Schema(description = "当前时间点的下限数据")
    private BigDecimal lowerData;

    /**************** 价值计算的数据 ***************/

    @Schema(description = "温度当前值")
    private BigDecimal tdbCurrentValue;

    @Schema(description = "温度当前值类型")
    private EnumCalculateValueType tdbCurrentValueType;

    @Schema(description = "CO2当前值")
    private BigDecimal carbonCurrentValue;

    @Schema(description = "CO2当前值类型")
    private EnumCalculateValueType carbonCurrentValueType;

}
