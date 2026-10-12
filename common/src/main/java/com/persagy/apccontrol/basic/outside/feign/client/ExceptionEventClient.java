package com.persagy.apccontrol.basic.outside.feign.client;


import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.DangerInstanceTreeQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.ExceptionEventProblemParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.exceptionevent.ExceptionEventQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerInstanceTreeResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventOriginalProblemResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventResponse;
import io.swagger.annotations.Api;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RefreshScope
@Validated
@Primary
@Api(
        value = "ExceptionEventClient",
        hidden = true
)
@FeignClient(
        name = "${persagy.serviceName.exp:yiheng-facility-ops-server}",
        path = "${persagy.service.path.exp:}",
        contextId = "exception-event",
        url = "${persagy.serviceUrl.exp:}"
)
public interface ExceptionEventClient {

//    /**
//     * 查询异常事件
//     *
//     * @param groupCode 组码
//     * @param projectId 项目id
//     * @param objIds 对象id列表
//     * @return {@code Map<String, List<ExceptionEventBaseInfoVO>> }
//     * @Deprecated 2025-07-23 已弃用, 请使用 {@link #listExceptionEventProblem}
//     */
//    @ApiOperation(
//            value = "根据对象id查询异常事件基本信息",
//            tags = {"异常事件对外接口"}
//    )
//    @PostMapping({"/exception/event/exception-event-baseInfo-objIds"})
//    @Nonnull
//    @Deprecated
//    Map<String, List<ExceptionEventBaseInfoVO>> exceptionEventBaseInfoByObjIds(@RequestHeader("group-code") String groupCode,
//                                                                               @RequestHeader("project-id") String projectId,
//                                                                               @RequestBody Collection<String> objIds);

    /**
     * 查询异常事件
     *
     * @param param 参数
     * @return {@code List<ExceptionEventBaseInfoVO> }
     * @since 7.250701.0
     */
    @PostMapping(value = "/open-api/exception-event/problem/query-original-problem", headers = {"Accept=application/r"})
    CustomerResponse<List<ExceptionEventOriginalProblemResponse>> listExceptionEventProblem(@RequestBody ExceptionEventProblemParam param);

    /**
     * 查询异常事件列表
     *
     * @param body 请求体
     * @return 异常事件列表
     * @since 5.241101.0
     */
    @PostMapping(value = "/exception-event/third-part/page")
    List<ExceptionEventResponse> listExceptionEvents(@RequestBody ExceptionEventQueryParam body);

    @PostMapping(value = "/open-api/rwd-instance-tree/list", headers = {"Accept=application/r"})
    CustomerResponse<List<DangerInstanceTreeResponse>> listDangerInstanceTree(@RequestBody DangerInstanceTreeQueryParam param);
}
