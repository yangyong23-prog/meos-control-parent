package com.persagy.apccontrol.controller.infocodeconfig;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.persagy.apccontrol.basic.model.context.infocodeconfig.excel.InfoCodeGroupImportContext;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeRemoveDaoParam;
import com.persagy.apccontrol.service.InfoCodeImportService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.persagy.apccontrol.basic.constant.VersionConstant.*;

/**
 * 信息点配置导入控制器
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@RestController
@RequestMapping(value = "/infoCodeConfig/import")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class InfoCodeImportController {

    private final InfoCodeImportService infoCodeImportService;

    @Operation(summary = "解析-前端配置表", tags = V7_250602_0)
    @PostMapping(value = "/parseWebConfig")
    public R<ObjectNode> parseWebConfig(@RequestPart MultipartFile file) {
        this.clearPublicContext();
        return infoCodeImportService.parseWebConfig(file);
    }

    @Operation(summary = "导入-分组表", tags = V5_240602_0)
    @PostMapping(value = "/groupSheet")
    public R<InfoCodeGroupImportContext> importGroupSheet(
            @RequestPart MultipartFile file, @RequestParam(required = false) boolean checkGroupCodeInConfig) {
        this.clearPublicContext();
        return infoCodeImportService.importGroupSheet(file, checkGroupCodeInConfig);
    }

    @Operation(summary = "导入-配置表", tags = V5_240602_0)
    @PostMapping(value = "/tableSheet")
    public R<Map<String, Collection<String>>> importTableSheet(
            @RequestPart List<MultipartFile> files,
            @RequestParam(required = false) boolean skipNotDefineInfoCode) {
        this.clearPublicContext();
        return infoCodeImportService.importTableSheet(files, skipNotDefineInfoCode);
    }

    @Operation(summary = "删除-信息点配置", tags = V7_250701_0)
    @PostMapping(value = "/del")
    public R<Boolean> del(@RequestBody @Valid InfoCodeTemplateTypeRemoveDaoParam param) {
        this.clearPublicContext();
        return R.success(infoCodeImportService.del(param));
    }

    @Operation(summary = "国际化-翻译信息点名称", tags = V7_250901_0)
    @PostMapping(value = "/translateInfoCodeName")
    public void translateInfoCodeName(HttpServletResponse response) throws IOException {
        this.clearPublicContext();
        infoCodeImportService.translateInfoCodeName(response);
    }

    @Operation(summary = "国际化-导入信息点名称", tags = V7_250902_0)
    @PostMapping(value = "/infoCodeName")
    public R<Collection<String>> importInfoCodeName(@RequestPart MultipartFile file) {
        this.clearPublicContext();
        return infoCodeImportService.importInfoCodeName(file);
    }

    /**
     * 信息点配置接口统一操作公共库，不接受请求上下文中的集团、项目路由值。
     *
     * @since v7.260701.0
     */
    private void clearPublicContext() {
        ContextUtil.setTenant(null);
        ContextUtil.setGroupCode(null);
        ContextUtil.setProjectId(null);
    }
}
