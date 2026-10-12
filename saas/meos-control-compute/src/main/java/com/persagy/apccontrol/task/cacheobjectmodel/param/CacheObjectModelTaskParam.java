package com.persagy.apccontrol.task.cacheobjectmodel.param;

import com.persagy.apccontrol.basic.exception.CacheObjectModelExceptionFactory;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.exception.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 缓存对象模型初始化任务参数。
 *
 * <p>参数只描述任务范围，不携带任务状态。一级运行监测类型由调用方传入的固定支持集合校验，
 * 避免把支持范围复制到任务参数中维护。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
public class CacheObjectModelTaskParam {

    /**
     * 指定集团编码；为空时遍历全部有效集团。
     *
     * @since v7.260701.0
     */
    private @Nullable LinkedHashSet<@NonNull String> groupCodes;

    /**
     * 指定项目 ID；为空时遍历集团下全部有效项目。
     *
     * @since v7.260701.0
     */
    private @Nullable LinkedHashSet<@NonNull String> projectIds;

    /**
     * 指定一级运行监测类型；为空时由固定支持集合提供默认值。
     *
     * @since v7.260701.0
     */
    private @NonNull LinkedHashSet<@NonNull RunningMonitorTypeEnum> runningMonitorTypes =
            new LinkedHashSet<>();

    /**
     * 解析任务参数并校验范围。
     *
     * <p>空参数表示全量范围。JSON 解析使用项目 fail-safe 工具；解析失败或范围包含不支持的类型时，
     * 在进入集团/项目遍历前直接抛出国际化业务异常，避免对每个项目重复报同一错误。</p>
     *
     * @param jobParam      XXL-JOB 原始参数，可为空
     * @param supportedTypes 当前版本允许的一级运行监测类型
     * @return 规范化任务参数
     * @throws BusinessException 参数 JSON、字符串集合或类型非法时抛出
     * @since v7.260701.0
     */
    public static @NonNull CacheObjectModelTaskParam parse(
            @Nullable String jobParam,
            @NonNull Set<RunningMonitorTypeEnum> supportedTypes) {
        CacheObjectModelTaskParam result;
        if (StringUtils.isBlank(jobParam)) {
            result = new CacheObjectModelTaskParam();
        } else {
            result = ObjectMapperUtils.readValueFailSafe(jobParam, CacheObjectModelTaskParam.class);
            if (result == null) {
                throw CacheObjectModelExceptionFactory.invalidStructure(
                        "cacheObjectModelTaskParam");
            }
        }
        result.normalize(supportedTypes);
        return result;
    }

    /**
     * 获取集团过滤条件。
     *
     * @return 集团编码集合；未限制时返回 {@code null}
     * @since v7.260701.0
     */
    public @Nullable LinkedHashSet<String> getGroupCodes() {
        return this.groupCodes;
    }

    /**
     * 设置集团过滤条件。
     *
     * @param groupCodes 集团编码集合
     * @since v7.260701.0
     */
    public void setGroupCodes(@Nullable LinkedHashSet<String> groupCodes) {
        this.groupCodes = groupCodes;
    }

    /**
     * 获取项目过滤条件。
     *
     * @return 项目 ID 集合；未限制时返回 {@code null}
     * @since v7.260701.0
     */
    public @Nullable LinkedHashSet<String> getProjectIds() {
        return this.projectIds;
    }

    /**
     * 设置项目过滤条件。
     *
     * @param projectIds 项目 ID 集合
     * @since v7.260701.0
     */
    public void setProjectIds(@Nullable LinkedHashSet<String> projectIds) {
        this.projectIds = projectIds;
    }

    /**
     * 获取一级运行监测类型过滤条件。
     *
     * @return 一级运行监测类型集合；解析后始终为非空集合
     * @since v7.260701.0
     */
    public @NonNull LinkedHashSet<@NonNull RunningMonitorTypeEnum> getRunningMonitorTypes() {
        return this.runningMonitorTypes;
    }

    /**
     * 设置一级运行监测类型过滤条件。
     *
     * @param runningMonitorTypes 一级运行监测类型集合
     * @since v7.260701.0
     */
    public void setRunningMonitorTypes(
            @Nullable LinkedHashSet<RunningMonitorTypeEnum> runningMonitorTypes) {
        this.runningMonitorTypes = runningMonitorTypes == null
                ? new LinkedHashSet<>() : runningMonitorTypes;
    }

    /**
     * 规范化字符串过滤条件和一级类型默认值。
     *
     * @param supportedTypes 当前版本固定支持的一级运行监测类型
     * @throws BusinessException 过滤值为空或类型不支持时抛出
     * @since v7.260701.0
     */
    private void normalize(@NonNull Set<RunningMonitorTypeEnum> supportedTypes) {
        this.groupCodes = this.normalizeTexts(this.groupCodes, "groupCodes");
        this.projectIds = this.normalizeTexts(this.projectIds, "projectIds");
        if (CollectionUtils.isEmpty(this.runningMonitorTypes)) {
            this.runningMonitorTypes = new LinkedHashSet<>(supportedTypes);
            return;
        }
        LinkedHashSet<RunningMonitorTypeEnum> normalizedTypes = new LinkedHashSet<>();
        for (RunningMonitorTypeEnum runningMonitorType : this.runningMonitorTypes) {
            if (runningMonitorType == null || !supportedTypes.contains(runningMonitorType)) {
                throw CacheObjectModelExceptionFactory.invalidStructure(
                        "runningMonitorTypes");
            }
            normalizedTypes.add(runningMonitorType);
        }
        this.runningMonitorTypes = normalizedTypes;
    }

    /**
     * 规范化可选文本集合；空集合保留为空集合，表示不限制范围。
     *
     * @param values    原始文本集合
     * @param valueName 稳定字段名
     * @return 去重后的文本集合；输入为 {@code null} 时返回 {@code null}
     * @throws BusinessException 集合元素为空时抛出
     * @since v7.260701.0
     */
    private @Nullable LinkedHashSet<String> normalizeTexts(
            @Nullable Collection<String> values,
            @NonNull String valueName) {
        if (values == null) {
            return null;
        }
        LinkedHashSet<String> normalizedValues = new LinkedHashSet<>();
        for (String value : values) {
            if (StringUtils.isBlank(value)) {
                throw CacheObjectModelExceptionFactory.requiredValue(valueName);
            }
            normalizedValues.add(value.trim());
        }
        return normalizedValues;
    }
}
