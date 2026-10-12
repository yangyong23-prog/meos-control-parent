package com.persagy.apccontrol.basic.structure.airterminal.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalEquipmentGraphVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalFloorEquipmentTypeStatisticsVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceGraphVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.AirTerminalSpaceWithStatusVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * 空调末端转换器
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AirTerminalConverter extends BaseConvert {

    // ------------------------- To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "controlled", ignore = true)
    @Mapping(target = "unableMonitor", ignore = true)
    @Mapping(target = "controlledRelEquipmentIds", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "tdbRule", ignore = true)
    @Mapping(target = "co2Rule", ignore = true)
    AirTerminalSpaceGraphVO toSpaceGraphVo(DtpRwdSpaceDTO source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "controlled", ignore = true)
    @Mapping(target = "tag", ignore = true)
    @Mapping(target = "controlledAirHandlingUnitIds", expression = "java(toControlledAirHandlingUnitIds(source))")
    @Mapping(target = "monitoringSurveillanceStatus", expression = "java(source.analyzeProperties(\"monitoringSurveillanceStatus\"))")
    AirTerminalEquipmentGraphVO toEquipmentGraphVo(DtpRwdEquipmentDTO source);

    /**
     * 将内部空间设备图结果转换为接口专用响应。
     *
     * <p>目标类型不声明模型视图字段及空间温度、二氧化碳规则标记，因此转换结果不会向
     * {@code /airTerminal/listSpaceEquipment} 透传这些非当前接口字段。</p>
     *
     * @param source 内部空间设备图结果，可以为空
     * @return 不包含模型视图字段及温度、二氧化碳规则标记的接口响应；源结果为空时返回空
     * @since v7.260701.0
     */
    @Nullable AirTerminalSpaceEquipmentVO toSpaceEquipmentVo(
            @Nullable AirTerminalSpaceWithStatusVO source);

    /**
     * 将内部空间图对象转换为空间设备接口空间对象。
     *
     * <p>嵌套返回类型的 JSpecify 注解必须标在 simple-name 上；标在外层限定符上会
     * 中断当前 Java 8 注解处理链。</p>
     *
     * @param source 内部空间图对象
     * @return 不包含模型视图字段及温度、二氧化碳规则标记的空间对象
     * @since v7.260701.0
     */
    AirTerminalSpaceEquipmentVO.@NonNull SpaceVO toSpaceEquipmentSpaceVo(
            @NonNull AirTerminalSpaceGraphVO source);

    /**
     * 将内部设备图对象转换为空间设备接口设备对象。
     *
     * @param source 内部设备图对象
     * @return 不包含模型视图字段的设备对象
     * @since v7.260701.0
     */
    AirTerminalSpaceEquipmentVO.@NonNull EquipmentVO toSpaceEquipmentEquipmentVo(
            @NonNull AirTerminalEquipmentGraphVO source);

    default Set<String> toControlledAirHandlingUnitIds(DtpRwdEquipmentDTO source) {
        if (!DtpRwdClassCodeEnum.ACAHG_GROUP.value().equals(source.getClassCode())) {
            return null;
        }
        return source.getRelObjIds();
    }

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240502.0
     */
    @Mapping(target = "relFloors", ignore = true)
    @Mapping(target = "equipmentStatistics", ignore = true)
    @Mapping(target = "equipmentGroupStatistics", ignore = true)
    AirTerminalFloorEquipmentTypeStatisticsVO toFloorEquipmentTypeStatisticsVo(DtpRwdObjectDTO source);

}
