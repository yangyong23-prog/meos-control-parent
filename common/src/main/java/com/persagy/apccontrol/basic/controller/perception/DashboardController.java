package com.persagy.apccontrol.basic.controller.perception;

import com.alibaba.fastjson.JSONArray;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.enumtype.terminal.AbnormalStatusTypeEnum;
import com.persagy.apccontrol.basic.model.param.dashboard.AbnormalStateParam;
import com.persagy.apccontrol.basic.model.param.dashboard.DashboardQueryParam;
import com.persagy.apccontrol.basic.model.param.dashboard.PeriodDataBatchQueryParam;
import com.persagy.apccontrol.basic.model.vo.CurveVO;
import com.persagy.apccontrol.basic.model.vo.PeriodDataBatchVO;
import com.persagy.apccontrol.basic.service.DashBoardService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author ：wzq
 * @description：
 * @date ：Created in 2023/10/9 7:33 PM
 */
@Tag(name = "系统运行看板")
@Validated
@RestController
@RequestMapping("/dashboard")
public class DashboardController implements LogComponent {

    @Autowired
    private DashBoardService dashBoardService;

    @Operation(summary = "查询系统运行效率信息", tags = VersionConstant.V5_7_0)
    @PostMapping("/getSystemEfficiency")
    public R<JSONArray> getSystemEfficiency(@RequestBody DashboardQueryParam param) {
        try {
            return R.success(dashBoardService.getSystemEfficiency(param));
        } catch (Exception e) {
            logProjectInstance().error("查询系统运行效率信息异常", e, Pair.of("param", param));
        }
        return R.success(null);
    }

    @Operation(summary = "查询冷机运行信息", tags = VersionConstant.V5_7_0)
    @PostMapping("/getChillerLoad")
    public R<List<CurveVO>> getChillerLoad(@RequestBody DashboardQueryParam param) {
        try {
            return R.success(dashBoardService.getChillerLoad(param));
        } catch (Exception e) {
            logProjectInstance().error("查询冷机运行信息异常", e, Pair.of("param", param));
        }
        return R.success(null);
    }

    @Operation(summary = "查询冷却侧温度分析信息", tags = VersionConstant.V5_7_0)
    @PostMapping("/getChillingTemp")
    public R<List<CurveVO>> getChillingTemp(@RequestBody DashboardQueryParam param) {
        try {
            return R.success(dashBoardService.getChillingTemp(param));
        } catch (Exception e) {
            logProjectInstance().error("查询冷却侧温度信息异常", e, Pair.of("param", param));
        }
        return R.success(null);
    }

    @Operation(summary = "查询冷冻侧供回水温差信息", tags = VersionConstant.V5_7_0)
    @PostMapping("/getFreezingTempDiff")
    public R<List<CurveVO>> getFreezingTempDiff(@RequestBody DashboardQueryParam param) {
        try {
            return R.success(dashBoardService.getFreezingTempDiff(param));
        } catch (Exception e) {
            logProjectInstance().error("查询冷冻侧供回水温差信息异常", e, Pair.of("param", param));
        }
        return R.success(null);
    }

    @Operation(summary = "查询冷冻侧供水温度信息", tags = VersionConstant.V5_7_0)
    @PostMapping("/getFreezingTemp")
    public R<List<CurveVO>> getFreezingTemp(@RequestBody DashboardQueryParam param) {
        try {
            return R.success(dashBoardService.getFreezingTemp(param));
        } catch (Exception e) {
            logProjectInstance().error("查询冷冻侧供水温度信息异常", e, Pair.of("param", param));
        }
        return R.success(null);
    }

    @Operation(summary = "查询冷冻二次侧信息", tags = VersionConstant.V5_7_0)
    @PostMapping("/getFreezingSecondary")
    public R<List<CurveVO>> getFreezingSecondary(@RequestBody DashboardQueryParam param) {
        try {
            return R.success(dashBoardService.getFreezingSecondary(param));
        } catch (Exception e) {
            logProjectInstance().error("查询冷冻二次侧信息异常", e, Pair.of("param", param));
        }
        return R.success(null);
    }

    @Operation(summary = "批量查询分精度数据", tags = VersionConstant.V5_240902_0)
    @PostMapping("/batchQueryPeriodData")
    public R<List<PeriodDataBatchVO>> batchQueryPeriodData(@RequestBody PeriodDataBatchQueryParam param) {
        return R.success(dashBoardService.batchQueryPeriodData(param));
    }

    @Operation(summary = "查询对象异常状态", tags = VersionConstant.V5_241201_0)
    @PostMapping("/listAbnormalState")
    public R<Map<String, List<AbnormalStatusTypeEnum>>> listAbnormalState(
            @RequestBody AbnormalStateParam param) {
        return R.success(dashBoardService.listAbnormalState(param));
    }

    @Override
    public String logTag() {
        return "DashboardController";
    }

}
