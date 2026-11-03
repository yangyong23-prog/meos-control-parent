package com.persagy.apccontrol.basic.opt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * opt 分支 B10：下游没有批量接口时，把“按 key 逐个远程查询”的循环改为并行查询。
 * <p>
 * 结果按 keys 的迭代顺序放入 LinkedHashMap（重复 key 只查一次），与顺序循环的结果一致。
 * 任一 key 失败时，等全部完成后按 key 顺序抛出第一个异常（原样，不包装），与顺序循环第一次失败抛出的异常相同。
 * executor 为 null 或只有一个 key 时在调用线程顺序执行。
 */
public final class OptParallelLookup {

    private OptParallelLookup() {
    }

    public static <K, V> Map<K, V> map(Collection<K> keys, Function<? super K, ? extends V> lookup, Executor executor) {
        Map<K, V> result = new LinkedHashMap<>();
        if (keys == null || keys.isEmpty()) {
            return result;
        }
        List<K> distinct = new ArrayList<>(new LinkedHashSet<>(keys));
        if (executor == null || distinct.size() == 1) {
            for (K k : distinct) {
                result.put(k, lookup.apply(k));
            }
            return result;
        }
        List<CompletableFuture<V>> futures = new ArrayList<>(distinct.size());
        for (K k : distinct) {
            futures.add(CompletableFuture.supplyAsync(() -> lookup.apply(k), executor));
        }
        RuntimeException first = null;
        for (int i = 0; i < distinct.size(); i++) {
            try {
                result.put(distinct.get(i), futures.get(i).get());
            } catch (ExecutionException e) {
                if (first == null) {
                    first = unwrap(e.getCause());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
        if (first != null) {
            throw first;
        }
        return result;
    }

    private static RuntimeException unwrap(Throwable t) {
        while (t instanceof CompletionException && t.getCause() != null) {
            t = t.getCause();
        }
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        if (t instanceof Error) {
            throw (Error) t;
        }
        return new IllegalStateException(t);
    }
}
