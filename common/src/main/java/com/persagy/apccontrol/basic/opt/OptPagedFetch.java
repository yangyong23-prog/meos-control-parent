package com.persagy.apccontrol.basic.opt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * opt 分支 B10（switch.opt.feignPageFirstLimit）：不分页查询的首发也按 limit 分页。
 * <p>
 * 原逻辑首发 size=Integer.MAX_VALUE，下游不限流时一次返回全部数据。改为首发 (1, limit)，再按 total 拉剩余页，
 * 结果按页序拼接。下游不返回 total 且首页已满时无法判断是否还有数据，退回原逻辑（一次查全部），保证结果不丢。
 */
public final class OptPagedFetch {

    private OptPagedFetch() {
    }

    /**
     * @param limit      每页条数
     * @param call       (pageNo, pageSize) -> 响应
     * @param contentOf  取响应中的数据
     * @param totalOf    取响应中的总数，可为 null
     * @param setContent 回写合并后的数据（同时应更新 count）
     * @param legacyAll  原逻辑：一次查全部
     * @param executor   剩余页并行执行的线程池；为 null 时顺序执行
     */
    public static <R, T> R fetchAll(int limit,
                                    BiFunction<Integer, Integer, R> call,
                                    Function<R, List<T>> contentOf,
                                    Function<R, Integer> totalOf,
                                    BiConsumer<R, List<T>> setContent,
                                    Supplier<R> legacyAll,
                                    Executor executor) {
        R first = call.apply(1, limit);
        if (first == null) {
            return null;
        }
        List<T> content = contentOf.apply(first);
        Integer total = totalOf.apply(first);
        if (total == null) {
            if (content == null || content.size() < limit) {
                return first;
            }
            OptMetrics.incAlways("feign.page.noTotalFallback");
            return legacyAll.get();
        }
        if (content == null || content.size() >= total) {
            return first;
        }
        int pages = (int) ((total + (long) limit - 1) / limit);
        List<List<T>> rest = new ArrayList<>(pages);
        if (executor == null) {
            for (int p = 2; p <= pages; p++) {
                rest.add(page(call, contentOf, p, limit));
            }
        } else {
            List<CompletableFuture<List<T>>> futures = new ArrayList<>(pages);
            for (int p = 2; p <= pages; p++) {
                final int pageNo = p;
                futures.add(CompletableFuture.supplyAsync(() -> page(call, contentOf, pageNo, limit), executor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            futures.forEach(f -> rest.add(f.join()));
        }
        List<T> all = new ArrayList<>(total);
        all.addAll(content);
        rest.forEach(all::addAll);
        setContent.accept(first, all);
        OptMetrics.inc("feign.page.limitedFirst");
        return first;
    }

    private static <R, T> List<T> page(BiFunction<Integer, Integer, R> call, Function<R, List<T>> contentOf, int pageNo, int size) {
        R r = call.apply(pageNo, size);
        List<T> c = r == null ? null : contentOf.apply(r);
        return c == null ? new ArrayList<>() : c;
    }
}
