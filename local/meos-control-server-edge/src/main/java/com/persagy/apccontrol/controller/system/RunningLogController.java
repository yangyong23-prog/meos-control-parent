package com.persagy.apccontrol.controller.system;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.SystemLogDTO;
import com.persagy.apccontrol.basic.model.entity.RcSysLog;
import com.persagy.apccontrol.basic.resource.RcSysLogService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 运行监测--2D系统图使用
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 14:22
 */
@Slf4j
@RestController
@Tag(name = "运行监测-日志")
@RequestMapping(value = "/running-log")
public class RunningLogController {

    @Autowired
    private RcSysLogService rcSysLogService;

    @Operation(summary = "运行日志查询", tags = {VersionConstant.V5_7_0})
    @PostMapping("/queryRunningLogList")
    public R<List<RcSysLog>> queryRunningLogList(@Validated @RequestBody SystemLogDTO logDTO) {
        logDTO.setGroupCode(ContextUtil.getGroupCode());
        if (logDTO.getExcludePageBroadcast() == null) {
            logDTO.setExcludePageBroadcast(true);
        }
        List<RcSysLog> rcSysLogs = this.rcSysLogService.queryRunningLogList(logDTO, false);
        return R.success(rcSysLogs);
    }

}
