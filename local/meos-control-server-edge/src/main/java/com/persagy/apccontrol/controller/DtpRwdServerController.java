package com.persagy.apccontrol.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.vo.CommonServerForwardVO;
import com.persagy.apccontrol.service.ILocalToSaasService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.util.json.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 转发dtp-rwd-server服务接口
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/2/20 16:40
 */
@Slf4j
@RestController
@Tag(name = "dtp-rwd-server")
@RequestMapping(value = "/dtp-rwd-server")
public class DtpRwdServerController {
    
    @Value("${inner-url.bdtp.dtp-rwd.service:}")
    private String dtpRwdServer;

    @Autowired
    private ILocalToSaasService localToSaasService;

    @Operation(summary = "通用转发接口", tags = {VersionConstant.V5_7_0})
    @PostMapping("/common")
    public void commonRwd(@RequestBody @Validated CommonServerForwardVO forwardVO, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String contentType = StrUtil.isBlank(forwardVO.getContentType()) ? ContentType.APPLICATION_JSON.toString() : forwardVO.getContentType();

        String response = this.localToSaasService.dtpRwdServerCommon(ContextUtil.getGroupCode(), forwardVO.getProjectId(), forwardVO.getMethod(), forwardVO.getUri(), forwardVO.getContentType(), forwardVO.getBody(), forwardVO.getParam());

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", contentType);
        outputStream.write(StrUtil.isBlank(response) ? JsonUtils.toJson(R.success(null)).getBytes() : response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 请求本地的rwd服务，测试使用
     *
     * @param forwardVO
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws Exception
     */
    @PostMapping("/local")
    public void requestRwdServer(@RequestBody @Validated CommonServerForwardVO forwardVO, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String url = dtpRwdServer + (forwardVO.getUri().startsWith("/") ? forwardVO.getUri() : ("/" + forwardVO.getUri()));

        HttpRequest httpRequest = null;
        if (Method.GET.equals(forwardVO.getMethod())) {
            httpRequest = HttpUtil.createGet(url);
        } else if (Method.POST.equals(forwardVO.getMethod())) {
            httpRequest = HttpUtil.createPost(url);
        }
        if (httpRequest == null) {
            return;
        }

        String contentType = StrUtil.isBlank(forwardVO.getContentType()) ? ContentType.APPLICATION_JSON.toString() : forwardVO.getContentType();
        httpRequest.header("group-code", ContextUtil.getGroupCode()).header("groupCode", ContextUtil.getGroupCode())
                .header("project-id", forwardVO.getProjectId()).header("projectId", forwardVO.getProjectId()).timeout(180 * 1000);
        if (forwardVO.getBody() != null) {
            httpRequest.body(JsonUtils.toJson(forwardVO.getBody()), contentType);
        }
        if (CollectionUtil.isNotEmpty(forwardVO.getParam())) {
            httpRequest.form(forwardVO.getParam());
        }

        String response = httpRequest.execute().body();

        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        httpServletResponse.setHeader("Content-Type", contentType);
        outputStream.write(StrUtil.isBlank(response) ? JsonUtils.toJson(R.success(null)).getBytes() : response.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}
