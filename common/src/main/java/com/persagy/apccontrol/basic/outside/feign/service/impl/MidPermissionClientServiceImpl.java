package com.persagy.apccontrol.basic.outside.feign.service.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.convert.MidPermissionConvert;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.dto.GroupDTO;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.opt.OptOperatorNames;
import com.persagy.apccontrol.basic.opt.OptProjectInfo;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.basic.outside.feign.client.MidPermissionClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.GroupQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.LoginUserParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.ProjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.midpermission.ProjectValidQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.midpermission.ProjectResponse;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import io.jsonwebtoken.lang.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 运维平台客户端服务实现
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Service
@Slf4j
public class MidPermissionClientServiceImpl implements MidPermissionClientService {

    private static final String LOG_TAG = "运维平台服务";

    @Autowired
    private MidPermissionConvert midPermissionConvert;
    @Autowired
    private MidPermissionClient midPermissionClient;
    @Autowired
    private OptProjectInfo optProjectInfo;
    @Autowired
    private OptOperatorNames optOperatorNames;

    @Override
    public Map<String, String> getUserNameByUserIds(String groupCode, Set<String> userIds) {
        Map<String, String> result = new HashMap<>();
        if (CollectionUtil.isEmpty(userIds)) {
            return result;
        }
        if (OptSwitch.on(OptSwitch.OPERATOR_NAME_LOCAL)) {
            // B15：只用前端随请求带来的姓名，取不到的 userId 不返回（调用方保持写 userId）
            return optOperatorNames.lookupAll(userIds);
        }
        try {
            //查询用户id对应的账号信息
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("userIds", Sets.newHashSet(userIds));
            CustomerResponse<List<JSONObject>> userInfo = midPermissionClient.getUserInfoListByUserIds(JSON.toJSONString(jsonObj));
            if (userInfo != null && "success".equals(userInfo.getResult()) && CollectionUtil.isNotEmpty(userInfo.getContent())) {
                List<JSONObject> content = userInfo.getContent();
                for (JSONObject object : content) {
                    result.put(object.getString("userId"), object.getString("userName"));
                }
            }
        } catch (Exception e) {
            log.error("查询账号信息失败， e:{}", ExceptionUtils.getStackTrace(e));
        }
        return result;
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.SAAS_GROUP_VALID, condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<GroupDTO> listGroupOfValid(boolean queryCache, boolean isThrows) {
        GroupQueryParam groupQueryParam = new GroupQueryParam();
        groupQueryParam.setStatus(1);
        return query(isThrows, null, "null", LocaleMessageUtil.getMessage("key_c8ff811cb5234f69b587f5dd1fa73141"), groupQueryParam,
                () -> midPermissionClient.listGroup(groupQueryParam), ArrayList::new).stream()
                .map(midPermissionConvert::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO getProject() {
        String groupCode = ContextUtil.getGroupCode();
        String projectId = ContextUtil.getProjectId();
        if (OptSwitch.on(OptSwitch.PROJECT_FROM_RWD)) {
            ProjectDTO project = optProjectInfo.get(groupCode, projectId, id -> remoteGetProject(groupCode, id));
            if (project == null) {
                throw new IllegalStateException("project not found: " + projectId);
            }
            return project;
        }
        return remoteGetProject(groupCode, projectId);
    }

    private ProjectDTO remoteGetProject(String groupCode, String projectId) {
        ProjectQueryParam projectQueryParam = new ProjectQueryParam();
        projectQueryParam.setGroupCode(groupCode);
        projectQueryParam.setProjectId(projectId);
        List<ProjectResponse> response = query(true, groupCode, projectId, LocaleMessageUtil.getMessage("key_c8ff811cb5234f69b587f5dd1fa73141"), projectQueryParam,
                () -> midPermissionClient.getProject(groupCode, projectQueryParam), null);
        return midPermissionConvert.toDto(response.get(0), groupCode);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.SAAS_PROJECT, key = "#projectId", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public ProjectDTO getProject(boolean queryCache, boolean isThrows, @NonNull String projectId) {
        String groupCode = getGroupCode();
        if (OptSwitch.on(OptSwitch.PROJECT_FROM_RWD)) {
            return optProjectInfo.get(groupCode, projectId, id -> remoteGetProject(isThrows, groupCode, id));
        }
        return remoteGetProject(isThrows, groupCode, projectId);
    }

    private ProjectDTO remoteGetProject(boolean isThrows, String groupCode, String projectId) {
        ProjectQueryParam projectQueryParam = new ProjectQueryParam();
        projectQueryParam.setGroupCode(groupCode);
        projectQueryParam.setProjectId(projectId);
        List<ProjectResponse> response = query(isThrows, groupCode, projectId, LocaleMessageUtil.getMessage("key_c8ff811cb5234f69b587f5dd1fa73141"), projectQueryParam,
                () -> midPermissionClient.getProject(groupCode, projectQueryParam), null);
        if (CollectionUtils.isEmpty(response)) {
            return null;
        }
        return midPermissionConvert.toDto(response.get(0), groupCode);
    }

    @Override
    @Cacheable(cacheNames = CacheKey.Dynamic.SAAS_PROJECT_VALID, key = "#groupCode + #projectIds", condition = "#queryCache",
            unless = "#result == null || #result.isEmpty()")
    public List<ProjectDTO> listProjectOfValid(
            boolean queryCache, boolean isThrows,
            @Nullable String groupCode,
            @Nullable Collection<String> projectIds) {
        String tempGroupCode = StringUtils.defaultString(groupCode, getGroupCode());

        ProjectValidQueryParam projectValidQueryParam = new ProjectValidQueryParam();
        projectValidQueryParam.setGroupCode(tempGroupCode);
        projectValidQueryParam.setProjectIds(projectIds);
        return query(isThrows, tempGroupCode, Objects.nullSafeToString(projectIds), LocaleMessageUtil.getMessage("key_c8ff811cb5234f69b587f5dd1fa73141"), projectValidQueryParam,
                () -> midPermissionClient.listProjectOfValid(tempGroupCode, projectValidQueryParam),
                ArrayList::new).stream()
                .map(item -> midPermissionConvert.toDto(item, tempGroupCode))
                .collect(Collectors.toList());
    }

    @Override
    public String getTokenByLoginUser(String groupCode, String loginName, String password) {
        LoginUserParam loginUserParam = new LoginUserParam(loginName, password);
        CustomerResponse<List<JSONObject>> customerResponse = this.midPermissionClient.loginUserServiceForEncrypt(groupCode, JsonUtils.toJson(loginUserParam));
        List<JSONObject> objectList = customerResponse == null || CollectionUtil.isEmpty(customerResponse.getContent()) ? null : customerResponse.getContent();
        return objectList.get(0) == null ? null : objectList.get(0).getString("token");
    }

    @Override
    public Logger log() {
        return log;
    }

    @Override
    public String logTag() {
        return LOG_TAG;
    }
}
