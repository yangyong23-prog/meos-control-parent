package com.persagy.apccontrol.basic.outside.feign.client;

import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "fm-workorder-server", url = "${inner-url.fm.fm-workorder.service:}")
public interface FmWorkOrderClient {

    @ApiOperation(
            value = "【任务查询】任务查询",
            notes = "根据对象实例，批量查询最近一条已完成的观测任务"
    )
    @PostMapping({"/work-order/task/last-operate-by-instanceIds"})
    R<List<TaskInfoDTO>> getTaskByRwdInstanceIds(@RequestBody @ApiParam("对象实例id") Collection<String> rwdInstanceIds);

}
