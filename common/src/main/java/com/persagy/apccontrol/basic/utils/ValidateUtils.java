package com.persagy.apccontrol.basic.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.lang.NonNull;

import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 校验工具类
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
public class ValidateUtils {

    /**
     * 校验参数
     *
     * @param object 校验对象
     * @param groups 校验分组
     * @throws BusinessException 业务异常
     * @since 5.240402.0
     */
    public static <T> void validate(@NonNull T object, Class<?>... groups)
            throws ValidationException {
        List<String> userTips = new ArrayList<>();
        Set<ConstraintViolation<T>> constraintViolations = SpringUtil.getBean(Validator.class).validate(object, groups);
        if (CollectionUtils.isNotEmpty(constraintViolations)) {
            for (ConstraintViolation<T> constraintViolation : constraintViolations) {
                userTips.add(constraintViolation.getMessage());
            }
            throw new ValidationException(String.join(",", userTips));
        }
    }

    /**
     * 校验参数
     *
     * @param objects 校验对象
     * @param groups  校验分组
     * @throws BusinessException 业务异常
     * @since 5.240402.0
     */
    public static <T> void validate(@NonNull List<T> objects, Class<?>... groups)
            throws ValidationException {
        objects.forEach(item -> validate(item, groups));
    }
}
