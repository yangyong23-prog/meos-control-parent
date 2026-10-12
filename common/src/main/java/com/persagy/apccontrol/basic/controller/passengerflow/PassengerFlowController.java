package com.persagy.apccontrol.basic.controller.passengerflow;

import com.persagy.apccontrol.basic.model.vo.passengerflow.ClientServerHomeStatistics;
import com.persagy.apccontrol.basic.model.vo.passengerflow.PassengerFlowHomeStatistics;
import com.persagy.apccontrol.basic.service.passengerflow.PassengerFlowService;
import com.persagy.framework.common.entity.R;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240802_0;
import static com.persagy.apccontrol.basic.constant.VersionConstant.V5_240902_0;

/**
 * 客流
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/21 15:45
 * @since v5.240802.0
 */
@RestController
@RequestMapping(value = "/passengerFlow")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(value = "客流", tags = V5_240802_0)
public class PassengerFlowController {

    private final PassengerFlowService passengerFlowService;

    /**
     * 查询客流首页统计
     *
     * @return {@link R }<{@link PassengerFlowHomeStatistics }>
     */
    @Operation(summary = "查询-查询客流首页统计", tags = V5_240802_0)
    @PostMapping("/getPassagewayHeadCount")
    public R<List<PassengerFlowHomeStatistics>> getPassagewayHeadCount() {
        return R.success(passengerFlowService.getPassagewayHeadCount());
    }

    /**
     * cs首页统计
     *
     * @return {@link R }<{@link ClientServerHomeStatistics }>
     */
    @Operation(summary = "查询-cs首页统计", tags = V5_240902_0)
    @PostMapping("/homeStatistics")
    public R<ClientServerHomeStatistics> homeStatistics() {
        return R.success(passengerFlowService.homeStatistics());
    }

}
