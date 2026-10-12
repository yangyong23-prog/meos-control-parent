package com.persagy.apccontrol.service;


import com.persagy.apccontrol.common.outside.model.dto.DeviceDTO;
import com.persagy.apccontrol.common.outside.model.dto.LockDTO;

/**
 * @author tzy
 */
public interface SystemOperationRecordsService {

    /**
     * 切换设备锁定状态
     * @author tangzy
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean switchDeviceLockStatus(LockDTO dto);

    /**
     * 修改设备强制状态
     * @author tangzy
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean mandatoryState(DeviceDTO dto);

}
