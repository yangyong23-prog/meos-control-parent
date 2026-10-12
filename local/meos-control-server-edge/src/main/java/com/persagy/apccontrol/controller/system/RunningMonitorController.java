package com.persagy.apccontrol.controller.system;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.QueryFanDTO;
import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import com.persagy.apccontrol.basic.model.dto.SystemDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.common.service.RunningMonitorService;
import com.persagy.apccontrol.controller.dto.LinkForceControlDTO;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
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
import java.util.TreeMap;

/**
 * 运行监测--2D系统图使用
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 14:22
 */
@Slf4j
@RestController("edgeRunningMonitorController")
@Tag(name = "运行监测")
@RequestMapping(value = "/running-monitor")
public class RunningMonitorController {

    @Autowired
    private RunningMonitorService runningMonitorService;

    @Operation(summary = "获取当前系统一键手自动切换所涉及到的设备类", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryManualAutoClassList")
    public R<List<JSONObject>> queryManualAutoClassList(@Validated @RequestBody SystemDTO systemDTO) {
        if (StrUtil.isBlank(systemDTO.getSystemId())) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_d2f13cb924cc4271b2846bdd55695bcd"));
        }
        List<JSONObject> resultList =
                this.runningMonitorService.queryManualAutoClassList(systemDTO.getSystemGraphId(),
                        systemDTO.getProjectId(), systemDTO.getSystemId());
        return R.success(resultList);
    }

    @Operation(summary = "获取手自动切换原因枚举类型", tags = {VersionConstant.V5_6_0})
    @PostMapping("/getManualAutoChangeReason")
    public R<TreeMap<String, String>> getManualAutoChangeReason(@Validated @RequestBody SystemDTO systemDTO) {
        return R.success(this.runningMonitorService.getManualAutoChangeReason(ContextUtil.getGroupCode(), systemDTO.getProjectId(), systemDTO.getSystemId()));
    }



    @Operation(summary = "修改当前对象控制模式", tags = {VersionConstant.V5_6_0})
    @PostMapping("/changeLinkForceControlStatus")
    public R<List<RcInfoCodeCfg>> changeLinkForceControlStatus(@Validated @RequestBody LinkForceControlDTO dto) {
        List<RcInfoCodeCfg> rcInfoCodeCfgList = this.runningMonitorService.changeLinkForceControlStatus(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(), dto.getObjectId(), dto.getInfoCode(), dto.getModel());
        return R.success(rcInfoCodeCfgList);
    }

    @Operation(summary = "查询冷却塔控制单元下的风机和水阀", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryFanAndWaterValveByCoolingTowerId")
    public R<List<JSONObject>> queryFanAndWaterValveByCoolingTowerId(@Validated @RequestBody QueryFanDTO dto) {
        List<JSONObject> relationObjectList = this.runningMonitorService.queryFanAndWaterValveListByCoolingTowerId(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(), dto.getCoolingTowerId());
        return R.success(relationObjectList);
    }

    @Operation(summary = "查询半剖图的信息点配置", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryClassPositionCodeConfigs")
    public R<List<JSONObject>> queryClassPositionCodeConfigs(@Validated @RequestBody SingleObjectDTO dto) {
        // 根据设备半剖图的配置，返回单设备对应的信息点分组数据
        return R.success(this.runningMonitorService.queryClassPositionCodeConfigs(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObjectId()));
    }

    @Operation(summary = "查询-清零设备类集合", tags = {VersionConstant.V5_6_0})
    @Deprecated
    @PostMapping("/listAccRuntimeClass-delete")
    public R<List<JSONObject>> listAccRuntimeClass(@Validated @RequestBody SystemDTO dto) {
        if (StrUtil.isBlank(dto.getSystemId())) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_d2f13cb924cc4271b2846bdd55695bcd"));
        }
        return R.success(this.runningMonitorService.listAccRuntimeClass(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(), dto.getSystemClassCode()));
    }

    @Operation(summary = "操作-清零某设备类的累计时长", tags = {VersionConstant.V5_6_0})
    @PostMapping("/clearAccRuntimeClass")
    public R<Boolean> clearAccRuntimeClass(@Validated @RequestBody SingleObjectDTO dto) {
        this.runningMonitorService.clearAccRuntimeClass(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObjectId(), dto.getClassCode());
        return R.success(true);
    }

}
