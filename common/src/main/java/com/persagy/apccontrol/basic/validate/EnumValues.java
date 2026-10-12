package com.persagy.apccontrol.basic.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 枚举值限制
 *
 * @author yuecaipu
 * @date 2024/7/9 15:34
 * @since v5.240701
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValuesValidator.class)
public @interface EnumValues {
    String[] acceptedValues() default {};

    String message() default "枚举值参数不支持，请检查";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
