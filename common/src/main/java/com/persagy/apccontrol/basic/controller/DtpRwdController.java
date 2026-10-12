package com.persagy.apccontrol.basic.controller;

import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.knowledge.DtpRwdExistsRuleInfoCodeQueryParam;
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
import java.util.Map;
import java.util.Set;

/**
 * 物理世界控制器
 *
 * @author JiYinchuan
 * @since 7.251202.0
 */
@Tag(name = "物理世界控制器")
@RestController
@RequestMapping("/rwd")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "rawtypes", "unchecked"})
public class DtpRwdController {

    private final AbstractIotDataCacheServer abstractIotDataCacheServer;

    @Operation(summary = "查询-对象ID和已存在规则信息点映射", tags = VersionConstant.V7_251202_0)
    @PostMapping("/mapObjectId2ExistsRuleInfoCodes")
    public R<Map<String, Set<String>>> mapObjectId2ExistsRuleInfoCodes(
            @RequestBody @Valid DtpRwdExistsRuleInfoCodeQueryParam param) {
        return R.success(abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(
                param.getClassCode2ObjectIds(), param.getInfoCodes()));
    }
}
