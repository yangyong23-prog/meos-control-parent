package com.persagy.apccontrol.basic.outside.feign.wrapper;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.outside.feign.client.ExceptionEventClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.BasePageParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerInstanceTreeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerInstanceTreeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExceptionEventClientPageWrapper implements LogComponent {

    private final ExceptionEventClient ExceptionEventClient;


    public CustomerResponse<List<DangerInstanceTreeResponse>> listDangerInstanceTree(DangerInstanceTreeQueryParam body) {
        BasePageParam<String> basePageParam = new BasePageParam<>();
        basePageParam.setObjects(body.getRwdInstanceIds());
        return FeignParamPageExecutor.execute(basePageParam, 1000, pageQuery ->
                ExceptionEventClient.listDangerInstanceTree(DangerInstanceTreeQueryParam.of(Lists.newArrayList(pageQuery.getObjects()), body.getFollowMark())));
    }


    @Override
    public String logTag() {
        return "风险事件";
    }
}
