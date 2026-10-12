package com.persagy.apccontrol.controller.runanalysis;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.ProjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.param.runanalysis.*;
import com.persagy.apccontrol.basic.model.vo.runanalysis.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.SyncHistoryStorageParam;
import com.persagy.apccontrol.basic.service.runanalysis.RunAnalysisService;
import com.persagy.apccontrol.task.SpaceAnalyseTaskHandlerV2;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/runAnalysis")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "运行分析", description = VersionConstant.V5_241102_0)
public class RunAnalysisController {

    private final RunAnalysisService runAnalysisService;
    private final SpaceAnalyseTaskHandlerV2 spaceAnalyseTaskHandlerV2;

    @Operation(summary = "统计", tags = VersionConstant.V5_241102_0)
    @PostMapping("/statistics")
    public R<com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisStatisticsVO> statistics(@Validated @RequestBody RunAnalysisBaseParam param) {
        return R.success(runAnalysisService.statistics(param));
    }

    @Operation(summary = "能耗曲线数据查询", tags = VersionConstant.V5_241102_0)
    @PostMapping("/listEnergyCurveData")
    public R<com.persagy.apccontrol.basic.model.vo.runanalysis.EnergyCurveDataVO> listEnergyCurveData(@Validated @RequestBody EnergyCoverDataParam param) {
        return R.success(runAnalysisService.listEnergyCurveData(param));
    }

    @Operation(summary = "获取分析数据数", tags = VersionConstant.V5_241102_0)
    @PostMapping("/getAnalysisDataTree")
    public R<List<AnalysisDataTreeVO>> getAnalysisDataTree(@Validated @RequestBody AnalysisDataTreeParam param) {
        return R.success(runAnalysisService.getAnalysisDataTree(param));
    }

    @Operation(summary = "获取分析类型", tags = VersionConstant.V5_241102_0)
    @GetMapping("/listAnalysisType/{date}")
    public R<List<AnalysisTypeVO>> listAnalysisType(@PathVariable("date") LocalDate date) {
        return R.success(runAnalysisService.listAnalysisType(date));
    }

    @Operation(summary = "同步蓄冷蓄热数据", tags = VersionConstant.V5_241102_0)
    @PostMapping("/history/storageTag/sync")
    public R<Boolean> storageSync(@RequestBody SyncHistoryStorageParam param) {
        return R.success(runAnalysisService.storageTagSync(param));
    }

    @Operation(summary = "获取空间详情", tags = VersionConstant.V5_241201_0)
    @PostMapping("/spaceDetail")
    public R<com.persagy.apccontrol.basic.model.vo.runanalysis.SpaceDetailVO> spaceDetail(@RequestBody @Validated AnalysisSpaceDetailParam param) {
        return R.success(runAnalysisService.spaceDetail(param));
    }

    @Operation(summary = "获取末端设备类列表", tags = VersionConstant.V7_250102_0)
    @PostMapping("/listTerminalClassCode/{secondType}")
    public R<List<ClassCodeVO>> listTerminalClassCode(@PathVariable("secondType") RunningMonitorSecondTypeEnum secondType) {
        return R.success(runAnalysisService.listTerminalClassCode(secondType));
    }

    @Operation(summary = "设备筛选", tags = VersionConstant.V5_241201_0)
    @PostMapping("/listEquip")
    public R<List<AnalysisEquipmentVO>> listEquipment(@RequestBody @Validated AnalysisListEquipmentParam param) {
        return R.success(runAnalysisService.listEquip(param));
    }

    @Operation(summary = "信息点查询", tags = VersionConstant.V5_241201_0)
    @PostMapping("/listInfoCode")
    public R<Collection<AnalysisInfoCodeVO>> listInfoCode(@RequestBody @Validated AnalysisInfoCodeParam param) {
        return R.success(runAnalysisService.listInfoCode(param));
    }


    @Operation(summary = "空调查询空间列表全量分析数据", tags = VersionConstant.V5_241201_0)
    @PostMapping("/airConditioner/querySpaceList")
    public R<AirConditionerSpaceAnalysisVO> querySpaceAnalysisList(@RequestBody @Validated BasicsAnalysisParam param) {
        return R.success(runAnalysisService.querySpaceAnalysisList(param));
    }

    @Operation(summary = "新版空调查询空间列表全量分析数据", tags = VersionConstant.V5_241201_0)
    @PostMapping("/airConditioner/getSpaceList")
    public R<AirConditionerSpaceAnalysisVO> getSpaceAnalysisList(@RequestBody @Validated BasicsAnalysisParam param) {
        return R.success(runAnalysisService.getSpaceAnalysisList(param));
    }

    @Operation(summary = "手动触发计算", tags = VersionConstant.V7_250902_0)
    @GetMapping("/runSpaceAnalyseTask/{date}")
    public R<Boolean> runSpaceAnalyseTask(@PathVariable("date") LocalDate date) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectId(ContextUtil.getProjectId());
        return R.success(spaceAnalyseTaskHandlerV2.processWithDay(ContextUtil.getGroupCode(),projectDTO,date));
    }

    @Operation(summary = "获取空间标签数量", tags = VersionConstant.V7_250902_0)
    @PostMapping("/airConditioner/getSpaceTagCount")
    public R<Map<String, Integer>> getSpaceTagCount(@RequestBody @Validated BasicsAnalysisParam param) {
        return R.success(runAnalysisService.getSpaceTagCount(param.getDate(), param.getType(), param.getInstanceIds()));
    }

    @Operation(summary = "空间整体概述", tags = VersionConstant.V7_250902_0)
    @PostMapping("/airConditioner/getSpaceSummary")
    public R<SpaceSummaryVo> getSpaceSummary(@RequestBody @Validated BasicsAnalysisParam param) {
        return R.success(runAnalysisService.getSpaceSummary(param));
    }
}
