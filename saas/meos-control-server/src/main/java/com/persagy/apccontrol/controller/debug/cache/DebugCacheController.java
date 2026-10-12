package com.persagy.apccontrol.controller.debug.cache;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CacheLockKey;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.message.CacheTriggerEvent;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.basic.structure.cacherwd.service.CacheRwdDomainService;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.repository.DemandConfigResultRefreshRepository;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 缓存调试控制器。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@RestController
@RequestMapping(value = "/debug/cache")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DebugCacheController implements LogComponent {

    /**
     * 需求配置结果刷新仓储。
     *
     * @since v7.260701.0
     */
    private final @NonNull DemandConfigResultRefreshRepository demandConfigResultRefreshRepository;

    /**
     * 物理世界对象缓存领域服务，用于触发完整缓存对象模型构建。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheRwdDomainService cacheRwdDomainService;

    /**
     * 缓存对象模型状态服务，用于校验当前版本支持的一级运行监测类型。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService cacheObjectModelStatusService;

    @Override
    public @NonNull String logTag() {
        return "DebugCacheController";
    }

    /**
     * 触发需求配置结果初始化。
     *
     * <p>该调试接口直接使用请求头中的当前集团和项目上下文调用结果刷新业务，
     * 不复用 XXL-JOB 的集团、项目遍历逻辑，避免业务方法清理外层任务上下文。</p>
     *
     * @return 执行结果
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @PostMapping("/demandConfigResult/init")
    public @NonNull R<Object> initDemandConfigResult() {
        CacheLockKey.Key key = CacheLockKey.Trigger.cache();
        new CacheTriggerEvent().executeWithLock(this, key, () -> {
            for (DemandTemplateTypeEnum templateType :
                    this.demandConfigResultRefreshRepository.listSupportedTemplateTypes()) {
                this.demandConfigResultRefreshRepository.refreshByTemplateType(templateType);
            }
        });
        return R.success(true);
    }

    /**
     * 触发当前集团和项目的缓存对象模型初始化。
     *
     * <p>该调试接口只执行指定的一级运行监测类型，并直接复用完整缓存构建入口；
     * 不进入 XXL-JOB 的全集团、全项目遍历。完整构建成功后，领域服务会在构建锁释放前统一
     * 持久化 READY 并刷新共享 Redis。</p>
     *
     * @param runningMonitorType 一级运行监测类型，仅支持 {@code AIR_TERMINAL}、{@code LIGHTING}
     * @return 执行结果
     * @throws BusinessException 类型不受支持时抛出
     * @author JiYinchuan
     * @since v7.260701.0
     */
    @PostMapping("/cacheObjectModel/init")
    public @NonNull R<Object> initCacheObjectModel(
            @RequestParam("runningMonitorType")
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        if (!this.cacheObjectModelStatusService.isSupported(runningMonitorType)) {
            throw CacheObjectModelExceptionFactory.invalidStructure(
                    "runningMonitorType");
        }
        return R.success(this.cacheRwdDomainService.buildCache(runningMonitorType));
    }

}
