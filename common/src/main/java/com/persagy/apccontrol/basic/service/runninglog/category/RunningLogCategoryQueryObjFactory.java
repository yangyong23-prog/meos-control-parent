package com.persagy.apccontrol.basic.service.runninglog.category;


import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 日志分类对象查询工厂
 */
@Service
public class RunningLogCategoryQueryObjFactory {
    
    
    @Resource
    private List<RunningLogCategoryQueryObjStrategy> runningLogCategoryQueryObjStrategylist;
    
    /**
     * 查询系统对象 通过对象 ID 和 classCode 优化查询
     *
     * @param objId
     * @param classCode
     * @return
     */
    public List<SystemEquipmentInfo> queryObj(String objId, String classCode) {
        List<SystemEquipmentInfo> objList = new ArrayList<>();
        //系统对象列表
        runningLogCategoryQueryObjStrategylist.forEach(service -> {
            SystemEquipmentInfo obj = service.queryObj(objId, classCode);
            if (obj != null) {
                objList.add(obj);
            }
            
        });
        return objList;
    }
    
    
}
