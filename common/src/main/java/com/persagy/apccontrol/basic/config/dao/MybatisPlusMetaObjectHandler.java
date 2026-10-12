package com.persagy.apccontrol.basic.config.dao;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.SpringBootConfiguration;

import java.time.LocalDateTime;

/**
 * MybatisPlus自动填充配置
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
@SpringBootConfiguration
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 构造器
     *
     * @param objectMapper objectMapper
     * @since v5.8.0
     */
    public MybatisPlusMetaObjectHandler(ObjectMapper objectMapper) {
        JacksonTypeHandler.setObjectMapper(objectMapper);
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, BaseDO.FIELD_CREATE_TIME, DateUtils::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, BaseDO.FIELD_UPDATE_TIME, DateUtils::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, BaseDO.FIELD_CREATE_USER, ContextUtil::getUserId, String.class);
        this.strictInsertFill(metaObject, BaseDO.FIELD_UPDATE_USER, ContextUtil::getUserId, String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, BaseDO.FIELD_UPDATE_TIME, DateUtils::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, BaseDO.FIELD_UPDATE_USER, ContextUtil::getUserId, String.class);
    }
}
