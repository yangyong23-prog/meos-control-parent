package com.persagy.apccontrol.basic.config.dao.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Set;

/**
 * Jackson Set集合Long类型映射
 *
 * @author JiYinchuan
 * @since 5.241001.0
 */
public class JacksonSetWithLongTypeHandler extends JacksonTypeHandler {

    /**
     * 构造器
     *
     * @since 5.241001.0
     */
    public JacksonSetWithLongTypeHandler() {
        super(Set.class);
    }

    @Override
    protected Object parse(String json) {
        try {
            return getObjectMapper().readValue(json, new TypeReference<Set<Long>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
