package com.persagy.apccontrol.basic.controller.outside;

import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionTaskConfigDataDTO;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionTaskConfigOutsideQueryParam;
import com.persagy.apccontrol.basic.service.interventiontask.InterventionTaskConfigService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "干预需求外部调用接口")
@Slf4j
@RestController
@RequestMapping(value = "/outside/intervention")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OutsideInterventionTaskConfigController {
    
    private final InterventionTaskConfigService interventionTaskConfigService;
    
    @Operation(summary = "外部查询-干预任务查询", tags = {
            VersionConstant.V7_250601_0,
            ExternalServerConstant.DTP_MEOS_SERVER_EDGE
    })
    @PostMapping("/taskConfigList")
    public R<List<? extends InterventionTaskConfigDataDTO>> taskConfigList(@Validated @RequestBody InterventionTaskConfigOutsideQueryParam queryParam) {
        return R.success(this.interventionTaskConfigService.outsideQueryTaskConfigListList(queryParam));
    }
}
