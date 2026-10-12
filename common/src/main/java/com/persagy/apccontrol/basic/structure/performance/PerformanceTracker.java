package com.persagy.apccontrol.basic.structure.performance;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 性能追踪器。
 *
 * <p>该类用于记录一次请求或一次业务流程中的方法调用树、步骤耗时与并发分支，
 * 以便在日志中输出完整、可读的性能轨迹。
 *
 * <p>基于调用栈管理方法调用树的生命周期。每个 HTTP 请求线程通过
 * {@link #bind(String)} 绑定一个独享实例到线程上下文，
 * 并借助 {@code TransmittableThreadLocal} 在线程池场景下传递必要上下文。
 *
 * <p>快速开始：
 * <pre>{@code
 * PerformanceTracker tracker = PerformanceTracker.bind("查询信息点");
 * try (PerformanceTracker.SafeNode ignored = tracker.track("InfoCodeGatewayImpl#fillInfoCodeDataModel")) {
 *     tracker.recordStep("准备参数");
 *     tracker.recordStep("查询远程服务");
 * }
 * if (tracker.isFinished()) {
 *     System.out.println(tracker.prettyPrint());
 * }
 * }</pre>
 *
 * <p>并发场景示例：
 * <pre>{@code
 * PerformanceTracker.ParallelNode node = tracker.trackParallel("loadRoomData");
 * CompletableFuture.runAsync(() -> {
 *     try {
 *         node.recordStep("查询远程服务");
 *     } finally {
 *         node.finish();
 *     }
 * }, executor);
 * }</pre>
 *
 * <p>并发子任务内调用其他带埋点的方法时，需要用 {@link ParallelNode#openScope()} 建立上下文：
 * <pre>{@code
 * ParallelNode node = tracker.trackParallel("asyncTask");
 * CompletableFuture.runAsync(() -> {
 *     try (ParallelScope scope = node.openScope()) {
 *         otherService.doWork();  // 内部可能有 track 埋点
 *     } finally {
 *         node.finish();
 *     }
 * }, executor);
 * }</pre>
 *
 * <h2>线程安全</h2>
 * <p>该类是线程安全的：
 * <ul>
 *   <li>根节点使用 {@code AtomicReference} + CAS 设置，消除 Check-Then-Act 竞争</li>
 *   <li>调用栈和安全节点栈使用 {@code TransmittableThreadLocal} 实现线程隔离</li>
 *   <li>时间线使用 {@code synchronizedList} 保证并发写入安全</li>
 *   <li>步骤耗时 {@code lastStepNanos} 声明为 {@code volatile} 保证跨线程可见性</li>
 * </ul>
 *
 * <h2>异常安全</h2>
 * <p>该类实现了主流程零干扰保障：
 * <ul>
 *   <li>所有公开方法内部捕获 {@code Throwable}，失败时静默降级</li>
 *   <li>{@code track()}/{@code enter()}/{@code enterParallel()} 失败时返回空操作节点</li>
 *   <li>{@code exit()}/{@code recordStep()} 失败时静默忽略</li>
 *   <li>查询方法失败时返回保守默认值</li>
 *   <li>空操作节点的所有方法均为空实现，不会影响业务流程</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since 7.250302.0
 */
public class PerformanceTracker {

    /**
     * 线程上下文中的追踪器实例
     */
    private static final TransmittableThreadLocal<PerformanceTracker> CONTEXT_HOLDER = new TransmittableThreadLocal<>();

    /**
     * 追踪器标识（对应请求接口名称/方法名）
     */
    private final String id;

    /**
     * 根节点（第一次 enter 时设置）。
     *
     * <p>使用 {@link AtomicReference} 保证 CAS 语义：首次 compareAndSet 的线程成功设置根节点，
     * 后续线程（包括子线程）不会覆盖已设置的根节点，彻底消除 Check-Then-Act 竞争窗口。
     */
    private final AtomicReference<MethodCallNode> rootRef = new AtomicReference<>();

    /**
     * 主调用栈（线程局部）：栈顶为当前线程同步调用链正在执行的方法节点。
     *
     * <p>使用 {@link TransmittableThreadLocal} 实现每线程独立隔离，防止多个子线程
     * 通过 TTL 继承同一 tracker 实例时并发操作同一个 {@link ArrayDeque} 导致数据竞争。
     *
     * <p>TTL 的 {@code copy} 语义保证子线程继承父线程调用栈快照，使子线程能感知
     * 父线程当前的调用深度，正确确定根节点位置；但后续 push/pop 操作完全隔离。
     *
     * <p>只有主请求线程（{@link #bind} 的线程）的调用栈变化会影响 {@link #root} 的设置。
     */
    private final TransmittableThreadLocal<Deque<MethodCallNode>> callStackHolder = newCallStackHolder();

    /**
     * SafeNode 并发分支的节点栈：记录当前线程中通过 {@link #enter} 进入的并发节点层级。
     *
     * <p>当 {@code callStack} 为空时（并发子任务线程内），{@link #enter} 通过此栈
     * 追踪正确的父节点，使子方法节点能精确挂载到调用方节点下，而无需显式传参。
     */
    private final TransmittableThreadLocal<Deque<MethodCallNode>> safeNodeStackHolder = newStackHolder();

    /**
     * 构造追踪器
     *
     * @param id 追踪器标识
     */
    private PerformanceTracker(@NonNull String id) {
        this.id = id;
    }

    /**
     * 创建一个空操作的 {@link SafeNode}。
     *
     * <p>每次创建新实例是因为 {@link SafeNode} 是非静态内部类，无法作为静态字段存储。
     * 该实例的所有操作均为空实现，安全且轻量。
     *
     * @return 空操作安全节点
     */
    private SafeNode noopSafeNode() {
        return new SafeNode(null, true);
    }

    /**
     * 创建一个空操作的 {@link ParallelNode}。
     *
     * @return 空操作并发节点
     */
    private ParallelNode noopParallelNode() {
        return new ParallelNode(this, null, null, true);
    }

    /**
     * 创建用于保存主调用栈的线程变量（不继承父线程快照）。
     *
     * <p>主调用栈的子线程初始值始终为空，确保子线程中的 {@link #enter} 调用
     * 不会误判为主线程同步模式（避免操作共享 callStack 引发并发竞争）。
     * 子线程应通过 {@link ParallelNode#openScope()} 建立正确的并发作用域。
     *
     * @return 不带复制语义的线程局部栈（子线程始终从空栈开始）
     */
    @NonNull
    private static TransmittableThreadLocal<Deque<MethodCallNode>> newCallStackHolder() {
        return new TransmittableThreadLocal<Deque<MethodCallNode>>() {
            @Override
            public Deque<MethodCallNode> copy(Deque<MethodCallNode> parentValue) {
                // 子线程主调用栈始终为空：子线程不参与主线程同步压栈，避免并发竞争
                return new ArrayDeque<>(8);
            }

            @Override
            protected Deque<MethodCallNode> initialValue() {
                return new ArrayDeque<>(8);
            }
        };
    }

    /**
     * 创建用于保存线程上下文栈的线程变量。
     *
     * <p>当追踪器被传递到子线程时，TTL 会复制父线程的栈快照，而不是复用原始栈实例，
     * 从而保证：
     * <ul>
     *   <li>子线程能继承父线程的调用上下文，正确确定挂载父节点；</li>
     *   <li>父子线程后续对栈的 push/pop 操作彼此隔离，不会互相污染。</li>
     * </ul>
     *
     * @return 带复制语义的线程局部栈
     */
    @NonNull
    private static TransmittableThreadLocal<Deque<MethodCallNode>> newStackHolder() {
        return new TransmittableThreadLocal<Deque<MethodCallNode>>() {
            @Override
            public Deque<MethodCallNode> copy(Deque<MethodCallNode> parentValue) {
                return copyStack(parentValue);
            }

            @Override
            protected Deque<MethodCallNode> initialValue() {
                return new ArrayDeque<>(8);
            }
        };
    }

    /**
     * 复制一份调用栈快照，保证跨线程传递时的栈状态隔离。
     *
     * @param source 原始栈，允许为 {@code null}
     * @return 新的栈实例
     */
    @NonNull
    private static Deque<MethodCallNode> copyStack(@Nullable Deque<MethodCallNode> source) {
        return source == null ? new ArrayDeque<>(8) : new ArrayDeque<>(source);
    }

    /**
     * 获取当前线程的主调用栈。
     *
     * @return 当前线程专属的主调用栈
     */
    @NonNull
    private Deque<MethodCallNode> callStack() {
        return callStackHolder.get();
    }

    /**
     * 获取当前线程的安全节点栈。
     *
     * @return 当前线程专属的安全节点栈
     */
    @NonNull
    private Deque<MethodCallNode> safeNodeStack() {
        return safeNodeStackHolder.get();
    }

    /**
     * 创建一个新的性能追踪器实例。
     *
     * <p>该方法通常仅在请求入口创建一次，后续通过线程上下文反复复用同一个追踪器实例。
     *
     * <p>使用示例：
     * <pre>{@code
     * PerformanceTracker tracker = PerformanceTracker.create("查询设备详情");
     * try (PerformanceTracker.SafeNode node = tracker.track("Service#method")) {
     *     node.recordStep("处理请求");
     * }
     * }</pre>
     *
     * @param id 追踪器标识（如接口名称）
     * @return 新建的追踪器实例
     * @since 7.250302.0
     */
    public static PerformanceTracker create(@NonNull String id) {
        return new PerformanceTracker(id);
    }

    /**
     * 获取当前线程已绑定的方法性能追踪器。
     *
     * <p>该方法只负责读取当前线程上下文，不会自动创建，也不会写回 {@link ThreadLocal}。
     *
     * @return 当前线程已绑定的追踪器；若尚未绑定则返回 {@code null}
     * @since 7.250302.0
     */
    @Nullable
    public static PerformanceTracker current() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 绑定当前线程的方法性能追踪器。
     *
     * <p>若当前线程尚未绑定追踪器，则使用给定标识创建一个新实例并写入上下文；
     * 若已经存在，则直接复用已有实例。
     *
     * <p>通常用于请求入口或其他负责在 {@link #clear()} 中清理上下文的基础设施代码。
     *
     * @param id 追踪器标识
     * @return 当前线程绑定的追踪器
     * @since 7.250302.0
     */
    @NonNull
    public static PerformanceTracker bind(@NonNull String id) {
        PerformanceTracker tracker = current();
        if (tracker == null) {
            tracker = create(id);
            CONTEXT_HOLDER.set(tracker);
        }
        return tracker;
    }

    /**
     * 获取当前线程绑定的追踪器；若不存在则返回一个独立追踪器。
     *
     * <p>该方法不会修改线程上下文：有则复用，无则只创建一个未绑定的新实例。
     *
     * @param id 创建独立追踪器时使用的标识
     * @return 当前线程追踪器或独立追踪器
     * @since 7.250302.0
     */
    @NonNull
    public static PerformanceTracker currentOrDetached(@NonNull String id) {
        PerformanceTracker tracker = current();
        return tracker != null ? tracker : create(id);
    }

    /**
     * 清除当前线程的方法性能追踪器。
     *
     * <p>应在请求结束时（AOP切面 finally 块中）调用，防止内存泄露。
     *
     * @since 7.250302.0
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 开始一个自适应追踪作用域，返回可用于 {@code try-with-resources} 的句柄。
     *
     * <p>该方法会根据当前线程上下文自动选择同步模式或并发分支模式：
     * <ul>
     *   <li>首次调用（无上下文）：使用同步模式，节点压入调用栈</li>
     *   <li>已在追踪链路中：使用并发延迟挂载模式，节点在关闭时挂载到父节点</li>
     * </ul>
     *
     * <p><b>异常安全</b>：该方法内部捕获所有 {@link Throwable}，若追踪初始化失败，
     * 将静默返回一个空操作的 {@link SafeNode}，调用方的主流程不受任何影响。
     *
     * <p>使用示例：
     * <pre>{@code
     * try (PerformanceTracker.SafeNode node = tracker.track("InfoCodeGatewayImpl#of")) {
     *     node.recordStep("查询配置");
     * }
     * }</pre>
     *
     * @param methodName 方法名称
     * @return 可自动关闭的作用域句柄；失败时返回无操作节点
     */
    @NonNull
    public SafeNode track(@NonNull String methodName) {
        try {
            return enterInternal(methodName, methodName);
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] track 初始化失败，降级为 NOOP，原因: {}", t.toString());
            return noopSafeNode();
        }
    }

    /**
     * 开始一个自适应追踪作用域，并同时指定完整调用路径。
     *
     * <p><b>异常安全</b>：该方法内部捕获所有 {@link Throwable}，若追踪初始化失败，
     * 将静默返回一个空操作的 {@link SafeNode}，调用方的主流程不受任何影响。
     *
     * <p>使用示例：
     * <pre>{@code
     * try (PerformanceTracker.SafeNode node = tracker.track("Controller#query", "com.demo.Controller#query")) {
     *     node.recordStep("处理请求");
     * }
     * }</pre>
     *
     * @param methodName 方法展示名
     * @param callPath   完整调用路径
     * @return 可自动关闭的作用域句柄；失败时返回无操作节点
     */
    @NonNull
    public SafeNode track(@NonNull String methodName, @NonNull String callPath) {
        try {
            return enterInternal(methodName, callPath);
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] track 初始化失败，降级为 NOOP，原因: {}", t.toString());
            return noopSafeNode();
        }
    }

    /**
     * 以统一命名方式创建并发追踪节点。
     *
     * <p><b>异常安全</b>：内部捕获所有异常，失败时返回空操作并发节点。
     *
     * @param methodName 方法名称
     * @return 并发追踪节点；失败时返回无操作节点
     */
    @NonNull
    public ParallelNode trackParallel(@NonNull String methodName) {
        try {
            return enterParallelInternal(methodName);
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] trackParallel 初始化失败，降级为 NOOP，原因: {}", t.toString());
            return noopParallelNode();
        }
    }

    /**
     * 底层压栈实现，供 {@link #enter} 的根层场景调用。
     */
    private void doEnter(@NonNull String methodName, @NonNull String callPath) {
        Deque<MethodCallNode> callStack = callStack();
        MethodCallNode node = new MethodCallNode(methodName, callPath);
        node.start();
        if (callStack.isEmpty()) {
            // CAS 设置根节点：首次 compareAndSet 成功的线程成为根节点持有者，
            // 其他线程（含子线程）无论是否 callStack 为空，均不会覆盖已有的根节点
            rootRef.compareAndSet(null, node);
            // 子线程 callStack 为空但 root 已存在时，当前节点作为子线程独立根，不挂载到主线程树
        } else {
            callStack.peek().addChild(node);
        }
        callStack.push(node);
    }

    /**
     * 进入一个方法，并根据当前线程上下文自动选择同步模式或并发分支模式。
     *
     * <p>这是 {@link #enter(String, String)} 的简化版本，callPath 与 methodName 相同。
     *
     * @param methodName 方法简名
     * @return 自适应的节点句柄
     * @since 7.250302.0
     */
    public SafeNode enter(@NonNull String methodName) {
        return enter(methodName, methodName);
    }

    /**
     * 进入一个方法，并根据当前线程上下文自动选择同步模式或并发分支模式。
     *
     * <ul>
     *   <li>若 {@code safeNodeStack} 为空（主线程同步调用）：
     *       使用标准压栈语义，节点压入 {@code callStack}。</li>
     *   <li>若 {@code safeNodeStack} 不为空（并发分支中）：
     *       使用延迟挂载语义，节点压入 {@code safeNodeStack}，在关闭时挂载到父节点。</li>
     * </ul>
     *
     * <p>适用于同一方法既可能作为入口被直接调用，也可能在并发子任务中被调用的场景。
     * 该方法会优先读取当前线程独立的栈快照，避免父线程与子线程共享同一栈实例导致的节点串挂。
     *
     * <p>典型用法：
     * <pre>{@code
     *   try (PerformanceTracker.SafeNode node = tracker.enter("ServiceA#doSomething")) {
     *       node.recordStep("步骤1");
     *       // ...
     *   }
     * }</pre>
     *
     * @param methodName 方法简名
     * @param callPath   完整调用路径
     * @return 自适应的节点句柄
     *
     * <p><b>注意：</b>返回的句柄必须成对关闭，否则调用树将缺失结束时间或出现层级残留。</p>
     * @since 7.250302.0
     */
    public SafeNode enter(@NonNull String methodName, @NonNull String callPath) {
        try {
            return enterInternal(methodName, callPath);
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] enter 失败，降级为 NOOP，原因: {}", t.toString());
            return noopSafeNode();
        }
    }

    /**
     * 进入方法的内部实现，区分同步模式与并发分支模式。
     *
     * <p>根据当前线程的 {@code safeNodeStack} 状态选择执行路径：
     * <ul>
     *   <li>空栈 → 同步模式，调用 {@link #doEnter} 压入主调用栈</li>
     *   <li>非空栈 → 并发分支模式，创建延迟挂载的 SafeNode</li>
     * </ul>
     *
     * @param methodName 方法简名
     * @param callPath   完整调用路径
     * @return 自适应的节点句柄
     */
    @NonNull
    private SafeNode enterInternal(@NonNull String methodName, @NonNull String callPath) {
        Deque<MethodCallNode> safeNodeStack = safeNodeStack();
        if (safeNodeStack.isEmpty()) {
            doEnter(methodName, callPath);
            return new SafeNode(null);
        }

        MethodCallNode parent = safeNodeStack.peek();
        MethodCallNode node = new MethodCallNode(methodName, callPath, true);
        node.start();
        safeNodeStack.push(node);
        return new SafeNode(new ParallelNode(PerformanceTracker.this, node, parent));
    }

    /**
     * 自适应节点句柄，由 {@link #enter} 返回。
     *
     * @since 7.250302.0
     */
    public class SafeNode implements AutoCloseable {

        /**
         * 并发模式下的句柄；为 null 表示标准压栈模式
         */
        private final ParallelNode parallelNode;
        /**
         * 关闭状态标记，用于保证 {@link #exit()} 的幂等性
         */
        private final AtomicBoolean closed = new AtomicBoolean(false);
        /**
         * true 表示此实例是空操作节点，
         * 所有操作均为空实现，不压栈也不挂载，彻底隔离追踪器故障。
         */
        private final boolean noop;

        /**
         * 构造正常模式的 SafeNode。
         *
         * @param parallelNode 并发模式下的句柄，为 null 表示标准压栈模式
         */
        private SafeNode(ParallelNode parallelNode) {
            this.parallelNode = parallelNode;
            this.noop = false;
        }

        /**
         * 构造可能为空操作模式的 SafeNode。
         *
         * @param parallelNode 并发模式下的句柄
         * @param noop         是否为空操作模式
         */
        private SafeNode(ParallelNode parallelNode, boolean noop) {
            this.parallelNode = parallelNode;
            this.noop = noop;
        }

        /**
         * 记录当前节点内最近一个业务阶段的耗时。
         *
         * <p>每次调用都会记录“自上一次 {@code enter}/{@code recordStep} 之后到当前时刻”的耗时。
         * 该方法既支持普通同步节点，也支持由 {@link #enter(String)} 创建的并发节点。
         *
         * <p>使用示例：
         * <pre>{@code
         * safeNode.recordStep("查询数据库");
         * safeNode.recordStep("组装返回结果");
         * }</pre>
         *
         * @param stepName 步骤名称
         * @since 7.250302.0
         */
        public void recordStep(@NonNull String stepName) {
            if (noop) {
                return;
            }
            if (parallelNode != null) {
                parallelNode.recordStep(stepName);
            } else {
                PerformanceTracker.this.recordStep(stepName);
            }
        }

        /**
         * 在当前节点下创建一个子方法节点，而不依赖外层线程调用栈。
         *
         * <p>适用于从并发分支节点内部调用子方法的场景：子方法是否在并发上下文中被调用
         * 未知，但希望它始终挂载在当前节点下而不是取决于 {@code callStack}。
         *
         * <p>使用示例：
         * <pre>{@code
         * PerformanceTracker.SafeNode child = safeNode.enterChild("loadCache");
         * try {
         *     child.recordStep("读取本地缓存");
         * } finally {
         *     child.exit();
         * }
         * }</pre>
         *
         * @param methodName 子方法简名
         * @return 子节点句柄，需在 {@code finally} 中调用 {@link SafeNode#exit()}
         * @since 7.250302.0
         */
        public SafeNode enterChild(@NonNull String methodName) {
            if (noop) {
                return PerformanceTracker.this.noopSafeNode();
            }
            try {
                Deque<MethodCallNode> safeNodeStack = safeNodeStack();
                Deque<MethodCallNode> callStack = callStack();
                MethodCallNode currentNode = parallelNode != null ? parallelNode.node :
                        (callStack.isEmpty() ? null : callStack.peek());
                MethodCallNode childNode = new MethodCallNode(methodName, methodName, true);
                childNode.start();
                safeNodeStack.push(childNode);
                return new SafeNode(new ParallelNode(PerformanceTracker.this, childNode, currentNode));
            } catch (Throwable t) {
                LoggerFactory.getLogger(PerformanceTracker.class)
                        .warn("[性能追踪] enterChild 失败，降级为 NOOP，原因: {}", t.toString());
                return PerformanceTracker.this.noopSafeNode();
            }
        }

        /**
         * 关闭当前节点并结束计时。
         *
         * <p>该方法具备幂等性，重复调用不会重复挂载节点，也不会破坏当前线程的节点栈。
         *
         * <p>注意事项：
         * <ul>
         *   <li>建议始终放在 {@code finally} 中调用；</li>
         *   <li>若节点由 {@link PerformanceTracker#enter(String)} 返回，则必须使用本方法退出，
         *       不要混用 {@link PerformanceTracker#exit()}。</li>
         * </ul>
         *
         * @since 7.250302.0
         */
        public void exit() {
            if (noop) {
                return;
            }
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                if (parallelNode != null) {
                    Deque<MethodCallNode> safeNodeStack = safeNodeStack();
                    if (safeNodeStack.peekFirst() == parallelNode.node) {
                        safeNodeStack.pollFirst();
                    } else {
                        safeNodeStack.removeFirstOccurrence(parallelNode.node);
                    }
                    parallelNode.finish();
                } else {
                    // 使用线程局部 callStack，天然线程安全，无需额外判空
                    PerformanceTracker.this.exit();
                }
            } catch (Throwable t) {
                LoggerFactory.getLogger(PerformanceTracker.class)
                        .warn("[性能追踪] SafeNode.exit 失败，原因: {}", t.toString());
            }
        }

        /**
         * 关闭当前安全节点，等价于 {@link #exit()}。
         */
        @Override
        public void close() {
            exit();
        }
    }

    /**
     * 为并发子任务创建一个独立计时节点。
     *
     * <p>与 {@link #enter} 的区别：
     * <ul>
     *   <li>{@code enter} 将节点压入共享的 {@code callStack}，适用于当前线程顺序调用。</li>
     *   <li>{@code enterParallel} 不操作 {@code callStack}，返回独立的 {@link ParallelNode} 用于子线程的计时和结束，
     *       适用于 {@code CompletableFuture.runAsync} 等并发场景。</li>
     * </ul>
     *
     * <p>典型用法：
     * <pre>{@code
     *   PerformanceTracker.ParallelNode pNode = tracker.enterParallel("fillInfoCodeDataModel[IOT]");
     *   CompletableFuture.runAsync(() -> {
     *       try {
     *           // 并发子任务
     *       } finally {
     *           pNode.finish();
     *       }
     *   }, executor);
     * }</pre>
     *
     * <p>该方法不会修改当前线程的调用栈，而是在创建时捕获父节点引用，并在
     * {@link ParallelNode#finish()} 时延迟挂载，从而保证日志时间线顺序更加贴近真实执行顺序。
     *
     * <p>使用示例：
     * <pre>{@code
     * PerformanceTracker.ParallelNode node = tracker.enterParallel("loadRoomData");
     * CompletableFuture.runAsync(() -> {
     *     try {
     *         node.recordStep("读取远程接口");
     *     } finally {
     *         node.finish();
     *     }
     * }, executor);
     * }</pre>
     *
     * @param methodName 方法简名
     * @return 独立的并发计时节点，需在子线程任务结束时调用 {@link ParallelNode#finish()}
     * @since 7.250302.0
     */
    public ParallelNode enterParallel(@NonNull String methodName) {
        try {
            return enterParallelInternal(methodName);
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] enterParallel 失败，降级为 NOOP，原因: {}", t.toString());
            return noopParallelNode();
        }
    }

    /**
     * 创建并发追踪节点的内部实现。
     *
     * <p>创建一个独立的 {@link MethodCallNode}，优先捕获当前并发作用域节点，
     * 否则捕获同步调用栈顶作为父节点。实际挂载延迟到
     * {@link ParallelNode#finish()} 时执行。
     *
     * @param methodName 方法简名
     * @return 独立的并发计时节点
     */
    @NonNull
    private ParallelNode enterParallelInternal(@NonNull String methodName) {
        MethodCallNode node = new MethodCallNode(methodName, methodName, true);
        node.start();
        Deque<MethodCallNode> safeNodeStack = safeNodeStack();
        MethodCallNode parent;
        if (safeNodeStack.isEmpty()) {
            Deque<MethodCallNode> callStack = callStack();
            parent = callStack.isEmpty() ? null : callStack.peek();
        } else {
            parent = safeNodeStack.peek();
        }
        return new ParallelNode(this, node, parent);
    }

    /**
     * 并发子任务的计时句柄，由 {@link #enterParallel} 返回。
     *
     * <p>子线程任务完成后必须在 {@code finally} 中调用 {@link #finish()}，
     * 否则该节点耐时将为 0。
     *
     * @since 7.250302.0
     */
    public static class ParallelNode {

        /**
         * 所属的追踪器实例
         */
        private final PerformanceTracker owner;
        /**
         * 当前并发节点对应的调用节点
         */
        private final MethodCallNode node;
        /**
         * 完成后挂载的父节点，null 表示无父节点
         */
        private final MethodCallNode parent;
        /**
         * 完成状态标记，用于保证 {@link #finish()} 的幂等性
         */
        private final AtomicBoolean finished = new AtomicBoolean(false);
        /**
         * true 表示此实例是空操作节点，所有操作均为空实现。
         */
        private final boolean noop;

        /**
         * 构造正常模式的 ParallelNode。
         *
         * @param owner  所属追踪器
         * @param node   当前调用节点
         * @param parent 完成后挂载的父节点，可为 null
         */
        private ParallelNode(@NonNull PerformanceTracker owner, @NonNull MethodCallNode node, MethodCallNode parent) {
            this.owner = owner;
            this.node = node;
            this.parent = parent;
            this.noop = false;
        }

        /**
         * 构造可能为空操作模式的 ParallelNode。
         *
         * @param owner  所属追踪器
         * @param node   当前调用节点（可为 null）
         * @param parent 完成后挂载的父节点
         * @param noop   是否为空操作模式
         */
        private ParallelNode(@NonNull PerformanceTracker owner, MethodCallNode node, MethodCallNode parent, boolean noop) {
            this.owner = owner;
            this.node = node;
            this.parent = parent;
            this.noop = noop;
        }

        /**
         * 打开并发子任务的作用域，将当前节点压入安全节点栈。
         *
         * <p>该方法用于在并发子线程中建立正确的追踪上下文，使得后续的
         * {@link PerformanceTracker#track(String)} 调用能够正确挂载到当前节点下。
         *
         * <p>返回的 {@link ParallelScope} 在 {@link ParallelScope#close()} 时会自动调用
         * {@link #finish()}，因此无需额外在 {@code finally} 中手动调用。
         *
         * <p>使用示例：
         * <pre>{@code
         * ParallelNode node = tracker.trackParallel("asyncTask");
         * CompletableFuture.runAsync(() -> {
         *     try (ParallelScope scope = node.openScope()) {
         *         try (PerformanceTracker.SafeNode sn = tracker.track("subMethod")) {
         *             sn.recordStep("处理数据");
         *         }
         *     } // scope.close() 自动调用 finish()
         * }, executor);
         * }</pre>
         *
         * @return 可自动关闭的作用域句柄
         */
        public ParallelScope openScope() {
            if (noop) {
                return new ParallelScope(owner, null, this, true);
            }
            try {
                Deque<MethodCallNode> safeNodeStack = owner.safeNodeStack();
                safeNodeStack.push(node);
                return new ParallelScope(owner, node, this, false);
            } catch (Throwable t) {
                LoggerFactory.getLogger(PerformanceTracker.class)
                        .warn("[性能追踪] openScope 失败，降级为 NOOP，原因: {}", t.toString());
                return new ParallelScope(owner, null, this, true);
            }
        }

        /**
         * 结束并发子任务计时，并在首次关闭时挂载到父节点时间线。
         *
         * <p>该方法具备幂等性，重复调用只会保留第一次完成结果，避免重复子节点和重复计时。
         *
         * <p>注意事项：
         * <ul>
         *   <li>必须在并发任务的 {@code finally} 中调用；</li>
         *   <li>若任务中抛出异常，也要确保执行本方法，否则总耗时与调用树都不完整。</li>
         * </ul>
         *
         * @since 7.250302.0
         */
        public void finish() {
            if (noop) {
                return;
            }
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            try {
                node.finish();
                // 延迟挂载：finish 时再插入父节点 timeline，保证出现在对应 recordStep 之后
                if (parent != null) {
                    parent.addChild(node);
                }
            } catch (Throwable t) {
                LoggerFactory.getLogger(PerformanceTracker.class)
                        .warn("[性能追踪] ParallelNode.finish 失败，原因: {}", t.toString());
            }
        }

        /**
         * 在并发子任务内记录一个执行步骤的耗时。
         *
         * <p>使用方式与 {@link PerformanceTracker#recordStep(String)} 一致，但作用域仅限当前并发节点。
         *
         * @param stepName 步骤名称
         * @since 7.250302.0
         */
        public void recordStep(@NonNull String stepName) {
            if (noop || node == null) {
                return;
            }
            try {
                node.recordStep(stepName);
            } catch (Throwable t) {
                LoggerFactory.getLogger(PerformanceTracker.class)
                        .warn("[性能追踪] ParallelNode.recordStep 失败，原因: {}", t.toString());
            }
        }
    }

    /**
     * 并发子任务的作用域句柄，由 {@link ParallelNode#openScope()} 返回。
     *
     * <p>该类用于在并发子线程中建立正确的追踪上下文，配合 {@code try-with-resources} 使用
     * 可自动管理安全节点栈的压入、弹出以及节点的结束计时。
     *
     * <p>在 {@link #close()} 时会自动调用关联的 {@link ParallelNode#finish()}，
     * 因此无需额外在 {@code finally} 中手动调用。
     *
     * @since 7.250302.0
     */
    public static final class ParallelScope implements AutoCloseable {

        /**
         * 所属的追踪器实例
         */
        private final PerformanceTracker owner;
        /**
         * 当前作用域对应的调用节点
         */
        private final MethodCallNode node;
        /**
         * 关联的并发节点，用于自动调用 finish()
         */
        private final ParallelNode parallelNode;
        /**
         * 关闭状态标记，用于保证 {@link #close()} 的幂等性
         */
        private final AtomicBoolean closed = new AtomicBoolean(false);
        /**
         * true 表示此实例是空操作节点，所有操作均为空实现
         */
        private final boolean noop;

        /**
         * 构造正常模式的作用域。
         *
         * @param owner        所属追踪器
         * @param node         当前调用节点
         * @param parallelNode 关联的并发节点
         */
        private ParallelScope(@NonNull PerformanceTracker owner, @NonNull MethodCallNode node,
                              @NonNull ParallelNode parallelNode) {
            this.owner = owner;
            this.node = node;
            this.parallelNode = parallelNode;
            this.noop = false;
        }

        /**
         * 构造可能为空操作模式的作用域。
         *
         * @param owner        所属追踪器
         * @param node         当前调用节点（可为 null）
         * @param parallelNode 关联的并发节点
         * @param noop         是否为空操作模式
         */
        private ParallelScope(@NonNull PerformanceTracker owner, MethodCallNode node,
                              @NonNull ParallelNode parallelNode, boolean noop) {
            this.owner = owner;
            this.node = node;
            this.parallelNode = parallelNode;
            this.noop = noop;
        }

        /**
         * 关闭当前作用域，自动弹出安全节点栈并结束计时。
         *
         * <p>该方法具备幂等性，重复调用不会产生副作用。
         * 同时会自动调用关联的 {@link ParallelNode#finish()} 完成节点计时和挂载。
         */
        @Override
        public void close() {
            if (noop) {
                return;
            }
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                Deque<MethodCallNode> safeNodeStack = owner.safeNodeStack();
                if (safeNodeStack.peekFirst() == node) {
                    safeNodeStack.pollFirst();
                } else {
                    safeNodeStack.removeFirstOccurrence(node);
                }
                // 自动结束计时并挂载到父节点
                parallelNode.finish();
            } catch (Throwable t) {
                LoggerFactory.getLogger(PerformanceTracker.class)
                        .warn("[性能追踪] ParallelScope.close 失败，原因: {}", t.toString());
            }
        }
    }

    /**
     * 在当前线程正在执行的方法节点内记录一个执行步骤的耗时。
     *
     * <p>调用此方法可以标记当前方法到此步骤结束的耗时。
     * 多次调用时，每次记录的是上一次 {@code recordStep} 或 {@code enter} 之后的耗时。
     *
     * <p><b>异常安全</b>：内部捕获所有异常，失败时仅打印警告，不影响主流程。
     *
     * <p>使用示例：
     * <pre>{@code
     * tracker.recordStep("初始化参数");
     * tracker.recordStep("查询数据库");
     * }</pre>
     *
     * @param stepName 步骤名称
     * @since 7.250302.0
     */
    public void recordStep(@NonNull String stepName) {
        try {
            Deque<MethodCallNode> callStack = callStack();
            if (!callStack.isEmpty()) {
                callStack.peek().recordStep(stepName);
            }
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] recordStep 失败，原因: {}", t.toString());
        }
    }

    /**
     * 退出当前线程最顶层的方法节点并结束计时。
     *
     * <p>若当前方法已是根节点，退出后调用栈为空，可通过 {@link #isFinished()} 判断追踪完成。
     *
     * <p>注意事项：
     * <ul>
     *   <li>必须与 {@link #enter(String)} / {@link #enter(String, String)} 成对使用；</li>
     *   <li>当调用栈为空时，本方法不会抛异常，而是安全忽略。</li>
     * </ul>
     *
     * @since 7.250302.0
     */
    /*
     * 仅用于关闭通过 enter(...) 打开的节点。对于 track(...)
     * 等可自动关闭的作用域接口，不要再额外调用本方法。
     */
    public void exit() {
        try {
            Deque<MethodCallNode> callStack = callStack();
            if (!callStack.isEmpty()) {
                MethodCallNode node = callStack.pop();
                node.finish();
            }
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] exit 失败，已忽略，原因: {}", t.toString());
        }
    }

    /**
     * 判断当前线程的调用栈是否已全部退出。
     *
     * <p>通常在请求入口切面的 {@code finally} 中调用，用于判断整条调用链是否采集完成。
     *
     * <p>使用示例：
     * <pre>{@code
     * tracker.enter("API");
     * try {
     *     tracker.recordStep("query-db");
     * } finally {
     *     tracker.exit();
     * }
     * if (tracker.isFinished()) {
     *     log.debug(tracker.prettyPrint());
     * }
     * }</pre>
     *
     * @return {@code true} 表示追踪已完成
     * @since 7.250302.0
     */
    public boolean isFinished() {
        try {
            return callStack().isEmpty() && rootRef.get() != null;
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] isFinished 判断失败，按未完成处理，原因: {}", t.toString());
            return false;
        }
    }

    /**
     * 判断当前线程是否处于根调用层级（调用栈深度为 1）。
     *
     * <p>在 AOP 切面的 finally 块中调用 {@code exit()} <b>之前</b> 用于判断是否为入口方法，
     * 以决定是否打印完整追踪报告。
     *
     * @return {@code true} 表示当前方法是根调用
     * @since 7.250302.0
     */
    public boolean isRootLevel() {
        try {
            return callStack().size() == 1;
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] isRootLevel 判断失败，按非根层处理，原因: {}", t.toString());
            return false;
        }
    }

    /**
     * 获取追踪器标识。
     *
     * @return 标识字符串
     * @since 7.250302.0
     */
    public String getId() {
        return id;
    }

    /**
     * 获取当前调用树的根节点。
     *
     * <p>返回值适合用于单元测试断言或自定义统计输出；若尚未开始采集，则返回 {@code null}。
     *
     * @return 根节点，若尚未 enter 则返回 {@code null}
     * @since 7.250302.0
     */
    @Nullable
    public MethodCallNode getRoot() {
        return rootRef.get();
    }

    /**
     * 将整棵调用树渲染为可直接输出到日志的字符串格式。
     *
     * <p>仅在 {@link #isFinished()} 为 {@code true} 时调用有意义。
     *
     * <p>使用示例：
     * <pre>{@code
     * if (tracker.isFinished()) {
     *     System.out.println(tracker.prettyPrint());
     * }
     * }</pre>
     *
     * @return 格式化的性能报告字符串
     * @since 7.250302.0
     */
    public String prettyPrint() {
        return prettyPrintTree();
    }

    /**
     * 以树形方式输出完整调用链。
     *
     * @return 树形输出字符串
     */
    public String prettyPrintTree() {
        MethodCallNode root = rootRef.get();
        if (root == null) {
            return "PerformanceTracker '" + id + "': no data recorded";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("PerformanceTracker '").append(id).append("'\n");
        sb.append(root.prettyPrint());
        return sb.toString();
    }

    /**
     * 以扁平列表方式输出完整调用链。
     *
     * <p>每一行都会展示当前节点或步骤的深度、类型、名称、耗时、时间戳以及异步标识，
     * 便于日志采集系统或人工检索。
     *
     * @return 扁平列表输出字符串
     */
    public String prettyPrintFlat() {
        MethodCallNode root = rootRef.get();
        if (root == null) {
            return "PerformanceTracker '" + id + "': no data recorded";
        }
        List<String> lines = new ArrayList<>();
        collectFlatLines(root, 0, lines);
        return "PerformanceTracker '" + id + "'\n" + String.join("\n", lines);
    }

    /**
     * 递归收集扁平化输出行。
     *
     * <p>遍历节点树，将每个方法和步骤转换为单行文本，包含深度、类型、名称、耗时等信息。
     *
     * @param node  当前节点
     * @param depth 当前深度
     * @param lines 输出行集合
     */
    private void collectFlatLines(@NonNull MethodCallNode node, int depth, @NonNull List<String> lines) {
        lines.add(String.format(Locale.ROOT,
                "depth=%d type=METHOD name=%s async=%s total=%s self=%s async_wait=%s start=%d end=%d",
                depth,
                node.getMethodName(),
                node.isParallel(),
                formatDuration(node.getTotalNanos()),
                formatDuration(node.getExclusiveNanos()),
                formatDuration(node.getAsyncWaitNanos()),
                node.getStartEpochMillis(),
                node.getEndEpochMillis()));
        for (MethodCallNode.StepSnapshot stepSnapshot : node.getStepSnapshots()) {
            lines.add(String.format(Locale.ROOT,
                    "depth=%d type=STEP name=%s duration=%s",
                    depth + 1,
                    stepSnapshot.getName(),
                    formatDuration(stepSnapshot.getDurationNanos())));
        }
        for (MethodCallNode child : node.getChildren()) {
            collectFlatLines(child, depth + 1, lines);
        }
    }

    /**
     * 将纳秒耗时格式化为秒（保留 3 位小数）。
     *
     * @param nanos 纳秒数
     * @return 格式化字符串，如 "3.125s"
     */
    private static String formatDuration(long nanos) {
        return String.format(Locale.ROOT, "%.3fs", nanos / 1_000_000_000.0);
    }

    /**
     * 获取当前线程的调用栈深度。
     *
     * <p>该方法常用于测试、诊断或在切面中进行保护性判断。
     *
     * @return 栈深度
     * @since 7.250302.0
     */
    public int getStackDepth() {
        try {
            return callStack().size();
        } catch (Throwable t) {
            LoggerFactory.getLogger(PerformanceTracker.class)
                    .warn("[性能追踪] getStackDepth 失败，返回 0，原因: {}", t.toString());
            return 0;
        }
    }

}
