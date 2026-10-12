package com.persagy.apccontrol.task;

import com.persagy.apccontrol.basic.model.dto.ProjectDTO;

import java.util.concurrent.Future;

/**
 * @author shaohongbo
 * @date 2022/8/15 11:27
 * XXjob 定时任务 入口
 */
public interface TecXxJobHandler {

    /**
     * 获取任务执行名称
     *
     * @return 任务名称
     */
    public String getHandlerName();

    /**
     * 执行方法
     *
     * @param groupCode  groupCode
     * @param projectDTO projectDTO
     * @author yuyao
     * @date 2022/8/15 11:24
     * @Return java.util.concurrent.Future<java.lang.String>
     */
    Future<String> processAsync(String groupCode, ProjectDTO projectDTO);

    /**
     * 执行方法
     *
     * @param groupCode  groupCode
     * @param projectDTO projectDTO
     * @author yuyao
     * @date 2022/8/15 11:24
     * @Return java.util.concurrent.Future<java.lang.String>
     */
    void process(String groupCode, ProjectDTO projectDTO);

}
