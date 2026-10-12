package com.persagy.apccontrol.basic.controller.common;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.log.PageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.FeedbackInfoMsgMqttVO;
import com.persagy.apccontrol.basic.service.common.PageBroadcastMsgQueryService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 页面播报
 *
 * @author JiYinchuan
 * @since 5.241201.0
 */
@RestController
@RequestMapping("/pageBroadcast")
public class PageBroadcastController {

    @Resource
    private PageBroadcastMsgQueryService pageBroadcastMsgQueryService;

    @Operation(summary = "查询最新播报内容", tags = VersionConstant.V5_6_0)
    @PostMapping("/listLatestContent")
    public R<List<FeedbackInfoMsgMqttVO.PageBroadcastContent>> listLatestContent(@RequestBody @Valid PageBroadcastQueryParam param) {
        return  R.success(pageBroadcastMsgQueryService.listPageBroadcastMsg(param));
    }
    
}
