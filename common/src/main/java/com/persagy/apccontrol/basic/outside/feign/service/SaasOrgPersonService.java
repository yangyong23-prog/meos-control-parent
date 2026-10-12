package com.persagy.apccontrol.basic.outside.feign.service;

import java.util.Map;
import java.util.Set;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/10/8 13:57
 */
public interface SaasOrgPersonService {

    Map<String, String> queryPersonNameByUserIds(String groupCode, String projectId, Set<String> personUserId);

    /**
     * 先查询人员信息，没有人员信息则查询账号信息
     */
    Map<String, String> queryPersonNameOrUserNameByUserIds(String groupCode, String projectId, Set<String> personUserIds);
}
