package com.persagy.apccontrol.basic.controller.intervention;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigBaseDTO;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDTO;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigQueryParam;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskManualControlParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingSpaceFuncTypeCircuitParam;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigSaveOrUpdateVO;
import com.persagy.apccontrol.basic.model.vo.intervention.InterventionTaskConfigVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSpaceFuncTypeCircuitVO;
import com.persagy.apccontrol.basic.service.interventiontask.InterventionTaskConfigService;
import com.persagy.apccontrol.basic.service.interventiontask.LightingInterventionTaskConfigHandler;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V7_250601_0;

/**
 * 干预任务配置接口
 *
 */
@Slf4j
@RestController
@RequestMapping(value = "/interventionTaskConfig")
public class InterventionTaskConfigController {
    
    @Resource
    private InterventionTaskConfigService interventionTaskConfigService;
    @Resource(name = "lightingInterventionTaskConfigHandler")
    private LightingInterventionTaskConfigHandler lightingInterventionTaskConfigHandler;
    

    @Operation(summary = "干预任务配置新增或编辑", tags = {VersionConstant.V7_250601_0})
    @PostMapping("/saveOrUpdate")
    public R<InterventionTaskConfigSaveOrUpdateVO> saveOrUpdate(
            @Validated @RequestBody InterventionTaskConfigDTO dto) {
        return R.success(interventionTaskConfigService.saveOrUpdate(dto));
    }

    @Operation(summary = "手动控制", tags = {VersionConstant.V7_260101_0})
    @PostMapping("/manulalControl")
    public R<Boolean> manulalControl(@RequestBody @Valid InterventionTaskManualControlParam param) {
        return R.success(lightingInterventionTaskConfigHandler.manualControl(param));
    }
    
    @Operation(summary = "干预任务删除", tags = {VersionConstant.V7_250601_0})
    @PostMapping("/remove")
    public R<Boolean> remove(@Validated @RequestBody InterventionTaskConfigBaseDTO dto) {
        interventionTaskConfigService.del(dto.getTaskId());
        return R.success(true);
    }
    
    
    @Operation(summary = "干预任务列表查询", tags = {VersionConstant.V7_250601_0})
    @PostMapping("/list")
    public R<List<InterventionTaskConfigVO>> queryList(@Validated @RequestBody InterventionTaskConfigQueryParam queryParam) {
        return R.success(interventionTaskConfigService.queryList(queryParam));
    }
    
    
    @Operation(summary = "照明业务接口-查询照明空间的回路功能类型和关联的控制模块", tags = V7_250601_0)
    @PostMapping(value = "/lighting/getSpaceFuncTypeCircuitController")
    public R<List<LightingSpaceFuncTypeCircuitVO>> getSpaceFuncTypeCircuitController(@RequestBody @Validated LightingSpaceFuncTypeCircuitParam param) {
        return R.success(lightingInterventionTaskConfigHandler.getSpaceFuncTypeCircuitController(param));
    }
    
}
