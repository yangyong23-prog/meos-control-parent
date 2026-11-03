package com.persagy.apccontrol.common.opt;

import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR;
import com.persagy.apccontrol.common.outside.twin.DtpTwinClientServiceImpl;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * opt 分支 B8（switch.opt.twinWriteDiff，默认关）：壳配置同步到 Twin 时只写有变化的配置。
 * <p>
 * 只在 {@link #runScheduledSync} 包住的定时同步（InnerSchdule.synShellConfigToTwin）里生效，界面操作等其它调用路径不受影响：
 * <ul>
 *     <li>payload 每一项都不变且距上次成功写入不到 {@code opt.twin-write.full-rewrite-hours}（默认 24）小时不写（见 {@link OptTwinWriteLedger}）；</li>
 *     <li>写入结果经 {@link DtpTwinClientServiceImpl#handleResult} 校验，失败抛异常、不记账，下一轮重写。</li>
 * </ul>
 * 开关关闭或不在定时同步内时，原样调用、原样忽略返回值。
 */
@Component
public class OptTwinWriteGuard {

    private static final ThreadLocal<Boolean> SCHEDULED_SYNC = new ThreadLocal<>();

    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private ProjectCfg projectCfg;

    @Value("${opt.twin-write.full-rewrite-hours:24}")
    private long fullRewriteHours;

    private OptTwinWriteLedger ledger;

    @PostConstruct
    void init() {
        ledger = new OptTwinWriteLedger(new OptRedisRealtimeStore(redisCacheUtil), System::currentTimeMillis, fullRewriteHours);
    }

    public static void runScheduledSync(Runnable body) {
        SCHEDULED_SYNC.set(Boolean.TRUE);
        try {
            body.run();
        } finally {
            SCHEDULED_SYNC.remove();
        }
    }

    public static boolean active() {
        return Boolean.TRUE.equals(SCHEDULED_SYNC.get()) && OptSwitch.on(OptSwitch.TWIN_WRITE_DIFF);
    }

    /**
     * @param group 记账分组：保存和对应的删除用同一组
     * @param op    写操作名
     */
    public void write(String group, String op, Collection<?> payload, Supplier<DtpTwinR<Object>> call) {
        if (!active()) {
            call.get();
            return;
        }
        ledger.write(group, op, projectCfg.getProjectId(), payload, () -> {
            DtpTwinClientServiceImpl.handleResult(call.get());
            return true;
        });
    }
}
