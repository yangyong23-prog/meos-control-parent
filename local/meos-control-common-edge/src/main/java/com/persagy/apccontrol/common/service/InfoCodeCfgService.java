package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;

/**
 * @author shaohongbo
 * @date 2023/4/4 19:11
 */
public interface InfoCodeCfgService {

    /**通过classCode及信息点查询信息点相关配置
     * @param classCode
     * @param infoCode
     * @return com.persagy.apccontrol.model.entity.RcInfoCodeCfg
     * @author shaohongbo
     * @date 2023/4/4 19:26
     */
    public RcInfoCodeCfg queryOneByInfCode(String classCode, String infoCode);
}
