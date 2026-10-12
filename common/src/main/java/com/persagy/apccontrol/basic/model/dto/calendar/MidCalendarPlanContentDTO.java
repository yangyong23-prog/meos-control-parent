package com.persagy.apccontrol.basic.model.dto.calendar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.Map;

/**
 * 工作历计划内容DTO
 *
 * @author zhangqiankun
 * @version v5.7.0
 */
@Data
public class MidCalendarPlanContentDTO {

    /**
     * 时刻
     * <p>每天24小时, 按照5分钟进行分割, 总共分割288分, num的值的范围为 0~287
     * <p>ex: 要保持00:00~00:05的计划时, num就传0
     */
    @Range(min = 0, max = 287, message = "{key_0462c3f2440c4cd7a3d04426dd0000db}")
    private Integer num;

    /**
     * 需求目标数值
     */
    @JsonInclude
    private Map<String, Object> planContentMap;

    /**
     * 是否营业
     */
    public static final String OPENING = "Opening";

    /**
     * 空调需求时间
     */
    public static final String AC_AND_HEAT = "ACandHeat";

    /**
     * 新风需求时间
     */
    public static final String FRESH_AIR = "FreshAir";

    /**
     * 环境温度设定值
     */
    public static final String T_AVG = "T";

    /**
     * 环境温度目标值
     */
    public static final String T_TAR = "Ttarget";

    /**
     * 环境温度最大值
     */
    public static final String T_MAX = "Tmax";

    /**
     * 环境温度最小值
     */
    public static final String T_MIN = "Tmin";

    /**
     * CO2浓度设定值
     */
    public static final String CO2 = "CO2";

    /**
     * CO2浓度目标值
     */
    public static final String CO2_TAR = "CO2target";

    /**
     * CO2浓度最大值
     */
    public static final String CO2_MAX = "CO2max";

    /**
     * CO浓度目标值
     */
    public static final String CO_TAR = "COtarget";



}