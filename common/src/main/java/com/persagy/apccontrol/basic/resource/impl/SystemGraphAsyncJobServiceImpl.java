package com.persagy.apccontrol.basic.resource.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.persagy.apccontrol.basic.mapper.SystemGraphAsyncJobMapper;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphJobParam;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.GraphAsyncGenerateDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphAsyncJob;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumJobStatus;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRelationalGraphClientService;
import com.persagy.apccontrol.basic.resource.SystemGraphAsyncJobService;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphAsyncJobServiceImpl extends ServiceImpl<SystemGraphAsyncJobMapper, SystemGraphAsyncJob>
        implements SystemGraphAsyncJobService {

    private final DtpRelationalGraphClientService dtpRelationalGraphClientService;


    @Override
    public SystemGraphAsyncJob getByJobId(@NonNull String jobId) {
        LambdaQueryWrapper<SystemGraphAsyncJob> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphAsyncJob::getJobId, jobId);
        return getOne(qw);
    }


    @Override
    public SystemGraphAsyncJob getBySystemObjectIdAndGraphTypeCodeAndTemplateOriId(@NonNull String systemObjectId, String graphTypeCode) {
        LambdaQueryWrapper<SystemGraphAsyncJob> qw = new LambdaQueryWrapper<>();
        qw.eq(SystemGraphAsyncJob::getSystemObjectId, systemObjectId);
        qw.eq(StringUtils.isNotBlank(graphTypeCode), SystemGraphAsyncJob::getGraphTypeCode, graphTypeCode);
        List<SystemGraphAsyncJob> list = this.list(qw);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public Boolean updateByJobId(@NonNull String jobId, @Nullable String message, @Nullable EnumJobStatus status) {
        LambdaUpdateWrapper<SystemGraphAsyncJob> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SystemGraphAsyncJob::getJobStatus, status)
                .set(SystemGraphAsyncJob::getJobMessage, message)
                .eq(SystemGraphAsyncJob::getJobId, jobId);
        return update(updateWrapper);
    }

    @Override
    public SystemGraphAsyncJob checkJob(String graphTypeCode, String systemObjectId) {
        SystemGraphAsyncJob job = getBySystemObjectIdAndGraphTypeCodeAndTemplateOriId(systemObjectId, graphTypeCode);
        if (Objects.isNull(job)) {
            return null;
        }
        EnumJobStatus jobStatus = job.getJobStatus();
        GraphAsyncGenerateDTO graphAsyncGenerateDTO = dtpRelationalGraphClientService.asyncJobStatus(new GraphJobParam(job.getJobId()));
        if (EnumJobStatus.RUNNING == jobStatus) {
            //防止消息通知不及时，直接查询接口获取结果
            if (graphAsyncGenerateDTO.isResultFinish()) {
                if (graphAsyncGenerateDTO.isResultSuccess()) {
                    updateByJobId(job.getJobId(), graphAsyncGenerateDTO.getResultMessage(), EnumJobStatus.SUCCESS);
                    return job;
                } else {
                    updateByJobId(job.getJobId(), graphAsyncGenerateDTO.getResultMessage(), EnumJobStatus.FAIL);
                    throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), graphAsyncGenerateDTO.getResultMessage(),
                            LocaleMessageUtil.getMessage("key_eb8bc5c7cfe84879b0d6d4486d73dc79"));
                }
            } else {
                throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), graphAsyncGenerateDTO.getResultMessage(),
                        LocaleMessageUtil.getMessage("key_f7a531a2a8a24c60bf07e31e3ac42ddf"));
            }
        }
        if (EnumJobStatus.FAIL == jobStatus) {
            //防止失败后无法重新初始化
            updateByJobId(job.getJobId(), graphAsyncGenerateDTO.getResultMessage(), null);
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), graphAsyncGenerateDTO.getResultMessage(),
                    LocaleMessageUtil.getMessage("key_eb8bc5c7cfe84879b0d6d4486d73dc79"));
        }
        return null;
    }

    @Override
    public String logTag() {
        return "系统图异步任务";
    }
}
