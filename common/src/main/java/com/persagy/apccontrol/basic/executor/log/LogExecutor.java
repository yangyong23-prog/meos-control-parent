package com.persagy.apccontrol.basic.executor.log;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 日志执行器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
public class LogExecutor {

    // ------------------------- 基础实例 -------------------------

    /**
     * 普通日志实例
     *
     * @return 普通日志实例
     * @since v5.1.0
     */
    public static LogProvider instance() {
        return new LogSimpleExecutor(null, null);
    }

    /**
     * 普通日志组件实例
     *
     * @param component 日志组件
     * @return 普通日志组件实例
     * @since 5.240501.0
     */
    public static LogComponentProvider instance(@NonNull LogComponent component) {
        return new LogComponentSimpleExecutor(component, null, null);
    }

    /**
     * 集团日志实例
     *
     * @param groupCode 集团编码
     * @return 集团日志实例
     * @since v5.1.0
     */
    public static LogProvider group(@Nullable String groupCode) {
        return new LogSimpleExecutor(groupCode, null);
    }

    /**
     * 集团日志组件实例
     *
     * @param component 日志组件
     * @param groupCode 集团编码
     * @return 集团日志组件实例
     * @since 5.240501.0
     */
    public static LogComponentProvider group(@NonNull LogComponent component, @Nullable String groupCode) {
        return new LogComponentSimpleExecutor(component, groupCode, null);
    }

    /**
     * 项目日志实例
     *
     * @return 项目日志实例
     * @since v6.0.0
     */
    public static LogProvider project() {
        return new LogSimpleExecutor(null, null);
    }

    /**
     * 项目日志实例
     *
     * @param projectId 项目ID
     * @return 项目日志实例
     * @since v6.0.0
     */
    public static LogProvider project(@NonNull String projectId) {
        return new LogSimpleExecutor(null, projectId);
    }

    /**
     * 项目日志实例
     *
     * @param projectId 项目ID
     * @return 项目日志实例
     * @since 5.240501.0
     */
    public static LogComponentProvider project(@NonNull LogComponent component, @NonNull String projectId) {
        return new LogComponentSimpleExecutor(component, null, projectId);
    }

    /**
     * 日志实例
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @return 日志实例
     * @since 5.240402.0
     */
    public static LogProvider instance(@Nullable String groupCode, @Nullable String projectId) {
        return new LogSimpleExecutor(groupCode, projectId);
    }

    /**
     * 日志组件实例
     *
     * @param component 日志组件
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @return 日志组件实例
     * @since 5.240501.0
     */
    public static LogComponentProvider instance(
            @NonNull LogComponent component,
            @Nullable String groupCode, @Nullable String projectId) {
        return new LogComponentSimpleExecutor(component, groupCode, projectId);
    }

    // ------------------------- RPC -------------------------

    /**
     * RPC日志执行器
     *
     * @param param    RPC参数
     * @param response RPC响应
     * @return RPC日志执行器
     * @since v5.1.0
     */
    public static LogRpcExecutor rpc(@Nullable Object param, @Nullable Object response) {
        return rpc(null, param, response);
    }

    /**
     * RPC日志执行器
     *
     * @param groupCode 集团编码
     * @param param     RPC参数
     * @param response  RPC响应
     * @return RPC日志执行器
     * @since v5.1.0
     */
    public static LogRpcExecutor rpc(@Nullable String groupCode, @Nullable Object param, @Nullable Object response) {
        return new LogRpcExecutor(groupCode, param, response);
    }

    /**
     * RPC日志执行器
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param param     RPC参数
     * @param response  RPC响应
     * @return RPC日志执行器
     * @since 5.240402.0
     */
    public static LogRpcExecutor rpc(
            @Nullable String groupCode, @Nullable String projectId,
            @Nullable Object param, @Nullable Object response) {
        return new LogRpcExecutor(groupCode, projectId, param, response);
    }

    // ------------------------- 项目 -------------------------

    /**
     * 项目日志执行器
     *
     * @param projectDTO 项目DTO
     * @return 项目日志执行器
     * @since v5.1.0
     */
    public static LogProvider project(@NonNull ProjectDTO projectDTO) {
        return new LogProjectExecutor(projectDTO);
    }

    /**
     * 项目日志执行器
     *
     * @param projectDTO 项目DTO
     * @return 项目日志执行器
     * @since 5.240501.0
     */
    public static LogComponentProvider project(@NonNull LogComponent component, @NonNull ProjectDTO projectDTO) {
        return new LogComponentProjectExecutor(component, projectDTO);
    }
}
