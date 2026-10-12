package com.persagy.apccontrol.basic.outside.feign.model.param.weather.common;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.persagy.apccontrol.basic.model.base.BaseCheckParamProvider;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ValidationException;

/**
 * 天气服务公共查询参数
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
@Data
public class WeatherCommonQueryParam implements BaseCheckParamProvider {

    /**
     * 城市编码, 优先级最高
     */
    private String cityCode;

    /**
     * 项目ID, 优先级高
     */
    private String projectId;

    /**
     * 城市名称, 优先级低
     */
    private String cityName;

    /**
     * AppId
     */
    protected String appId = "meos";

    @Override
    public void checkAndSet() {
        if (StringUtils.isAllBlank(cityCode, projectId, cityName)) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_24a6d06e4bf64d5db54b93f19e1f249a"));
        }
    }
}
