package com.persagy.apccontrol.basic.controller.demand;

import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateDateSettingQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.template.DemandTemplateTypeQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateDateSettingVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateTypeVO;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.service.demand.DemandCalendarQueryService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 需求管理-需求日历模版控制器
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@RestController
@RequestMapping("/demand/template")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandCalendarController {

    private final RedisCacheTemplate redisCacheTemplate;
    private final DemandCalendarQueryService demandCalendarQueryService;

    @Operation(summary = "查询-模版类型列表", tags = VersionConstant.V5_17_0)
    @PostMapping("/listTemplateType")
    public R<List<DemandTemplateTypeVO>> listTemplateType(@RequestBody @Valid DemandTemplateTypeQueryParam param) {
        return R.success(redisCacheTemplate.asyncRefreshTemplate("listTemplateType",
                param, () -> demandCalendarQueryService.listTemplateType(param)));
    }

    @Operation(summary = "查询-模版列表", tags = VersionConstant.V6_0_0)
    @PostMapping("/listTemplate")
    public R<List<DemandTemplateVO>> listTemplate(@RequestBody @Valid DemandTemplateBatchQueryParam param) {
        return R.success(demandCalendarQueryService.listTemplate(param));
    }

    @Operation(summary = "查询-日期配置", tags = {
            VersionConstant.V6_0_0,
            ExternalServerConstant.CUSTOM_HR
    })
    @PostMapping("/listDateSetting")
    public R<List<DemandTemplateDateSettingVO>> listDateSetting(
            @RequestBody @Valid DemandTemplateDateSettingQueryParam param) {
        return R.success(demandCalendarQueryService.listDateSetting(param));
    }
}
