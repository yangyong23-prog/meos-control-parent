package com.persagy.apccontrol.controller;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
/**
 * 调试控制器
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Slf4j
@Tag(name = "调试控制器")
@RestController
@RequestMapping("/debug")
public class EdgeDebugController {
    
    @Autowired
    private DtpRwdClientService dtpRwdClientService;
    
    @Operation(summary = "查询冷冻泵组")
    @PostMapping("/listFreezePumpGroup")
    public List<DtpRwdFreezePumpDTO> listFreezePumpGroup(
            @RequestParam String projectId, @RequestParam String coldSystemId) {
        return dtpRwdClientService.listFreezePumpGroup(false, true,
                projectId, Lists.newArrayList(coldSystemId), null, null, false);
    }

}
