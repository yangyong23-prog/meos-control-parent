package com.persagy.apccontrol.common.resource.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.outside.feign.service.MidPermissionClientService;
import com.persagy.apccontrol.common.mapper.RcUserMapper;
import com.persagy.apccontrol.common.model.entity.RcUser;
import com.persagy.apccontrol.common.resource.RcUserService;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * @description 针对表【rc_user(本地用户信息表)】的数据库操作Service实现
 * @createDate 2023-03-15 20:30:22
 */
@Service
public class RcUserServiceImpl extends SuperServiceImpl<RcUserMapper, RcUser>
        implements RcUserService {

    @Autowired
    private MidPermissionClientService midPermissionClientService;

    @Override
    public RcUser queryUserByUserName(String projectId, String userName) {
        LambdaQueryWrapper<RcUser> queryWrapper = new LambdaQueryWrapper<RcUser>();
        queryWrapper.eq(RcUser::getProjectId, projectId);
        queryWrapper.eq(RcUser::getUserName, userName);
        queryWrapper.last(CommonConst.LIMIT_ONE);
        return this.getOne(queryWrapper);
    }


    @Override
    public Map<String, String> getUserNameByUserIds(String groupCode, Set<String> userIds) {
        if (CollectionUtil.isEmpty(userIds)) {
            return new HashMap<>();
        }
        Map<String, String> userNameByUserIds = this.midPermissionClientService.getUserNameByUserIds(groupCode, userIds);
        return userNameByUserIds;
    }
}




