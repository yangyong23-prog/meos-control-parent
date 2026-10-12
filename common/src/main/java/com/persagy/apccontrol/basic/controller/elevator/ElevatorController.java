package com.persagy.apccontrol.basic.controller.elevator;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.elevator.ElevatorStatisticsParam;
import com.persagy.apccontrol.basic.model.vo.elevator.ElevatorSummaryVO;
import com.persagy.apccontrol.basic.service.elevator.ElevatorService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/8/17 14:54
 */
@Slf4j
@RestController
@RequestMapping("/elevator")
public class ElevatorController {

    @Autowired
    private ElevatorService elevatorService;
    
    @Operation(summary = "查询-全楼、单楼层直梯数据统计", tags = {VersionConstant.V5_240802_0})
    @PostMapping("/statisticsVerticalRunningData")
    public R<ElevatorSummaryVO> statisticsVerticalRunningData(@Validated @RequestBody ElevatorStatisticsParam param) throws Exception {
        param.setClassCode(DtpRwdClassCodeEnum.ELELEL.getValue());
        return R.success(this.elevatorService.statisticsRunningByClassCode(param));
    }
    
    @Operation(summary = "查询-全楼、单楼层扶梯数据统计", tags = {VersionConstant.V5_240802_0})
    @PostMapping("/statisticsEscalatorRunningData")
    public R<ElevatorSummaryVO> statisticsEscalatorRunningData(@Validated @RequestBody ElevatorStatisticsParam param) throws Exception {
        param.setClassCode(DtpRwdClassCodeEnum.ELELES.getValue());
        return R.success(this.elevatorService.statisticsRunningByClassCode(param));
    }

}
