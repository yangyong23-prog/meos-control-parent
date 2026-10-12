package com.persagy.apccontrol.basic.resource.param;

import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.BaseDO;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.util.*;

/**
 * 排序Dao参数
 *
 * @author JiYinchuan
 * @since v5.8.0
 */
@Data
public class OrderByDaoParam<T extends BaseDO> {

    /**
     * 字段列表
     */
    private List<OrderBy<T>> orderBys;

    /**
     * 构造器
     *
     * @since 5.9.0
     */
    public OrderByDaoParam() {
    }

    /**
     * 构造器
     *
     * @param columns 字段列表
     * @since v5.9.0
     */
    @SafeVarargs
    public OrderByDaoParam(SFunction<T, Object>... columns) {
        this(true, columns);
    }

    /**
     * 构造器
     *
     * @param isAsc   是否升序
     * @param columns 字段列表
     * @since v5.9.0
     */
    @SafeVarargs
    public OrderByDaoParam(boolean isAsc, SFunction<T, Object>... columns) {
        this.orderBys = Lists.newArrayList(new OrderBy<>(isAsc, columns));
    }

    @SafeVarargs
    public final OrderByDaoParam<T> orderByDesc(SFunction<T, Object>... columns) {
        this.orderBys = Optional.ofNullable(this.orderBys).orElseGet(ArrayList::new);
        this.orderBys.add(new OrderBy<>(false, columns));
        return this;
    }

    @SafeVarargs
    public final OrderByDaoParam<T> orderByAsc(SFunction<T, Object>... columns) {
        this.orderBys = Optional.ofNullable(this.orderBys).orElseGet(ArrayList::new);
        this.orderBys.add(new OrderBy<>(true, columns));
        return this;
    }

    @SafeVarargs
    public final OrderByDaoParam<T> orderBy(boolean isAsc, SFunction<T, Object>... columns) {
        this.orderBys = Optional.ofNullable(this.orderBys).orElseGet(ArrayList::new);
        this.orderBys.add(new OrderBy<>(isAsc, columns));
        return this;
    }

    /**
     * 获取排序SQL片段
     *
     * @return 排序SQL片段
     * @since v5.9.0
     */
    public String getOrderSqlSegment() {
        if (CollectionUtils.isEmpty(this.orderBys)) {
            return "";
        }
        final MergeSegments mergeSegments = new MergeSegments();
        for (OrderBy<T> orderBy : this.orderBys) {
            final SqlKeyword mode = orderBy.isAsc ? SqlKeyword.ASC : SqlKeyword.DESC;
            if (CollectionUtils.isNotEmpty(orderBy.columns)) {
                for (SFunction<T, Object> column : orderBy.columns) {
                    mergeSegments.add(SqlKeyword.ORDER_BY, () -> getColumnCache(column).getColumn(), mode);
                }
            }
        }
        return mergeSegments.getSqlSegment();
    }

    /**
     * 获取字段缓存
     *
     * @param column 列
     * @return 字段缓存
     * @since v5.9.0
     */
    private ColumnCache getColumnCache(SFunction<T, ?> column) {
        LambdaMeta meta = LambdaUtils.extract(column);
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        Class<?> instantiatedClass = meta.getInstantiatedClass();
        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(instantiatedClass);
        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(fieldName));
        Assert.notNull(columnCache, "can not find lambda cache for this property [%s] of entity [%s]",
                fieldName, instantiatedClass.getName());
        return columnCache;
    }

    static class OrderBy<T extends BaseDO> {

        /**
         * 是否升序
         */
        final boolean isAsc;

        /**
         * 字段列表
         */
        final List<SFunction<T, Object>> columns;


        /**
         * 构造器
         *
         * @param columns 字段列表
         * @since v5.9.0
         */
        @SafeVarargs
        OrderBy(SFunction<T, Object>... columns) {
            this(true, columns);
        }

        /**
         * 构造器
         *
         * @param isAsc   是否升序
         * @param columns 字段列表
         * @since v5.9.0
         */
        @SafeVarargs
        OrderBy(boolean isAsc, SFunction<T, Object>... columns) {
            this.isAsc = isAsc;
            this.columns = Arrays.asList(columns);
        }
    }
}
