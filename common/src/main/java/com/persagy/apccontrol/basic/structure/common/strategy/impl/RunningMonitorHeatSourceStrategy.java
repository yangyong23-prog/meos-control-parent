package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorSpaceQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.DemandInstanceValueObject;
import com.persagy.framework.common.context.ContextUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS;
import static com.persagy.apccontrol.basic.model.base.CommonConst.MONITORING_SURVEILLANCE_STATUS_LIST;
import static com.persagy.apccontrol.basic.service.duty.strategy.HeatSourceHomePageCardDataHandler.CLASS_CODE_LIST;
import static com.persagy.apccontrol.basic.service.duty.strategy.HeatSourceHomePageCardDataHandler.WITH_COLUMNS;

/**
 * 热源策略实现
 *
 * @author JiYinchuan
 * @since 5.241001.0
 * @version 2.0 since 5.241101.0
 */
@Component(RunningMonitorHeatSourceStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorHeatSourceStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "runningMonitorHeatSourceStrategy";
    private final DtpRwdClientService dtpRwdClientService;

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.HEAT_SOURCE;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return InheritConfigTypeEnum.FIELD;
    }


    @Override
    public List<DtpRwdSpaceDTO> listSpaces(@NonNull RunningMonitorSpaceQueryParam param) {
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
        queryParam.addWithColumnWithSimple(DtpRwdObjectDTO.Fields.id);
        List<DtpRwdSpaceDTO> spaces = param.queryObject(queryParam);
        if (CollectionUtils.isEmpty(spaces)) {
            return Collections.emptyList();
        }
        //过滤温度实例化的空间
        return filterInstantiateRuleToIntersection(spaces, Collections.singletonList(InfoCodeEnum.SPACE_TDB.getCode()));

    }


    @Override
    public List<DtpRwdSpaceDTO> listSpaces(
            @NonNull RunningMonitorSecondTypeEnum runningMonitorSecondType,
            @NonNull DemandInstanceValueObject instance,
            @Nullable String... withColumns) {
        return Collections.emptyList();
    }

    /**
     * 查询热源指定系统的热源类型
     *
     * @param withColumns 返回的字段
     * @param conditions  热源系统类的条件
     * @return 根据热源系统返回的热源系统实例信息
     */
    public List<DtpRwdObjectDTO> queryTheTypeOfSpecifiedHeatSource(ArrayList<String> withColumns, JSONObject conditions) {
        DtpRwdObjectQueryParam criteria = new DtpRwdObjectQueryParam();
        criteria.setClassCode(DtpRwdClassCodeEnum.HOT_SYSTEM.value());
        criteria.addInfoCode("heatSoureType", conditions)
                .addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST)
                .setWithColumns(withColumns);
        return dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), criteria);
    }

    /**
     * 获取热源系统关联的设备
     *
     * @param systemIds 系统id
     * @return 设备信息
     */
    public List<DtpRwdObjectDTO> getTheHeatSourceAssociatedWithAllDevices(Set<String> systemIds) {

        // 查询系统下的设备
        DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam()
                .setWithColumns(WITH_COLUMNS)
                .addWithColumn(DtpRwdObjectDTO.Fields.relObjIds);
        queryParam.setClassCodeEnum(CLASS_CODE_LIST);
        queryParam.addInfoCode(MONITORING_SURVEILLANCE_STATUS, MONITORING_SURVEILLANCE_STATUS_LIST);
        queryParam.setRelationTo(new DtpRwdObjectRelationQueryParam(
                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM).setObjFrom(systemIds));
        return dtpRwdClientService.listObject(
                false, false, ContextUtil.getProjectId(), queryParam);
    }


    @Override
    public String logTag() {
        return "运行检测-热源策略";
    }
}
