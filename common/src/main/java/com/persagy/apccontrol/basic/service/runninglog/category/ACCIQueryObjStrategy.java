package com.persagy.apccontrol.basic.service.runninglog.category;

import com.google.common.collect.ImmutableSet;
import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import org.springframework.stereotype.Service;

import java.util.Set;


/**
 * 查询冷热源的设备
 */
@Service
public class ACCIQueryObjStrategy extends CategoryQueryObjStrategyAbstract {
    
    //过滤的设备类
    public static final Set<String> classList = ImmutableSet.of(
            DtpRwdClassCodeEnum.ACCHCH.value(),
            DtpRwdClassCodeEnum.ACCCOF.value(),
            DtpRwdClassCodeEnum.ACPPC_GROUP.value()
    );
    
    @Override
    public SystemEquipmentInfo queryObj(String objId, String classCode) {
        if (!classList.contains(classCode)) {
            return null;
        }
        return querySystemObj(objId, DtpRwdSystemClassCodeEnum.ACCI, classCode,
                NowRunningLogCategory.StatusAndControl_AC);
    }
    
    
}
