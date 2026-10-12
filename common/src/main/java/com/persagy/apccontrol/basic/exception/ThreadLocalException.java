package com.persagy.apccontrol.basic.exception;

import com.persagy.apccontrol.basic.exception.enumeration.ThreadLocalErrorEnum;
import com.persagy.validate.utility.exception.ApplicationException;

/**
 * ThreadLocal自定义异常
 *
 * @author JiYinchuan
 * @see ThreadLocalErrorEnum
 * @since v5.0.0
 */
public class ThreadLocalException extends ApplicationException {

    private static final long serialVersionUID = -5180519574894804740L;

    public ThreadLocalException(ThreadLocalErrorEnum threadLocalErrorEnum) {
        super(threadLocalErrorEnum);
    }

    public ThreadLocalException(ThreadLocalErrorEnum threadLocalErrorEnum, String userTip) {
        super(threadLocalErrorEnum, userTip);
    }
}
