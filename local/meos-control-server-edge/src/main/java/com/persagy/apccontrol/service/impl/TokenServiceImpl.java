package com.persagy.apccontrol.service.impl;

import com.persagy.apccontrol.basic.outside.feign.client.MidPermissionClient;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.utils.JwtUtil;
import com.persagy.apccontrol.config.TokenProperties;
import com.persagy.apccontrol.service.ITokenService;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author shaohongbo
 * @date 2023年03月27日 16:29
 */
@Service
@Slf4j
public class TokenServiceImpl implements ITokenService {

    private static final String USER_TOKEN_KEY = "RC-USER-TOKEN-KEY";

    private static final String SPLIT = ":";


    @Resource
    private RedisCacheUtil redisCacheUtil;
    @Resource
    private MidPermissionClient midPermissionClient;
    @Resource
    private TokenProperties tokenProperties;


    @Override
    public String createToken(String userName) {
        String uerTokenKey = createUerTokenKey(userName);
        Object o = redisCacheUtil.get(uerTokenKey);
        String token = null;
        if (o == null) {
            //第一次登录，需要生成token
            token = JwtUtil.getToken(userName);
        } else {
            token = (String) o;
        }
        redisCacheUtil.setEx(createUerTokenKey(userName), token, tokenProperties.getTokenExpiration());
        return token;
    }


    private String createUerTokenKey(String userName) {
        return USER_TOKEN_KEY + SPLIT + userName;
    }


    @Override
    public Boolean verifyToken(String token, String loginType) {
        //走运维平台统一校验 取消服务级别的校验
        return true;
        /*
        if (BooleanUtils.isNotTrue(tokenProperties.getEnabled())) {
            return true;
        }
        if (StringUtils.isBlank(loginType)) {
            throw BusinessExceptionUtil.exception(BusinessErrorCodeConstants.BUSINESS_ERROR_K0001);
        }

        if (SystemEnum.SAAS.getCode().equals(loginType)) {
            //return saasVerifyToken(token);
            return true;
        }
        if (StringUtils.isBlank(token)) {
            throw BusinessExceptionUtil.exception(BusinessErrorCodeConstants.BUSINESS_ERROR_K0002);
        }
        //边缘端本地校验
        return localVerifyToken(token);*/
    }

    private Boolean saasVerifyToken(String token) {
        try {
            CustomerResponse.handleResult(midPermissionClient.verifyToken(token));
        } catch (Exception e) {
            log.error("云端token服务校验token失败:{}", e.getMessage());
            return false;
        }
        return true;
    }

    private Boolean localVerifyToken(String token) {
        Claims claimsBody = JwtUtil.getClaimsBody(token);
        String id = (String) claimsBody.get("id");
        String uerTokenKey = createUerTokenKey(id);
        Object o = redisCacheUtil.get(uerTokenKey);
        if (o == null || !o.equals(token)) {
            return false;
        }
        redisCacheUtil.setEx(uerTokenKey, token, tokenProperties.getTokenExpiration());
        return true;

    }
}
