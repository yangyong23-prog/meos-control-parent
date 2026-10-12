package com.persagy.apccontrol.controller.system;


import com.persagy.apccontrol.common.outside.model.dto.DeviceDTO;
import com.persagy.apccontrol.common.outside.model.dto.LockDTO;
import com.persagy.apccontrol.service.SystemOperationRecordsService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author tzy
 */
@Tag(name = "本地系统运行记录")
@Validated
@RestController
@RequestMapping("system")
@Slf4j
public class SystemOperationRecordsController {

    @Resource
    private SystemOperationRecordsService systemOperationRecordsService;

    @Operation(summary = "切换设备锁定状态")
    @PostMapping("/switchDeviceLockStatus")
    public R<Boolean> switchDeviceLockStatus(@Valid @RequestBody LockDTO dto) {
        return R.success(systemOperationRecordsService.switchDeviceLockStatus(dto));
    }

    @Operation(summary = "修改设备强制状态")
    @PostMapping("/mandatoryState")
    public R<Boolean> mandatoryState(@Valid @RequestBody DeviceDTO dto) {
        return R.success(systemOperationRecordsService.mandatoryState(dto));
    }

}
