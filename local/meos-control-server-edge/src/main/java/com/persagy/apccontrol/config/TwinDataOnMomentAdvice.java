package com.persagy.apccontrol.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.framework.common.entity.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @description: IOT统一拦截供前端计算过期时间
 * @author: wxf
 * @create: 2024-07-24 14:40
 **/
@RestControllerAdvice
@Slf4j
public class TwinDataOnMomentAdvice implements ResponseBodyAdvice {
    @Autowired
    private DtpTwinClientService dtpTwinClientService;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtil.isNull(servletRequestAttributes))
            return body;

        HttpServletRequest httpRequest = servletRequestAttributes.getRequest();
        if (ObjectUtil.isNull(httpRequest))
            return body;

        List<DtpTwinIotData> dtpTwinIotDataList = dtpTwinClientService.getTwinDataOnMoment(httpRequest);
        dtpTwinClientService.removeTwinDataOnMoment(httpRequest);
        if (CollectionUtil.isEmpty(dtpTwinIotDataList))
            return body;

        if (body instanceof R) {
            R rbody = (R) body;
            if (rbody.getIsSuccess()) {
                Map<String, List<DtpTwinIotData>> extra = Optional.ofNullable(rbody.getExtra()).orElseGet(HashMap::new);
                extra.put("expiringIots", dtpTwinIotDataList);
                rbody.setExtra(extra);
                return rbody;
            }
        }

        return body;
    }
}