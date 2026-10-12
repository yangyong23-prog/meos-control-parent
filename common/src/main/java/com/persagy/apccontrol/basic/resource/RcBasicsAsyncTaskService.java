package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.model.entity.RcBasicsAsyncTask;
import com.persagy.framework.database.service.SuperService;

/**
 * @author yuyao
 * @description 针对表【rc_basics_async_task(基础任务表（配置定时任务）)】的数据库操作Service
 * @createDate 2023-03-15 20:30:22
 */
public interface RcBasicsAsyncTaskService extends SuperService<RcBasicsAsyncTask> {


    /**
     * 同步方法
     *
     * @param task
     */
    public void syn(RcBasicsAsyncTask task);

}
