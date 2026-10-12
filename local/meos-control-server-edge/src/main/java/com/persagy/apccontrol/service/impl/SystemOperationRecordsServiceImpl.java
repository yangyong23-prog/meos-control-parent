package com.persagy.apccontrol.service.impl;

import com.persagy.apccontrol.common.outside.model.dto.DeviceDTO;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import com.persagy.apccontrol.common.outside.model.dto.LockDTO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.service.SystemOperationRecordsService;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author tzy
 */
@Slf4j
@Service
public class SystemOperationRecordsServiceImpl implements SystemOperationRecordsService {

    @Resource
    DtpTwinClientService dtpTwinClientService;

    /**
     * 切换设备锁定状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author tangzy
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean switchDeviceLockStatus(LockDTO dto) {
        dtpTwinClientService.switchEquipLock(dto.getProjectId(), ContextUtil.getGroupCode(), dto.getObjectId(), dto.getLock());
        return true;
    }

    /**
     * 修改设备强制状态
     *
     * @param dto
     * @return java.lang.Boolean
     * @author tangzy
     **/
    @Override
    public Boolean mandatoryState(DeviceDTO dto) {
        dtpTwinClientService.manualSend(DtpTwinManualSend.builder().objectId(dto.getObjectId()).infoCode(dto.getInfoCode()).value(dto.getValue()).build());
        return true;
    }

}