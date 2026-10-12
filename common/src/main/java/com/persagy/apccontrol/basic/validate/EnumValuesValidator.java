package com.persagy.apccontrol.basic.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
/**
 * 自定义枚举参数校验
 *
 * @author yuecaipu
 * @date 2024/7/9 15:45
 * @since v5.240701
 */
public class EnumValuesValidator implements ConstraintValidator<EnumValues, Enum<?>> {
    private String[] acceptedValues;

    @Override
    public void initialize(EnumValues constraintAnnotation) {
        this.acceptedValues = constraintAnnotation.acceptedValues();
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        for (String acceptedValue : acceptedValues) {
            if (acceptedValue.equals(value.toString())) {
                return true;
            }
        }
        return false;
    }
}