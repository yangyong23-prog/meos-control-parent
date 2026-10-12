package com.persagy.apccontrol.basic.controller.common;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.model.param.common.CommonConfigSaveParam;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Objects;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240602_0;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_241001_0;

/**
 * 公共配置接口
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
@RestController
@RequestMapping(value = "/common")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommonConfigController {

    private final CommonConfigService commonConfigService;

    @Operation(summary = "公共配置保存", tags = V5_240602_0)
    @PostMapping(value = "/config/save")
    public R<Boolean> save(@RequestBody @Valid CommonConfigSaveParam param) {
        return R.success(commonConfigService.saveConfig(param));
    }

    @Operation(summary = "公共配置保存", tags = V5_240602_0)
    @GetMapping(value = "/config/remove/{id}")
    public R<Boolean> remove(@PathVariable("id") String id) {
        return R.success(commonConfigService.removeById(id));
    }

    @Operation(summary = "公共配置查询", tags = V5_240602_0)
    @GetMapping(value = "/config/get")
    public R<JSONArray> save(@RequestParam @Valid String tag) {
        return R.success(commonConfigService.getCommonConfigByTag(tag));
    }

    @Operation(summary = "刷新缓存", tags = V5_241001_0)
    @GetMapping(value = "/config/refreshCacheData")
    public R<Boolean> refreshCacheData() {
        Boolean commonConfigByTag = commonConfigService.refreshCacheData();
        return R.success(commonConfigByTag);
    }

    @Operation(summary = "导入关系图图管道配置数据")
    @PostMapping(value = "/uploadSystemGraphPipelineStyle")
    public R<String> uploadSystemGraphPipelineStyle(@RequestParam(value = "sheetNum", required = false) Integer sheetNum, @RequestParam("file") MultipartFile file) {
        if (Objects.isNull(file)) {
            return R.fail(ResponseCode.SERVICE_ERROR_C0400.getCode(), "fail:file is null");
        }
        String msg = commonConfigService.uploadSystemGraphPipelineStyle(sheetNum, file);
        return R.success(msg);
    }

}
