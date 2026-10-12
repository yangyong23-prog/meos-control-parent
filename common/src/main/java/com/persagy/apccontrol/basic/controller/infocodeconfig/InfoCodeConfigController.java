package com.persagy.apccontrol.basic.controller.infocodeconfig;

import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.param.infocodeconfig.*;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeConfigOriginVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeObjectGroupVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeTemplateTreeVO;
import com.persagy.apccontrol.basic.model.vo.infocode.InfoCodeTemplateVO;
import com.persagy.apccontrol.basic.service.infocodeconfig.InfoCodeConfigService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * 信息点配置控制器
 *
 * @author JiYinchuan
 * @since 7.250302.0
 */
@RestController
@RequestMapping(value = "/infoCodeConfig")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeConfigController {

    private final InfoCodeConfigService infoCodeConfigService;

    @Operation(summary = "查询-模版类型列表")
    @PostMapping("/listTemplateTypes")
    public R<List<InfoCodeTemplateVO>> listTemplateTypes(
            @RequestBody @Valid InfoCodeConfigTemplateTypeQueryParam param) {
        return R.success(infoCodeConfigService.listTemplateTypes(param));
    }

    @Operation(summary = "查询-模版类型对象存在规则的IOT信息点")
    @PostMapping("/listObjectIotInfoCodesWithRuleByTemplateType")
    public R<Collection<InfoCodeDefineModel>> listObjectIotInfoCodesWithRuleByTemplateType(
            @RequestBody @Valid InfoCodeConfigIotWithRuleByTemplateTypeQueryParam param) {
        return infoCodeConfigService.listObjectIotInfoCodesWithRuleByTemplateType(param);
    }

    @Operation(summary = "查询-原始信息点配置列表")
    @PostMapping("/listOriginInfoCodeConfigs")
    public R<List<InfoCodeConfigOriginVO>> listOriginInfoCodeConfigs(
            @RequestBody @Valid InfoCodeConfigsOriginQueryParam param) {
        return R.success(infoCodeConfigService.listOriginInfoCodeConfigs(param));
    }

    @Operation(summary = "查询-信息点配置-按模版ID")
    @PostMapping("/listInfoCodeConfigsByTemplateId")
    public R<Collection<InfoCodeObjectGroupVO>> listInfoCodeConfigsByTemplateId(
            @RequestBody @Valid InfoCodeConfigByTemplateIdQueryParam param) {
        return infoCodeConfigService.listInfoCodeConfigsByTemplateType(param);
    }

    @Operation(summary = "查询-信息点配置-按模版类型")
    @PostMapping("/listInfoCodeConfigsByTemplateType")
    public R<List<InfoCodeObjectGroupVO>> listInfoCodeConfigsByTemplateType(
            @RequestBody @Valid InfoCodeConfigObjectByFiltersQueryParam param) {
        return R.success(infoCodeConfigService.listInfoCodeConfigsByObjectFilters(param));
    }

    @Operation(summary = "查询-信息点配置-按对象ID")
    @PostMapping("/listInfoCodeConfigsByObjectId")
    public R<List<InfoCodeObjectGroupVO>> listInfoCodeConfigsByObjectId(
            @RequestBody @Valid InfoCodeConfigByObjectQueryParam param) {
        return R.success(infoCodeConfigService.listInfoCodeConfigsByObject(param));
    }

    @Operation(summary = "查询-模版类型列表")
    @PostMapping("/treeTemplateType")
    public R<Set<InfoCodeTemplateTreeVO>> treeTemplateType(
            @RequestBody @Valid InfoCodeConfigTemplateTypeQueryParam param) {
        return R.success(infoCodeConfigService.treeTemplateType(param));
    }
}
