package com.persagy.apccontrol.basic.structure.translation;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.exception.enumeration.BusinessErrorEnum;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import com.persagy.validate.utility.exception.ApplicationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.Environment;

/**
 * 基础翻译模版
 *
 * @author JiYinchuan
 * @since 7.250901.0
 */
public abstract class BaseTranslationTemplate implements LogComponent {

    /**
     * 获取AppId
     *
     * @return AppId
     * @since 7.250901.0
     */
    protected String getAppId() {
        Environment environment = SpringUtil.getBean(Environment.class);
        String appId = environment.getProperty("translation.appid");
        if (StringUtils.isBlank(appId)) {
            throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR, "Can not find translation app id");
        }
        return appId;
    }

    /**
     * 获取AppSecret
     *
     * @return AppSecret
     * @since 7.250901.0
     */
    protected String getAppSecret() {
        Environment environment = SpringUtil.getBean(Environment.class);
        String appSecret = environment.getProperty("translation.secret");
        if (StringUtils.isBlank(appSecret)) {
            throw new ApplicationException(BusinessErrorEnum.SETTING_ERROR, "Can not find translation app secret");
        }
        return appSecret;
    }

    /**
     * 解析响应
     *
     * @param response 响应
     * @return JsonNode
     * @since 7.250901.0
     */
    protected JsonNode parseResponse(@Nullable String response) {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        try {
            return objectMapper.readValue(response, JsonNode.class);
        } catch (JsonProcessingException e) {
            logProjectInstance().error("解析响应失败", e, Pair.of("response", response));
            throw new ApplicationException(ErrorCodeConstants.SERVICE_ERROR_C0122, "Can not parse response");
        }
    }
}
