package com.persagy.apccontrol.common.config;

/**
 * 系统初始化
 *
 * @author shaohongbo
 * @date 2023/3/29 19:23
 */
public interface SystemInitService {

    public void init() throws Exception;

    public String getName();
}
