package com.persagy.apccontrol.basic.opt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * opt 分支 B10（switch.opt.feignPageFirstLimit）：不分页查询的首发也按 limit 分页。
 * <p>
 * 原逻辑首发 size=Integer.MAX_VALUE，下游不限流时一次返回全部数据。改为首发 (1, limit)，再按 total 拉剩余页，
 * 结果按页序拼接。下游不返回 total 且首页已满时无法判断是否还有数据，退回原逻辑（一次查全部），保证结果不丢。
 * <p>
 * 分页结果依赖下游排序稳定、两页之间数据不变，否则会重复或漏行。开关取 {@code verify} 时仍返回原逻辑结果，
 * 同时分页再查一次比对条数（{@link #verify}），确认 {@code feign.page.verifyMismatch} 为 0 后再取 {@code true}。
 */
public final class OptPagedFetch {

    private static final Logger LOG = LoggerFactory.getLogger(OptPagedFetch.class);
    private static final AtomicLong LAST_MISMATCH_LOG = new AtomicLong();

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

    /** 开关取值：verify 时返回原逻辑结果并比对 */
    public static final String MODE_VERIFY = "verify";

    /**
     * verify 模式：返回值总是原逻辑结果；另用分页方式查一次，比对条数并记指标，分页失败不影响返回。
     *
     * @param legacy    原逻辑（一次查全部）的结果
     * @param paged     分页方式查询
     * @param contentOf 取响应中的数据
     * @param tag       日志标识
     * @return legacy 原样返回
     */
    public static <R, T> R verify(R legacy, Supplier<R> paged, Function<R, List<T>> contentOf, String tag) {
        int expected = sizeOf(legacy, contentOf);
        int actual;
        try {
            actual = sizeOf(paged.get(), contentOf);
        } catch (RuntimeException e) {
            OptMetrics.incAlways("feign.page.verifyError");
            return legacy;
        }
        if (expected == actual) {
            OptMetrics.incAlways("feign.page.verifyMatch");
        } else {
            OptMetrics.incAlways("feign.page.verifyMismatch");
            long now = System.currentTimeMillis();
            long last = LAST_MISMATCH_LOG.get();
            if (now - last >= 60_000L && LAST_MISMATCH_LOG.compareAndSet(last, now)) {
                LOG.warn("[opt] 分页首发比对不一致 {}：原逻辑 {} 条，分页 {} 条（每分钟最多记一条）", tag, expected, actual);
            }
        }
        return legacy;
    }

    private static <R, T> int sizeOf(R r, Function<R, List<T>> contentOf) {
        List<T> c = r == null ? null : contentOf.apply(r);
        return c == null ? 0 : c.size();
    }

    private static <R, T> List<T> page(BiFunction<Integer, Integer, R> call, Function<R, List<T>> contentOf, int pageNo, int size) {
        R r = call.apply(pageNo, size);
        List<T> c = r == null ? null : contentOf.apply(r);
        return c == null ? new ArrayList<>() : c;
    }
}
