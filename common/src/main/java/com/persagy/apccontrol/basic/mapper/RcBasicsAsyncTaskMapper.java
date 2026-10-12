package com.persagy.apccontrol.basic.mapper;

import com.persagy.apccontrol.basic.model.entity.RcBasicsAsyncTask;
import com.persagy.framework.database.mapper.SuperMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yuyao
 * @description 针对表【rc_basics_async_task(基础任务表（配置定时任务）)】的数据库操作Mapper
 * @createDate 2023-03-15 20:30:22
 * @Entity generator.domain.RcBasicsAsyncTask
 */
@Mapper
@Repository
public interface RcBasicsAsyncTaskMapper extends SuperMapper<RcBasicsAsyncTask> {


    /**
     * 获取配置列表 获取存在项目ID的配置和通配的配置
     *
     * @param projectId
     * @return
     */
    public List<RcBasicsAsyncTask> queryProjectTaskList(@Param("projectId") String projectId, @Param("sys") String sys);

}




