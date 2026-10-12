package com.persagy.apccontrol.basic.controller;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.base.domain.model.ClassCodeDefineModel;
import com.persagy.apccontrol.basic.model.base.domain.model.InfoCodeDefineModel;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeClassCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeInfoCodeBatchQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpKnowledgeInfoCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.knowledge.ObjectsExistRuleQueryParam;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.service.knowledge.BizDtpKnowledgeService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 数据字典控制器
 *
 * @author JiYinchuan
 * @since 24Q1 T3S1-5.17.0
 */
@Tag(name = "数据字典控制器")
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpRwdKnowledgeController {

    private final BizDtpKnowledgeService bizDtpKnowledgeService;

    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);

    @Operation(summary = "查询-设备类编码定义", tags = VersionConstant.V5_241101_0)
    @PostMapping("/listClassCodes")
    public R<List<ClassCodeDefineModel>> listClassCodes(
            @RequestBody @Valid DtpKnowledgeClassCodeQueryParam param) {
        List<ClassCodeDefineModel> classCodeDefineModels = bizDtpKnowledgeService.listClassCodes(param);
        if (classCodeDefineModels != null) {
            classCodeDefineModels.sort(Comparator.comparing(
                model -> model != null ? model.getName() : null, 
                Comparator.nullsFirst(COLLATOR))
            );
        }
        return R.success(classCodeDefineModels);
    }

    @Operation(summary = "查询-信息点编码定义", tags = VersionConstant.V7_250601_0)
    @PostMapping("/getInfoCode")
    public R<InfoCodeDefineModel> getInfoCode(@RequestBody @Valid DtpKnowledgeInfoCodeQueryParam param) {
        return R.success(bizDtpKnowledgeService.getInfoCode(param));
    }

    @Operation(summary = "查询-信息点编码定义", tags = VersionConstant.V5_17_0)
    @PostMapping("/listInfoCodes")
    public R<List<InfoCodeDefineModel>> listInfoCodes(@RequestBody @Valid DtpKnowledgeInfoCodeBatchQueryParam param) {
        return R.success(bizDtpKnowledgeService.listInfoCodes(param));
    }

    @Operation(summary = "查询-是否有绑点信息", tags = VersionConstant.V5_240902_0)
    @PostMapping("/queryObjectsExistRule")
    public R<List<ObjectsExistRule>> queryObjectsExistRule(
            @RequestBody @Valid ObjectsExistRuleQueryParam param) {
        return R.success(bizDtpKnowledgeService.queryObjectsExistRule(param));
    }
}
