package com.persagy.apccontrol.basic.executor.log;

import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 基础日志执行器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public abstract class BaseLogExecutor implements LogProvider {

    /**
     * 集团编码
     */
    protected final String groupCode;

    /**
     * 项目ID
     */
    protected final String projectId;

    /**
     * 构造器
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @since v5.1.0
     */
    protected BaseLogExecutor(@Nullable String groupCode, @Nullable String projectId) {
        this.groupCode = StringUtils.defaultString(groupCode, ContextUtil.getGroupCode());
        this.projectId = StringUtils.defaultString(projectId, ContextUtil.getProjectId());
    }

    @Override
    public String log(
            @NonNull Logger logger, @NonNull Level level, @NonNull String logTag,
            @NonNull String logDetailTag, @Nullable Throwable throwable,
            @Nullable List<Pair<String, Object>> additionalMessages) {
        ArgArray builder = ArgArray.builder();
        if (StringUtils.isNotBlank(groupCode)) {
            builder.originalMessages(Pair.of("groupCode", groupCode));
        }
        if (StringUtils.isNotBlank(projectId)) {
            builder.originalMessages(Pair.of("projectId", projectId));
        }
        builder.additionalMessages(additionalMessages);
        return LogProvider.super.log(logger, level, logTag, logDetailTag, throwable, builder.parse());
    }

    /**
     * 参数数组类
     *
     * @author JiYinchuan
     * @since v5.1.0
     */
    protected static class ArgArray {

        /**
         * 原始信息
         */
        private final List<Pair<String, Object>> originalMessages = new ArrayList<>();

        /**
         * 附加信息
         */
        private final List<Pair<String, Object>> additionalMessages = new ArrayList<>();

        /**
         * 构造器
         *
         * @since v5.1.0
         */
        private ArgArray() {
        }

        /**
         * 创建当前实例
         *
         * @return 当前实例
         * @since v5.1.0
         */
        public static ArgArray builder() {
            return new ArgArray();
        }

        /**
         * 原始信息
         *
         * @param originalMessages 原始信息
         * @return 当前实例
         * @since v5.1.0
         */
        @SafeVarargs
        public final ArgArray originalMessages(Pair<String, Object>... originalMessages) {
            if (ArrayUtils.isNotEmpty(originalMessages)) {
                this.originalMessages.addAll(Arrays.asList(originalMessages));
            }
            return this;
        }

        /**
         * 原始信息
         *
         * @param originalMessages 原始信息
         * @return 当前实例
         * @since v5.1.0
         */
        public final ArgArray originalMessages(List<Pair<String, Object>> originalMessages) {
            if (CollectionUtils.isNotEmpty(originalMessages)) {
                this.originalMessages.addAll(originalMessages);
            }
            return this;
        }

        /**
         * 附加信息
         *
         * @param additionalMessages 附加信息
         * @return 当前实例
         * @since v5.1.0
         */
        @SafeVarargs
        public final ArgArray additionalMessages(Pair<String, Object>... additionalMessages) {
            if (ArrayUtils.isNotEmpty(additionalMessages)) {
                this.additionalMessages.addAll(Arrays.asList(additionalMessages));
            }
            return this;
        }

        /**
         * 附加信息
         *
         * @param additionalMessages 附加信息
         * @return 当前实例
         * @since v5.1.0
         */
        public final ArgArray additionalMessages(List<Pair<String, Object>> additionalMessages) {
            if (CollectionUtils.isNotEmpty(additionalMessages)) {
                this.additionalMessages.addAll(additionalMessages);
            }
            return this;
        }

        /**
         * 解析参数
         *
         * @return 解析后的参数
         * @since v5.1.0
         */
        public List<Pair<String, Object>> parse() {
            List<Pair<String, Object>> result = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(originalMessages)) {
                result.addAll(originalMessages);
            }
            if (CollectionUtils.isNotEmpty(additionalMessages)) {
                result.addAll(additionalMessages);
            }
            return result;
        }
    }
}
