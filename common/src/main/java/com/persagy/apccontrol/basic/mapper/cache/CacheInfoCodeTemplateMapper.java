package com.persagy.apccontrol.basic.mapper.cache;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.model.entity.cache.CacheInfoCodeTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * 缓存-信息点模版Mapper
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Mapper
@Repository
public interface CacheInfoCodeTemplateMapper extends BaseMapper<CacheInfoCodeTemplate> {

    /**
     * 使用单条多值 SQL 批量新增缓存信息点模板。
     *
     * @param templates 缓存信息点模板
     * @since v7.260701.0
     */
    void insertBatch(@NonNull @Param("templates") Collection<CacheInfoCodeTemplate> templates);
}
