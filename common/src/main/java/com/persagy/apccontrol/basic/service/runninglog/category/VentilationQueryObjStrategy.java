package com.persagy.apccontrol.basic.service.runninglog.category;

import com.google.common.collect.ImmutableSet;
import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.param.common.RunningMonitorEquipmentQueryParam;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorVentilationStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 查询通风的设备
 */
@Service
public class VentilationQueryObjStrategy extends CategoryQueryObjStrategyAbstract {
    
    //过滤的设备类
    public static final Set<String> classList = ImmutableSet.of(
            DtpRwdClassCodeEnum.FFSCAF.value(),
            DtpRwdClassCodeEnum.FFSCMF.value(),
            DtpRwdClassCodeEnum.ACVTSF.value(),
            DtpRwdClassCodeEnum.CFCSWN.value(),
            DtpRwdClassCodeEnum.CFCSEW.value()
    );
    
    @Resource
    private RunningMonitorVentilationStrategy runningMonitorVentilationStrategy;
    
    @Override
    public SystemEquipmentInfo queryObj(String objId, String classCode) {
        
        //不包含设备类不查询
        if (!classList.contains(classCode)) {
            return null;
        }
        //查询设备信息
        RunningMonitorEquipmentQueryParam equipmentQueryParam = new RunningMonitorEquipmentQueryParam();
        equipmentQueryParam.setFilterClassCodes(classList);
        equipmentQueryParam.addWithColumn(DtpRwdObjectDTO.Fields.id);
        List<? extends DtpRwdEquipmentDTO> dtpRwdEquipmentDTOS = runningMonitorVentilationStrategy.listEquipments(equipmentQueryParam);

        if (CollectionUtils.isEmpty(dtpRwdEquipmentDTOS)) {
            return null;
        }
        //一级分类 对象
        return new SystemEquipmentInfo(NowRunningLogCategory.ventilatexplantrunmonitor,
                dtpRwdEquipmentDTOS);
    }
    
}
