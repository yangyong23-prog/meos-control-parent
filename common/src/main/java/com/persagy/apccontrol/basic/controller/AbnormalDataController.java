package com.persagy.apccontrol.basic.controller;

import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventCommonParam;
import com.persagy.apccontrol.basic.model.param.abnormal.AbnormalEventQueryParam;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.abnormal.AbnormalEventService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

import static com.persagy.apccontrol.basic.constant.VersionConstant.*;

/**
 * @ClassName 薛涛
 * @Description 异常事件
 * @Author 薛涛
 * @Date 2024/5/29 15:00
 * @Version 1.0
 */
@RestController
@RequestMapping(value = "/abnormalEvent")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AbnormalDataController {

    private final AbnormalEventService abnormalEventService;

    @Operation(summary = "查询-异常信息列表", tags = V5_240502_0)
    @PostMapping(value = "/listAbnormalEvent")
    public R<List<ExceptionEventBaseInfoVO>> listAbnormalEvent(
            @RequestBody @Valid AbnormalEventQueryParam abnormalEventQueryParam) {
        return R.success(abnormalEventService.listAbnormalEvent(abnormalEventQueryParam));
    }

    @Operation(summary = "新查询-异常信息列表", tags = V7_260802_0)
    @PostMapping(value = "/new/listAbnormalEvent")
    public R<List<ExceptionEventBaseInfoVO>> listAbnormalEventNew(
            @RequestBody @Valid AbnormalEventQueryParam abnormalEventQueryParam) {
        return R.success(abnormalEventService.listAbnormalEventNew(abnormalEventQueryParam));
    }

    @Operation(summary = "查询-空间带设备异常信息列表", tags = V5_241101_0)
    @PostMapping(value = "/listAbnormalEventBySpaceWithEquipments")
    public R<List<ExceptionEventBaseInfoVO>> listAbnormalEventBySpaceWithEquipments(
            @RequestBody @Valid AbnormalEventQueryParam param) {
        return R.success(abnormalEventService.listAbnormalEventBySpaceWithEquipments(param));
    }

    @Operation(summary = "查询-按系统划分异常事件", tags = V5_240802_0)
    @PostMapping(value = "/getAbnormalDataListDivideSystem")
    public R<List<ExceptionEventBaseInfoVO>> getAbnormalDataListDivideSystem(
            @RequestBody @Valid AbnormalEventCommonParam abnormalEventQueryParam) {
        return R.success(abnormalEventService.getAbnormalDataListDivideSystem(abnormalEventQueryParam));
    }

    @Operation(summary = "统计-根据入参的系统二级运行监测类型查询哪些对象有异常事件", tags = V5_241201_0)
    @PostMapping(value = "/countAbnormalEvent")
    public R<Set<String>> countAbnormalEvent(
            @RequestBody @Valid AbnormalEventQueryParam abnormalEventQueryParam) {
        return R.success(abnormalEventService.countAbnormalEvent(abnormalEventQueryParam));
    }

}
