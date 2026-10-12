package com.persagy.apccontrol.controller.calculate;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.constant.ExternalServerConstant;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.AirConditionDemandDTO;
import com.persagy.apccontrol.basic.service.demand.airconditioning.param.ValueResultQueryParam;
import com.persagy.apccontrol.common.handle.RedisLockHandler;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 价值计算对外接口数据
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/9/26 11:11
 */
@Slf4j
@RestController
@Tag(name = "需求管理")
@RequestMapping(value = "/calculate")
public class CalculateController {

    @Autowired
    protected RedisLockHandler redisLockHandler;

    @Operation(summary = "查询空调需求时间", tags = {
            VersionConstant.V5_7_0,
            ExternalServerConstant.AI_AU_SERVER
    })
    @PostMapping("/querySpaceAirConditionDemand")
    public R<Map<String, List<AirConditionDemandDTO>>> querySpaceAirConditionDemand(@RequestBody ValueResultQueryParam param) throws Exception {
        return R.success(this.redisLockHandler.queryAirConditionDemands(param.getProjectId(), Lists.newArrayList(param.getSpaceId())));
    }
}
