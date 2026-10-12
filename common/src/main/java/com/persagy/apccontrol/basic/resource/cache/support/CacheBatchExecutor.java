package com.persagy.apccontrol.basic.resource.cache.support;

import com.google.common.collect.Lists;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 缓存资源层固定大小分片执行器。
 *
 * <p>执行器只负责生成稳定的集合快照并同步执行每个分片，事务边界、持久化默认字段和具体
 * Mapper 操作仍由调用方负责。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public final class CacheBatchExecutor {

    /**
     * 单个缓存批次的最大记录数。
     *
     * <p>固定为 1000 条，在减少批量 DML 数据库往返次数的同时，继续限制多值 SQL
     * 长度和事务内单批内存占用。</p>
     *
     * @since v7.260701.0
     */
    private static final int BATCH_SIZE = 1000;

    /**
     * 禁止创建无状态分片执行器实例。
     *
     * @since v7.260701.0
     */
    private CacheBatchExecutor() {
        super();
    }

    /**
     * 按固定大小同步执行集合分片。
     *
     * <p>空集合不会执行回调。方法先复制调用方集合，避免执行期间原集合变化导致分片边界漂移。</p>
     *
     * @param records         待分片记录
     * @param partitionAction 单个分片的同步处理动作
     * @param <T>             记录类型
     * @since v7.260701.0
     */
    public static <T> void execute(
            @NonNull Collection<T> records,
            @NonNull Consumer<List<T>> partitionAction) {
        for (List<T> partition : Lists.partition(
                new ArrayList<>(records), CacheBatchExecutor.BATCH_SIZE)) {
            partitionAction.accept(partition);
        }
    }
}
