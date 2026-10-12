package com.persagy.apccontrol.basic.service.runninglog.category;


import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 对象查询策略
 */
public interface RunningLogCategoryQueryObjStrategy {
    
    SystemEquipmentInfo queryObj(String objId, String classCode);
    
    /**
     * 构建但对象
     *
     * @param objId
     * @return
     */
    static List<DtpRwdObjectDTO> getDtpRwdObjectDTOS(String objId) {
        List<DtpRwdObjectDTO> objList = new ArrayList<>();
        DtpRwdObjectDTO obj = new DtpRwdObjectDTO();
        obj.setId(objId);
        objList.add(obj);
        return objList;
    }
}
