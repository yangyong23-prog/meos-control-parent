package com.persagy.apccontrol.basic.controller.cfg;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.excel.EasyExcel;
import com.persagy.apccontrol.basic.excel.ClassPositionCodeConfigExcel;
import com.persagy.apccontrol.basic.excel.ClassPositionCodeConfigExcelListener;
import com.persagy.apccontrol.basic.excel.RcObjectRelationCfgExcel;
import com.persagy.apccontrol.basic.excel.RcObjectRelationCfgExcelListener;
import com.persagy.apccontrol.basic.resource.ClassPositionCodeConfigService;
import com.persagy.apccontrol.basic.resource.RcInfoCodeCfgService;
import com.persagy.apccontrol.basic.resource.RcObjectRelationCfgService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * excel导入
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/17 14:49
 */
@Slf4j
@RestController
@RequestMapping(value = "/running-excel")
@Tag(name = "EXCEL导入-内部接口")
public class ExportExcelController {

    @Autowired
    private RcInfoCodeCfgService rcInfoCodeCfgService;

    @Autowired
    private RcObjectRelationCfgService rcObjectRelationCfgService;

    @Autowired
    private ClassPositionCodeConfigService classPositionCodeConfigService;

    @Operation(summary = "导入对象关系配置数据")
    @PostMapping(value = "/uploadObjectRelationCfg")
    public R<String> uploadObjectRelationCfg(@RequestParam("file") MultipartFile file) {
        try {
            RcObjectRelationCfgExcelListener rcObjectRelationCfgExcelListener = new RcObjectRelationCfgExcelListener(this.rcObjectRelationCfgService);
            EasyExcel.read(file.getInputStream(), RcObjectRelationCfgExcel.class, rcObjectRelationCfgExcelListener).sheet().doRead();

        } catch (Exception e) {
            log.error("导入项目信息点管理范围配置出现异常,msg:{}", e);
        }
        return R.success(LocaleMessageUtil.getMessage("key_2a35069ae93d42e7aa04fa4f10d9e0f4"));
    }

    @Operation(summary = "导入设备半剖图配置数据")
    @PostMapping(value = "/uploadClassPositionCodeConfig")
    public R<String> uploadClassPositionCodeConfig(@RequestParam("file") MultipartFile file) {
        try {
            ClassPositionCodeConfigExcelListener classPositionCodeConfigExcelListener = new ClassPositionCodeConfigExcelListener(this.classPositionCodeConfigService);
            EasyExcel.read(file.getInputStream(), ClassPositionCodeConfigExcel.class, classPositionCodeConfigExcelListener).sheet().doRead();

        } catch (Exception e) {
            log.error("导入项目信息点管理范围配置出现异常,msg:{}", e);
        }
        return R.success(LocaleMessageUtil.getMessage("key_2a35069ae93d42e7aa04fa4f10d9e0f4"));
    }

}
