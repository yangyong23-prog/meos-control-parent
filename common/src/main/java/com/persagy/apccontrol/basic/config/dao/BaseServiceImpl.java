package com.persagy.apccontrol.basic.config.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 封装 MybatisServiceImpl
 *
 * <p>该类为 {@link BaseService} 实现类, 与 {@link BaseService} 联合使用, 以替代 [Mybatis] 中的 {@link ServiceImpl}
 *
 * @author JiYinchuan
 * @see BaseService
 * @since v6.0.0
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseAssignIdDO> extends ServiceImpl<M, T> implements BaseService<T> {

    @Autowired
    private IdentifierGenerator identifierGenerator;

    /**
     * 填充自定义批量新增默认值。
     *
     * <p>手写XML批量新增不会经过MyBatis-Plus标准insert流程，调用方需要先调用该方法，
     * 补齐 ASSIGN_ID 和 MetaObjectHandler 通常负责的主键及审计字段。
     *
     * @param entity 新增实体
     * @since v7.260701.0
     */
    protected void fillInsertDefaultValue(@NonNull T entity) {
        if (Objects.isNull(entity.getId())) {
            entity.setId(this.identifierGenerator.nextId(entity).longValue());
        }
        // XML批量insert不会触发MyBatis-Plus MetaObjectHandler，这里只补缺失值，
        // 保留调用方已经显式设置过的审计字段。
        LocalDateTime now = DateUtils.now();
        if (Objects.isNull(entity.getCreateTime())) {
            entity.setCreateTime(now);
        }
        if (Objects.isNull(entity.getUpdateTime())) {
            entity.setUpdateTime(now);
        }
        if (Objects.isNull(entity.getCreateUser())) {
            entity.setCreateUser(ContextUtil.getUserId());
        }
        if (Objects.isNull(entity.getUpdateUser())) {
            entity.setUpdateUser(ContextUtil.getUserId());
        }
    }
}
