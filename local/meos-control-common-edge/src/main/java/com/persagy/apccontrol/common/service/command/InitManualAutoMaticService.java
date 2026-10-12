package com.persagy.apccontrol.common.service.command;


import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;

/**
 * 初始化需求手自动状态
 */
public interface InitManualAutoMaticService {


    /**
     * 初始化设备状态为自动状态
     */
    public void setManualAutoMatic(DemandTypeEnum type);


    /**
     * 初始化照明设备状态为自动状态
     */
    public void setDemandLightingManualAutoMatic();


    /**
     * 初始化冷源设备状态为自动状态
     */
    public void setColdSourceManualAutoMatic();


}
