package com.persagy.apccontrol.basic.model.vo;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.EnumCalculateValueType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * 管控空间实体类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/12/7 19:51
 */
@Data
public class DemandControlSpaceVO implements Comparable<DemandControlSpaceVO> {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "对象名称")
    private String name;

    @Schema(description = "对象本地编码")
    private String localId;

    @Schema(description = "对象本地名称")
    private String localName;

    @Schema(description = "对象类型")
    private String objType;

    @Schema(description = "实例类型")
    private InstanceTypeEnum instanceType;

    @Schema(description = "是否自定义配置")
    private Boolean isCustomizedSetting;

    @Schema(description = "是否为自定义管控空间")
    private Boolean isCustomControlSpace;

    @Schema(description = "是否为管控空间")
    private Boolean isControlSpace;

    @Schema(description = "是否被屏蔽")
    private Boolean isShielded;

    @Schema(description = "系统ID")
    private Set<String> systemIds;

    @Schema(description = "序列ID")
    private Integer sequenceId;

    @Schema(description = "子节点")
    private List<DemandControlSpaceVO> children;

    @Schema(description = "楼层序列ID")
    private Integer floorSequenceId;

    private String floorId;

    private String floorLocalId;

    private String floorLocalName;

    private String buildingId;

    private String buildingLocalId;

    private String buildingLocalName;

    @Schema(description = "当前空间是否异常")
    private Boolean abnormalStatus;

    @Schema(description = "是否超上限")
    private Boolean upperLimit;

    @Schema(description = "是否超下限")
    private Boolean lowerLimit;

    @Schema(description = "是否偏离目标时长")
    private Boolean offsetTarget;

    @Schema(description = "偏离目标持续时长")
    private Integer offsetTargetTime;

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

    @Schema(description = "温度单位")
    private String tdbUnit;

    @Schema(description = "CO2单位")
    private String carbonUnit;

    @Schema(description = "温度实时数据")
    private String tdbCurrentData;

    @Schema(description = "CO2实时数据")
    private String carbonCurrentData;

    // 配置数据
    @Schema(description = "温度目标数据")
    private BigDecimal tdbTargetData;

    // 配置数据
    @Schema(description = "CO2目标数据")
    private BigDecimal carbonTargetData;

    /**************** 价值计算的数据 ***************/

    @Schema(description = "温度当前值")
    private BigDecimal tdbCurrentValue;

    @Schema(description = "温度当前值类型")
    private EnumCalculateValueType tdbCurrentValueType;

    @Schema(description = "CO2当前值")
    private BigDecimal carbonCurrentValue;

    @Schema(description = "CO2当前值类型")
    private EnumCalculateValueType carbonCurrentValueType;

    /*@Schema(description = "温度阈值")
    private BigDecimal tdbThresold;

    @Schema(description = "CO2阈值")
    private BigDecimal co2Thresold;*/

    @Override
    public int compareTo(@NotNull DemandControlSpaceVO o) {
        if (this.localId != null && o.getLocalId() != null) {
            return this.localId.compareTo(o.getLocalId());
        }
        return -1;
    }

}
