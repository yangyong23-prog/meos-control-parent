package com.persagy.apccontrol.basic.structure.infocodeconfig.lifecycle;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.structure.infocodeconfig.context.InfoCodeAggregateInitContext;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.validate.utility.enumeration.base.BaseEnum;
import org.slf4j.event.Level;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 信息点聚合根生命周期日志回调
 *
 * @author JiYinchuan
 * @since 5.240801.0
 */
@Component
public class InfoCodeAggregateLifeLogAware implements InfoCodeAggregateLifeAware, LogComponent {

    private static final Level LOG_LEVEL = Level.TRACE;

    @Override
    public void beforeContextInit(@NonNull InfoCodeInitByObjectParam param) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "初始化上下文开始");
        }
    }

    @Override
    public void handleDatabaseData(@org.jspecify.annotations.NonNull InfoCodeAggregateInitContext context) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "数据库数据处理开始");
        }
    }

    @Override
    public void afterContextInit(@NonNull InfoCodeAggregateInitContext context) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "初始化上下文结束");
        }
    }

    @Override
    public void afterAggregateInit(@NonNull List<InfoCodeAggregate> aggregates) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "聚合根初始化结束");
        }
    }

    @Override
    public void beforeFillInfoCodeDefineModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "填充信息点定义模型开始");
        }
    }

    @Override
    public void afterFillInfoCodeDefineModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "填充信息点定义模型结束");
        }
    }

    @Override
    public void beforeFillInfoCodeDataModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "填充信息点值模型开始");
        }
    }

    @Override
    public void afterFillInfoCodeDataModel(@NonNull Collection<InfoCodeAggregate> aggregates) {
        if (logEnabled()) {
            logProjectInstance().log(LOG_LEVEL, "填充信息点值模型结束");
        }
    }

    /**
     * 日志是否启用
     *
     * @return 日志是否启用
     * @since 5.240801.0
     */
    private boolean logEnabled() {
        switch (LOG_LEVEL) {
            case TRACE:
                return log().isTraceEnabled();
            case DEBUG:
                return log().isDebugEnabled();
            case INFO:
                return log().isInfoEnabled();
            case WARN:
                return log().isWarnEnabled();
            case ERROR:
                return log().isErrorEnabled();
            default:
                throw BaseEnum.DEFAULT_ANALYZE_EXCEPTION.apply(LOG_LEVEL.getClass().getSimpleName(), LOG_LEVEL);
        }
    }

    @Override
    public String logTag() {
        return "信息点聚合根生命周期回调-日志";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
