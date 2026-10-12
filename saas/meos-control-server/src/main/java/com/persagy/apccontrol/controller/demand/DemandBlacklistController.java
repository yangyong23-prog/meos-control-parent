package com.persagy.apccontrol.controller.demand;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.demand.DemandBlacklistConfigAddParam;
import com.persagy.apccontrol.basic.model.vo.demand.DemandBlacklistRuleVO;
import com.persagy.apccontrol.basic.service.demand.blacklist.DemandBlacklistService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

/**
 * 需求黑名单管理控制器
 */
@RestController
@RequestMapping("/demand/blacklist")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DemandBlacklistController {

    private final DemandBlacklistService demandBlacklistService;

    @Operation(summary = "保存黑名单规则", tags = VersionConstant.V7_250202_0)
    @PostMapping("/addRules")
    public R<Boolean> addRules(@RequestBody @Valid DemandBlacklistConfigAddParam param) {
        demandBlacklistService.addRules(param);
        return R.success(true);
    }

    @Operation(summary = "新增根据规则查询黑名单空间ID", tags = VersionConstant.V7_250202_0)
    @PostMapping("/listSpaceIdsByRule")
    public R<Set<String>> listSpaceIdsByRule(@RequestBody @Valid DemandBlacklistConfigAddParam param) {
        return R.success(demandBlacklistService.listSpaceIdsByRule(param));
    }

    @Operation(summary = "新增根据规则查询黑名单空间ID", tags = VersionConstant.V7_250202_0)
    @PostMapping("/query")
    public R<DemandBlacklistRuleVO> query(@RequestBody @Valid DemandBlacklistConfigAddParam param) {
        return R.success(demandBlacklistService.query(param));
    }


}
