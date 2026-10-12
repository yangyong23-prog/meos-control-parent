package com.persagy.apccontrol.basic.structure.performance;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 方法调用树节点
 *
 * <p>记录单个方法的整体耗时、内部各步骤耗时，以及嵌套调用的子方法节点。
 * 支持树形结构的性能数据组织，便于展示完整的调用链路信息。
 *
 * <p>步骤与子调用共用同一条时间线（{@code timeline}），按插入顺序混排输出，
 * 保证并发子节点能够出现在其对应步骤之后。
 *
 * @author JiYinchuan
 * @since 7.250302.0
 */
public class MethodCallNode {

    // -----------------------------------------------------------------------
    // 时间线条目类型标记
    // -----------------------------------------------------------------------
    private static final int TYPE_STEP = 0;
    private static final int TYPE_CHILD = 1;

    /**
     * 统一时间线条目：步骤或子节点，按插入顺序排列。
     *
     * <ul>
     *   <li>类型 = {@link #TYPE_STEP}：{@code stepNanos} 为步骤耗时，{@code name} 为步骤名</li>
     *   <li>类型 = {@link #TYPE_CHILD}：{@code child} 为 {@code MethodCallNode}，{@code name} 为 {@code null}</li>
     * </ul>
     */
    private final List<TimelineEntry> timeline = Collections.synchronizedList(new ArrayList<>(8));

    /**
     * 方法简名（类名#方法名）
     */
    private final String methodName;

    /**
     * 方法调用路径（完整类名#方法名）
     */
    private final String callPath;

    /**
     * 是否为并发子任务节点（由 {@link PerformanceTracker#enterParallel} 创建）
     */
    private final boolean parallel;

    /**
     * 方法开始时间（纳秒）
     */
    private long startNanos;

    /**
     * 方法开始时间戳（毫秒）
     */
    private long startEpochMillis;

    /**
     * 方法总耗时（纳秒），finish() 调用后设置
     */
    private long totalNanos;

    /**
     * 方法结束时间戳（毫秒）
     */
    private long endEpochMillis;

    /**
     * 上一个步骤记录时间，用于计算步骤间隔。
     *
     * <p>声明为 {@code volatile} 确保多线程场景下的可见性：尽管实际上同一节点的
     * {@code recordStep} 应由单一线程调用，但 volatile 可防御误用或未来扩展场景下的间隔计算分层。
     */
    private volatile long lastStepNanos;

    /**
     * 构造方法调用节点（仅方法名）
     *
     * @param methodName 方法简名
     */
    public MethodCallNode(@NonNull String methodName) {
        this.methodName = methodName;
        this.callPath = methodName;
        this.parallel = false;
    }

    /**
     * 构造方法调用节点
     *
     * @param methodName 方法简名
     * @param callPath   完整调用路径
     */
    public MethodCallNode(@NonNull String methodName, @NonNull String callPath) {
        this.methodName = methodName;
        this.callPath = callPath;
        this.parallel = false;
    }

    /**
     * 构造方法调用节点
     *
     * @param methodName 方法简名
     * @param callPath   完整调用路径
     * @param parallel   是否为并发子任务节点
     */
    public MethodCallNode(@NonNull String methodName, @NonNull String callPath, boolean parallel) {
        this.methodName = methodName;
        this.callPath = callPath;
        this.parallel = parallel;
    }

    /**
     * 开始计时并初始化步骤起点。
     *
     * <p>该方法通常仅由 {@link PerformanceTracker} 调用一次。
     *
     * <p>使用示例：
     * <pre>{@code
     * MethodCallNode node = new MethodCallNode("ServiceA#query");
     * node.start();
     * }</pre>
     *
     * @since 7.250302.0
     */
    public void start() {
        this.startEpochMillis = System.currentTimeMillis();
        this.startNanos = System.nanoTime();
        this.lastStepNanos = this.startNanos;
    }

    /**
     * 记录当前步骤耗时，并将当前时刻设为下一步骤的起点。
     *
     * <p>这里仅记录“阶段性耗时”，不直接修改总耗时；总耗时由 {@link #finish()} 在节点结束时统一计算。
     *
     * <p>使用示例：
     * <pre>{@code
     * node.recordStep("查询数据库");
     * node.recordStep("组装结果");
     * }</pre>
     *
     * @param stepName 步骤名称
     * @since 7.250302.0
     */
    public void recordStep(@NonNull String stepName) {
        long now = System.nanoTime();
        long elapsed = now - this.lastStepNanos;
        this.timeline.add(new TimelineEntry(TYPE_STEP, stepName, elapsed, null));
        this.lastStepNanos = now;
    }

    /**
     * 结束计时并记录节点总耗时。
     *
     * <p>总耗时定义为：从 {@link #start()} 到当前调用的完整耗时。
     *
     * <p>注意：重复调用会覆盖之前的总耗时，因此上层句柄应保证只结束一次。
     *
     * @since 7.250302.0
     */
    public void finish() {
        this.endEpochMillis = System.currentTimeMillis();
        this.totalNanos = System.nanoTime() - this.startNanos;
    }

    /**
     * 添加子方法调用节点，并保留其在时间线中的插入顺序。
     *
     * <p>这使得步骤与子调用可以按真实记录顺序混排输出，而不是先输出全部步骤再输出全部子节点。
     *
     * @param child 子节点
     * @since 7.250302.0
     */
    public void addChild(@NonNull MethodCallNode child) {
        this.timeline.add(new TimelineEntry(TYPE_CHILD, null, 0L, child));
    }

    /**
     * 获取方法简名
     *
     * @return 方法简名
     * @since 7.250302.0
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 获取完整调用路径
     *
     * @return 调用路径
     * @since 7.250302.0
     */
    public String getCallPath() {
        return callPath;
    }

    /**
     * 获取总耗时（纳秒）
     *
     * @return 总耗时纳秒
     * @since 7.250302.0
     */
    public long getTotalNanos() {
        return totalNanos;
    }

    public long getStartEpochMillis() {
        return startEpochMillis;
    }

    public long getEndEpochMillis() {
        return endEpochMillis;
    }

    public boolean isParallel() {
        return parallel;
    }

    /**
     * 获取子节点列表。
     *
     * <p>返回结果按照时间线中的出现顺序排列，适合做断言、诊断或自定义渲染。
     *
     * @return 子节点列表
     * @since 7.250302.0
     */
    public List<MethodCallNode> getChildren() {
        List<MethodCallNode> result = new ArrayList<>();
        for (TimelineEntry entry : snapshotTimeline()) {
            if (entry.type == TYPE_CHILD) {
                result.add(entry.child);
            }
        }
        return result;
    }

    /**
     * 将当前节点及其所有子节点渲染为树形字符串。
     *
     * <p>渲染结果会保留步骤与子节点的时间线顺序，便于排查慢步骤与并发分支的位置关系。
     *
     * <p>使用示例：
     * <pre>{@code
     * String report = tracker.getRoot().prettyPrint();
     * }</pre>
     *
     * @return 格式化输出字符串
     * @since 7.250302.0
     */
    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        printNode(sb, this, "", true);
        return sb.toString();
    }

    /**
     * 递归渲染节点，按“父步骤先、子调用后”的阅读顺序输出步骤与子节点。
     *
     * <p>算法说明：
     * <ol>
     *   <li>先输出当前节点的总耗时；</li>
     *   <li>基于快照计算步骤名称最大宽度，保证输出对齐；</li>
     *   <li>遍历时间线时先暂存子节点，等遇到后续步骤时再输出，使“步骤摘要”位于对应子调用之前；</li>
     *   <li>若后续没有新的步骤，则将暂存子节点输出在节点末尾。</li>
     * </ol>
     *
     * @param sb     输出缓冲
     * @param node   当前节点
     * @param prefix 行前缀
     * @param isRoot 是否为根节点
     * @since 7.250302.0
     */
    private void printNode(@NonNull StringBuilder sb,
                           @NonNull MethodCallNode node,
                           @NonNull String prefix,
                           boolean isRoot) {
        List<TimelineEntry> snapshot = node.snapshotTimeline();
        String duration = formatDuration(node.totalNanos);
        if (isRoot) {
            sb.append(String.format(Locale.ROOT, ">>> [%s]  %s%n", node.methodName, duration));
        } else {
            String marker = node.parallel ? " ~async" : "";
            sb.append(String.format(Locale.ROOT, "%s+-- [%s]%s  %s%n", prefix, node.methodName, marker, duration));
        }

        String itemPrefix = isRoot ? "    " : prefix + "    ";
        String childPrefix = itemPrefix + "  ";

        // 计算所有步骤名称的最大长度，用于对齐
        int maxStepLen = 0;
        for (TimelineEntry entry : snapshot) {
            if (entry.type == TYPE_STEP && entry.name != null) {
                maxStepLen = Math.max(maxStepLen, entry.name.length());
            }
        }
        String stepFmt = "%s  %-" + Math.max(maxStepLen, 1) + "s  %s  (%4.1f%%)%n";

        // 将子节点挂到其后的步骤之后输出，更贴近“某个步骤内部触发了哪些子调用”的阅读习惯
        List<MethodCallNode> pendingChildren = new ArrayList<>();
        for (TimelineEntry entry : snapshot) {
            if (entry.type == TYPE_STEP) {
                long stepNanos = entry.stepNanos;
                double pct = node.totalNanos > 0 ? (stepNanos * 100.0 / node.totalNanos) : 0.0;
                sb.append(String.format(Locale.ROOT, stepFmt, itemPrefix, entry.name,
                        formatDuration(stepNanos), pct));
                for (MethodCallNode child : pendingChildren) {
                    printNode(sb, child, childPrefix, false);
                }
                pendingChildren.clear();
            } else {
                pendingChildren.add(entry.child);
            }
        }

        if (!pendingChildren.isEmpty()) {
            for (MethodCallNode child : pendingChildren) {
                printNode(sb, child, childPrefix, false);
            }
        }
    }

    /**
     * 将纳秒格式化为秒（保留 3 位小数）。
     *
     * @param nanos 纳秒数
     * @return 格式化耗时字符串，如 {@code 3.125s}
     * @since 7.250302.0
     */
    private static String formatDuration(long nanos) {
        return String.format(Locale.ROOT, "%.3fs", nanos / 1_000_000_000.0);
    }

    /**
     * 获取当前时间线的稳定快照。
     *
     * <p>时间线在并发场景下可能持续写入，这里统一复制快照用于遍历，既避免
     * {@link java.util.ConcurrentModificationException} 风险，也显著降低
     * {@code CopyOnWriteArrayList} 在高频写入场景下的复制成本。
     *
     * @return 当前时间线快照
     */
    @NonNull
    private List<TimelineEntry> snapshotTimeline() {
        synchronized (timeline) {
            return new ArrayList<>(timeline);
        }
    }

    /**
     * 获取步骤名称列表。
     *
     * <p>列表顺序与 {@link #recordStep(String)} 的调用顺序一致。
     *
     * @return 步骤名称列表
     * @since 7.250302.0
     */
    @NonNull
    public List<String> getStepNames() {
        List<String> names = new ArrayList<>();
        for (TimelineEntry entry : snapshotTimeline()) {
            if (entry.type == TYPE_STEP) {
                names.add(entry.name);
            }
        }
        return names;
    }

    /**
     * 获取步骤耗时列表（纳秒数组）。
     *
     * <p>每个元素都对应一次 {@link #recordStep(String)} 调用，顺序与步骤名称列表一致。
     *
     * @return 步骤耗时列表，每个元素为 long[]{耗时纳秒}
     * @since 7.250302.0
     */
    public List<long[]> getSteps() {
        List<long[]> result = new ArrayList<>();
        for (TimelineEntry entry : snapshotTimeline()) {
            if (entry.type == TYPE_STEP) {
                result.add(new long[]{entry.stepNanos});
            }
        }
        return result;
    }

    long getExclusiveNanos() {
        long childNanos = 0L;
        for (MethodCallNode child : getChildren()) {
            childNanos += child.getTotalNanos();
        }
        return Math.max(0L, totalNanos - childNanos);
    }

    long getAsyncWaitNanos() {
        long syncChildNanos = 0L;
        long asyncChildNanos = 0L;
        for (MethodCallNode child : getChildren()) {
            if (child.isParallel()) {
                asyncChildNanos += child.getTotalNanos();
            } else {
                syncChildNanos += child.getTotalNanos();
            }
        }
        if (asyncChildNanos == 0L) {
            return 0L;
        }
        long stepNanos = 0L;
        for (StepSnapshot stepSnapshot : getStepSnapshots()) {
            stepNanos += stepSnapshot.getDurationNanos();
        }
        return Math.max(0L, totalNanos - syncChildNanos - stepNanos);
    }

    List<StepSnapshot> getStepSnapshots() {
        List<StepSnapshot> result = new ArrayList<>();
        for (TimelineEntry entry : snapshotTimeline()) {
            if (entry.type == TYPE_STEP) {
                result.add(new StepSnapshot(entry.name, entry.stepNanos));
            }
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // 内部类
    // -----------------------------------------------------------------------

    /**
     * 时间线条目：封装一条 Step 或一个 Child 节点
     */
    private static class TimelineEntry {
        final int type;
        /**
         * 步骤名称（type == TYPE_STEP 时非 null）
         */
        final String name;
        /**
         * 步骤耗时（type == TYPE_STEP 时非 null）
         */
        final long stepNanos;
        /**
         * 子节点（type == TYPE_CHILD 时非 null）
         */
        final MethodCallNode child;

        TimelineEntry(int type, String name, long stepNanos, MethodCallNode child) {
            this.type = type;
            this.name = name;
            this.stepNanos = stepNanos;
            this.child = child;
        }
    }

    static final class StepSnapshot {
        private final String name;
        private final long durationNanos;

        StepSnapshot(String name, long durationNanos) {
            this.name = name;
            this.durationNanos = durationNanos;
        }

        String getName() {
            return name;
        }

        long getDurationNanos() {
            return durationNanos;
        }
    }
}
