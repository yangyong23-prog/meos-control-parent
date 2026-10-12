package com.persagy.apccontrol.basic.resource.impl;

import com.persagy.apccontrol.basic.mapper.RcBasicsAsyncTaskMapper;
import com.persagy.apccontrol.basic.model.entity.RcBasicsAsyncTask;
import com.persagy.apccontrol.basic.resource.RcBasicsAsyncTaskService;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.database.service.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author yuyao
 * @description 针对表【rc_basics_async_task(基础任务表（配置定时任务）)】的数据库操作Service实现
 * @createDate 2023-03-15 20:30:22
 */
@Service
public class RcBasicsAsyncTaskServiceImpl extends SuperServiceImpl<RcBasicsAsyncTaskMapper, RcBasicsAsyncTask>
        implements RcBasicsAsyncTaskService {


    @Override
    public void syn(RcBasicsAsyncTask task) {
        //先删除后添加
        removeById(task.getId());
        //添加数据
        task.setSynTime(DateUtils.getDateNow());
        save(task);
    }
}




