package com.persagy.apccontrol.controller.lighting;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.persagy.apccontrol.basic.model.param.demand.lighting.DemandLightingInstanceQueryParam;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandConfigLightingDataVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandConfigLightingGlobalConfigVO;
import com.persagy.apccontrol.basic.model.vo.demand.lighting.DemandConfigLightingSpaceConfigVO;
import com.persagy.apccontrol.basic.service.demand.lighting.DemandLightingQueryService;
import com.persagy.framework.common.entity.R;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 描述
 *
 * @author yuecaipu
 * @date 2024/7/25 11:33
 * @since v5.240402.00
 */
@RestController
@RequestMapping("/demand/lighting")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DamendLightingController {

    private final DemandLightingQueryService demandLightingQueryService;
    @PostMapping("/getSceneObjectSettings")
    public R<DemandConfigLightingDataVO> getSceneObjectSettings(@RequestBody @Validated DemandLightingInstanceQueryParam instanceQueryParam) {
        DemandConfigLightingDataVO sceneObjectSettings = demandLightingQueryService.getSceneObjectSettings(instanceQueryParam);
        // 移除灯具类型层级 防止影响前端
        removeLampFuncType(sceneObjectSettings);

        return R.success(sceneObjectSettings);
    }

    private void removeLampFuncType(DemandConfigLightingDataVO sceneObjectSettings) {
        if (null != sceneObjectSettings) {
            DemandConfigLightingGlobalConfigVO global = sceneObjectSettings.getGlobal();
            if (null != global && CollectionUtil.isNotEmpty(global.getFuncTypeSettings())) {
                global.getFuncTypeSettings().forEach(funcTypeSetting -> {
                    funcTypeSetting.setLampFuncTypeSettings(null);
                });
            }
            DemandConfigLightingSpaceConfigVO space = sceneObjectSettings.getSpace();
            if (null != space && CollUtil.isNotEmpty(space.getSettings())) {
                space.getSettings().forEach(setting -> {
                    setting.getFuncTypeSettings().forEach(funcTypeSetting -> {
                        funcTypeSetting.setLampFuncTypeSettings(null);
                    });
                });
            }
        }
    }

}
