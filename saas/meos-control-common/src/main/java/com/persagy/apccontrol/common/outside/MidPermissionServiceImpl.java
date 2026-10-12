package com.persagy.apccontrol.common.outside;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.CacheKey;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.outside.feign.client.MidPermissionClient;
import com.persagy.apccontrol.basic.utils.ParameterCheck;
import com.persagy.apccontrol.common.outside.dto.GroupDTO;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 运维平台查询服务
 *
 * @author strootman
 * @version 1.0
 * @date 2018年8月17日
 */
@Slf4j
@Service
public class MidPermissionServiceImpl implements MidPermissionService {

    @Resource
    MidPermissionClient midPermissionClient;

    @Cacheable(cacheNames = "queryValidGroupList", key = "'-60m'", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER, sync = true)
    @Override
    public List<GroupDTO> queryValidGroupList() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", 1);
        JSONObject result = JSONObject.parseObject(midPermissionClient.queryGroupList(jsonObject));
        if ("failure".equals(result.getString("Result"))) {
            throw new BusinessException(ErrorCodeConstants.SERVICE_ERROR_C0001.getCode(), LocaleMessageUtil.getMessage("key_bc4890a42c2d4ece9ec4d7449cfc5dea"));
        }
        JSONArray json = result.getJSONArray("Content");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        return JSONObject.parseArray(json.toJSONString(), GroupDTO.class);
    }

    @Override
    public List<ProjectDTO> queryIsInUseProjectDTOList(String groupCode, List<String> projectIdList) {
        JSONObject param = new JSONObject();
        JSONObject puser = new JSONObject();
        puser.put("userId", CommonConst.USER_ID);
        puser.put("loginDevice", CommonConst.LOGINDEVICE);
        param.put("puser", puser);
        if (!CollectionUtils.isEmpty(projectIdList)) {
            param.put("projectIds", projectIdList);
        }
        param.put(CommonConst.GROUP_CODE, groupCode);
        String resultStr = midPermissionClient.queryInUseProjectsByIds(param.toJSONString());
        JSONObject parseObject = JSONObject.parseObject(resultStr);
        if ("failure".equals(parseObject.getString("result"))) {
            throw new BusinessException(ErrorCodeConstants.SERVICE_ERROR_C0001.getCode(), LocaleMessageUtil.getMessage("key_486b01af40f5450b9aaf095a0a420cdd") + parseObject.getString("reson"));
        }
        List<ProjectDTO> result = new ArrayList<>();
        JSONArray jsonArray = parseObject.getJSONArray("content");
        if (jsonArray == null || jsonArray.isEmpty()) {
            return result;
        }

        for (Object object : jsonArray) {
            JSONObject obj = (JSONObject) object;
            ProjectDTO project = new ProjectDTO();
            project.setProjectId(obj.getString(CommonConst.PROJECT_ID));
            project.setProjectLocalId(obj.getString("projectLocalID"));
            project.setProjectLocalName(obj.getString("projectLocalName"));
            project.setCityCode(obj.getString("city"));
            result.add(project);
        }
        return result;
    }

    //@Cacheable(cacheNames = "queryIsInUseProjectDTOList", key = "#groupCode + '-60m'", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER)
    @Override
    public List<ProjectDTO> queryIsInUseProjectDTOList(String groupCode) {
        return queryIsInUseProjectDTOList(groupCode, null);
    }

    @Override
    public ProjectDTO queryIsInUseProjectDTOByProjectId(String groupCode, String projectId) {
        List<ProjectDTO> projectDTOS = queryIsInUseProjectDTOList(groupCode, Collections.singletonList(projectId));
        if (CollectionUtils.isEmpty(projectDTOS)) {
            return null;
        }

        return projectDTOS.get(0);
    }

    @Cacheable(cacheNames = "queryProjectDTOByProjectId", key = "#projectId+'-1m'", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER, sync = true)
    @Override
    public ProjectDTO queryProjectDTOByProjectId(String projectId) {

        ProjectDTO projectDTO;
        try {
            projectDTO = queryIsInUseProjectDTOByProjectId(ContextUtil.getGroupCode(), projectId);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeConstants.SERVICE_ERROR_C0001.getCode(), LocaleMessageUtil.getMessage("key_82732436e5e14f129c5549c324cde8c0"));
        }
        ParameterCheck.isTrue(projectDTO == null, LocaleMessageUtil.getMessage("key_7d4088a7ef4b458298be808cb2360d00") + projectId);
        return projectDTO;
    }

    @Override
    @Cacheable(cacheNames = "queryProjectDTOByCache", key = "#projectId+'-30m'", cacheManager = CacheKey.CAFFEINE_CACHE_MANAGER, sync = true)
    public ProjectDTO queryProjectDTOByCache(String projectId) {
        return queryProjectDTOByProjectId(projectId);
    }

    @Override
    public void synUser(String groupCode, String projectId) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(CommonConst.GROUP_CODE, groupCode);
        jsonObject.put(CommonConst.PROJECT_ID, projectId);
        JSONObject result = JSONObject.parseObject(midPermissionClient.synUserByProjectId(jsonObject));
        if ("failure".equals(result.getString("Result"))) {
            throw new Exception(LocaleMessageUtil.getMessage("key_e66052b2f60d46c1b3a9ca4a3a98bccf") + result.get("ResultMsg"));
        }
    }

}
