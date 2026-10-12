package com.persagy.apccontrol.basic.controller.infocodeconfig;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.*;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeTemplateVO;
import com.persagy.apccontrol.basic.model.vo.infocode.SpaceByBuildingIdGroupFloorVO;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;


/**
 * 信息点配置控制器
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@RestController
@RequestMapping(value = "/equipment/infoCodeConfig")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OldInfoCodeConfigController {

    private final InfoCodeConfigService infoCodeConfigService;

    @Operation(summary = " 查询-模版类型列表")
    @PostMapping(value = "/listTemplateTypes")
    public R<List<InfoCodeTemplateVO>> listTemplateTypes(
            @RequestBody @Valid InfoCodeConfigTemplateTypeQueryParam param) {
        return LocalContextStorage.deprecatedVersion(() ->
                R.success(infoCodeConfigService.listTemplateTypes(param)));
    }

    @Operation(summary = " 查询-模版类型对象存在规则的IOT信息点")
    @PostMapping(value = "/listObjectIotInfoCodesWithRuleByTemplateType")
    public R<Collection<InfoCodeDefineModel>> listObjectIotInfoCodesWithRuleByTemplateType(
            @RequestBody @Valid InfoCodeConfigIotWithRuleByTemplateTypeQueryParam param) {
        return LocalContextStorage.deprecatedVersion(() ->
                infoCodeConfigService.listObjectIotInfoCodesWithRuleByTemplateType(param));
    }

    @Operation(summary = " 查询-信息点配置-按模版类型")
    @PostMapping(value = "/listInfoCodeConfigsByTemplateType")
    public R<Collection<InfoCodeObjectGroupVO>> listInfoCodeConfigsByTemplateType(
            @RequestBody @Valid InfoCodeConfigByTemplateIdQueryParam param) {
        return LocalContextStorage.deprecatedVersion(() ->
                infoCodeConfigService.listInfoCodeConfigsByTemplateType(param));
    }

    @Operation(summary = " 查询-信息点配置-按对象", tags = ExternalServerConstant.CUSTOM_YX)
    @PostMapping(value = "/listInfoCodeConfigsByObject")
    public R<List<InfoCodeObjectGroupVO>> listInfoCodeConfigsByObject(
            @RequestBody @Valid InfoCodeConfigByObjectQueryParam param) {
        return LocalContextStorage.deprecatedVersion(() ->
                R.success(infoCodeConfigService.listInfoCodeConfigsByObject(param)));
    }

    @Operation(summary = " 查询-信息点配置-按模版编码")
    @PostMapping(value = "/listInfoCodeConfigsByObjectFilters")
    public R<List<InfoCodeObjectGroupVO>> listInfoCodeConfigsByObjectFilters(
            @RequestBody @Valid InfoCodeConfigObjectByFiltersQueryParam param) {
        return LocalContextStorage.deprecatedVersion(() ->
                R.success(infoCodeConfigService.listInfoCodeConfigsByObjectFilters(param)));
    }

    @Operation(summary = " 信息点配置表-通用接口-查询建筑下空间（按照楼层分组）")
    @PostMapping(value = "/listSpaceByBuildingIdGroupFloor")
    public R<List<SpaceByBuildingIdGroupFloorVO>> listSpaceByBuildingIdGroupFloor(
            @RequestBody @Valid SpaceByBuildingIdGroupFloorQueryParam param) {
        return LocalContextStorage.deprecatedVersion(() ->
                R.success(infoCodeConfigService.listSpaceByBuildingIdGroupFloor(param)));
    }

    @Operation(summary = " 信息点配置表-通用接口-查询项目下建筑下的空间（按照楼层分组）")
    @PostMapping(value = "/listBuildingAndSpaceByProjectIdGroupFloor")
    public R<JSONObject> listBuildingAndSpaceByProjectIdGroupFloor(@RequestHeader String projectId,
                                                                   @RequestBody @Valid BuildingAndSpaceByProjectIdQueryParam param) {
        return LocalContextStorage.deprecatedVersion(() -> {
            param.setProjectId(projectId);
            JSONObject data = new JSONObject();
            // 为了适配前端要求
            data.put("buildings", infoCodeConfigService.listBuildingAndSpaceByProjectIdGroupFloor(param));
            return R.success(data);
        });
    }
}
