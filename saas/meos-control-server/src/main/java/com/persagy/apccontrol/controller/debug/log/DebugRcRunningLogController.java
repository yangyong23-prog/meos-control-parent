package com.persagy.apccontrol.controller.debug.log;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcRunningLog;
import com.persagy.apccontrol.basic.resource.RcRunningLogService;
import com.persagy.apccontrol.common.outside.MidPermissionService;
import com.persagy.apccontrol.common.outside.dto.GroupDTO;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 更新云端所有集团下的日志
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@RestController
@RequestMapping(value = "/debug/rcRunningLog")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DebugRcRunningLogController {

    private final RcRunningLogService rcRunningLogService;
    private final MidPermissionService midPermissionService;

    @GetMapping("/transfer")
    public R<Object> transfer() {
        List<GroupDTO> groupDTO = midPermissionService.queryValidGroupList();
        if (CollectionUtils.isEmpty(groupDTO)) {
            return R.success(true);
        }
        for (GroupDTO dto : groupDTO) {
            try {
                String groupCode = dto.getCode();
                List<ProjectDTO> projectList = midPermissionService.queryIsInUseProjectDTOList(groupCode);
                if (CollectionUtil.isEmpty(projectList)) {
                    continue;
                }
                ContextUtil.setGroupCode(groupCode);
                ContextUtil.setTenant(groupCode);

                LambdaUpdateWrapper<RcRunningLog> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getFirstLevelCategory, "ACCC").eq(RcRunningLog::getFirstLevelCategory, "coldstation");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getFirstLevelCategory, "ACCI").eq(RcRunningLog::getFirstLevelCategory, "cool_hot_system");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getFirstLevelCategory, "ACCH").eq(RcRunningLog::getFirstLevelCategory, "hotstation");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getFirstLevelCategory, "AIR_TERMINAL").eq(RcRunningLog::getFirstLevelCategory, "air_conditioning_terminal");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getSecondLevelCategory, "coldstation_device_exception").eq(RcRunningLog::getSecondLevelCategory, "order_fail_alarm_cold_result");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getSecondLevelCategory, "coldstation_device_exception").eq(RcRunningLog::getSecondLevelCategory, "online_status_cold_result");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getSecondLevelCategory, "hot_device_exception").eq(RcRunningLog::getSecondLevelCategory, "hot_order_fail_alarm_alarm");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getSecondLevelCategory, "hot_device_exception").eq(RcRunningLog::getSecondLevelCategory, "hot_online_status_alarm");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getThirdLevelCategory, "coldstation_order_fail_alarm_result").eq(RcRunningLog::getThirdLevelCategory, "order_fail_alarm_cold_alarm");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getThirdLevelCategory, "coldstation_online_status_result").eq(RcRunningLog::getThirdLevelCategory, "online_status_cold_alarm");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getThirdLevelCategory, "hot_order_fail_alarm_result").eq(RcRunningLog::getThirdLevelCategory, "hot_order_fail_alarm_alarm");
                this.rcRunningLogService.update(updateWrapper);

                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(RcRunningLog::getThirdLevelCategory, "hot_online_status_result").eq(RcRunningLog::getThirdLevelCategory, "hot_online_status_alarm");
                this.rcRunningLogService.update(updateWrapper);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ContextUtil.remove();
            }
        }
        return R.success(true);
    }

}
