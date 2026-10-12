package com.persagy.apccontrol.basic.outside.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 天气服务结果
 *
 * @author yx
 * @date 2023/3/29
 */
@Data
public class WeatherResult<T> implements Serializable {
    protected static final String SUCCESS = "success";
    protected static final String FAIL = "fail";

    /**
     * 结果
     */
    private List<T> content;
    /**
     * 原因
     */
    private String reason;
    /**
     * 结果
     */
    private String result;
    /**
     * 版本
     */
    private String version;

    public boolean isSuccess() {
        return StringUtils.equals(result, SUCCESS);
    }
}
