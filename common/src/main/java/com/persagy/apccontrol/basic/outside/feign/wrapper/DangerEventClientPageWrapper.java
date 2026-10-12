package com.persagy.apccontrol.basic.outside.feign.wrapper;

import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.outside.feign.client.DangerEventClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerEventInstance;
import com.persagy.apccontrol.basic.structure.event.converter.DangerEventConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DangerEventClientPageWrapper implements DangerEventClient, LogComponent {

    private final DangerEventClient dangerEventClient;
    private final DangerEventConverter dangerEventConverter;

    @Override
    public CustomerResponse<List<DangerEventInstance>> listDangerEvents(@NonNull DangerEventQueryParam body) {
        return FeignParamPageExecutor.execute(body, pageQuery -> {
            return dangerEventClient.listDangerEvents(pageQuery);
        });

    }

    @Override
    public String logTag() {
        return "风险事件";
    }
}
