package com.persagy.apccontrol.basic.controller.common;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.BatchObjectInfoCodeConfigDTO;
import com.persagy.apccontrol.basic.model.dto.SystemDTO;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceStatusQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.service.common.RunningMonitorService;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 运行监测控制器
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@RestController
@RequestMapping("/running-monitor")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Validated
public class RunningMonitorController {

    private final RunningMonitorService runningMonitorService;

    @Operation(summary = "统计空间关联设备手自动状态", tags = {VersionConstant.V5_2404_2})
    @PostMapping("/countSpaceRelatedEquipManualAutoStatus")
    public R<JSONObject> countSpaceRelatedEquipManualAutoStatus(@Validated @RequestBody LightingInstanceStatusQueryParam instanceParam) {
        JSONObject result = this.runningMonitorService.countSpaceRelatedEquipManualAutoStatus(instanceParam);
        return R.success(result);
    }

    @Operation(summary = "批量查询信息点值")
    @PostMapping("/batchQueryInfoCode")
    public R<List<InfoCodeValueVO>> listLatestContent(@RequestBody @Valid BatchObjectInfoCodeConfigDTO param) {
        return R.success(runningMonitorService.batchQueryInfoCode(param));
    }

    @Operation(summary = "查询-清零设备类集合", tags = {VersionConstant.V5_6_0})
    @PostMapping("/listAccRuntimeClass")
    public R<List<JSONObject>> listAccRuntimeClass(@Validated @RequestBody SystemDTO dto) {
        if (StrUtil.isBlank(dto.getSystemId())) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_d2f13cb924cc4271b2846bdd55695bcd"));
        }
        return R.success(this.runningMonitorService.listAccRuntimeClass(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(), dto.getSystemClassCode()));
    }
}
