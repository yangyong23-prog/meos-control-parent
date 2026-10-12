package com.persagy.apccontrol.basic.structure.bizcheck.model.dto;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAirSeasonTypeEnum;
import com.persagy.apccontrol.basic.structure.bizcheck.enumeration.BizCheckStatusEnum;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import lombok.Data;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 工作历检查结果
 *
 * @author JiYinchuan
 * @since 7.260301.0
 */
@Data
public class WorkCalendarCheckDTO {

    /**
     * 日期 -> 系统营业时间检查
     */
    private Map<LocalDate, List<SystemBusinessCheckDTO>> date2SystemBusiness;

    /**
     * 系统营业时间检查结果
     */
    private BizCheckStatusEnum systemBusinessStatus;

    /**
     * 日期 -> 空调季检查
     */
    private Map<LocalDate, MidCalendarAirSeasonTypeEnum> date2AirSeasonType;

    /**
     * 空调季检查结果
     */
    private BizCheckStatusEnum airSeasonStatus;

    /**
     * 日期 -> 空调工况检查
     */
    private Map<LocalDate, MidCalendarAcSeasonTypeEnum> date2AcSeasonType;

    /**
     * 空调工况检查结果
     */
    private BizCheckStatusEnum acSeasonStatus;

    /**
     * 是否异常
     *
     * @return 是否异常
     * @since 7.260301.0
     */
    @JsonIgnore
    public boolean anyException() {
        return this.systemBusinessStatus == BizCheckStatusEnum.EXCEPTION
                || this.airSeasonStatus == BizCheckStatusEnum.EXCEPTION
                || this.acSeasonStatus == BizCheckStatusEnum.EXCEPTION;
    }

    /**
     * 系统营业时间检查
     *
     * @author JiYinchuan
     * @since 7.260301.0
     */
    @Data
    public static class SystemBusinessCheckDTO {

        /**
         * 对象
         */
        private DtpRwdObjectDTO object;

        /**
         * 空间营业时间信息列表
         */
        private List<Range<LocalTime>> spaceBusinessHours;


        @JsonGetter("spaceBusinessHours")
        public List<String> serializeSpaceBusinessHours() {
            return RangeUtils.toString(this.spaceBusinessHours,
                    item -> item.format(DatePattern.NORM_TIME_FORMATTER), ArrayList::new);
        }

        @JsonSetter("spaceBusinessHours")
        public void deserializeSpaceBusinessHours(List<String> spaceBusinessHours) {
            this.spaceBusinessHours = RangeUtils.instanceByArray(spaceBusinessHours,
                    item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER), ArrayList::new);
        }


    }
}