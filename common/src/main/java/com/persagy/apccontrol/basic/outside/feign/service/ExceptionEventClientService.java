package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerInstanceTreeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerEventInstance;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerInstanceTreeResponse;
import feign.FeignException;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * @author wxf
 * @description
 * @date 17:26 2025/2/17
**/
public interface ExceptionEventClientService extends BaseFeignClientService {

    /**
     * @author wxf
     * @description 查询风险事件
     * @date 17:27 2025/2/17
     * @param param
     * @return java.util.List<com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO>
    **/
    List<DangerEventInstance> listDangerEvent(@NonNull DangerEventQueryParam param) throws FeignException;

    /**
     * @author wxf
     * @description 查询风险事件
     * @date 17:27 2025/2/17
     * @param param
     * @return java.util.List<com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO>
     **/
    List<DangerEventInstance> listDangerEventTest(@NonNull DangerEventQueryParam param) throws FeignException;

    List<DangerInstanceTreeResponse> listDangerInstanceTree(DangerInstanceTreeQueryParam param);

}