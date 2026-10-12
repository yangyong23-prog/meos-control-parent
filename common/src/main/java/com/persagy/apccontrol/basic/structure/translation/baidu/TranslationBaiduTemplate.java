package com.persagy.apccontrol.basic.structure.translation.baidu;

import cn.hutool.http.HttpUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.enumtype.LanguageEnum;
import com.persagy.apccontrol.basic.structure.translation.BaseTranslationTemplate;
import com.persagy.apccontrol.basic.structure.translation.TranslationProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 百度翻译模版
 *
 * @author JiYinchuan
 * @since 7.250901.0
 */
public class TranslationBaiduTemplate extends BaseTranslationTemplate implements TranslationProvider {

    private static final String BASE_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    @Override
    public String translate(
            @NonNull String text,
            @NonNull LanguageEnum fromLanguage,
            @NonNull LanguageEnum toLanguage) {
        String appId = this.getAppId();
        String appSecret = this.getAppSecret();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("q", text);
        params.put("from", fromLanguage.value());
        params.put("to", toLanguage.value());

        params.put("appid", this.getAppId());
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);
        params.put("sign", DigestUtils.md5Hex(appId + text + salt + appSecret));
        JsonNode response = this.parseResponse(HttpUtil.get(BASE_URL, params));
        if (Objects.isNull(response)) {
            logProjectInstance().error("响应为空", Pair.of("params", params));
            return null;
        }
        JsonNode errorCode = response.get("error_code");
        JsonNode errorMessage = response.get("error_msg");
        if (Objects.nonNull(errorCode) && StringUtils.isNotBlank(errorCode.textValue())) {
            logProjectInstance().error("响应失败", Arrays.asList(
                    Pair.of("errorCode", errorCode.textValue()),
                    Pair.of("errorCode", errorMessage.textValue()),
                    Pair.of("params", params)
            ));
            return null;
        }
        JsonNode result = response.get("trans_result");
        if (Objects.isNull(result) || !result.isArray()) {
            return null;
        }
        JsonNode dst = result.get(0).get("dst");
        if (Objects.isNull(dst)) {
            return null;
        }
        return dst.textValue();
    }

    @Override
    public String logTag() {
        return "百度翻译";
    }
}
