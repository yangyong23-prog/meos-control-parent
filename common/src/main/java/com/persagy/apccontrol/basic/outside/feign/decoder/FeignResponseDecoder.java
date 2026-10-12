package com.persagy.apccontrol.basic.outside.feign.decoder;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.config.FeignConfiguration;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.utils.feign.FeignUtils;
import com.persagy.framework.common.entity.R;
import feign.FeignException;
import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.event.Level;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Feign响应解码器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public class FeignResponseDecoder implements Decoder, LogComponent {

    private static final String SUCCESS = "success";

    private final SpringDecoder decoder;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    public FeignResponseDecoder(SpringDecoder decoder, ObjectProvider<ObjectMapper> objectMapperProvider) {
        this.decoder = decoder;
        this.objectMapperProvider = objectMapperProvider;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        Request request = response.request();
        Method method = request.requestTemplate().methodMetadata().method();
        if (ClassUtils.isAssignable(R.class, method.getReturnType())) {
            boolean isError = false;
            R<?> res = (R<?>) decoder.decode(response, type);
            if (Objects.isNull(res)) {
                isError = true;
                printLog(LocaleMessageUtil.getMessage("key_6ce58225521641f2936572c4cab1f93f"), Level.ERROR, request, null);
            } else if (res.getIsFail()) {
                isError = true;
                printLog(LocaleMessageUtil.getMessage("key_9fd7ee4ebc83423c92580c1aaa106bb3"), Level.ERROR, request, res);
            }
            if (isError) {
                throw new FeignException.FeignServerException(response.status(), res.getMessage(),
                        request, toResponseJsonBytes(request, res));
            }
            printLog(LocaleMessageUtil.getMessage("key_2f018f1086fd42a8bfbfb9ac09651922"), isTraceEnabled() ? Level.TRACE : Level.DEBUG, request, res);
            return res;
        } else if (ClassUtils.isAssignable(CustomerResponse.class, method.getReturnType())) {
            boolean isError = false;
            CustomerResponse<?> res = (CustomerResponse<?>) decoder.decode(response, type);
            if (Objects.isNull(res)) {
                isError = true;
                printLog(LocaleMessageUtil.getMessage("key_6ce58225521641f2936572c4cab1f93f"), Level.ERROR, request, null);
            } else if (!R.isSuccess(res.getResultCode()) && !SUCCESS.equals(res.getResult())) {
                isError = true;
                printLog(LocaleMessageUtil.getMessage("key_9fd7ee4ebc83423c92580c1aaa106bb3"), Level.ERROR, request, res);
            }
            if (isError) {
                throw new FeignException.FeignServerException(response.status(), res.getResultMsg(),
                        request, toResponseJsonBytes(request, res));
            }
            printLog(LocaleMessageUtil.getMessage("key_2f018f1086fd42a8bfbfb9ac09651922"), isTraceEnabled() ? Level.TRACE : Level.DEBUG, request, res);
            return res;
        } else {
            Object res = decoder.decode(response, type);
            if (ObjectUtils.isEmpty(res)) {
                printLog(LocaleMessageUtil.getMessage("key_6ce58225521641f2936572c4cab1f93f"), Level.DEBUG, request, null);
            } else {
                printLog(LocaleMessageUtil.getMessage("key_2f018f1086fd42a8bfbfb9ac09651922"), isTraceEnabled() ? Level.TRACE : Level.DEBUG, request, res);
            }
            return res;
        }
    }

    /**
     * 转换响应JSON字符
     *
     * @param request  请求参数
     * @param response 响应结果
     * @return 响应JSON字符
     * @since 5.241101.0
     */
    private byte[] toResponseJsonBytes(@NonNull Request request, @Nullable Object response) {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (Objects.nonNull(objectMapper)) {
            try {
                return objectMapper.writeValueAsBytes(response);
            } catch (JsonProcessingException e) {
                printLog(LocaleMessageUtil.getMessage("key_e8bd500756e743ddb467f542a391deac"), Level.ERROR, request, response);
                return null;
            }
        }
        return JSONObject.toJSONBytes(response);
    }

    /**
     * 打印日志
     *
     * @param logTag   日志标签
     * @param level    日志级别
     * @param request  请求参数
     * @param response 响应结果
     * @since v5.1.0
     */
    private void printLog(@NonNull String logTag, @NonNull Level level, @NonNull Request request, @Nullable Object response) {
        Map<String, Collection<String>> headers = request.headers();
        List<Pair<String, Object>> additionalMessages = Lists.newArrayList(Pair.of("url", request.url()));
        Collection<String> requestTimestamp = null;
        try {
            requestTimestamp = headers.get(FeignConfiguration.HEADER_REQUEST_TIMESTAMP);
            if (CollectionUtils.isNotEmpty(requestTimestamp)) {
                long timestamp = Long.parseLong(IterableUtils.first(requestTimestamp));
                additionalMessages.add(Pair.of("executeTime", (System.currentTimeMillis() - timestamp) + "ms"));
            }
        } catch (Exception e) {
            logProjectInstance().warn("解析请求执行时间失败", Pair.of("requestTimestamp", requestTimestamp));
        }
        additionalMessages.add(Pair.of("headers", headers));
        String logRequest = null;
        Object logResponse = null;
        // 为TRAC或ERROR或请求调试为TRACE时才打印请求和响应参数
        if (level == Level.TRACE || level == Level.ERROR || LocalContextStorage.isRequestDebug(Level.TRACE)) {
            logRequest = FeignUtils.parseRequestBody(request);
            logResponse = response;
        }
        LogExecutor.rpc(logRequest, logResponse).log(log(), level, logTag(), logTag, additionalMessages);
    }

    @Override
    public String logTag() {
        return "RPC Decoder";
    }
}
