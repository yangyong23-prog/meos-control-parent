package com.persagy.apccontrol.basic.opt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OptPagedFetchTest {

    /** 简化的响应：content + total */
    static class Resp {
        List<Integer> content;
        Integer total;

        Resp(List<Integer> content, Integer total) {
            this.content = content;
            this.total = total;
        }
    }

    private final List<int[]> calls = new ArrayList<>();
    private final AtomicInteger legacy = new AtomicInteger();

    /** 下游：共 n 条，按 (pageNo, size) 返回；cap>0 时单页最多 cap 条；withTotal=false 时不返回 total */
    private Resp downstream(int n, int pageNo, int size, int cap, boolean withTotal) {
        synchronized (calls) {
            calls.add(new int[]{pageNo, size});
        }
        int eff = cap > 0 ? Math.min(size, cap) : size;
        long from = (long) (pageNo - 1) * eff;
        List<Integer> c = IntStream.range((int) Math.min(from, n), (int) Math.min(from + eff, n)).boxed().collect(Collectors.toList());
        return new Resp(c, withTotal ? n : null);
    }

    private Resp fetch(int n, int limit, int cap, boolean withTotal, boolean parallel) {
        return OptPagedFetch.fetchAll(limit,
                (no, size) -> downstream(n, no, size, cap, withTotal),
                r -> r.content, r -> r.total, (r, all) -> r.content = all,
                () -> {
                    legacy.incrementAndGet();
                    return downstream(n, 1, Integer.MAX_VALUE, cap, withTotal);
                },
                parallel ? Executors.newFixedThreadPool(4) : null);
    }

    @Test
    void pagesInOrderWithoutMaxValueRequest() {
        Resp r = fetch(2500, 1000, 0, true, true);
        Assertions.assertEquals(IntStream.range(0, 2500).boxed().collect(Collectors.toList()), r.content, "按页序拼接，结果与一次查全部一致");
        Assertions.assertEquals(3, calls.size());
        Assertions.assertTrue(calls.stream().allMatch(c -> c[1] == 1000), "不再请求 Integer.MAX_VALUE");
    }

    @Test
    void singlePageAndSequentialMode() {
        Assertions.assertEquals(800, fetch(800, 1000, 0, true, false).content.size());
        Assertions.assertEquals(1, calls.size());
        calls.clear();
        Assertions.assertEquals(2001, fetch(2001, 1000, 0, true, false).content.size());
        Assertions.assertEquals(3, calls.size());
    }

    @Test
    void noTotalAndFullFirstPageFallsBackToLegacy() {
        Assertions.assertEquals(1500, fetch(1500, 1000, 0, false, false).content.size());
        Assertions.assertEquals(1, legacy.get());
        legacy.set(0);
        Assertions.assertEquals(300, fetch(300, 1000, 0, false, false).content.size(), "未满一页说明已取完");
        Assertions.assertEquals(0, legacy.get());
    }

    @Test
    void downstreamIgnoringPagingIsNotRefetched() {
        // 下游忽略 size 直接返回全部（content.size() >= total）
        Resp r = OptPagedFetch.fetchAll(1000, (no, size) -> downstream(1500, 1, Integer.MAX_VALUE, 0, true),
                x -> x.content, x -> x.total, (x, all) -> x.content = all, () -> null, null);
        Assertions.assertEquals(1500, r.content.size());
        Assertions.assertEquals(1, calls.size());
    }

    @Test
    void verifyReturnsLegacyAndCountsMismatch() {
        Resp legacyResp = new Resp(IntStream.range(0, 10).boxed().collect(Collectors.toList()), 10);
        OptMetrics.drain();

        Assertions.assertSame(legacyResp, OptPagedFetch.verify(legacyResp,
                () -> new Resp(new ArrayList<>(legacyResp.content), 10), r -> r.content, "t"));
        Assertions.assertEquals(Long.valueOf(1), OptMetrics.drain().get("feign.page.verifyMatch"));

        // 两页之间数据变化导致漏行
        Assertions.assertSame(legacyResp, OptPagedFetch.verify(legacyResp,
                () -> new Resp(legacyResp.content.subList(0, 9), 10), r -> r.content, "t"));
        Assertions.assertEquals(Long.valueOf(1), OptMetrics.drain().get("feign.page.verifyMismatch"));

        // 分页查询失败不影响返回
        Assertions.assertSame(legacyResp, OptPagedFetch.verify(legacyResp, () -> {
            throw new IllegalStateException("down");
        }, r -> r.content, "t"));
        Assertions.assertEquals(Long.valueOf(1), OptMetrics.drain().get("feign.page.verifyError"));
    }
}
