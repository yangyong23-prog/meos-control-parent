package com.persagy.apccontrol.task.cacheobjectmodel;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.utils.context.ContextUtils;
import com.persagy.apccontrol.task.BaseXXJob;
import com.persagy.apccontrol.task.TecXxJobHandler;
import com.persagy.apccontrol.task.cacheobjectmodel.param.CacheObjectModelTaskParam;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.message.CacheTriggerEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.event.Level;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 缓存对象模型手动初始化任务处理器。
 *
 * <p>该处理器复用物理世界与信息点模板的完整重建链路生成新版缓存对象模型。每个一级类型
 * 构建成功后，由领域服务在构建锁内统一提交 READY 并刷新共享 Redis。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component(CacheObjectModelInitTaskHandler.BEAN_NAME)
public class CacheObjectModelInitTaskHandler extends BaseXXJob implements TecXxJobHandler {

    /**
     * Spring Bean 和 XXL-JOB 处理器名称。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String BEAN_NAME = "cacheObjectModelInitTaskHandler";

    /**
     * 任务描述和日志标签。
     *
     * @since v7.260701.0
     */
    public static final @NonNull String LOG_TAG = "缓存对象模型手动初始化";

    /**
     * 物理世界对象缓存领域服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRwdDomainService cacheRwdDomainService;

    /**
     * 缓存模型状态服务，用于取得本版本固定支持类型集合。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService statusService;

    /**
     * 创建缓存对象模型手动初始化任务处理器。
     *
     * @param cacheRwdDomainService 物理世界对象缓存领域服务
     * @param statusService         缓存模型状态服务
     * @since v7.260701.0
     */
    public CacheObjectModelInitTaskHandler(
            @NonNull CacheRwdDomainService cacheRwdDomainService,
            @NonNull CacheObjectModelStatusService statusService) {
        super();
        this.cacheRwdDomainService = cacheRwdDomainService;
        this.statusService = statusService;
    }

    /**
     * 执行手动初始化任务。
     *
     * <p>在进入集团和项目遍历前先校验任务参数，避免非法参数对每个项目
     * 重复执行和记录失败。</p>
     *
     * @return XXL-JOB 执行结果
     * @throws BusinessException 任务参数非法时抛出
     * @since v7.260701.0
     */
    public @NonNull ReturnT<String> execute() {
        CacheObjectModelTaskParam.parse(
                XxlJobHelper.getJobParam(), this.statusService.supportedRunningMonitorTypes());
        return super.processHandleWithoutTimeCheck(this);
    }

    /**
     * 获取任务名称。
     *
     * @return 缓存对象模型初始化任务名称
     * @since v7.260701.0
     */
    @Override
    public @NonNull String getHandlerName() {
        return CacheObjectModelInitTaskHandler.LOG_TAG;
    }

    /**
     * 缓存初始化只在 XXL-JOB 保留任务起止和实际项目终态，遍历明细写入 DEBUG。
     *
     * @return 遍历进度日志级别
     * @since v7.260701.0
     */
    @Override
    protected @NonNull Level traversalProgressLogLevel() {
        return Level.DEBUG;
    }

    /**
     * 执行一个项目的初始化并返回已完成结果。
     *
     * <p>公共 XXL-JOB 基类按项目收集 Future 结果。初始化内部按一级类型串行
     * 执行，因此这里不再引入额外线程池。</p>
     *
     * @param groupCode 集团编码
     * @param projectDTO 项目
     * @return 已完成的项目执行结果
     * @since v7.260701.0
     */
    @Override
    public @NonNull Future<String> processAsync(
            @NonNull String groupCode,
            @NonNull ProjectDTO projectDTO) {
        this.process(groupCode, projectDTO);
        return new AsyncResult<>(this.getHandlerName() + "执行完成:"
                + groupCode + ";projectId=" + projectDTO.getProjectId());
    }

    /**
     * 按当前 XXL-JOB 参数执行一个项目的初始化。
     *
     * <p>项目过滤在初始化上下文之前完成；进入项目后按一级运行监测类型隔离失败，并在
     * 当前线程退出前清理租户上下文，避免污染公共任务线程。</p>
     *
     * @param groupCode 集团编码
     * @param projectDTO 项目
     * @throws BusinessException 任务参数或任一一级类型初始化失败时抛出
     * @since v7.260701.0
     */
    @Override
    public void process(@NonNull String groupCode, @NonNull ProjectDTO projectDTO) {
        CacheObjectModelTaskParam param = CacheObjectModelTaskParam.parse(
                XxlJobHelper.getJobParam(), this.statusService.supportedRunningMonitorTypes());
        boolean skipGroup = CollectionUtils.isNotEmpty(param.getGroupCodes())
                && !param.getGroupCodes().contains(groupCode);
        boolean skipProject = CollectionUtils.isNotEmpty(param.getProjectIds())
                && !param.getProjectIds().contains(projectDTO.getProjectId());
        if (skipGroup || skipProject) {
            return;
        }
        // BaseXXJob 可能复用任务线程，项目上下文必须与本次项目执行保持同一 try/finally 边界。
        ContextUtils.initGroupCodeProjectId(groupCode, projectDTO.getProjectId());
        StopWatch stopWatch = StopWatch.createStarted();
        List<String> runningMonitorTypes = param.getRunningMonitorTypes().stream()
                .map(RunningMonitorTypeEnum::value)
                .collect(Collectors.toList());
        try {
            CacheLockKey.Key key = CacheLockKey.Trigger.cache();
            new CacheTriggerEvent().executeWithLock(this, key, () ->
                    this.executeTypes(param));
            this.logProjectInstance().info("缓存对象模型项目构建成功", Arrays.asList(
                    Pair.of("runningMonitorTypes", runningMonitorTypes),
                    Pair.of("costTimeMs", stopWatch.getTime())));
        } catch (RuntimeException exception) {
            // 项目最终日志不重复打印堆栈，异常堆栈由任务边界统一记录；这里保留可检索的项目和原因。
            String failureReason = StringUtils.defaultIfBlank(
                    exception.getMessage(), exception.getClass().getSimpleName());
            this.logProjectInstance().error("缓存对象模型项目构建失败", Arrays.asList(
                    Pair.of("runningMonitorTypes", runningMonitorTypes),
                    Pair.of("costTimeMs", stopWatch.getTime()),
                    Pair.of("failureType", exception.getClass().getSimpleName()),
                    Pair.of("failureReason", failureReason)));
            throw exception;
        } finally {
            ContextUtil.unloadContext();
        }
    }

    /**
     * 在当前项目中逐类型初始化并汇总失败。
     *
     * <p>单个类型失败后继续执行后续类型。存在真实异常时优先原样抛出首个异常；只有
     * {@code false} 失败时才创建国际化业务异常，使公共 XXL-JOB 基类正确标记当前项目失败。</p>
     *
     * @param param 已规范化的任务参数
     * @throws RuntimeException 任一类型抛出运行时异常时原样抛出首个异常
     * @throws BusinessException 类型只返回失败标识、没有原始异常时抛出
     * @since v7.260701.0
     */
    private void executeTypes(
            @NonNull CacheObjectModelTaskParam param) {
        RuntimeException firstException = null;
        RunningMonitorTypeEnum firstIncompleteType = null;
        for (RunningMonitorTypeEnum runningMonitorType : param.getRunningMonitorTypes()) {
            try {
                // 完整构建入口同时执行类型策略和通用模板策略，并在成功后于构建锁内提交 READY。
                if (!Boolean.TRUE.equals(this.cacheRwdDomainService.buildCache(runningMonitorType))) {
                    // false 没有原始异常，但仍代表当前项目失败；继续其他类型后统一失败项目。
                    if (firstIncompleteType == null) {
                        firstIncompleteType = runningMonitorType;
                    }
                }
            } catch (RuntimeException exception) {
                if (firstException == null) {
                    // 继续隔离执行后续类型，但最终必须保留首个异常的根因。
                    firstException = exception;
                }
            }
        }
        if (firstException != null) {
            throw firstException;
        }
        if (firstIncompleteType != null) {
            throw CacheObjectModelExceptionFactory.operationFailed(
                    "cacheBuild." + firstIncompleteType.value());
        }
    }

    /**
     * 获取日志标签。
     *
     * @return 缓存对象模型初始化日志标签
     * @since v7.260701.0
     */
    @Override
    public @NonNull String logTag() {
        return CacheObjectModelInitTaskHandler.LOG_TAG;
    }
}
