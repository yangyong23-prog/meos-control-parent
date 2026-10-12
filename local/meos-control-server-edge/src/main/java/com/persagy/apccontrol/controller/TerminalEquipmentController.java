package com.persagy.apccontrol.controller;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import com.persagy.apccontrol.controller.vo.TerminalEquipmentVO;
import com.persagy.apccontrol.service.TerminalEquipmentService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 末端设备控制器
 *
 * @author MAF
 * @date 2023/12/12
 */
@RestController
@RequestMapping("/terminal-equipment")
public class TerminalEquipmentController {

    @Resource
    private TerminalEquipmentService terminalEquipmentService;


    /**
     * 后评估用
     * @param objectDTO
     * @return
     */
    @Operation(summary = "末端设备统计数", tags = {VersionConstant.V5_8_0})
    @PostMapping("/getTerminalStatistics")
    public R<List<TerminalEquipmentVO>> getTerminalStatistics(@RequestBody @Validated SingleObjectDTO objectDTO) {
        List<TerminalEquipmentVO> result = terminalEquipmentService.getTerminalStatistics(ContextUtil.getGroupCode(), objectDTO.getProjectId(), objectDTO.getObjectId());
        return R.success(result);
    }

}
