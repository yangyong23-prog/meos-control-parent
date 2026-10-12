package com.persagy.apccontrol.basic.controller.weakpower;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.weakpower.WeakPowerTreeEquipmentsQueryParam;
import com.persagy.apccontrol.basic.model.vo.weakpower.WeakPowerSystemVO;
import com.persagy.apccontrol.basic.model.vo.weakpower.WeakPowerTreeEquipmentsVO;
import com.persagy.apccontrol.basic.service.weakpower.WeakPowerService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V7_250301_0;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V7_251202_0_1;

/**
 * 弱电感知控制器
 *
 * @author JiYinchuan
 * @since 7.250301.0
 */
@RestController
@RequestMapping("/weakPower")
@Schema(name = "弱电")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class WeakPowerController {

    private final WeakPowerService weakPowerService;
    private final RedisCacheTemplate redisCacheTemplate;

    @Operation(summary = "查询设备树", tags = {V7_250301_0,V7_251202_0_1})
    @PostMapping("/treeEquipments")
    public R<WeakPowerTreeEquipmentsVO> treeEquipments(@Validated @RequestBody WeakPowerTreeEquipmentsQueryParam param) {
        return R.success(redisCacheTemplate.asyncRefreshTemplate("weakPower/treeEquipments", param, () -> weakPowerService.treeEquipments(param)));
    }

    @Operation(summary = "查询-需求空间树", tags = VersionConstant.V7_250301_0)
    @PostMapping("/listSystems")
    public R<Collection<WeakPowerSystemVO>> listSystems() {
        return R.successMulti(weakPowerService.listSystems());
    }
}
