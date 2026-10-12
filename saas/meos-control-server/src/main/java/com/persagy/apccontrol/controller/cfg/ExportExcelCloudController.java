package com.persagy.apccontrol.controller.cfg;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.fastjson.JSON;
import com.persagy.apccontrol.basic.controller.cfg.excel.InfoCodeConfigExcel;
import com.persagy.apccontrol.basic.controller.cfg.excel.InfoCodeConfigExcelListener;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.resource.RcInfoCodeCfgService;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.enums.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/running-excel")
@Tag(name = "EXCEL导入-内部接口")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExportExcelCloudController {

    private final RcInfoCodeCfgService rcInfoCodeCfgService;
    private final LocalRedisCacheHandler localRedisCacheHandler;

    @Operation(summary = "导入信息点配置数据")
    @PostMapping(value = "/uploadInfoCodeCfg")
    public R<String> uploadInfoCodeCfg(@RequestParam("file") List<MultipartFile> file) {
        if (CollectionUtils.isEmpty(file)) {
            return R.fail(ResponseCode.SERVICE_ERROR_C0400.getCode(), "fail");
        }
        List<String> errorMessages = new ArrayList<>();
        file.forEach(multipartFile -> {
            try {
                InfoCodeConfigExcelListener infoCodeConfigExcelListener = new InfoCodeConfigExcelListener(rcInfoCodeCfgService);
                EasyExcel.read(multipartFile.getInputStream(), InfoCodeConfigExcel.class, infoCodeConfigExcelListener).extraRead(CellExtraTypeEnum.MERGE).doReadAll();
                this.localRedisCacheHandler.resetGlobalLocalRedisCache(TerminalEnum.CLOUD, true);
            } catch (Exception e) {
                log.error("导入项目信息点管理范围配置出现异常", e);
                String msg = "[" + multipartFile.getOriginalFilename() + "]" + LocaleMessageUtil.getMessage("key_a1e92efd2da847f598847f47df3f8625");
                errorMessages.add(msg);
            }
        });
        if (CollectionUtils.isNotEmpty(errorMessages)) {
            return R.success(JSON.toJSONString(errorMessages));
        }
        return R.success(LocaleMessageUtil.getMessage("key_2a35069ae93d42e7aa04fa4f10d9e0f4"));
    }
}
