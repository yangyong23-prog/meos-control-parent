package com.persagy.apccontrol.basic.service.runninglog.category;

import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RunningLog;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogSourceTypeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RunningLogCategoryServiceImpl implements RunningLogCategoryService {
    
    @Resource
    private RunningLogCategoryQueryObjFactory runningLogCategoryQueryObjFactory;
    
    @Override
    public void buildLogCategory(RunningLog runLog) {
        
        if (runLog == null) {
            return;
        }
        // dtp-meos 分类设置
        if (RunningLogSourceTypeEnum.isDtpMeos(runLog.getSourceType())) {
            dtpMeos(runLog);
            return;
        }
        //twin 分类设置 不会带系统信息需要单独查询
        if (RunningLogSourceTypeEnum.isTwin(runLog.getSourceType())) {
            twin(runLog);
        }
    }
    
    /**
     * twin 服务匹配日志分类
     *
     * @param runLog
     */
    private void twin(RunningLog runLog) {
        
        //查询对象信息
        List<SystemEquipmentInfo> systemEquipmentInfoList =
                runningLogCategoryQueryObjFactory.queryObj(runLog.getObjectId(), runLog.getClassCode());
        
        SystemEquipmentInfo category = SystemEquipmentInfo.getSystemEquipmentInfoByObjectId(systemEquipmentInfoList,
                runLog.getObjectId());
        if (category != null) {
            //一级分类
            runLog.setFirstLevelCategory(category.getFirstLevelCategory());
            //如果是系统类型 填充系统对象信息 二级分类是系统 ID
            if (NowRunningLogCategory.getSystemCategoryByClassCode().containsKey(runLog.getFirstLevelCategory().getClassCode())) {
                DtpRwdObjectDTO systemObject = category.getSystemObject();
                if (systemObject == null) {
                    //系统不存在不进行记录
                    return;
                }
                runLog.setSystemId(systemObject.getId());
                runLog.setSystemName(systemObject.getLocalName());
                runLog.setSystemClassCode(systemObject.getClassCode());
            }
            //二级分类
            runLog.buildSecondLevelCategory();
            
        }
    }
    
    /**
     * dtp-meos 服务匹配日志分类
     *
     * @param runLog
     */
    private void dtpMeos(RunningLog runLog) {
        
        //判断是否是系统类型
        NowRunningLogCategory firstLevelCategory =
                NowRunningLogCategory.getSystemCategoryByClassCode().get(runLog.getSystemClassCode());
        if (firstLevelCategory != null) {
            //一级分类
            runLog.setFirstLevelCategory(firstLevelCategory);
            runLog.buildSecondLevelCategory();
            return;
        }
        //非系统类处理
        // 通过系统编码获取二级分类  SystemClassCode = modelClass 是前端传递的一级分类
        runLog.setFirstLevelCategory(NowRunningLogCategory.getEnumByCode(runLog.getSystemClassCode()));
        runLog.buildSecondLevelCategory();
        
        //其它的没有系统 去掉系统 ID 和系统类型
        runLog.setSystemId(null);
        runLog.setSystemClassCode(null);
    }
}
