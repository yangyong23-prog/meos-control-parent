package com.persagy.apccontrol.basic.resource;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphAsyncJob;
import com.baomidou.mybatisplus.extension.service.IService;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumJobStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public interface SystemGraphAsyncJobService extends IService<SystemGraphAsyncJob>, LogComponent {


    /**
     * 按jobid获取
     *
     * @param jobId 作业ID
     * @return {@link SystemGraphAsyncJob }
     */
    SystemGraphAsyncJob getByJobId(@NonNull String jobId);

    /**
     * 通过系统对象id、图形类型代码和模板oriid获取
     *
     * @param systemObjectId 基准对象id
     * @param graphTypeCode  图类型
     * @return {@link SystemGraphAsyncJob }
     */
    @Nullable
    SystemGraphAsyncJob getBySystemObjectIdAndGraphTypeCodeAndTemplateOriId(@NonNull String systemObjectId, String graphTypeCode);

    /**
     * 按工作id更新
     *
     * @param jobId  作业ID
     * @param status 状态
     * @return {@link Boolean }
     */
    Boolean updateByJobId(@NonNull String jobId,@Nullable String message, @NonNull EnumJobStatus status);

    SystemGraphAsyncJob checkJob(String graphTypeCode, String systemObjectId);
}
