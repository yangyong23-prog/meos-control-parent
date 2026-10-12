package com.persagy.apccontrol.interceptor;

import com.persagy.apccontrol.service.ITokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * token过滤器
 *
 * @author shaohongbo
 * @date 2023/3/27 21:02
 */
@Slf4j
public class LoginTokenInterceptor implements HandlerInterceptor {

    private static final String TOKEN = "TOKEN";
    private static final String LONGIN_TYPE = "LOGIN-TYPE";


    @Autowired
    private ITokenService iTokenService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handler1 = (HandlerMethod) handler;
        if ("org.springdoc.webmvc.ui.SwaggerConfigResource".equals(handler1.getBeanType().getName()) ||
                "org.springdoc.webmvc.api.MultipleOpenApiWebMvcResource".equals(handler1.getBeanType().getName())
        ) {
            return true;
        }

        String token = request.getHeader(TOKEN);
        String loginType = request.getHeader(LONGIN_TYPE);
        // LOGIN-TYPE 先去掉，因为2D系统图数据的转发，无法传递这个header
       /* if (StrUtil.isNotBlank(loginType) && !iTokenService.verifyToken(token, loginType)) {
            throw BusinessExceptionUtil.exception(BusinessErrorCodeConstants.BUSINESS_ERROR_K0003);
        }*/
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
