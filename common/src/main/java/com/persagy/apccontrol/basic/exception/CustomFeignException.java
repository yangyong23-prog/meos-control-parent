package com.persagy.apccontrol.basic.exception;

import feign.FeignException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 自定义Feign异常
 *
 * @author JiYinchuan
 * @since 7.250702.0
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CustomFeignException extends FeignException {

    /**
     * 用户提示
     */
    private final String userTip;

    /**
     * 构造器
     *
     * @param e       FeignException
     * @param userTip 用户提示
     * @since 7.250702.0
     */
    public CustomFeignException(FeignException e, String userTip) {
        super(e.status(), e.getLocalizedMessage(), e.getCause());
        this.userTip = userTip;
    }
}
