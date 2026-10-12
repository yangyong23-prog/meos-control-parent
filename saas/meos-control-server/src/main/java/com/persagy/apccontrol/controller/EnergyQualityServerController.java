package com.persagy.apccontrol.controller;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.outside.feign.service.EnergyQualityServerService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 转发energy-quality-server服务接口
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/2/20 16:40
 */
@Slf4j
@RestController
@Tag(name = "energy-quality-server")
@RequestMapping(value = "/energy-quality-server")
public class EnergyQualityServerController {

    @Autowired
    private EnergyQualityServerService energyQualityServerService;

    @Operation(summary = "查询气候区", tags = {VersionConstant.V5_241102_0})
    @PostMapping("/quality/common/querySeason")
    public R<Object> querySeason(@RequestBody JSONObject param) throws Exception {
        Object o = this.energyQualityServerService.querySeason(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), param);
        return R.success(o == null ? new Object() : o);
    }

    @Operation(summary = "查询集团标准", tags = {VersionConstant.V5_241102_0})
    @PostMapping("/quality/common/queryStandard")
    public R<Object> queryStandard(@RequestBody JSONObject param) throws Exception {
        Object o = this.energyQualityServerService.queryStandard(ContextUtil.getGroupCode(), ContextUtil.getProjectId(), param);
        return R.success(o == null ? new Object() : o);
    }

}
