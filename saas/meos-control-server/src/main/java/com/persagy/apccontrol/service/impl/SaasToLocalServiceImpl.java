package com.persagy.apccontrol.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.base.BusinessErrorCodeConstants;
import com.persagy.apccontrol.basic.model.vo.LocalityProjectCfgVO;
import com.persagy.apccontrol.basic.utils.AesUtil;
import com.persagy.apccontrol.basic.utils.JwtUtil;
import com.persagy.apccontrol.common.outside.MidPermissionService;
import com.persagy.apccontrol.service.ISaasToLocalService;
import com.persagy.framework.common.exception.util.BusinessExceptionUtil;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author shaohongbo
 * @date 2023年03月27日 22:24
 */
@Service
@Slf4j
public class SaasToLocalServiceImpl implements ISaasToLocalService {

    private static final String LOCAL_PROJECT_INFO_KEY = "RC_LOCAL_PROJECT_INFO_KEY:";

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private MidPermissionService midPermissionService;

    @Override
    public LocalityProjectCfgVO getLocalBaseUrl(String projectId) {
        Object vo = redisCacheUtil.get(LOCAL_PROJECT_INFO_KEY + projectId);
        if (vo != null) {
            return BeanUtil.toBean(vo, LocalityProjectCfgVO.class);
        }
        return null;
    }

    @Override
    public String handleHeart(String heartPackage) throws Exception {
        LocalityProjectCfgVO parse = JSONObject.parseObject(AesUtil.decrypt(heartPackage), LocalityProjectCfgVO.class);
        redisCacheUtil.setEx(LOCAL_PROJECT_INFO_KEY + parse.getProjectId(), parse, 600L);
        return JwtUtil.getToken(parse.getProjectId());
    }


    @Override
    public void synUser(String groupCode, String projectId) {
        try {
            midPermissionService.synUser(groupCode, projectId);
        } catch (Exception e) {
            log.error("同步账号失败:{}", e.getMessage());
            throw BusinessExceptionUtil.exception(BusinessErrorCodeConstants.BUSINESS_ERROR_K0003);
        }
    }


}
