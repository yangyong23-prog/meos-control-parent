package com.persagy.apccontrol.basic.model.base.domain.repository;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.*;

/**
 * 基础仓储查询参数
 *
 * @param <T> 查询字段所属对象类型
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Data
public abstract class BaseRepositoryQueryParam<T extends BaseDO> {

    /**
     * 查询日期, 不为 {@literal null} 且为历史日期时查询快照表, 否则查询当前配置
     */
    protected LocalDate date;

    /**
     * 查询字段, 为空时查询所有
     */
    protected LinkedHashSet<SFunction<T, ?>> selectColumns;

    /**
     * 添加查询字段
     *
     * @param columns 查询字段
     * @since 5.240902.0
     */
    @SafeVarargs
    public final void addColumns(@Nullable SFunction<T, ?>... columns) {
        if (ArrayUtils.isNotEmpty(columns)) {
            this.addColumns(Arrays.asList(Objects.requireNonNull(columns)));
        }
    }

    /**
     * 添加查询字段
     *
     * @param columns 查询字段
     * @since 5.240902.0
     */
    public final void addColumns(@Nullable Collection<SFunction<T, ?>> columns) {
        if (CollectionUtils.isNotEmpty(columns)) {
            if (CollectionUtils.isEmpty(this.selectColumns)) {
                this.selectColumns = new LinkedHashSet<>();
            }
            this.selectColumns.addAll(columns);
        }
    }

    /**
     * 转换查询字段
     *
     * @return 查询字段
     * @since 5.240902.0
     */
    public final String[] toSelectColumns() {
        if (CollectionUtils.isEmpty(selectColumns)) {
            return null;
        }
        int index = 0;
        String[] columns = new String[selectColumns.size()];
        for (SFunction<T, ?> selectColumn : selectColumns) {
            LambdaMeta meta = LambdaUtils.extract(selectColumn);
            String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
            Class<?> instantiatedClass = meta.getInstantiatedClass();
            Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(instantiatedClass);
            ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(fieldName));
            Assert.notNull(columnCache, "can not find lambda cache for this property [%s] of entity [%s]",
                    fieldName, instantiatedClass.getName());
            columns[index++] = columnCache.getColumn();
        }
        return columns;
    }

    /**
     * 转换查询字段
     *
     * @return 查询字段
     * @since 7.250801.0
     */
    public final @NonNull SFunction<@NonNull T, ?>[] toSelectFunctions() {
        if (CollectionUtils.isEmpty(this.selectColumns)) {
            return null;
        }
        // noinspection unchecked
        return this.selectColumns.toArray(new SFunction[0]);
    }
}
