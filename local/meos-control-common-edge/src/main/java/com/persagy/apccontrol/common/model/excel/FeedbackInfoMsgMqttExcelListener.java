package com.persagy.apccontrol.common.model.excel;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.common.service.MqttService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


/**
 * 手动通过 excel 发数
 *
 * @author YangWanYi
 * @version 1.0
 * @company persagy
 * @date 2022-08-11  15:29
 */
@Slf4j
public class FeedbackInfoMsgMqttExcelListener extends AnalysisEventListener<FeedbackInfoMsgMqttExcel> {

    private MqttService mqttService;

    private List<FeedbackInfoMsgMqttVO> dataList = new ArrayList<>();

    public FeedbackInfoMsgMqttExcelListener(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @Override
    public void invoke(FeedbackInfoMsgMqttExcel feedbackInfoMsgMqttExcel, AnalysisContext analysisContext) {
        FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO = new FeedbackInfoMsgMqttVO();
        BeanUtil.copyProperties(feedbackInfoMsgMqttExcel, feedbackInfoMsgMqttVO);
        dataList.add(feedbackInfoMsgMqttVO);
    }

    @SneakyThrows
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (dataList.size() > 0) {
            for (FeedbackInfoMsgMqttVO feedbackInfoMsgMqttVO : dataList) {
                this.mqttService.expense(feedbackInfoMsgMqttVO);
            }
        }
        dataList.clear();
    }

}
