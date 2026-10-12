package com.persagy.apccontrol.basic.controller.system;


import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryDTO;
import com.persagy.apccontrol.basic.service.system.SystemListDataService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;


@Tag(name = "系统数据接口")
@Validated
@RestController
@RequestMapping("system")
@Slf4j
public class SystemListDataController {

    @Resource
    private SystemListDataService systemListDataService;

    @Operation(summary = "查询系统列表数据", tags = {VersionConstant.V5_6_0})
    @PostMapping("/querySystemListData")
    public R<List<DtpRwdObjectDTO>> querySystemListData(@Valid @RequestBody QueryDTO params) {
        params.setGroupCode(ContextUtil.getGroupCode());
        List<DtpRwdObjectDTO> buildingDTOS = systemListDataService.querySystemListData(params);
        if (CollectionUtil.isNotEmpty(buildingDTOS)) {
            buildingDTOS.stream().sorted(Comparator.comparing(DtpRwdObjectDTO::getLocalId));
        }
        return R.success(buildingDTOS == null ? Lists.newArrayList() : buildingDTOS);
    }

    @Operation(summary = "根据设备类编码获取系统列表", tags = {VersionConstant.V5_241102_0})
    @GetMapping("/listSystemByClassCode/{classCode}")
    public R<List<DtpRwdObjectDTO>> listSystemByClassCode(@PathVariable String classCode){
        return R.success(systemListDataService.listSystemByClassCode(classCode));
    }

}
