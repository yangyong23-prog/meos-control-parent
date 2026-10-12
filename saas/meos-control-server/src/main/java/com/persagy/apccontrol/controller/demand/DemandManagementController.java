package com.persagy.apccontrol.controller.demand;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.demand.airconditioning.ValueSelectConfigLogDto;
import com.persagy.apccontrol.basic.model.entity.RcCalculateResult;
import com.persagy.apccontrol.basic.resource.RcCalculateResultService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.ValueSelectionService;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueResultQueryParam;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueSelectQueryParamByPage;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/demandManagement")
@Slf4j
public class DemandManagementController {

    @Autowired
    private ValueSelectionService valueSelectionService;

    @Autowired
    private RcCalculateResultService calculateResultService;

    @Operation(summary = "查询价值选择计算结果", tags = VersionConstant.V5_7_0)
    @PostMapping("/listComputeResult")
    public R<List<RcCalculateResult>> listConfig(@RequestBody @Valid ValueResultQueryParam param) {
        List<RcCalculateResult> rcCalculateResults = this.calculateResultService.queryCalculateResultList(ContextUtil.getGroupCode(), param.getProjectId(), param.getSpaceId(), param.getWorkDate(), param.getCalculateType());
        return R.success(CollectionUtil.isEmpty(rcCalculateResults) ?  Lists.newArrayList() : rcCalculateResults);
    }

    @Operation(summary = "查询价值选择配置日志", tags = VersionConstant.V5_7_0)
    @PostMapping("/listValueSelectionLog")
    public R<List<ValueSelectConfigLogDto>> listLog(@RequestBody @Valid ValueSelectQueryParamByPage param) {
        return R.success(valueSelectionService.listLog(param));
    }
}
