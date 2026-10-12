package com.persagy.apccontrol.basic.utils.feign;

import feign.Request;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.lang.NonNull;

import java.nio.charset.Charset;

/**
 * Feign工具类
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
public class FeignUtils {

    /**
     * 解析请求体字符串
     *
     * @param request 请求
     * @return 请求体字符串
     * @since v5.1.0
     */
    public static String parseRequestBody(@NonNull Request request) {
        byte[] body = request.body();
        Charset charset = request.charset();
        boolean binary = request.isBinary();
        if (binary) {
            return "File";
        }
        if (ArrayUtils.isEmpty(body)) {
            return null;
        }
        return new String(body, charset);
    }
}
