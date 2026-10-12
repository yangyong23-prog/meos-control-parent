package com.persagy.apccontrol.service;

import cn.hutool.http.Method;

import java.util.Map;

/**
 * 边缘端与saas端交互
 *
 * @author shaohongbo
 * @date 2023/3/25 01:07
 */
public interface ILocalToSaasService {

    /**
     * 请求云端 meoi-aim-server服务
     *
     * @param groupCode
     * @param projectId
     * @param method
     * @param uri
     * @param contentType
     * @param body
     * @param param GET 参数
     * @return
     */
    String meoiAimServerCommon(String groupCode, String projectId, Method method, String uri, String contentType, Object body, Map<String, Object> param);

    /**
     * 请求云端 dtp-rwd-server服务
     *
     * @param groupCode
     * @param projectId
     * @param method
     * @param uri
     * @param contentType
     * @param body
     * @param param GET 参数
     * @return
     */
    String dtpRwdServerCommon(String groupCode, String projectId, Method method, String uri, String contentType, Object body, Map<String, Object> param);

}
