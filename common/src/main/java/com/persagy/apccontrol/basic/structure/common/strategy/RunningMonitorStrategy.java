package com.persagy.apccontrol.basic.structure.common.strategy;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSystemQueryParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * 运行监测类型策略
 *
 * <p>------------------------- 公共方法 -------------------------
 * <ul>
 *     <li><b>runningMonitorType</b> - 运行监测类型</li>
 *     <li><b>demandInheritType</b> - 需求继承类型</li>
 *     <li><b>listSpaces</b> - 查询空间</li>
 *     <li><b>listEquipments</b> - 查询设备</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
public interface RunningMonitorStrategy extends LogComponent {

    ArrayList<String> RISK_EVENT_LEVEL_SECOND_LIST = Lists.newArrayList(RiskMeasureAimLevel2.E5.getCode(), RiskMeasureAimLevel2.E34.getCode(),RiskMeasureAimLevel2.E35.getCode());

    /**
     * 运行监测类型
     *
     * @return 信息点类型
     * @since 5.240702.0
     */
    RunningMonitorTypeEnum runningMonitorType();

    /**
     * 需求继承类型
     *
     * @return 需求继承类型
     * @since 5.240702.0
     */
    InheritConfigTypeEnum demandInheritType();

    /**
     * 查询空间
     *
     * @param param 空间查询参数
     * @return 二级类型下的空间
     * @since 7.250201.0
     */
    List<DtpRwdSpaceDTO> listSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param);

    /**
     * 查询空间
     *
     * @param runningMonitorSecondType 运行监测二级类型
     * @param instance                 实例对象, 查询某个层级下的空间
     * @param withColumns              查询字段, 为空时查询标准字段
     * @return 二级类型下的空间
     * @since 5.240702.0
     * @deprecated {{@link #listSpaces(RunningMonitorSpaceQueryParam)}}
     */
    @Deprecated
    default List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return Lists.newArrayList();
    }

    /**
     * 查询Dashboard空间
     *
     * @param param 查询参数
     * @return Dashboard空间
     * @since 7.250201.0
     */
    List<? extends DtpRwdSpaceVO> listDashboardSpaces(@NonNull @Valid RunningMonitorSpaceQueryParam param);

    /**
     * 查询Dashboard系统
     *
     * @param param 查询参数
     * @return Dashboard系统
     * @since 7.250302.0
     */
    List<? extends DtpRwdObjectDTO> listDashboardSystems(@NonNull @Valid RunningMonitorSystemQueryParam param);

    /**
     * 查询Dashboard空间
     *
     * @param runningMonitorSecondType 运行监测二级类型
     * @param instance                 实例对象, 查询某个层级下的空间
     * @param withColumns              查询字段, 为空时查询标准字段
     * @return 二级类型下的空间
     * @since 5.241201.0
     * @deprecated {@link #listDashboardSpaces(RunningMonitorSpaceQueryParam)}
     */
    @Deprecated
    List<? extends DtpRwdSpaceVO> listDashboardSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns);

    /**
     * 查询设备
     *
     * @param param 设备查询参数
     * @return 设备
     * @since 7.250201.0
     */
    List<? extends DtpRwdEquipmentDTO> listEquipments(@NonNull @Valid RunningMonitorEquipmentQueryParam param);

    /**
     * 查询设备
     *
     * @param runningMonitorType 运行监测类型
     * @param instance           实例对象, 查询某个层级下的空间
     * @param withColumns        查询字段, 为空时查询标准字段
     * @return 设备
     * @since 5.241201.0
     * @deprecated {{@link #listEquipments(RunningMonitorEquipmentQueryParam)}}
     */
    @Deprecated
    default List<? extends DtpRwdEquipmentVO> listEquipments(
            @NonNull RunningMonitorTypeEnum runningMonitorType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return new ArrayList<>();
    }

    /**
     * 异常事件叠加条件
     *
     * @return 叠加条件，无风控目标为null
     */
    default Function<List<ExceptionEventBaseInfoVO>, List<ExceptionEventBaseInfoVO>> exceptionFilterCondition() {
        return null;
    }

    /**
     * 二级风控目标
     *
     * @return {@code Collection<RiskMeasureAimLevel2> }
     * @since 7.250701.0
     */
    default Collection<RiskMeasureAimLevel2> exceptionFilterRiskMeasureAimLevel2() {
        return null;
    }

    /**
     * 如果需要有指定的查询条件则在策略中实现逻辑
     *
     * @param infoCodeObjectQueryParam 查询条件增加逻辑
     */
    default void setExceptionEventObjectQueryParam(InfoCodeObjectQueryParam infoCodeObjectQueryParam) {
    }
}
