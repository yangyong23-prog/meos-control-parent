package com.persagy.apccontrol.service;

/**
 * token服务
 *
 * @author shaohongbo
 * @date 2023/3/25 01:07
 */
public interface ITokenService {


    String createToken(String userName);


    Boolean verifyToken(String token, String loginType);
}
