package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.convert.CalculateResultConditionConvert;
import com.persagy.apccontrol.basic.convert.CalculateResultConvert;
import com.persagy.apccontrol.basic.convert.LocalTimeConvert;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.calculate.CalculateResult;
import com.persagy.apccontrol.basic.model.calculate.CalculateResultCondition;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

/**
 * 价值计算结果
 *
 * @TableName rc_calculate_result
 */
@Data
@Accessors(chain = true)
@TableName(value = "rc_calculate_result", autoResultMap = true)
public class RcCalculateResult implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId
    private String id;

    private String groupCode;

    /**
     * 项目id
     */
    private String projectId;

    private String systemId;

    private String spaceId;

    private String spaceLocalId;

    private String spaceLocalName;

    /**
     * 日期 yyyyMMdd
     */
    private String workDate;

    /**
     * 开机时间 HH:mm
     */
    @TableField(value = "`work_time`", typeHandler = LocalTimeConvert.class)
    private List<LocalTime> workTime;

    /**
     * 季节 ACSeason-1：供冷季   ACSeason-2：供暖季    ACSeason-3：过渡季
     */
    private MidCalendarAcSeasonTypeEnum season;

    /**
     *     VALUE_SELECT_LOWEST_CAST("value_select_lowest_cast", "费用最低", "项目纬度"),
     *     VALUE_SELECT_LOWEST_CARBON_EMISSIONS("value_select_Lowest_carbon_emissions", "碳排放最低", "项目纬度"),
     *     VALUE_SELECT_LOWEST_ENERGY("value_select_lowest_energy", "能耗最低", "项目纬度"),
     *     VALUE_SELECT_QUALITY_FIRST("value_select_quality_first", "品质优先", "项目纬度"),
     */
    private BasicsConfigTypeEnum calculateType;

    /**
     * 价值计算的结果
     */
    @TableField(value = "`calculate_result`", typeHandler = CalculateResultConvert.class)
    private CalculateResult calculateResult;

    /**
     * 价值计算结果-每个时间点对应的相关前置数据
     */
    @TableField(value = "`extend_info`", typeHandler = CalculateResultConditionConvert.class)
    private CalculateResultCondition extendInfo;

    /**
     * 创建时间
     */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

}