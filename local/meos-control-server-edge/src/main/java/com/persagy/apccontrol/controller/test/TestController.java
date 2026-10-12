package com.persagy.apccontrol.controller.test;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.common.config.UriFilter;
import com.persagy.apccontrol.common.service.MqttService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;


@Slf4j
@Tag(name = "测试接口用于问题排查")
@RestController
@RequestMapping("/test")
public class TestController {
    @Resource
    private MqttService mqttService;
    
    @Operation(summary = "接口调用统计", tags = VersionConstant.V5_240902_0)
    @GetMapping("/callStatistics")
    public R<List<String>> callStatistics() {
        return R.success(UriFilter.statistics());
    }

    @Operation(summary = "模拟发数")
    @PostMapping("/sendData")
    public void sendData(@RequestBody @Valid FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO) {
        mqttService.expense(feedbackInfoMsgMqttVO);
    }
}
