package com.persagy.apccontrol.controller;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import com.alibaba.excel.EasyExcel;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.utils.DailyStringCounter;
import com.persagy.apccontrol.common.model.excel.FeedbackInfoMsgMqttExcel;
import com.persagy.apccontrol.common.model.excel.FeedbackInfoMsgMqttExcelListener;
import com.persagy.apccontrol.common.service.MqttService;
import com.persagy.apccontrol.util.mqtt.MqttCounter;
import com.persagy.framework.annotation.mqtt.ConditionalOnMqttEnabled;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * @author yuyao
 */
@RestController
@RequestMapping("/mqtt")
@Slf4j
@Tag(name = "MQTT模拟发数接口")
@ConditionalOnMqttEnabled
public class MqttController {
    @Resource
    private MqttService mqttService;
    
    @Resource
    private MqttCounter mqttCounter;

    @Operation(summary = "模拟发数")
    @PostMapping("/sendData")
    public void sendData(@RequestBody @Valid FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO) {
        mqttService.expense(feedbackInfoMsgMqttVO);
    }

    @Operation(summary = "批量模拟发数")
    @PostMapping(value = "/sendBatchIotData")
    public R<String> sendBatchIotData(@RequestParam("file") MultipartFile file) {
        try {
            FeedbackInfoMsgMqttExcelListener feedbackInfoMsgMqttExcelListener = new FeedbackInfoMsgMqttExcelListener(this.mqttService);
            EasyExcel.read(file.getInputStream(), FeedbackInfoMsgMqttExcel.class, feedbackInfoMsgMqttExcelListener).sheet().doRead();
        } catch (Exception e) {
            log.error("批量模拟发数出现异常,msg:{}", e);
        }
        return R.success(LocaleMessageUtil.getMessage("key_cdb32ce6a0eb44d6bee84fd4cb8dad09"));
    }
    
    
    @Operation(summary = "获取sse发数统计")
    @GetMapping(path = "/sse/statistics")
    public Map<String, Integer> getSseStatistics() {
        return DailyStringCounter.getCount();
    }
    
    @Operation(summary = "获取sse发数统计开关设置")
    @GetMapping(path = "/sse/statisticsSwitch")
    public void statisticsSwitch(Boolean sw) {
        if (sw == null) {
            return;
        }
        DailyStringCounter.SWITCH = sw;
    }
    
    @Operation(summary = "获取sse发数线程池队列大小")
    @GetMapping(path = "/sse/executorQueueSize")
    public Map<String, Object> getExecutorQueueSize() {
        return mqttCounter.getExecutorQueueSize();
    }

}
