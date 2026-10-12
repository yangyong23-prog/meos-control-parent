package com.persagy.apccontrol.basic.controller.demand;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.scene.DemandSceneCopyParam;
import com.persagy.apccontrol.basic.model.param.demand.scene.DemandSceneSaveParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateCopyParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.scene.DemandAllSceneVO;
import com.persagy.apccontrol.basic.model.vo.demand.scene.DemandSceneVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.service.demand.DemandSceneApplicationService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 需求管理-基础接口
 */
@RestController
@RequestMapping("/demand/scene")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandSceneController {

    private final DemandSceneApplicationService demandSceneApplicationService;

    @Operation(summary = "查询-当前模板下的所有场景", tags = VersionConstant.V7_250801_0)
    @PostMapping("/listSceneConfig")
    public R<List<DemandSceneVO>> listSceneConfig(@RequestBody @Validated DemandTemplateQueryParam templateIdParam) {
        return R.success(demandSceneApplicationService.listSceneConfig(templateIdParam.getTemplateId()));
    }

    @Operation(summary = "查询-当前类型下的所有场景", tags = VersionConstant.V7_250801_0)
    @PostMapping("/listAllScene")
    public R<List<DemandAllSceneVO>> listAllScene(@RequestBody @Validated DemandTemplateBatchQueryParam param) {
        return R.success(demandSceneApplicationService.listAllScene(param));
    }

    @Operation(summary = "编辑-新增/修改模板下的场景信息", tags = VersionConstant.V7_250801_0)
    @PostMapping("/editSceneConfigs")
    public R<Boolean> editSceneConfigs(@RequestBody @Validated DemandSceneSaveParam sceneSaveParam) {
        return R.success(demandSceneApplicationService.editSceneConfigs(sceneSaveParam));
    }

    @Operation(summary = "操作-复制其他场景配置", tags = VersionConstant.V7_250801_0)
    @PostMapping("/copySceneConfig")
    public R<Boolean> copySceneConfig(@RequestBody @Validated DemandSceneCopyParam fromSceneParam) {
        return R.success(demandSceneApplicationService.copySceneConfig(fromSceneParam));
    }

    @Operation(summary = "操作-复制其他模板下的场景配置", tags = VersionConstant.V7_250801_0)
    @PostMapping("/copySceneConfigByTemplate")
    public R<Boolean> copySceneConfigByTemplate(@RequestBody @Validated DemandTemplateCopyParam fromTemplateParam) {
        return R.success(demandSceneApplicationService.copySceneConfigByTemplate(fromTemplateParam.getTemplateId(), fromTemplateParam.getFromTemplateId()));
    }

    @Operation(summary = "查询-获取存在场景配置的模板列表", tags = VersionConstant.V7_250801_0)
    @PostMapping("/listExistsSceneConfigTemplate")
    public R<List<DemandTemplateVO>> listExistsSceneConfigTemplate(@RequestBody @Validated DemandTemplateQueryParam templateIdParam) {
        return R.success(demandSceneApplicationService.listExistsSceneConfigTemplate(templateIdParam.getTemplateId()));
    }

}
