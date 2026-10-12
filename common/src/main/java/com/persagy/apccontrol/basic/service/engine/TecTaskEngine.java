package com.persagy.apccontrol.basic.service.engine;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsAsyncTask;

/**
 * @Author : yuyao
 * @Date : 2022/4/13 15:56
 * @Description ：定时任务执行引擎
 */
public interface TecTaskEngine {


    /**
     * 任务执行
     *
     * @param basicsAsyncTask 任务
     * @author yuyao
     * @date 2022/4/13 15:59
     * @Return void
     */
    void execute(RcBasicsAsyncTask basicsAsyncTask, ProjectDTO projectDTO);


}
