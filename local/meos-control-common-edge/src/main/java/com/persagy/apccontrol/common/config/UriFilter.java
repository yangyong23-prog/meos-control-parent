package com.persagy.apccontrol.common.config;

import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @description:
 * @author: wxf
 * @create: 2024-10-25 18:45
 **/
@WebFilter(filterName = "uriFilter", urlPatterns = "/*", asyncSupported = true)
@Slf4j
public class UriFilter implements Filter {

    public static Map<String, AtomicInteger> ipCnt = new ConcurrentHashMap<>();
    private static final AtomicInteger cnt = new AtomicInteger(0);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 可以在这里进行初始化操作
    }

    public static List<String> statistics() {
        // 创建一个新的Map来存储每个键对应的比例值
        Map<String, Double> ratioMap = new ConcurrentHashMap<>();
        // 计算每个键对应的比例并存储到ratioMap中
        for (Map.Entry<String, AtomicInteger> entry : ipCnt.entrySet()) {
            String key = entry.getKey();
            AtomicInteger value = entry.getValue();
            double ratio = (double) value.get() / cnt.get();
            ratioMap.put(key, ratio);
        }
        List<String> r = new ArrayList<>();

        // 将ratioMap中的键值对转换为包含详细信息的字符串，并添加到列表r中
        for (Map.Entry<String, Double> entry : ratioMap.entrySet()) {
            r.add(entry.getKey()
                    + LocaleMessageUtil.getMessage("key_95f87e4bc3ff4afe9ec24688f8ef02de") + ipCnt.get(entry.getKey())
                    + LocaleMessageUtil.getMessage("key_3a96f82b28cf4276854e3e6ee569affa") + cnt.get() + LocaleMessageUtil.getMessage("key_20852d320b6d42daa78de5fcc77ab12e") + entry.getValue());
        }
        // 使用Comparator对列表r按照比例从高到低进行排序
        r.sort(Comparator.comparingDouble((String s) -> Double.parseDouble(s.split(LocaleMessageUtil.getMessage("key_20852d320b6d42daa78de5fcc77ab12e"))[1]))
                .reversed());
        return r;
    }

    @Override
    public void destroy() {
        // 可以在这里进行资源清理操作
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        StringBuffer ipCntVal = httpRequest.getRequestURL();
        if (ipCnt.get(ipCntVal.toString()) != null) {
            AtomicInteger atomicInteger = ipCnt.get(ipCntVal.toString());
            ipCnt.get(ipCntVal.toString()).set(atomicInteger.incrementAndGet());
        }else {
            AtomicInteger atomicInteger = new AtomicInteger(1);
            ipCnt.put(ipCntVal.toString(), atomicInteger);
        }
        cnt.incrementAndGet();

        StringBuilder info = new StringBuilder();
        info.append(httpRequest.getRemoteHost());
        info.append("____");
        info.append(httpRequest.getRemoteAddr());
        info.append("____");
        info.append(httpRequest.getHeader("user-agent"));
        info.append("____");
        info.append(httpRequest.getRequestURL());

        //LogExecutor.instance().error(log, "请求记录", info.toString());
        //LogExecutor.instance().error(log, "请求次数", String.valueOf(cnt.get()));
        // 继续过滤链
        chain.doFilter(request, response);
    }
}