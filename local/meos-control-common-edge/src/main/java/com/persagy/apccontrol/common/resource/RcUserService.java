package com.persagy.apccontrol.common.resource;

import com.persagy.apccontrol.common.model.entity.RcUser;
import com.persagy.framework.database.service.SuperService;

import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【rc_user(本地用户信息表)】的数据库操作Service
 * @createDate 2023-03-15 20:30:22
 */
public interface RcUserService extends SuperService<RcUser> {

    /**
     * 通过用户名查询账号信息
     *
     * @param userName
     * @return com.persagy.apccontrol.model.entity.RcUser
     * @author shaohongbo
     * @date 2023/3/27 16:50
     */
    public RcUser queryUserByUserName(String projectId, String userName);

    /**
     * 批量查询用户信息
     *
     * @param userIds
     * @return
     */
    Map<String, String> getUserNameByUserIds(String groupCode, Set<String> userIds);
}
