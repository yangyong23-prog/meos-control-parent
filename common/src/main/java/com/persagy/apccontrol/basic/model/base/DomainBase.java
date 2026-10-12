package com.persagy.apccontrol.basic.model.base;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author yuyao
 * @Date 2022/3/2 3:22 PM
 * @Description 通用顶级类
 */
public class DomainBase implements Serializable {

    private static final long serialVersionUID = -8750998217943844668L;

    /**
     * 统一toString 方法
     *
     * @return String
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
