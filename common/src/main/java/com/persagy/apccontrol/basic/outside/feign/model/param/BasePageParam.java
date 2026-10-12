package com.persagy.apccontrol.basic.outside.feign.model.param;

import lombok.Data;

import java.util.Collection;

@Data
public class BasePageParam<T> {

    /**
     * 要分页的数据
     */
    Collection<T> objects;

}
