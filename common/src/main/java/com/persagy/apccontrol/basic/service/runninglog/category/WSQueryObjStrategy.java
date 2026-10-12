package com.persagy.apccontrol.basic.service.runninglog.category;

import com.google.common.collect.ImmutableSet;
import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 查询生活热水的设备
 * TODO 待第三方接入后替换查询逻辑
 */
@Service
public class WSQueryObjStrategy extends CategoryQueryObjStrategyAbstract {

    //过滤的设备类
    public static final Set<String> classList = ImmutableSet.of("WSDHHE", "WSDHHP");

    @Override
    public SystemEquipmentInfo queryObj(String objId, String classCode) {
        //不包含设备类不查询
        if (!classList.contains(classCode)) {
            return null;
        }

        // TODO 待第三方接入后替换查询逻辑
        return querySystemObj(objId, DtpRwdSystemClassCodeEnum.WATER_SUPPLY_HOT_SYSTEM, classCode,
                NowRunningLogCategory.StatusAndControl_WS);
    }

}
