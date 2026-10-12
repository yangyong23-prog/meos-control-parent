package com.persagy.apccontrol.basic.controller.debugtool;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.param.debugtool.DebugToolObjectByClassCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.debugtool.DebugToolObjectInfoCodesQueryParam;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolTemplateTypeAndClassCodeVO;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolInfoCodeConfigStatusVO;
import com.persagy.apccontrol.basic.service.debugtool.DebugToolService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;

/**
 * 调试工具控制器
 *
 * @author JiYinchuan
 * @since 7.250102.0
 */
@RestController
@RequestMapping("/debugTool")
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DebugToolController {

    private final DebugToolService debugToolService;

    @Operation(summary = "查询-模版类型和设备类", tags = VersionConstant.V7_250102_0)
    @PostMapping("/listTemplateTypeAndClassCodes")
    public R<Collection<DebugToolTemplateTypeAndClassCodeVO>> listTemplateTypeAndClassCodes() {
        return R.successMulti(debugToolService.listTemplateTypeAndClassCodes());
    }

    @Operation(summary = "查询-根据设备类查询对象", tags = VersionConstant.V7_250102_0)
    @PostMapping("/listObjectsByClassCode")
    public R<Collection<DtpRwdObjectDTO>> listObjectsByClassCode(
            @RequestBody @Valid DebugToolObjectByClassCodeQueryParam param) {
        return R.successMulti(debugToolService.listObjectsByClassCode(param));
    }

    @Operation(summary = "查询-对象模板信息点", tags = VersionConstant.V7_250102_0)
    @PostMapping("/queryObjectInfoCodes")
    public R<Collection<DebugToolInfoCodeConfigStatusVO>> queryObjectInfoCodes(
            @RequestBody @Valid DebugToolObjectInfoCodesQueryParam param) {
        return R.successMulti(debugToolService.queryObjectInfoCodes(param));
    }

}
