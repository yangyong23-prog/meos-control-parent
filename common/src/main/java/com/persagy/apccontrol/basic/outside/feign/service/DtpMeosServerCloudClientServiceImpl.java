package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.outside.feign.client.DtpMeosServerCloudClient;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.AirTerminalZoneControllerParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosAllControllerParameterQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosScheduleInfoCodeDefineQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.AirTerminalZoneControllerParameterResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.DtpMeosAllControllerParameterResponse;
import com.persagy.apccontrol.basic.structure.control.model.SimpleInfoCodeDTO;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 控制服务客户端实现类
 *
 * @author JiYinchuan
 * @since 7.260201.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DtpMeosServerCloudClientServiceImpl implements DtpMeosServerCloudClientService {

    private final DtpMeosServerCloudClient dtpMeosServerCloudClient;

    @Override
    public List<DtpMeosAllControllerParameterResponse> getAllControllerParameter(
            @NonNull DtpMeosAllControllerParameterQueryParam param) {
        return this.query(false, "控制台参数", param,
                () -> dtpMeosServerCloudClient.getAllControllerParameter(param), Collections::emptyList);
    }

    @Override
    public Map<String, List<SimpleInfoCodeDTO>> listScheduleInfoCodeDefine(
            @NonNull DtpMeosScheduleInfoCodeDefineQueryParam param) {
        return this.query(false, "控制台参数定义", param,
                () -> dtpMeosServerCloudClient.listScheduleInfoCodeDefine(param), Collections::emptyMap);
    }

    @Override
    public AirTerminalZoneControllerParameterResponse airTerminalZoneControllerParameter(AirTerminalZoneControllerParam param) {
        return this.query(false, "控制台参数定义", param,
                () -> dtpMeosServerCloudClient.airTerminalZoneControllerParameter(param), null);
    }


    @Override
    public String logTag() {
        return "控制云边服务";
    }
}
