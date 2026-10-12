package com.persagy.apccontrol.basic.utils;

import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 参数校验工具
 *
 * @author yuyao
 */
@Slf4j
public class ParameterCheck {

    /**
     * 判断方法
     *
     * @param isTrue       是否为true
     * @param errorMessage 为true的异常信息
     */
    public static void isTrue(boolean isTrue, String errorMessage) {
        if (isTrue) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param isFalse      是否为 False
     * @param errorMessage 为true的异常信息
     */
    public static void isFalse(boolean isFalse, String errorMessage) {
        if (!isFalse) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }


    /**
     * 判断方法
     *
     * @param obj          是否为null
     * @param errorMessage 为true的异常信息
     */
    public static void isNull(Object obj, String errorMessage) {
        if (obj == null) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param obj          是否为null
     * @param errorMessage 为true的异常信息
     */
    public static void isNullByString(String obj, String errorMessage) {
        if (StringUtils.isBlank(obj)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param list         是否为null
     * @param errorMessage 为true的异常信息
     */
    public static void ListIsNull(List list, String errorMessage) {
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param list         是否不为null
     * @param errorMessage 为true的异常信息
     */
    public static void ListIsNotNull(List list, String errorMessage) {
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param obj          是否不为空
     * @param errorMessage 为true的异常信息
     */
    public static void isNotNull(Object obj, String errorMessage) {
        if (obj != null) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param isTrue       是否为true
     * @param errorMessage 为true的异常信息
     */
    public static void isTrueByBoolean(Boolean isTrue, String errorMessage) {
        if (isTrue == null || isTrue) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param isTrue       是否为false
     * @param errorMessage 为true的异常信息
     */
    public static void isFalseByBoolean(Boolean isTrue, String errorMessage) {
        if (!isTrue) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * @description: 判断参数是否大于0
     * @author 张延昌
     * @date 2022/4/13 8:44 下午
     * @version 1.0
     */
    public static void isGTZero(Double d, String errorMessage) {
        if (d == null || d <= 0) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * @param date1
     * @param date2
     * @return boolean
     * @description: 时间校验先后顺序
     * @date 2022/8/26 18:35
     * @author cs
     */
    public static void isDateBefore(Date date1, Date date2, String errorMessage) {
        boolean b = false;
        if (date1 != null && date2 != null) {
            b = date1.before(date2);
        }

        if (!b) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }


    /**
     * 判断备注或者流水号是否超过50个字
     *
     * @author yx
     * @date 2023年1月4日16:02:48
     */
    public static void checkNoteOrFlowNumber(String note, String errorMessage) {
        if (!StringUtils.isBlank(note) && note.trim().length() > 50) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }

    /**
     * 判断方法
     *
     * @param list         是否不为null
     * @param errorMessage 为true的异常信息
     */
    public static void listIsNotNull(List list, String errorMessage) {
        if (!CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage);
        }
    }


}
