package com.persagy.apccontrol.controller.demand;


import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.RunningStandardBoardQueryObjectParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.RunningStandardBoardQueryParam;
import com.persagy.apccontrol.basic.model.param.runningstandardboard.SpecialBusinessTimeQuoteCountQueryParam;
import com.persagy.apccontrol.basic.model.vo.runningstandardboard.*;
import com.persagy.apccontrol.basic.structure.demand.enumeration.ParticipateInSchedulingEnum;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.apccontrol.service.RunningStandardService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 运行标准controller
 *
 * @author xuetao
 * @since 5.241102.0
 */

@RestController
@RequestMapping("/running-standard")
@Slf4j
public class RunningStandardController {

    @Autowired
    private RunningStandardService runningStandardService;


    @Operation(summary = "查询-某日营业时间的配置情况", tags = VersionConstant.V5_241102_0)
    @PostMapping("/previewProjectBusinessTime")
    public R<BusinessTimeObjectVo>  previewProjectBusinessTime(@RequestBody RunningStandardBoardQueryParam param){

        return R.success(runningStandardService.previewProjectBusinessTime(param));
    }

    @Operation(summary = "查询-某个对象得工作历引用条数", tags = VersionConstant.V5_241102_0)
    @PostMapping("/specialBusinessTimeQuoteCount")
    public R<List<SpecialBusinessTimeQuoteCountVo>>  specialBusinessTimeQuoteCount(@RequestBody SpecialBusinessTimeQuoteCountQueryParam param){

        return R.success(runningStandardService.specialBusinessTimeQuoteCount(param));
    }

    @Operation(summary = "查询-某日空调运行标准", tags = VersionConstant.V5_241102_0)
    @PostMapping("/previewProjectGlobalConfig")
    public R<List<AirconditioningRunningStandardObjectVo>> previewProjectGlobalConfig(@RequestBody RunningStandardBoardQueryParam param){

        return R.success(runningStandardService.previewProjectGlobalConfig(param));
    }

    @Operation(summary = "查询-某日空调运行标准 空间调度是否参与调度", tags = VersionConstant.V5_241102_0)
    @PostMapping("/participateInScheduling")
    public R<ParticipateInSchedulingEnum> participateInScheduling(@RequestBody RunningStandardBoardQueryParam param){
        LocalDate localDate = DateUtils.str2LocalDate(param.getWorkDate(), DateUtils.SDF_DAY);
        return R.success(runningStandardService.isParticipateInScheduling(localDate));
    }

    @Operation(summary = "查询-某日价值选择方案", tags = VersionConstant.V5_241102_0)
    @PostMapping("/previewProjectValueSelect")
    public R<ValueSelectObjectVo> previewProjectValueSelect(@RequestBody @Valid RunningStandardBoardQueryParam runningStandardBoardQueryParam) {
        return R.success(this.runningStandardService.previewProjectValueSelect(runningStandardBoardQueryParam));
    }


    @Operation(summary = "查询-某日特殊需求配置项", tags = VersionConstant.V5_241102_0)
    @PostMapping("/previewSpecialDemandConfig")
    public R<SpecialDemandConfigObjectVo> previewSpecialDemandConfig(@RequestBody @Valid RunningStandardBoardQueryParam runningStandardBoardQueryParam){
        return R.success(this.runningStandardService.previewSpecialDemandConfig(runningStandardBoardQueryParam));
    }

    @Operation(summary = "查询-引用对象列表", tags = VersionConstant.V5_241102_0)
    @PostMapping("/previewReferenceSpaceDetail")
    public R<List<ReferenceSpaceDetailVo>> previewReferenceSpaceDetail(@RequestBody @Valid RunningStandardBoardQueryObjectParam runningStandardBoardQueryObjectParam){
        return R.success(this.runningStandardService.previewReferenceSpaceDetail(runningStandardBoardQueryObjectParam));
    }

}
