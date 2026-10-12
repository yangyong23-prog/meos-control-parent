package com.persagy.apccontrol.basic.controller.kitchen;


import com.persagy.apccontrol.basic.model.param.kitchen.KitchenRangeHoodDemandTimeQueryParam;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitchenRangeHoodDemandTimeVO;
import com.persagy.apccontrol.basic.outside.feign.model.response.kitchen.KitchenExhaustResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.kitchen.KitchenRangeHoodService;
import com.persagy.apccontrol.basic.structure.common.strategy.impl.RunningMonitorKitChenRangeHoodStrategy;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(value = "/kitchenRangeHood")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class KitChenRangeHoodController {

    private final DtpRwdClientService dtpRwdClientService;

    private final KitchenRangeHoodService kitchenRangeHoodService;

    private final RunningMonitorKitChenRangeHoodStrategy runningMonitorKitChenRangeHoodStrategy;

    @GetMapping("/static/info/aggregate")
    public R<List<KitchenExhaustResponse>> queryStaticInfoList() {
        List<KitchenExhaustResponse> result = dtpRwdClientService.queryKitchenExhaust();
        return R.success(result);
    }


    @Operation(summary = "查询-设备统计聚合接口")
    @PostMapping("/equipment/statistics")
    public R<Map<String, KitChenRangeHoodEquipStatisticsVo>> queryEquipStatistics(@RequestBody Map<String, Set<String>> param) {
        return R.success(kitchenRangeHoodService.queryEquipStatistics(param));
    }

    /**
     * 查询厨房排油烟需求时间(风机与服务空间)
     *
     * @param param 查询参数, 包含空间与其关联设备
     * @return 需求时间结果, 空间与设备节点均回填合并后的需求时段
     * @since v7.261001.0
     */
    @Operation(summary = "查询-需求时间(风机与服务空间)")
    @PostMapping("/queryDemandTime")
    public R<KitchenRangeHoodDemandTimeVO> queryDemandTime(@RequestBody @Valid KitchenRangeHoodDemandTimeQueryParam param) {
        return R.success(this.runningMonitorKitChenRangeHoodStrategy.listDemandTime(param));
    }
}
