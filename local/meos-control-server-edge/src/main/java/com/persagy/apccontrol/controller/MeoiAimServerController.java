package com.persagy.apccontrol.controller;

import cn.hutool.core.util.StrUtil;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.common.handle.ResetLocalRedisCacheHandler;
import com.persagy.apccontrol.service.ILocalToSaasService;
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
    private ILocalToSaasService localToSaasService;

    @Autowired
    private ResetLocalRedisCacheHandler resetLocalRedisCacheHandler;

    @Operation(summary = "刷新本地缓存", tags = {VersionConstant.V5_7_0})
    @GetMapping("/flushConfig")
    public R<Boolean> flushConfig() throws Exception {
        this.resetLocalRedisCacheHandler.resetLocalRedisCache(true);
        return R.success(true);
    }

    @Operation(summary = "通用转发接口", tags = {VersionConstant.V5_7_0})
    @PostMapping("/common")
    public void common(@RequestBody @Validated CommonServerForwardVO forwardVO, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String contentType = StrUtil.isBlank(forwardVO.getContentType()) ? ContentType.APPLICATION_JSON.toString() : forwardVO.getContentType();

        String response = this.localToSaasService.meoiAimServerCommon(ContextUtil.getGroupCode(), forwardVO.getProjectId(), forwardVO.getMethod(), forwardVO.getUri(), forwardVO.getContentType(), forwardVO.getBody(), forwardVO.getParam());

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", contentType);
        outputStream.write(StrUtil.isBlank(response) ? JsonUtils.toJson(R.success(null)).getBytes() : response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
