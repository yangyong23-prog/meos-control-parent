package com.persagy.apccontrol.basic.model.base;


import com.persagy.apccontrol.basic.model.enumtype.system.OperateTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yuyao
 * @date 2022/4/13 16:15
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskTypeAnnotation {

    OperateTypeEnum value();

}
