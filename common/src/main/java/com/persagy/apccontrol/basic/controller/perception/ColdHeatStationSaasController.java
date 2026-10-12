package com.persagy.apccontrol.basic.controller.perception;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 冷热站
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/1/29 14:33
 */
@Slf4j
@RestController
@Tag(name = "冷热站")
@RequestMapping(value = "/cold-heat")
public class ColdHeatStationSaasController {

    @Autowired
    private BizDtpRwdService bizDtpRwdService;

    @Operation(summary = "查询空气源热泵对应的泵组", tags = {VersionConstant.V5_6_0})
    @PostMapping("/queryPumpGroupObjectByPumpId")
    public R<JSONObject> queryPumpGroupObjectByPumpId(@Validated @RequestBody SingleObjectDTO dto) {
        List<JSONObject> equipGroups = this.bizDtpRwdService.queryObjectList(ContextUtil.getGroupCode(), dto.getProjectId(), Lists.newArrayList("ACAHC"), "CombineSubset", "Eg2Eq", null, Lists.newArrayList(dto.getObjectId()));
        JSONObject relationObject = CollectionUtil.isEmpty(equipGroups) ? new JSONObject() : equipGroups.get(0);
        return R.success(relationObject);
    }

}
