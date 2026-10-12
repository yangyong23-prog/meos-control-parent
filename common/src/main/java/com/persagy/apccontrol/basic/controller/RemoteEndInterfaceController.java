package com.persagy.apccontrol.basic.controller;

import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.outside.dto.riskevent.TaskInfoDTO;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 边调云
 *
 * @author LiuYang
 * @date 2024-06-29 18:28
 * @since v5.240602.0
 */
@Tag(name = "边调云")
@Validated
@RestController
@RequestMapping("/edge")
public class RemoteEndInterfaceController {

    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Operation(summary = "查询巡检信息", tags = {VersionConstant.V5_240602_0})
    @PostMapping("/task/mapTaskInfo")
    public R<Map<String, TaskInfoDTO>> mapTaskInfo(@RequestBody List<String> objectIdList) {
        return R.success(this.abstractCommonServer.mapTaskInfo(objectIdList));
    }

}
