package com.persagy.apccontrol.basic.model.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.entity.AnalysisAirConditionerSpace;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.po.common.TimeTagPO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 运行分析-空调空间分析
 *
 * @TableName analysis_air_conditioner_spaces
 */
@TableName(value = "analysis_air_conditioner_space", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisAirConditionerSpacePO {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(value = "info_code_rule", typeHandler = JacksonTypeHandler.class)
    private Set<String> infoCodeRule;

    /**
     * 营业时间
     */
    @TableField(value = "business_time", typeHandler = JacksonTypeHandler.class)
    private LinkedHashSet<String> businessTime;

    /**
     * 时间标签数据
     */
    @TableField(value = "time_tag", typeHandler = JacksonTypeHandler.class)
    private Map<String, String> timeTag;

    /**
     * 创建日期
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;


    public static List<AnalysisAirConditionerSpacePO> toAnalysisAirConditionerSpacePOList(List<AnalysisAirConditionerSpace> analysis) {
        List<AnalysisAirConditionerSpacePO> space = new ArrayList<>();
        if (CollectionUtils.isEmpty(analysis)) {
            return space;
        }
        analysis.forEach(e -> {
            space.add(toAnalysisAirConditionerSpacePO(e));

        });
        return space;
    }

    public static AnalysisAirConditionerSpacePO toAnalysisAirConditionerSpacePO(AnalysisAirConditionerSpace e) {
        if (e == null) {
            return null;
        }
        AnalysisAirConditionerSpacePO v = AnalysisAirConditionerSpacePO.builder()
                .id(e.getId())
                .projectId(e.getProjectId())
                .buildingId(e.getBuildingId())
                .floorId(e.getFloorId())
                .spaceId(e.getSpaceId())
                .spaceName(e.getSpaceName())
                .day(e.getDay())
                .tagType(e.getTagType())
                .abnormalEvent(e.getAbnormalEvent())
                .timeNumber(e.getTimeNumber())
                .satisfyTimeNumber(e.getSatisfyTimeNumber())
                .infoCodeRule(e.getInfoCodeRule())
                .businessTime(e.serializeBusinessTime())
                .build();
        Map<String, String> customResultMap = new HashMap<>();
        for (TimeTagPO timeTagPO : e.getTimeTag()) {
            customResultMap.put(timeTagPO.serializeTime(), timeTagPO.getTag());
        }
        v.setTimeTag(customResultMap);
        return v;
    }

    public static List<AnalysisAirConditionerSpace> toAnalysisAirConditionerSpaceList(List<AnalysisAirConditionerSpacePO> analysis) {
        List<AnalysisAirConditionerSpace> space = new ArrayList<>();
        if (CollectionUtils.isEmpty(analysis)) {
            return space;
        }
        analysis.forEach(e -> {
            space.add(toAnalysisAirConditionerSpace(e));
        });
        return space;
    }

    public static AnalysisAirConditionerSpace toAnalysisAirConditionerSpace(AnalysisAirConditionerSpacePO e) {
        if (e == null) {
            return null;
        }
        AnalysisAirConditionerSpace po = AnalysisAirConditionerSpace.builder()
                .id(e.getId())
                .projectId(e.getProjectId())
                .buildingId(e.getBuildingId())
                .floorId(e.getFloorId())
                .spaceId(e.getSpaceId())
                .spaceName(e.getSpaceName())
                .day(e.getDay())
                .tagType(e.getTagType())
                .abnormalEvent(e.getAbnormalEvent())
                .timeNumber(e.getTimeNumber())
                .satisfyTimeNumber(e.getSatisfyTimeNumber())
                .infoCodeRule(e.getInfoCodeRule())
                .build();
        po.buildBusinessTime(e.getBusinessTime());
        List<TimeTagPO> timeTag = new ArrayList<>();
        e.getTimeTag().forEach((k, v) ->
                timeTag.add(TimeTagPO.builder()
                        .time(Range.between(LocalTime.parse(k.split(",")[0]), LocalTime.parse(k.split(",")[1])))
                        .tag(v)
                        .build()));
        po.setTimeTag(timeTag);
        return po;
    }
}