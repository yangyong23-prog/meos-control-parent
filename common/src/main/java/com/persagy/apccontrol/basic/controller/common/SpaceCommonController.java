package com.persagy.apccontrol.basic.controller.common;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.ObjectClassCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.param.common.EquipmentServiceSpaceBySpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.EquipmentServiceSpaceQueryParam;
import com.persagy.apccontrol.basic.model.param.common.SensorByServiceSpaceQueryParam;
import com.persagy.apccontrol.basic.model.vo.common.EquipmentServiceSpaceVO;
import com.persagy.apccontrol.basic.model.vo.common.SensorByServiceEquipmentVO;
import com.persagy.apccontrol.basic.model.vo.common.ServiceSpaceBySpaceVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.weather.WeatherSunRiseAndSetQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.WeatherClientService;
import com.persagy.apccontrol.basic.service.common.SpaceCommonService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @ClassName 薛涛
 * @Author Jiangnan Cui
 * @Date 2024/7/23 14:54
 * @Version 1.0
 */
@RestController
@RequestMapping(value = "/commonSpace")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SpaceCommonController {
    private final SpaceCommonService spaceCommonService;

    @Operation(summary = "信息点配置表-通用接口-查询服务空间的传感器信息（设备服务空间关系）", tags = VersionConstant.V5_240702_0)
    @PostMapping(value = "/listSensorByServiceSpace")
    public R<List<SensorByServiceEquipmentVO>> listSensorByServiceSpace(@RequestHeader String projectId ,@RequestBody SensorByServiceSpaceQueryParam param){
        param.setProjectId(projectId);
        return R.success(  spaceCommonService.listSensorByServiceSpace(param));
    }


    @Operation(summary = "设备服务空间", tags = VersionConstant.V5_240702_0)
    @PostMapping(value = "/listEquipmentServiceSpaceByEquipmentId")
    public R<List<EquipmentServiceSpaceVO>> listEquipmentServiceSpaceByEquipmentId(@RequestHeader String projectId , @RequestBody EquipmentServiceSpaceQueryParam param){
        param.setProjectId(projectId);
        return R.success(  spaceCommonService.listEquipmentServiceSpaceByEquipmentId(param));
    }

    @Operation(summary = "空间关联设备（服务空间的）", tags = VersionConstant.V5_240702_0)
    @PostMapping(value = "/listEquipmentServiceSpaceBySpaceId")
    @Deprecated
    public R<List<ServiceSpaceBySpaceVO>> listEquipmentServiceSpaceBySpaceId(@RequestHeader String projectId , @RequestBody EquipmentServiceSpaceBySpaceQueryParam param){
        param.setProjectId(projectId);
        return R.success(  spaceCommonService.listEquipmentServiceSpaceBySpaceId(param));
    }

    @Operation(summary = "查询-某对象所服务的空间树", tags = VersionConstant.V5_241102_0)
    @PostMapping(value = "/listObjectServiceSpaceByObjectId")
    public R<List<DtpRwdBuildingDTO>> listObjectServiceSpaceByObjectId(@RequestBody ObjectClassCodeDTO param){
        return R.success(spaceCommonService.listObjectServiceSpaceByObjectId(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), param.getObjectId()));
    }

    private final WeatherClientService weatherClientService;

    @PostMapping(value = "/test")
    public R<Object> test(@RequestBody WeatherSunRiseAndSetQueryParam param){
        param.setProjectId("");
        weatherClientService.listSunRiseAndSetCache("Pj9909990007", LocalDate.now(), LocalDate.now());
        return R.success(null);
    }



}
