package com.persagy.apccontrol.controller.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.common.model.param.ai.AiDemandConfigQueryParam;
import com.persagy.apccontrol.common.model.vo.ai.AiDemandConfigVO;
import com.persagy.apccontrol.service.ai.AiService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 运行智能体
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AiController {

    private final AiService aiService;

    @Operation(summary = "查询-需求配置", tags = {
            VersionConstant.V7_250702_0
    })
    @PostMapping("/getDemandConfig")
    public R<AiDemandConfigVO> getDemandConfig(@RequestBody @Valid AiDemandConfigQueryParam param) {
        return R.success(aiService.getDemandConfig(param));
    }

    @Operation(summary = "回调-计算状态", tags = {
            VersionConstant.V7_250702_0,
            ExternalServerConstant.AI_AUTORUN_AGENT
    })
    @PostMapping("/notifyCalcStatus")
    public R<Boolean> notifyCalcStatus(@RequestBody @Valid JsonNode param) {
        return R.success(aiService.notifyCalcStatus(param));
    }
}
