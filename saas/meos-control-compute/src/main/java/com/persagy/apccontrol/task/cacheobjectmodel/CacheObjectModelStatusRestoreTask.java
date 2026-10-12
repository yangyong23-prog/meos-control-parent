package com.persagy.apccontrol.task.cacheobjectmodel;

import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.config.cache.provider.redis.RedisCacheTemplate;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.cache.CacheObjectModelStatusEnum;
import com.persagy.apccontrol.basic.resource.cache.CacheObjectModelStatusService;
import com.persagy.apccontrol.common.outside.MidPermissionService;
import com.persagy.apccontrol.common.outside.dto.GroupDTO;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 云端服务启动时恢复缓存对象模型 READY 状态。
 *
 * <p>云端部署承载多个集团和项目，不能依赖本地 {@code ProjectCfg}。本任务通过权限服务遍历
 * 有效范围，在对应租户上下文中批量读取状态数据库，只以 SETNX 写入缺失的 READY Key。
 * 任一范围恢复失败保持旧链路并继续其他项目，不阻断服务启动。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Component
@ConditionalOnProperty(prefix = "project.cache.rwd", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class CacheObjectModelStatusRestoreTask implements CommandLineRunner, LogComponent {

    /**
     * 多集团和项目查询服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull MidPermissionService midPermissionService;

    /**
     * 缓存对象模型状态服务。
     *
     * @since v7.260701.0
     */
    private final @NonNull CacheObjectModelStatusService statusService;

    /**
     * 云端共享 Redis 操作模板。
     *
     * @since v7.260701.0
     */
    private final @NonNull RedisCacheTemplate redisCacheTemplate;

    /**
     * 创建云端 READY 状态恢复任务。
     *
     * @param midPermissionService 集团和项目查询服务
     * @param statusService        状态服务
     * @param redisCacheTemplate   云端 Redis 模板
     * @since v7.260701.0
     */
    public CacheObjectModelStatusRestoreTask(
            @NonNull MidPermissionService midPermissionService,
            @NonNull CacheObjectModelStatusService statusService,
            @NonNull RedisCacheTemplate redisCacheTemplate) {
        super();
        this.midPermissionService = midPermissionService;
        this.statusService = statusService;
        this.redisCacheTemplate = redisCacheTemplate;
    }

    /**
     * 遍历有效集团和项目恢复缺失的 READY Key。
     *
     * @param args 启动参数
     * @since v7.260701.0
     */
    @Override
    public synchronized void run(@NonNull String... args) {
        List<GroupDTO> groups;
        try {
            groups = this.midPermissionService.queryValidGroupList();
        } catch (RuntimeException exception) {
            this.logInstance().warn("缓存对象模型状态启动恢复查询集团失败，保持旧链路",
                    exception);
            return;
        }
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }
        for (GroupDTO group : groups) {
            if (StringUtils.isBlank(group.getCode())) {
                this.logInstance().warn("缓存对象模型状态启动恢复跳过非法集团");
                continue;
            }
            this.restoreGroup(group.getCode());
        }
    }

    /**
     * 恢复单个集团的全部在用项目。
     *
     * @param groupCode 集团编码
     * @since v7.260701.0
     */
    private void restoreGroup(@NonNull String groupCode) {
        List<ProjectDTO> projects;
        try {
            projects = this.midPermissionService.queryIsInUseProjectDTOList(groupCode);
        } catch (RuntimeException exception) {
            this.logInstance().warn("缓存对象模型状态启动恢复查询项目失败，跳过当前集团",
                    exception, Pair.of("groupCode", groupCode));
            return;
        }
        if (CollectionUtils.isEmpty(projects)) {
            return;
        }
        for (ProjectDTO project : projects) {
            if (StringUtils.isBlank(project.getProjectId())) {
                this.logInstance().warn("缓存对象模型状态启动恢复跳过非法项目",
                        Pair.of("groupCode", groupCode));
                continue;
            }
            if (!this.restoreProject(groupCode, project.getProjectId())) {
                // 同一集团的项目共用集团库；库或状态表不存在时继续遍历只会重复产生相同异常。
                return;
            }
        }
    }

    /**
     * 在租户上下文中批量读取数据库并恢复一个项目的 READY Key。
     *
     * @param groupCode 集团编码
     * @param projectId 项目 ID
     * @return 状态存储是否可用；不可用时跳过当前集团的后续项目
     * @since v7.260701.0
     */
    private boolean restoreProject(@NonNull String groupCode, @NonNull String projectId) {
        ContextUtil.setGroupCode(groupCode);
        ContextUtil.setTenant(groupCode);
        ContextUtil.setProjectId(projectId);
        try {
            Set<RunningMonitorTypeEnum> missingTypes = this.listMissingTypes(groupCode, projectId);
            if (CollectionUtils.isEmpty(missingTypes)) {
                return true;
            }
            // 只查询 Redis 缺失的类型；已有值可能由测试或运维手工维护，恢复任务不得覆盖。
            Map<RunningMonitorTypeEnum, CacheObjectModelStatusEnum> statuses =
                    this.statusService.getPersistedStatuses(missingTypes);
            for (Map.Entry<RunningMonitorTypeEnum, CacheObjectModelStatusEnum> entry
                    : statuses.entrySet()) {
                if (CacheObjectModelStatusEnum.READY.equals(entry.getValue())) {
                    this.writeReadyIfAbsent(groupCode, projectId, entry.getKey());
                }
            }
            return true;
        } catch (RuntimeException exception) {
            if (exception instanceof BadSqlGrammarException
                    && this.isMissingStatusStorage((BadSqlGrammarException) exception)) {
                this.logInstance().warn("缓存对象模型状态库或表不存在，跳过当前集团状态恢复",
                        Arrays.asList(
                                Pair.of("groupCode", groupCode),
                                Pair.of("projectId", projectId)));
                return false;
            }
            this.logProjectInstance().warn("缓存对象模型状态启动恢复失败，当前项目保持旧链路",
                    exception, Arrays.asList(
                            Pair.of("groupCode", groupCode),
                            Pair.of("projectId", projectId)));
            return true;
        } finally {
            ContextUtil.unloadContext();
        }
    }

    /**
     * 判断异常是否由集团库或状态表尚未创建导致。
     *
     * <p>启动恢复允许在部分集团尚未完成迁移时继续启动；这里只识别明确的 MySQL 缺库、
     * 缺表错误码，其他 SQL、连接和事务异常仍按原路径记录完整异常。</p>
     *
     * @param exception 待判断异常
     * @return 是否为状态存储不存在
     * @since v7.260701.0
     */
    private boolean isMissingStatusStorage(@NonNull BadSqlGrammarException exception) {
        SQLException sqlException = exception.getSQLException();
        int errorCode = sqlException.getErrorCode();
        return errorCode == 1049 || errorCode == 1146
                || "42S02".equals(sqlException.getSQLState());
    }

    /**
     * 查找当前集团项目中尚未存在状态 Key 的支持类型。
     *
     * <p>这里按 Key 是否存在判断，而不是解析 Key 的值：FAILED、未知值以及人工设置的值都属于
     * 已存在状态，恢复任务不得覆盖它们。后续写入仍使用 SETNX，保护检查和写入之间的并发窗口。</p>
     *
     * @param groupCode 集团编码
     * @param projectId 项目 ID
     * @return 缺失的支持类型集合
     * @since v7.260701.0
     */
    private @NonNull Set<RunningMonitorTypeEnum> listMissingTypes(
            @NonNull String groupCode, @NonNull String projectId) {
        Set<RunningMonitorTypeEnum> missingTypes = EnumSet.noneOf(RunningMonitorTypeEnum.class);
        Collection<RunningMonitorTypeEnum> supportedTypes =
                this.statusService.supportedRunningMonitorTypes();
        for (RunningMonitorTypeEnum runningMonitorType : supportedTypes) {
            String key = RedisKey.getCacheObjectModelStatusKey(
                    groupCode, projectId, runningMonitorType);
            if (!this.redisCacheTemplate.hasKey(key)) {
                missingTypes.add(runningMonitorType);
            }
        }
        return missingTypes;
    }

    /**
     * 原子写入缺失的 READY Key，保留运维已手工设置的任何 Redis 值。
     *
     * @param groupCode           集团编码
     * @param projectId           项目 ID
     * @param runningMonitorType 一级运行监测类型
     * @since v7.260701.0
     */
    private void writeReadyIfAbsent(
            @NonNull String groupCode,
            @NonNull String projectId,
            @NonNull RunningMonitorTypeEnum runningMonitorType) {
        String key = RedisKey.getCacheObjectModelStatusKey(
                groupCode, projectId, runningMonitorType);
        Boolean written = this.redisCacheTemplate.delegate().opsForValue().setIfAbsent(
                key, CacheObjectModelStatusEnum.READY.getCode());
        if (Boolean.TRUE.equals(written)) {
            this.logProjectInstance().info("缓存对象模型 READY 状态已从数据库恢复到云端 Redis",
                    Pair.of("runningMonitorType", runningMonitorType.value()));
        }
    }

    /**
     * 返回日志标签。
     *
     * @return 日志标签
     * @since v7.260701.0
     */
    @Override
    public @NonNull String logTag() {
        return "CacheObjectModelStatusRestoreTask";
    }
}
