package com.persagy.apccontrol.basic.outside.feign.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.StringUtils;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.outside.feign.client.MidPermissionClient;
import com.persagy.apccontrol.basic.outside.feign.client.SaasOrgPersonClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.service.SaasOrgPersonService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/10/8 13:58
 */
@Service
@Slf4j
public class SaasOrgPersonServiceImpl implements SaasOrgPersonService {

    @Autowired
    private SaasOrgPersonClient saasOrgPersonClient;
    @Autowired
    private MidPermissionClient midPermissionClient;

    @Override
    public Map<String, String> queryPersonNameByUserIds(String groupCode, String projectId, Set<String> personUserIds) {
        if (CollectionUtil.isEmpty(personUserIds)) {
            return new HashMap<>();
        }

        Map<String, String> result = new HashMap<>();
        JSONObject param = new JSONObject();
        param.put("projectId", projectId);
        for (String personUserId : personUserIds) {
            param.put("personUserId", personUserId);
            CustomerResponse<List<JSONObject>> personInfo = this.saasOrgPersonClient.queryPersonInfo(groupCode, param);
            log.info("saasOrgPersonClient.queryPersonInfo:{}", JSON.toJSONString(personInfo));
            if (personInfo == null || CollectionUtil.isEmpty(personInfo.getContent())) {
                continue;
            }
            String personName = personInfo.getContent().get(0).getString("name");
            result.put(personUserId, personName);
        }
        return result;
    }

    @Override
    public Map<String, String> queryPersonNameOrUserNameByUserIds(String groupCode, String projectId, Set<String> personUserIds) {

        Map<String, String> result = queryPersonNameByUserIds(groupCode, projectId, personUserIds);
        Set<String> userIds = personUserIds.stream().filter(person -> StringUtils.isEmpty(result.get(person))).collect(Collectors.toSet());
        if (CollectionUtil.isNotEmpty(userIds)) {
            //查询用户id对应的账号信息
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("userIds", Sets.newHashSet(userIds));
            try {
                CustomerResponse<List<JSONObject>> userInfo = midPermissionClient.getUserInfoListByUserIds(JSON.toJSONString(jsonObj));
                if (userInfo != null && "success".equals(userInfo.getResult()) && CollectionUtil.isNotEmpty(userInfo.getContent())) {
                    for (JSONObject user : userInfo.getContent()) {
                        result.put(user.getString("userId"), user.getString("userName"));
                    }
                }
            } catch (Exception e) {
                log.error("查询账号信息失败， e:{}", ExceptionUtils.getStackTrace(e));
            }

        }
        return result;
    }
}
