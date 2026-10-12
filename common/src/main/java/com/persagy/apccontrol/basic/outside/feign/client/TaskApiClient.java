package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Collection;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Api(
        value = "TaskApiClient",
        hidden = true
)
@FeignClient(
        name = "${persagy.serviceName.exp:yiheng-facility-ops-server}",
        path = "/task",
        contextId = "task-api-client",
        primary = false,
        url = "${persagy.serviceUrl.exp:}"
)
public interface TaskApiClient {
    @ApiOperation(
            value = "【任务查询】任务查询",
            notes = "根据对象实例，批量查询最近一条已完成的观测任务"
    )
    @PostMapping({"/alert-task-by-instance-id"})
    @ApiImplicitParams({@ApiImplicitParam(
            name = "group-code",
            value = "集团编码",
            required = true,
            paramType = "header"
    ), @ApiImplicitParam(
            name = "project-id",
            value = "项目编码",
            required = true,
            paramType = "header"
    )})
    List<TaskInfoDTO> getTaskByRwdInstanceIds(@RequestHeader("group-code") String groupCode,
                                              @RequestHeader("project-id") String projectId,
                                              @RequestBody @ApiParam("对象实例id") Collection<String> rwdInstanceIds);
}
