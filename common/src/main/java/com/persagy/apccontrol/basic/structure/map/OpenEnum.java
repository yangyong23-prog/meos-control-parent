package com.persagy.apccontrol.basic.structure.map;

import com.persagy.validate.utility.enumeration.base.BaseEnum;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 公开枚举
 *
 * <p>仅支持标记在实现了 {@link BaseEnum} 接口的 {@link Enum} 类上
 *
 * @author JiYinchuan
 * @see Enum
 * @see BaseEnum
 * @see OpenEnumKeyValue
 * @since v5.8.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface OpenEnum {

    /**
     * 自定义名称, 为空时与类名相同, 唯一值
     *
     * @return 自定义名称
     * @see #name()
     * @since v5.8.0
     */
    @AliasFor("name")
    String value() default "";

    /**
     * 自定义名称, 为空时与类名相同, 唯一值
     *
     * @return 自定义名称
     * @see #value()
     * @since v5.8.0
     */
    @AliasFor("value")
    String name() default "";

    /**
     * 是否序列化附加值
     *
     * @return 是否序列化附加值
     * @since v5.8.0
     */
    boolean serializeAdditionalValue() default false;

}
