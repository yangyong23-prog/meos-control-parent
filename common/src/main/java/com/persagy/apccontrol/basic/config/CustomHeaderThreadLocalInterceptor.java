package com.persagy.apccontrol.basic.config;

import cn.hutool.core.map.MapUtil;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.web.WebUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * 自定义 header 处理，在 com.persagy.framework.common.interceptor.HeaderThreadLocalInterceptor 之后执行即可
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/11/5 13:51
 */
@Order(10)
public class CustomHeaderThreadLocalInterceptor implements HandlerInterceptor {

    public CustomHeaderThreadLocalInterceptor() {

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        } else {
            if (ContextUtil.getBoot()) {
                HashMap<String, Object> extra = MapUtil.of(CommonConst.MODEL_TYPE_HEADER, WebUtils.getHeader(request, CommonConst.MODEL_TYPE_HEADER));
                extra.put(CommonConst.IPDATA_START_TIME, WebUtils.getHeader(request, CommonConst.IPDATA_START_TIME));
                extra.put(CommonConst.IPDATA_END_TIME, WebUtils.getHeader(request, CommonConst.IPDATA_END_TIME));
                extra.put(CommonConst.IPTYPE, WebUtils.getHeader(request, CommonConst.IPTYPE));
                extra.put(CommonConst.RUNNING_MONITOR_TYPE, WebUtils.getHeader(request, CommonConst.RUNNING_MONITOR_TYPE));
                ContextUtil.getContext().setExtra(extra);
            }
        }
        return true;
    }

}
