package com.persagy.apccontrol.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.CheckDeviceInfoDTO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventEquipDTO;
import com.persagy.apccontrol.basic.service.MeoiAimServerService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.util.json.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 转发meoi-ai-server服务接口
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/2/20 16:40
 */
@Slf4j
@RestController
@Tag(name = "meoi-aim-server")
@RequestMapping(value = "/meoi-aim-server")
public class MeoiAimServerController {

    @Autowired
    private MeoiAimServerService meoiAimServerService;
    @Autowired
    private LocalRedisCacheHandler localRedisCacheHandler;

    @Operation(summary = "通用转发接口", tags = {VersionConstant.V5_7_0})
    @GetMapping("/flushConfig")
    public R<Boolean> flushConfig() {
        this.localRedisCacheHandler.resetGlobalLocalRedisCache(TerminalEnum.CLOUD, true);
        return R.success(true);
    }

    @Operation(summary = "通用转发接口", tags = {VersionConstant.V5_7_0})
    @PostMapping("/common")
    public void commonRoute(@RequestBody @Validated CommonServerForwardVO forwardVO, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        forwardVO.setGroupCode(ContextUtil.getGroupCode());
        String response = this.meoiAimServerService.commonRoute(forwardVO);

        String contentType = StrUtil.isBlank(forwardVO.getContentType()) ? ContentType.APPLICATION_JSON.toString() : forwardVO.getContentType();
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", contentType);
        outputStream.write(StrUtil.isBlank(response) ? JsonUtils.toJson(R.success(null)).getBytes() : response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @Operation(summary = "iot数据信息", tags = {VersionConstant.V5_7_0})
    @PostMapping("/queryRiskEventList")
    @Deprecated
    public R<List<ExceptionEventEquipDTO>> queryRiskEventList(@RequestBody @Validated CheckDeviceInfoDTO infoDTO) throws Exception {
        List<ExceptionEventEquipDTO> exceptionEventEquipDTOS = this.meoiAimServerService.queryIotAndRiskEventList(ContextUtil.getGroupCode(), infoDTO.getProjectId(), infoDTO);
        if (CollectionUtil.isEmpty(exceptionEventEquipDTOS)) {
            exceptionEventEquipDTOS = Lists.newArrayList();
        }
        R<List<ExceptionEventEquipDTO>> success = R.success(exceptionEventEquipDTOS);
        success.setTotal((long)exceptionEventEquipDTOS.size());
        return success;
    }

}
