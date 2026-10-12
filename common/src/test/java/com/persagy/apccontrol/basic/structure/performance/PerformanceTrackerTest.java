package com.persagy.apccontrol.basic.structure.performance;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.persagy.apccontrol.basic.config.cache.LocalContextStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PerformanceTrackerTest {

    @Test
    public void shouldSupportTryWithResourcesAndStepRecording() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        try (PerformanceTracker.SafeNode ignored = tracker.track("Root")) {
            ignored.recordStep("prepare");
            tracker.recordStep("execute");
        }

        Assertions.assertNotNull(tracker.getRoot());
        Assertions.assertEquals("Root", tracker.getRoot().getMethodName());
        Assertions.assertEquals(2, tracker.getRoot().getStepNames().size());
        Assertions.assertEquals("prepare", tracker.getRoot().getStepNames().get(0));
        Assertions.assertEquals("execute", tracker.getRoot().getStepNames().get(1));
    }

    @Test
    public void shouldSupportEnterWithTryWithResources() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        Assertions.assertFalse(AutoCloseable.class.isAssignableFrom(PerformanceTracker.class));

        try (PerformanceTracker.SafeNode node = tracker.enter("Root")) {
            node.recordStep("prepare");
        }

        Assertions.assertTrue(tracker.isFinished());
        Assertions.assertNotNull(tracker.getRoot());
        Assertions.assertEquals("Root", tracker.getRoot().getMethodName());
        Assertions.assertEquals("prepare", tracker.getRoot().getStepNames().get(0));
    }

    @Test
    public void shouldBindAndReadCurrentTrackerExplicitly() {
        PerformanceTracker.clear();

        Assertions.assertNull(PerformanceTracker.current());

        PerformanceTracker tracker = PerformanceTracker.bind("custom");
        Assertions.assertNotNull(tracker);
        Assertions.assertSame(tracker, PerformanceTracker.current());

        PerformanceTracker.clear();
        Assertions.assertNull(PerformanceTracker.current());
    }

    @Test
    public void shouldReturnDetachedTrackerWithoutBindingThreadLocalWhenNoCurrentTrackerExists() {
        PerformanceTracker.clear();

        PerformanceTracker tracker = PerformanceTracker.currentOrDetached("detached");

        Assertions.assertNotNull(tracker);
        Assertions.assertEquals("detached", tracker.getId());
        Assertions.assertNull(PerformanceTracker.current());
    }

    @Test
    public void shouldNotExposeDeprecatedPerformanceTrackerApis() {
        long deprecatedApiCount = Arrays.stream(LocalContextStorage.class.getDeclaredMethods())
                .map(Method::getName)
                .filter("performanceTracker"::equals)
                .count();

        Assertions.assertEquals(0, deprecatedApiCount);
    }

    @Test
    public void shouldSupportNestedTrackingAcrossMethodsLoopAndRecursion() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        try (PerformanceTracker.SafeNode ignored = tracker.track("API")) {
            invokeChainA(tracker, 2);
            invokeRecursive(tracker, 2);
        }

        MethodCallNode root = tracker.getRoot();
        Assertions.assertNotNull(root);
        Assertions.assertEquals("API", root.getMethodName());
        Assertions.assertEquals(3, root.getChildren().size());
        Assertions.assertEquals("A", root.getChildren().get(0).getMethodName());
        Assertions.assertEquals("A", root.getChildren().get(1).getMethodName());
        Assertions.assertEquals("Recursive-2", root.getChildren().get(2).getMethodName());
        Assertions.assertEquals("B", root.getChildren().get(0).getChildren().get(0).getMethodName());
        Assertions.assertEquals("C", root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getMethodName());
        Assertions.assertEquals("Recursive-1", root.getChildren().get(2).getChildren().get(0).getMethodName());
    }

    @Test
    public void shouldCloseScopeAndKeepCollectedDataWhenBusinessThrowsException() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        try {
            try (PerformanceTracker.SafeNode ignored = tracker.track("API")) {
                tracker.recordStep("before-error");
                throw new IllegalStateException("boom");
            }
        } catch (IllegalStateException exception) {
            Assertions.assertEquals("boom", exception.getMessage());
        }

        Assertions.assertTrue(tracker.isFinished());
        Assertions.assertNotNull(tracker.getRoot());
        Assertions.assertEquals("API", tracker.getRoot().getMethodName());
        Assertions.assertEquals("before-error", tracker.getRoot().getStepNames().get(0));
    }

    @Test
    public void shouldRenderTreeAndFlatOutputsWithTimestamps() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        try (PerformanceTracker.SafeNode ignored = tracker.track("API")) {
            tracker.recordStep("prepare");
            try (PerformanceTracker.SafeNode nested = tracker.track("Service")) {
                tracker.recordStep("execute");
            }
        }

        String tree = tracker.prettyPrintTree();
        String flat = tracker.prettyPrintFlat();

        Assertions.assertTrue(tree.contains("[API]"));
        Assertions.assertTrue(tree.contains("[Service]"));
        Assertions.assertTrue(flat.contains("depth=0"));
        Assertions.assertTrue(flat.contains("depth=1"));
        Assertions.assertTrue(flat.contains("start="));
        Assertions.assertTrue(flat.contains("end="));
        Assertions.assertTrue(flat.contains("async_wait="));
    }

    @Test
    public void shouldKeepMainThreadAndAsyncThreadSafeStacksIsolated() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        ExecutorService rawExecutor = Executors.newSingleThreadExecutor();
        Executor executor = TtlExecutors.getTtlExecutor(rawExecutor);
        CountDownLatch asyncEntered = new CountDownLatch(1);
        CountDownLatch allowAsyncExit = new CountDownLatch(1);

        try {
            // 主线程先建立 Root
            try (PerformanceTracker.SafeNode root = tracker.track("Root")) {
                tracker.recordStep("主线程-开始");

                // 子线程在 Root 之后 enter（safeNodeStack 为空，走 doEnter）
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (PerformanceTracker.SafeNode asyncNode = tracker.enter("AsyncTask")) {
                        asyncEntered.countDown();
                        try {
                            Assertions.assertTrue(allowAsyncExit.await(3, TimeUnit.SECONDS));
                        } catch (InterruptedException exception) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(exception);
                        }
                    }
                }, executor);

                Assertions.assertTrue(asyncEntered.await(3, TimeUnit.SECONDS));

                // 主线程继续内层 enter
                try (PerformanceTracker.SafeNode mainNode = tracker.enter("MainTask")) {
                    Assertions.assertNotNull(mainNode);
                }

                tracker.recordStep("主线程-结束");

                allowAsyncExit.countDown();
                future.get(3, TimeUnit.SECONDS);

                // TTL callStack 隔离：子线程的 AsyncTask 走独立的 callStack，不挂载到主线程树下
                List<String> childMethodNames = tracker.getRoot().getChildren().stream()
                        .map(MethodCallNode::getMethodName)
                        .collect(Collectors.toList());
                // Root 下只有 MainTask
                Assertions.assertEquals(1, childMethodNames.size());
                Assertions.assertEquals("MainTask", childMethodNames.get(0));
            }
        } finally {
            rawExecutor.shutdownNow();
        }
    }

    @Test
    public void shouldMountParallelNodeOnlyOnceWhenFinishCalledRepeatedly() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        try (PerformanceTracker.SafeNode root = tracker.track("Root")) {
            PerformanceTracker.ParallelNode parallelNode = tracker.enterParallel("AsyncTask");
            parallelNode.finish();
            parallelNode.finish();
        }

        Assertions.assertNotNull(tracker.getRoot());
        Assertions.assertEquals(1, tracker.getRoot().getChildren().size());
    }

    /**
     * 验证并发作用域内创建的并发节点挂载到当前并发父节点。
     *
     * @since v7.260701.0
     */
    @Test
    public void shouldAttachNestedParallelNodeToCurrentParallelScope() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        try (PerformanceTracker.SafeNode root = tracker.track("Root")) {
            PerformanceTracker.ParallelNode outerNode = tracker.enterParallel("OuterAsync");
            try (PerformanceTracker.ParallelScope ignored = outerNode.openScope()) {
                PerformanceTracker.ParallelNode innerNode = tracker.enterParallel("InnerAsync");
                innerNode.finish();
            }
        }

        MethodCallNode outerNode = tracker.getRoot().getChildren().get(0);
        Assertions.assertEquals("OuterAsync", outerNode.getMethodName());
        Assertions.assertEquals(1, outerNode.getChildren().size());
        Assertions.assertEquals("InnerAsync", outerNode.getChildren().get(0).getMethodName());
    }

    @Test
    public void shouldKeepDemandConfigAsyncBranchAndSafeChildrenInTimeline() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        ExecutorService rawExecutor = Executors.newSingleThreadExecutor();
        Executor executor = TtlExecutors.getTtlExecutor(rawExecutor);
        try {
            try (PerformanceTracker.SafeNode root = tracker.track("InfoCodeGatewayImpl#fillInfoCodeDataModel")) {
                PerformanceTracker.ParallelNode demandConfigNode = tracker.enterParallel("fillInfoCodeDataModel[DEMAND_CONFIG]");
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (PerformanceTracker.ParallelScope ignored = demandConfigNode.openScope()) {
                        try (PerformanceTracker.SafeNode gatewayNode = tracker.enter("DemandConfigGatewayImpl#listConfigsByInstances")) {
                            gatewayNode.recordStep("init-context");

                            try (PerformanceTracker.SafeNode inheritNode = tracker.enter(
                                    "DemandConfigAggregateInitByInstanceContext#handleAggregateInheritConfig")) {
                                inheritNode.recordStep("handle-inherit");
                            }

                            gatewayNode.recordStep("finish-query");
                        }
                    }
                }, executor);

                future.join();
                tracker.recordStep("fill-data-model");
            }

            MethodCallNode root = tracker.getRoot();
            Assertions.assertNotNull(root);

            MethodCallNode demandConfigBranch = root.getChildren().stream()
                    .filter(item -> "fillInfoCodeDataModel[DEMAND_CONFIG]".equals(item.getMethodName()))
                    .findFirst()
                    .orElse(null);
            Assertions.assertNotNull(demandConfigBranch);

            MethodCallNode demandGatewayNode = demandConfigBranch.getChildren().stream()
                    .filter(item -> "DemandConfigGatewayImpl#listConfigsByInstances".equals(item.getMethodName()))
                    .findFirst()
                    .orElse(null);
            Assertions.assertNotNull(demandGatewayNode);

            Assertions.assertNull(root.getChildren().stream()
                    .filter(item -> "DemandConfigGatewayImpl#listConfigsByInstances".equals(item.getMethodName()))
                    .findFirst()
                    .orElse(null));

            MethodCallNode inheritNode = demandGatewayNode.getChildren().stream()
                    .filter(item -> "DemandConfigAggregateInitByInstanceContext#handleAggregateInheritConfig".equals(item.getMethodName()))
                    .findFirst()
                    .orElse(null);
            Assertions.assertNotNull(inheritNode);
        } finally {
            rawExecutor.shutdownNow();
        }
    }

    @Test
    public void shouldRenderParentStepBeforeAsyncChildWhenStepIsRecordedAfterChildCompletes() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        try (PerformanceTracker.SafeNode root = tracker.track("Root")) {
            PerformanceTracker.ParallelNode parallelNode = tracker.enterParallel("AsyncChild");
            parallelNode.finish();
            tracker.recordStep("load-data");
        }

        String tree = tracker.prettyPrintTree();

        Assertions.assertTrue(tree.contains("load-data"));
        Assertions.assertTrue(tree.contains("[AsyncChild]"));
        Assertions.assertTrue(tree.indexOf("load-data") < tree.indexOf("[AsyncChild]"));
    }

    @Test
    public void shouldDegradeToNoopWhenEnterFailsInternally() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");
        Field callStackHolderField = extractTrackerField("callStackHolder");
        Object original = callStackHolderField.get(tracker);

        try {
            callStackHolderField.set(tracker, null);
            PerformanceTracker.SafeNode safeNode = Assertions.assertDoesNotThrow(() -> tracker.enter("BrokenEnter"));
            Assertions.assertDoesNotThrow(() -> safeNode.recordStep("ignored"));
            Assertions.assertDoesNotThrow(safeNode::close);
            Assertions.assertNull(tracker.getRoot());
        } finally {
            callStackHolderField.set(tracker, original);
        }
    }

    @Test
    public void shouldDegradeToNoopWhenEnterParallelFailsInternally() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");
        Field callStackHolderField = extractTrackerField("callStackHolder");
        Object original = callStackHolderField.get(tracker);

        try {
            callStackHolderField.set(tracker, null);
            PerformanceTracker.ParallelNode parallelNode = Assertions.assertDoesNotThrow(
                    () -> tracker.enterParallel("BrokenAsync"));
            Assertions.assertDoesNotThrow(() -> parallelNode.recordStep("ignored"));
            Assertions.assertDoesNotThrow(() -> {
                try (PerformanceTracker.ParallelScope ignored = parallelNode.openScope()) {
                }
            });
            Assertions.assertDoesNotThrow(parallelNode::finish);
            Assertions.assertNull(tracker.getRoot());
        } finally {
            callStackHolderField.set(tracker, original);
        }
    }

    @Test
    public void shouldNotThrowWhenExitAndQueryApisFailInternally() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");
        Field callStackHolderField = extractTrackerField("callStackHolder");
        Object original = callStackHolderField.get(tracker);

        try {
            callStackHolderField.set(tracker, null);
            Assertions.assertDoesNotThrow(tracker::exit);
            Assertions.assertFalse(Assertions.assertDoesNotThrow(tracker::isFinished));
            Assertions.assertFalse(Assertions.assertDoesNotThrow(tracker::isRootLevel));
            Assertions.assertEquals(0, Assertions.assertDoesNotThrow(tracker::getStackDepth));
        } finally {
            callStackHolderField.set(tracker, original);
        }
    }

    @Test
    public void shouldDegradeToNoopWhenTrackFailsInternally() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");
        Field callStackHolderField = extractTrackerField("callStackHolder");
        Object original = callStackHolderField.get(tracker);

        try {
            callStackHolderField.set(tracker, null);
            // track() 应该捕获内部异常并返回 noop SafeNode
            PerformanceTracker.SafeNode safeNode = Assertions.assertDoesNotThrow(() -> tracker.track("BrokenTrack"));
            Assertions.assertDoesNotThrow(() -> safeNode.recordStep("ignored"));
            Assertions.assertDoesNotThrow(safeNode::close);
            Assertions.assertNull(tracker.getRoot());
        } finally {
            callStackHolderField.set(tracker, original);
        }
    }

    @Test
    public void shouldDegradeToNoopWhenTrackParallelFailsInternally() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");
        Field callStackHolderField = extractTrackerField("callStackHolder");
        Object original = callStackHolderField.get(tracker);

        try {
            callStackHolderField.set(tracker, null);
            // trackParallel() 应该捕获内部异常并返回 noop ParallelNode
            PerformanceTracker.ParallelNode parallelNode = Assertions.assertDoesNotThrow(
                    () -> tracker.trackParallel("BrokenTrackParallel"));
            Assertions.assertDoesNotThrow(() -> parallelNode.recordStep("ignored"));
            Assertions.assertDoesNotThrow(parallelNode::finish);
            Assertions.assertNull(tracker.getRoot());
        } finally {
            callStackHolderField.set(tracker, original);
        }
    }

    @Test
    public void shouldNotThrowWhenRecordStepOnEmptyStack() {
        PerformanceTracker tracker = PerformanceTracker.create("test");

        // 在没有任何 enter/track 的情况下调用 recordStep，应该静默忽略
        Assertions.assertDoesNotThrow(() -> tracker.recordStep("orphan-step"));
        Assertions.assertNull(tracker.getRoot());
    }

    @Test
    public void shouldDegradeGracefullyWhenSafeNodeEnterChildFailsInternally() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");
        Field safeNodeStackHolderField = extractTrackerField("safeNodeStackHolder");
        Object original = safeNodeStackHolderField.get(tracker);

        try {
            // 先正常创建一个 SafeNode
            PerformanceTracker.SafeNode safeNode = tracker.track("ParentNode");

            // 损坏 safeNodeStackHolder
            safeNodeStackHolderField.set(tracker, null);

            // enterChild 应该降级为 noop
            PerformanceTracker.SafeNode childNode = Assertions.assertDoesNotThrow(() -> safeNode.enterChild("BrokenChild"));
            Assertions.assertDoesNotThrow(() -> childNode.recordStep("ignored"));
            Assertions.assertDoesNotThrow(childNode::close);

            safeNode.close();
        } finally {
            safeNodeStackHolderField.set(tracker, original);
        }
    }

    @Test
    public void shouldDegradeGracefullyWhenParallelNodeOpenScopeFailsInternally() throws Exception {
        PerformanceTracker tracker = PerformanceTracker.create("test");
        Field safeNodeStackHolderField = extractTrackerField("safeNodeStackHolder");
        Object original = safeNodeStackHolderField.get(tracker);

        try {
            // 先正常创建一个 ParallelNode
            PerformanceTracker.ParallelNode parallelNode = tracker.enterParallel("ParallelParent");

            // 损坏 safeNodeStackHolder
            safeNodeStackHolderField.set(tracker, null);

            // openScope 应该降级为 noop ParallelScope
            PerformanceTracker.ParallelScope scope = Assertions.assertDoesNotThrow(() -> parallelNode.openScope());
            Assertions.assertDoesNotThrow(scope::close);

            parallelNode.finish();
        } finally {
            safeNodeStackHolderField.set(tracker, original);
        }
    }

    private Field extractTrackerField(String fieldName) throws Exception {
        Field field = PerformanceTracker.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    private void invokeChainA(PerformanceTracker tracker, int loopCount) {
        for (int index = 0; index < loopCount; index++) {
            try (PerformanceTracker.SafeNode ignored = tracker.track("A")) {
                invokeChainB(tracker);
            }
        }
    }

    private void invokeChainB(PerformanceTracker tracker) {
        try (PerformanceTracker.SafeNode ignored = tracker.track("B")) {
            invokeChainC(tracker);
        }
    }

    private void invokeChainC(PerformanceTracker tracker) {
        try (PerformanceTracker.SafeNode ignored = tracker.track("C")) {
            tracker.recordStep("leaf");
        }
    }

    private void invokeRecursive(PerformanceTracker tracker, int level) {
        try (PerformanceTracker.SafeNode ignored = tracker.track("Recursive-" + level)) {
            tracker.recordStep("level-" + level);
            if (level > 1) {
                invokeRecursive(tracker, level - 1);
            }
        }
    }
}

