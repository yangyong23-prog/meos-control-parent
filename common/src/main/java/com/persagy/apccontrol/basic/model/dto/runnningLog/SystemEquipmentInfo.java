package com.persagy.apccontrol.basic.model.dto.runnningLog;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import io.jsonwebtoken.lang.Collections;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统设备信息
 */
@Data
@NoArgsConstructor
public class SystemEquipmentInfo {
    
    //一级分类
    private NowRunningLogCategory firstLevelCategory;
    
    //一级分类下的设备
    private List<? extends DtpRwdObjectDTO> objList;
    
    //系统对象 只有系统分类才会有
    private DtpRwdObjectDTO systemObject;
    
    
    public SystemEquipmentInfo(NowRunningLogCategory firstLevelCategory, List<? extends DtpRwdObjectDTO> objList) {
        this.firstLevelCategory = firstLevelCategory;
        this.objList = objList;
    }
    
    //构建对象 ID 所在的 系统
    public static SystemEquipmentInfo getSystemEquipmentInfoByObjectId(List<SystemEquipmentInfo> systemEquipmentInfoList,
                                                                       String objectId) {
        if (Collections.isEmpty(systemEquipmentInfoList)) {
            return null;
        }
        //取出包含设备对象的数据
        return systemEquipmentInfoList.stream()
                                      .filter(systemEquipmentInfo -> systemEquipmentInfo.getObjList() != null)
                                      .filter(systemEquipmentInfo -> systemEquipmentInfo.getObjList().stream()
                                                                                        .anyMatch(obj -> obj != null && objectId.equals(obj.getId())))
                                      .findFirst()
                                      .orElse(null);
    }
    
}
