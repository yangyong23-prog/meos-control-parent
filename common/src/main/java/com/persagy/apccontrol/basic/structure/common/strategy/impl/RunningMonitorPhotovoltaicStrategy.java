package com.persagy.apccontrol.basic.structure.common.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.structure.common.enumeration.InheritConfigTypeEnum;
import com.persagy.apccontrol.basic.structure.common.strategy.BaseRunningMonitorStrategy;
import com.persagy.apccontrol.basic.structure.common.strategy.RunningMonitorStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component(RunningMonitorPhotovoltaicStrategy.BEAN_NAME)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunningMonitorPhotovoltaicStrategy extends BaseRunningMonitorStrategy implements RunningMonitorStrategy {

    public static final String BEAN_NAME = "RunningMonitorPhotovoltaicStrategy";
    public static final String LOG_TAG = "运行监测-光伏";

    @Override
    public RunningMonitorTypeEnum runningMonitorType() {
        return RunningMonitorTypeEnum.PHOTOVOLTAIC;
    }

    @Override
    public InheritConfigTypeEnum demandInheritType() {
        return null;
    }

    @Override
    public List<? extends DtpRwdEquipmentDTO> listEquipments(@NonNull RunningMonitorEquipmentQueryParam param) {

        //todo 临时处理
        param.setFilterClassCodes(Sets.newHashSet(DtpRwdClassCodeEnum.SEEA.getValue()));
        List<DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = param.queryObject(new DtpRwdObjectQueryParam());
        if(CollectionUtil.isEmpty(dtpRwdEquipmentDTOS)){
            return dtpRwdEquipmentDTOS;
        }

        //查系统下设备
        Set<String> SystemIds = dtpRwdEquipmentDTOS.stream().map(DtpRwdEquipmentDTO::getId).collect(Collectors.toSet());
        param.setFilterEquipmentInSystemIds(SystemIds);
        param.setFilterClassCodes(Sets.newHashSet(
                DtpRwdClassCodeEnum.SEDGGI.getValue(),DtpRwdClassCodeEnum.SEDGBP.getValue(),
                DtpRwdClassCodeEnum.SEDGCB.getValue(),DtpRwdClassCodeEnum.SEDGSP.getValue(),
                DtpRwdClassCodeEnum.SEDGSC.getValue(),DtpRwdClassCodeEnum.SEDGGC.getValue(),
                DtpRwdClassCodeEnum.SETDIV.getValue(),DtpRwdClassCodeEnum.WESEMS.getValue()
                ));


        List<DtpRwdEquipmentDTO> subRwdEquipmentDTOS = param.queryObject(new DtpRwdObjectQueryParam());
        subRwdEquipmentDTOS.addAll(dtpRwdEquipmentDTOS);
        return subRwdEquipmentDTOS;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
