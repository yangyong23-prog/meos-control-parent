package com.persagy.apccontrol.basic.resource.cache.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 缓存批量写入分片边界测试。
 *
 * @author JiYinchuan
 * @since v7.260802.0
 */
public class CacheBatchExecutorTest {

    /**
     * 验证空集合不回调，1000 条以内单批执行，超过边界后保持输入顺序分片。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldHonorBatchBoundaryAndPreserveOrder() {
        AtomicInteger emptyCalls = new AtomicInteger();
        CacheBatchExecutor.execute(Collections.emptyList(), records -> emptyCalls.incrementAndGet());
        Assertions.assertEquals(0, emptyCalls.get());

        List<Integer> oneRecord = Collections.singletonList(0);
        List<List<Integer>> oneRecordPartitions = new ArrayList<>();
        CacheBatchExecutor.execute(oneRecord,
                records -> oneRecordPartitions.add(new ArrayList<>(records)));
        Assertions.assertEquals(Collections.singletonList(oneRecord), oneRecordPartitions);

        List<Integer> oneThousandRecords = IntStream.range(0, 1000)
                .boxed()
                .collect(Collectors.toList());
        List<List<Integer>> oneThousandPartitions = new ArrayList<>();
        CacheBatchExecutor.execute(oneThousandRecords,
                records -> oneThousandPartitions.add(new ArrayList<>(records)));
        Assertions.assertEquals(Collections.singletonList(oneThousandRecords),
                oneThousandPartitions);

        List<Integer> oneThousandOneRecords = IntStream.range(0, 1001)
                .boxed()
                .collect(Collectors.toList());
        List<List<Integer>> oneThousandOnePartitions = new ArrayList<>();
        CacheBatchExecutor.execute(oneThousandOneRecords,
                records -> oneThousandOnePartitions.add(new ArrayList<>(records)));
        Assertions.assertEquals(Arrays.asList(1000, 1), oneThousandOnePartitions.stream()
                .map(List::size)
                .collect(Collectors.toList()));
        Assertions.assertEquals(oneThousandOneRecords, oneThousandOnePartitions.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    /**
     * 验证后续分片异常直接向调用方传播，不继续执行剩余批次。
     *
     * @since v7.260802.0
     */
    @Test
    public void shouldPropagateLaterPartitionFailure() {
        List<Integer> records = IntStream.range(0, 2001)
                .boxed()
                .collect(Collectors.toList());
        IllegalStateException failure = new IllegalStateException("second partition failed");
        AtomicInteger calls = new AtomicInteger();

        IllegalStateException actual = Assertions.assertThrows(
                IllegalStateException.class,
                () -> CacheBatchExecutor.execute(records, partition -> {
                    if (calls.incrementAndGet() == 2) {
                        throw failure;
                    }
                }));

        Assertions.assertSame(failure, actual);
        Assertions.assertEquals(2, calls.get());
    }
}
