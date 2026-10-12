package com.persagy.apccontrol.basic.config.annotation;

import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;

import java.lang.annotation.*;

/**
 * 信息点注解
 *
 * @author JiYinchuan
 * @since 7.250402.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface InfoCode {

    /**
     * 信息点名称
     *
     * @return 信息点名称
     * @since 7.250402.0
     */
    InfoCodeEnum value();

}
