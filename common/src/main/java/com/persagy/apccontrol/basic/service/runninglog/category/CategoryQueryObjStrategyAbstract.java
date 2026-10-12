package com.persagy.apccontrol.basic.service.runninglog.category;


import com.persagy.apccontrol.basic.model.dto.runnningLog.SystemEquipmentInfo;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.NowRunningLogCategory;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;

import javax.annotation.Resource;

/**
 * 抽象策略
 */
public abstract class CategoryQueryObjStrategyAbstract implements RunningLogCategoryQueryObjStrategy {
    
    @Resource
    private DtpRwdClientService dtpRwdClientService;
    
    /**
     * 查询设备服务的系统
     */
    SystemEquipmentInfo querySystemObj(String objId,
                                       DtpRwdSystemClassCodeEnum systemClassCode,
                                       String classCode,
                                       NowRunningLogCategory category) {
        //设备服务的系统
        DtpRwdObjectDTO systemObject = null;
        //如果本身是系统 就不查询直接构建
        if (systemClassCode.getValue().equals(classCode)) {
            systemObject = dtpRwdClientService.getObject(
                    new DtpRwdObjectQueryParam().setId(objId)
                                                .addWithColumn(DtpRwdObjectDTO.Fields.id,
                                                        DtpRwdObjectDTO.Fields.localName,
                                                        DtpRwdObjectDTO.Fields.classCode));
        } else {
            //查询系统信息
            systemObject = dtpRwdClientService.getObject(
                    new DtpRwdObjectQueryParam()
                            .setClassCode(systemClassCode.getValue())
                            .setRelationFrom(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQ_IN_SYSTEM)
                                    .setObjTo(objId))
                            .addWithColumn(DtpRwdObjectDTO.Fields.id,
                                    DtpRwdObjectDTO.Fields.localName,
                                    DtpRwdObjectDTO.Fields.classCode));
        }
        //设备没有匹配上系统直接返回
        if (systemObject == null) {
            return null;
        }
        //构建基本规则信息
        SystemEquipmentInfo r = new SystemEquipmentInfo(category,
                RunningLogCategoryQueryObjStrategy.getDtpRwdObjectDTOS(objId));
        r.setSystemObject(systemObject);
        return r;
    }
    
    
}
