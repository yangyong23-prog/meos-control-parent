package com.persagy.apccontrol.basic.service.runninglog.category;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.impl.InfoCodeGatewayImpl;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeObjectQueryParam;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 查询厨房排油烟的设备
 */
@Service
public class KitChenRangeHoodQueryObjStrategy extends CategoryQueryObjStrategyAbstract {
    
    //过滤的设备类
    public static final Set<String> classList = ImmutableSet.of(
            DtpRwdClassCodeEnum.ACKLSF.value(),
            DtpRwdClassCodeEnum.ACATFU.value(),
            DtpRwdClassCodeEnum.ACVTSF.value(),
            DtpRwdClassCodeEnum.ACKLAP.value()
    );
    
    @Resource
    private InfoCodeGatewayImpl infoCodeGateway;
    
    @Override
    public SystemEquipmentInfo queryObj(String objId, String classCode) {
        
        //不包含设备类不查询
        if (!classList.contains(classCode)) {
            return null;
        }
        //查询设备信息
        InfoCodeObjectQueryParam equipmentQueryParam = new InfoCodeObjectQueryParam();
        equipmentQueryParam.setClassCodes(classList);
        equipmentQueryParam.setRunningMonitorSecondType(RunningMonitorSecondTypeEnum.KITCHEN_RANGE_HOOD);
        equipmentQueryParam.setRunningMonitorType(RunningMonitorTypeEnum.KITCHEN_RANGE_HOOD);
        equipmentQueryParam.setTemplateTypes(Lists.newArrayList(InfoCodeTemplateTypeEnum.TABLE_LIST));
        List<DtpRwdObjectDTO> dtpRwdEquipmentDTOS = infoCodeGateway.listTemplateTypeObjects(equipmentQueryParam);
        //根据id过滤
        dtpRwdEquipmentDTOS = dtpRwdEquipmentDTOS.stream().filter(dtpRwdObjectDTO -> dtpRwdObjectDTO.getId().equals(objId)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(dtpRwdEquipmentDTOS)) {
            return null;
        }
        //一级分类 对象
        return new SystemEquipmentInfo(NowRunningLogCategory.StatusAndControl_KEF,
                dtpRwdEquipmentDTOS);
    }
    
}
