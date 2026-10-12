package com.persagy.apccontrol.common.service;

public interface SpatialAnomalySetService {


    /**
     * 获取空间异常状态
     *
     * @param objectId
     */
    String getSpatialAnomaly(String objectId,String infoCode,String mark);


}
