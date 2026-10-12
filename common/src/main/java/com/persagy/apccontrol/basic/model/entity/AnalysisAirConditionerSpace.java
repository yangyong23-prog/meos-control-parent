package com.persagy.apccontrol.basic.model.entity;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import com.persagy.apccontrol.basic.utils.range.RangeUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 运行分析-空调空间分析
 *
 * @TableName analysis_air_conditioner_spaces
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class AnalysisAirConditionerSpace {
    /**
     * 主键
     */
    private Long id;

    /**
     * 项目 ID
     */
    private String projectId;

    /**
     * 建筑 ID
     */
    private String buildingId;

    /**
     * 楼层id
     */
    private String floorId;

    /**
     * 空间id
     */
    private String spaceId;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 具体日期时间
     */
    private LocalDate day;

    /**
     * 标签类型
     */
    private RunningMonitorSecondTypeEnum tagType;

    /**
     * 是否有异常事件
     */
    private Boolean abnormalEvent;

    /**
     * 总时间数
     */
    private Integer timeNumber;

    /**
     * 达标时间数
     */
    private Integer satisfyTimeNumber;

    /**
     * 需要过滤信息点规则
     */
    private Set<String> infoCodeRule;

    /**
     * 营业时间
     */
    private LinkedHashSet<Range<LocalTime>> businessTime;

    /**
     * 时间标签数据
     */
    private List<TimeTagPO> timeTag;

    /**
     * 创建日期
     */
    private LocalDateTime createTime;

    @JsonSetter(Fields.businessTime)
    public void deserializeBusinessTime(
            LinkedHashSet<String> businessTime) {
        this.businessTime = RangeUtils.instanceByArray(businessTime,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }

    @JsonGetter(Fields.businessTime)
    public LinkedHashSet<String> serializeBusinessTime() {
        return RangeUtils.toString(this.businessTime,
                item -> item.format(DatePattern.NORM_TIME_FORMATTER), LinkedHashSet::new);
    }

    public void buildBusinessTime(LinkedHashSet<String> businessTime) {
        this.businessTime = RangeUtils.instanceByArray(businessTime,
                item -> LocalTime.parse(item, DatePattern.NORM_TIME_FORMATTER),
                LinkedHashSet::new);
    }


}