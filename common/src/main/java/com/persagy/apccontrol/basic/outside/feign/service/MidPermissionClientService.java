package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.model.dto.GroupDTO;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 运维平台客户端服务
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
public interface MidPermissionClientService extends BaseFeignClientService {

    /**
     * 根据账号ID，获取账号登录名
     *
     * @param groupCode
     * @param userIds
     * @return
     */
    Map<String, String> getUserNameByUserIds(String groupCode, Set<String> userIds);

    /**
     * 查询有效的集团列表
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @return 有效的集团列表
     * @since v6.0.0
     */
    List<GroupDTO> listGroupOfValid(boolean queryCache, boolean isThrows);

    /**
     * 查询项目
     *
     * @return 项目
     * @since 5.241102.0
     */
    ProjectDTO getProject();

    /**
     * 查询项目
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @return 项目
     * @since v5.7.0
     */
    ProjectDTO getProject(boolean queryCache, boolean isThrows, @NonNull String projectId);

    /**
     * 查询有效项目
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param groupCode  集团编码, 为空时使用当前集团
     * @param projectIds 项目ID, 为空时查询所有项目
     * @return 项目
     * @since v6.0.0
     */
    List<ProjectDTO> listProjectOfValid(
            boolean queryCache, boolean isThrows,
            @Nullable String groupCode,
            @Nullable Collection<String> projectIds);

    /**
     * 根据当前登录着，获取云端的登录token
     *
     * @param groupCode
     * @param loginName
     * @param password
     * @return
     */
    String getTokenByLoginUser(String groupCode, String loginName, String password);

}
