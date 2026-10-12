package com.persagy.apccontrol.basic.service.runninglog.category;

import com.google.common.collect.ImmutableSet;
import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 查询空调冷源的设备
 */
@Service
public class ACACQueryObjStrategy extends CategoryQueryObjStrategyAbstract {
    
    //过滤的设备类
    public static final Set<String> classList = ImmutableSet.of(
            DtpRwdClassCodeEnum.ACCPC_GROUP.value(),
            DtpRwdClassCodeEnum.ACCCOP.value(),
            DtpRwdClassCodeEnum.ACCCOF.value(),
            DtpRwdClassCodeEnum.ACCCCP.value()
    );
    
    @Override
    public SystemEquipmentInfo queryObj(String objId, String classCode) {
        //不包含设备类不查询
        if (!classList.contains(classCode)) {
            return null;
        }
        
        return querySystemObj(objId, DtpRwdSystemClassCodeEnum.ACAC_SYSTEM, classCode,
                NowRunningLogCategory.airConditioningColdSource);
    }
    
    
}
