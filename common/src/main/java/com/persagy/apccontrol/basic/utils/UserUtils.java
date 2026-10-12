package com.persagy.apccontrol.basic.utils;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.outside.feign.service.SaasOrgPersonService;
import com.persagy.framework.common.context.ContextUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author : yuyao
 * @Date : 2022/3/28 11:01 PM
 * @Description ：
 */
@Component
public class UserUtils {

    @Autowired
    private SaasOrgPersonService saasOrgPersonService;

    public static String getUserId() {
        if (StringUtils.isBlank(ContextUtil.getUserId())) {
            return CommonConst.SYSTEM;
        }
        return ContextUtil.getUserId();
    }

    public static String getUserName() {
        if (StringUtils.isBlank(ContextUtil.getUserName())) {
            return CommonConst.SYSTEM_NAME;
        }
        return UrlUtil.getURLDecoderString(ContextUtil.getUserName());
    }

    public String getPersonUserName(String projectId) {
        if (StringUtils.isBlank(ContextUtil.getUserId())) {
            return CommonConst.SYSTEM_NAME;
        }
        Map<String, String> userInfos = saasOrgPersonService.queryPersonNameByUserIds(ContextUtil.getGroupCode(), projectId, Sets.newHashSet(ContextUtil.getUserId()));
        if (userInfos != null && userInfos.get(ContextUtil.getUserId())!= null) {
            return UrlUtil.getURLDecoderString(userInfos.get(ContextUtil.getUserId()));
        }
        return UrlUtil.getURLDecoderString(ContextUtil.getUserName());
    }
}
