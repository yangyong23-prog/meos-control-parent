package com.persagy.apccontrol.common.outside;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.common.outside.dto.GroupDTO;

import java.util.List;

/**
 * @author yuyao
 * @date 2022/7/14 15:53
 * 运维平台数据查询
 */
public interface MidPermissionService {

    List<GroupDTO> queryValidGroupList();

    /**
     * 查询项目信息不是很稳定
     *
     * @param groupCode
     * @param projectIdList
     * @author yuyao
     * @date 2022/4/16 16:07
     * @Return java.util.List<com.persagy.apccontrol.service.dto.ProjectDTO>
     */
    List<ProjectDTO> queryIsInUseProjectDTOList(String groupCode, List<String> projectIdList);

    List<ProjectDTO> queryIsInUseProjectDTOList(String groupCode);

    ProjectDTO queryIsInUseProjectDTOByProjectId(String groupCode, String projectId);

    /**
     * 查询项目信息
     * 缓存1分钟
     *
     * @param projectId projectId
     * @author yuyao
     * @date 2022/7/14 15:53
     * @Return com.persagy.apccontrol.model.dto.ProjectDTO
     */
    ProjectDTO queryProjectDTOByProjectId(String projectId);

    /**
     * 缓存 30分钟
     *
     * @param projectId projectId
     * @return ProjectDTO
     */
    ProjectDTO queryProjectDTOByCache(String projectId);

    void synUser(String groupCode, String projectId) throws Exception;
}
