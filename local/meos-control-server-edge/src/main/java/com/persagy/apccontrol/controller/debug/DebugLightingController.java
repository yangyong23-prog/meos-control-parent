package com.persagy.apccontrol.controller.debug;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import com.persagy.apccontrol.common.service.lighting.LightingService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 照明调试控制器
 *
 * @author JiYinchuan
 * @since 5.240501.0
 */
@RestController
@RequestMapping(value = "/debug/lighting")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DebugLightingController {

    private final DemandLightingStrategy demandLightingStrategy;
    private final LightingService lightingService;

    @GetMapping("/listCircuits")
    public R<Object> listCircuits(
            @RequestParam DemandTemplateTypeEnum templateType,
            @RequestParam(required = false) Collection<String> circuitIds) {
        return R.success(demandLightingStrategy.listCircuits(ContextUtil.getProjectId(), templateType, circuitIds));
    }

    @GetMapping("/listScenesByAllTime")
    public R<Object> listScenesByAllTime(
            @RequestParam DemandTemplateTypeEnum templateType,
            @RequestParam LocalDate date) {
        return R.success(demandLightingStrategy.listScenesByAllTime(ContextUtil.getProjectId(), templateType, date));
    }

    @GetMapping("/mapCircuitIdAndTag")
    public R<Object> mapCircuitIdAndTag(
            @RequestParam String projectId,
            @RequestParam DemandTemplateTypeEnum templateType,
            @RequestParam(required = false) Collection<String> circuitIds) {
        Set<String> finalCircuitIds = Objects.nonNull(circuitIds)
                ? new HashSet<>(circuitIds)
                : demandLightingStrategy.listCircuits(projectId, templateType, null).stream()
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        return R.success(lightingService.mapCircuitIdAndTag(projectId, templateType, finalCircuitIds));
    }
}
