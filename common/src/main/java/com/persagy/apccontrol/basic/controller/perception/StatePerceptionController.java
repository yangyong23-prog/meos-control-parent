package com.persagy.apccontrol.basic.controller.perception;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.abstracts.AbstractRunningMonitorServer;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.*;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.RoomTempStatisticsVO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
@Tag(name = "运行监测-2D系统图")
@RequestMapping(value = "/running-monitor")
public class StatePerceptionController {

    @Autowired
    private AbstractRunningMonitorServer abstractRunningMonitorServer;
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Operation(summary = "查询-3D模型下单空间下的所有设备", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryObjectGroupInfoBySystemId")
    public R<JSONObject> queryObjectGroupInfoBySystemId(@Validated @RequestBody SystemDTO param) throws Exception {
        if (param.getSystemClassCode() == null) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_557bfb58f8d745409971af07def19556"));
        }
        JSONObject objectGroupList = this.abstractRunningMonitorServer.queryObjectGroupInfoBySystemId(ContextUtil.getGroupCode(), param.getProjectId(), param.getSystemId(), param.getSystemClassCode(), param.getGraphTypeCode());
        return R.success(objectGroupList);
    }

    @Operation(summary = "查询-系统运行参数", tags = {VersionConstant.V5_6_0})
    @PostMapping("/querySystemRunningParam")
    public R<JSONObject> querySystemRunningParam(@Validated @RequestBody SystemDTO dto) {
        JSONObject objectInfo = this.abstractRunningMonitorServer.querySystemRunningParam(
                ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(),
                dto.getSystemClassCode(), dto.getGraphTypeCode(), dto.getImageOrProductName());
        return R.success(objectInfo);
    }

    @Operation(summary = "查询-设备的信息点配置信息", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryInfoCodeConfigByObjectIds")
    public R<Map<String, JSONObject>> queryInfoCodeConfigByObjectIds(@Validated @RequestBody InfoCodeConfigDTO dto) {
        Map<String, JSONObject> objectInfoMap = this.abstractRunningMonitorServer.queryInfoCodeConfigByObjectIds(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(), dto.getObjectIds(), false);
        return R.success(objectInfoMap);
    }

    @Operation(summary = "查询-单个设备的信息点配置信息", tags = {VersionConstant.V5_18_0})
    @PostMapping("/queryInfoCodeConfigByOneObjectId")
    public R<JSONObject> queryInfoCodeConfigByOneObjectId(@Validated @RequestBody QueryOneInfoCodeConfigDTO dto) {
        Map<String, JSONObject> objectInfoMap = this.abstractRunningMonitorServer.queryInfoCodeConfigByObjectIds(
                ContextUtil.getGroupCode(),
                dto.getProjectId(),
                dto.getSystemId(),
                new HashSet<String>() {{
                    add(dto.getObjectId());
                }}, false);
        if (MapUtils.isNotEmpty(objectInfoMap)) {
            JSONObject jsonObject = objectInfoMap.get(dto.getObjectId());
            if (Objects.nonNull(jsonObject)) {
                return R.success(jsonObject);
            }
        }
        return R.success(null);
    }

    @Operation(summary = "查询单个设备的信息点配置信息", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryInfoCodeConfigByObjectId")
    public R<List<RcInfoCodeCfg>> queryInfoCodeConfigByObjectId(@Validated @RequestBody ObjectInfoCodeConfigDTO dto) {
        if (StrUtil.isBlank(dto.getProjectId())) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_79ae698d8ca34f22a0784247c6543796"));
        }
        List<RcInfoCodeCfg> objectInfoMap = this.abstractRunningMonitorServer.queryInfoCodeConfigByObjectId(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObjectId(), null, dto.getInfoCodes());
        return R.success(objectInfoMap);
    }

    @Operation(summary = "查询多个设备的信息点配置信息", tags = {VersionConstant.V5_6_0})
    @PostMapping("/batchQueryInfoCodeConfig")
    public R<Map<String, List<RcInfoCodeCfg>>> batchQueryInfoCodeConfig(@Validated @RequestBody BatchObjectInfoCodeConfigDTO dto) throws Exception {
        Map<String, List<RcInfoCodeCfg>> objectInfoMap = this.abstractRunningMonitorServer.batchQueryInfoCodeConfigByObjectId(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObject());
        return R.success(objectInfoMap);
    }

    @Operation(summary = "查询设备的运行状态", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryRunningStatusByObjectIds")
    public R<JSONObject> queryRunningStatusByObjectIds(@Validated @RequestBody InfoCodeConfigDTO dto) {
        JSONObject objectInfoMap = this.abstractRunningMonitorServer.queryRunningStatusByObjectIds(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObjectIds());
        return R.success(objectInfoMap);
    }

    @Operation(summary = "查询设备的信息点值", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryInfoCodeValueByObjectIds")
    public R<JSONObject> queryInfoCodeValueByObjectIds(@Validated @RequestBody InfoCodeConfigDTO dto) {
        JSONObject objectInfoMap = this.abstractRunningMonitorServer.queryInfoCodeValueByObjectIds(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObjectIds());
        return R.success(objectInfoMap);
    }

    @Operation(summary = "查询-风机对应的冷却塔控制单元的信息", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryCoolingTowerByFanId")
    public R<JSONObject> queryCoolingTowerByFanId(@Validated @RequestBody SingleObjectDTO dto) {
        JSONObject relationObject = this.abstractRunningMonitorServer.queryCoolingTowerByFanId(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getSystemId(), dto.getObjectId());
        return R.success(relationObject);
    }

    @Operation(summary = "查询-水泵对应的泵组信息", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryPumpGroupByWaterPump")
    public R<RcInfoCodeGroupDTO> queryPumpGroupByWaterPump(@Validated @RequestBody SingleObjectDTO dto) {
        RcInfoCodeGroupDTO relationObject = this.abstractRunningMonitorServer.queryPumpGroupByWaterPump(ContextUtil.getGroupCode(), dto.getProjectId(), dto.getObjectId());
        return R.success(relationObject);
    }

    @Operation(summary = "查询室内温度统计数据", tags = {VersionConstant.V5_7_0})
    @PostMapping("/queryRoomTempStatistics")
    public R<List<RoomTempStatisticsVO>> queryRoomTempStatistics(@Validated @RequestBody SystemDTO param) {
        return R.success(this.abstractRunningMonitorServer.queryRoomTempStatistics(ContextUtil.getGroupCode(), param.getProjectId(), param.getSystemId()));
    }

    @Operation(summary = "查询室内温度范围配置", tags = {VersionConstant.V5_7_0})
    @PostMapping("/queryTempRange")
    public R<RcBasicsConfig> queryTempRange(@Validated @RequestBody SystemDTO param) {
        return R.success(this.abstractRunningMonitorServer.queryTempRange(param.getProjectId()));
    }

    @Operation(summary = "异常事件列表", tags = {VersionConstant.V5_7_0})
    @PostMapping("/queryAbnormalEventList")
    public R<Collection<ExceptionEventBaseInfoVO>> queryAbnormalEventList(@Validated @RequestBody SystemGraphDTO param) {
        List<ExceptionEventBaseInfoVO> eventInfoDTOS = null;
        try {
            eventInfoDTOS = this.abstractRunningMonitorServer.queryAbnormalEventList(ContextUtil.getGroupCode(), param.getProjectId(), param.getSystemObjectId(), param.getClassCode(), param.getGraphTypeCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CollectionUtil.isEmpty(eventInfoDTOS)) {
            return R.successMulti(Lists.newArrayList(), 0L);
        }
        return R.successMulti(eventInfoDTOS, (long) eventInfoDTOS.size());
    }

    @Operation(summary = "异常事件列表", tags = {VersionConstant.V5_7_0})
    @PostMapping("/querySingleAbnormalEventList")
    public R<Collection<ExceptionEventBaseInfoVO>> querySingleAbnormalEventList(@Validated @RequestBody SingleObjectDTO param) {
        List<ExceptionEventBaseInfoVO> eventInfoDTOS = null;
        try {
            eventInfoDTOS = this.abstractRunningMonitorServer.querySingleAbnormalEventList(ContextUtil.getGroupCode(), param.getProjectId(), param.getObjectId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (CollectionUtil.isEmpty(eventInfoDTOS)) {
            return R.successMulti(Lists.newArrayList(), 0L);
        }
        return R.successMulti(eventInfoDTOS, (long) eventInfoDTOS.size());
    }

    @Operation(summary = "查询-hover信息点数据", tags = {VersionConstant.V5_240502_0})
    @PostMapping("/queryEquipOrSpaceHoverInfo")
    public R<List<ExceptionEventEquipDTO>> queryEquipmentOrSpaceIot(@Validated @RequestBody CheckDeviceInfoDTO infoDTO) {
        infoDTO.setHover("1");
        infoDTO.setResultIot(true);
        log.info("queryEquipOrSpaceHoverInfo infoDTO:{}", JSONObject.toJSONString(infoDTO));
        return R.success(this.abstractCommonServer.queryEquipmentOrSpaceIot(infoDTO));
    }

}
