package com.persagy.apccontrol.basic.outside.feign.service;

import com.persagy.apccontrol.basic.outside.BaseFeignClientService;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.AirTerminalZoneControllerParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosAllControllerParameterQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.DtpMeosScheduleInfoCodeDefineQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.AirTerminalZoneControllerParameterResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.dtpmeos.DtpMeosAllControllerParameterResponse;
import com.persagy.apccontrol.basic.structure.control.model.SimpleInfoCodeDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 控制服务客户端
 *
 * @author JiYinchuan
 * @since 7.260102.0
 */
public interface DtpMeosServerCloudClientService extends BaseFeignClientService {

    /**
     * 获取所有控制器参数
     *
     * @param param 查询参数
     * @return 控制器参数
     * @since 7.260102.0
     */
    List<DtpMeosAllControllerParameterResponse> getAllControllerParameter(
            @NonNull @Valid DtpMeosAllControllerParameterQueryParam param);

    /**
     * 获取所有控制器参数定义
     *
     * @param param 查询参数
     * @return 控制器参数定义
     * @since 7.260102.0
     */
    Map<String, List<SimpleInfoCodeDTO>> listScheduleInfoCodeDefine(
            @NonNull DtpMeosScheduleInfoCodeDefineQueryParam param);


    /**
     * 获取所有控制器参数定义
     *
     * @param param 查询参数
     * @return 控制器参数定义
     * @since 7.260102.0
     */
    AirTerminalZoneControllerParameterResponse airTerminalZoneControllerParameter(
            @RequestBody AirTerminalZoneControllerParam param);

}