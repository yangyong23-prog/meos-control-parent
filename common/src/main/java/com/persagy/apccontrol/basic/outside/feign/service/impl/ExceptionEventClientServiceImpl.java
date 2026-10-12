package com.persagy.apccontrol.basic.outside.feign.service.impl;

import com.persagy.apccontrol.basic.outside.feign.client.DangerEventClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerInstanceTreeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerEventInstance;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerInstanceTreeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.ExceptionEventClientService;
import com.persagy.apccontrol.basic.outside.feign.wrapper.DangerEventClientPageWrapper;
import com.persagy.apccontrol.basic.outside.feign.wrapper.ExceptionEventClientPageWrapper;
import com.persagy.framework.common.context.ContextUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wxf
 * @description
 * @date 17:26 2025/2/17
 **/
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExceptionEventClientServiceImpl implements ExceptionEventClientService {

    private static final String LOG_TAG = "风险事件服务";
    private final DangerEventClientPageWrapper dangerEventClientPageWrapper;
    private final ExceptionEventClientPageWrapper exceptionEventClientPageWrapper;
    private final DangerEventClient dangerEventClient;

    @Override
    public List<DangerEventInstance> listDangerEvent(@NonNull DangerEventQueryParam param) throws FeignException {

        return query(false, ContextUtil.getGroupCode(), ContextUtil.getProjectId(), "", param,
                () -> dangerEventClientPageWrapper.listDangerEvents(param), ArrayList::new);
    }

    @Override
    public List<DangerEventInstance> listDangerEventTest(@NonNull DangerEventQueryParam param) throws FeignException {

        return query(false, ContextUtil.getGroupCode(), ContextUtil.getProjectId(), "", param,
                () -> dangerEventClient.listDangerEvents(param), ArrayList::new);
    }

    @Override
    public List<DangerInstanceTreeResponse> listDangerInstanceTree(DangerInstanceTreeQueryParam param) {
        return query(false, "查询风险事件实例树", param,
                () -> exceptionEventClientPageWrapper.listDangerInstanceTree(param), ArrayList::new);
    }


    @Override
    public String logTag() {
        return LOG_TAG;
    }
}