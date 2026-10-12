package com.persagy.apccontrol.basic.controller;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogCategoryDto;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogDto;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogEquipmentDto;
import com.persagy.apccontrol.basic.model.dto.runnningLog.RunningLogResDto;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogTypeEnum;
import com.persagy.apccontrol.basic.model.param.log.NowPageBroadcastQueryParam;
import com.persagy.apccontrol.basic.model.vo.log.RunningLogVO;
import com.persagy.apccontrol.basic.model.vo.log.TodayLogSummaryVo;
import com.persagy.apccontrol.basic.resource.param.runningLog.*;
import com.persagy.apccontrol.basic.service.common.PageBroadcastMsgQueryService;
import com.persagy.apccontrol.basic.service.runninglog.NowRunningLogService;
import com.persagy.apccontrol.basic.service.runninglog.RunningLogService;
import com.persagy.framework.common.entity.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "运行日志")
@RequestMapping(value = "/runningLog")
public class RcRunningLogController {

    @Autowired
    private RunningLogService runningLogService;
    @Autowired
    private PageBroadcastMsgQueryService pageBroadcastMsgQueryService;
    @Resource
    private NowRunningLogService nowRunningLogService;

    @Operation(summary = "运行日志标识查询", tags = {VersionConstant.V5_2405_1})
    @GetMapping("/queryRunningLogType")
    public R<Map<String, String>> queryRunningLogType() {
        return R.success(RunningLogTypeEnum.getRunningLogTypeMap());
    }

    @Operation(summary = "运行日志列表查询", tags = {VersionConstant.V5_241102_0})
    @PostMapping("/listRunningLogByDate")
    public R<Collection<RunningLogDto>> listRunningLogByDate(@Validated @RequestBody RunningLogQueryParam param) {
        List<RunningLogDto> runningLogDtos = runningLogService.listRunningLogByDate(param);
        return R.success(runningLogDtos);
    }

    @Operation(summary = "运行日志查询", tags = {VersionConstant.V5_8_0})
    @PostMapping("/queryListByPage")
    public R<Collection<RunningLogDto>> queryListByPage(@Validated @RequestBody RunningLogPageQueryParam param) {
        return runningLogService.queryLogList(param);
    }


    @Operation(summary = "查询-某一级菜单下的所有二级菜单", tags = {VersionConstant.V5_241102_0})
    @PostMapping("/listSecondLevelCategory")
    public R<Collection<JSONObject>> listSecondLevelCategory(@Validated @RequestBody RunningLogQueryParam param) {
        List<JSONObject> secondLevelCategorys = runningLogService.listSecondLevelCategory(param.getFirstLevelCategory());
        return R.success(secondLevelCategorys);
    }
    
    @Operation(summary = "运行日志设备查询", tags = {VersionConstant.V7_250402_0})
    @PostMapping("/equipmentList")
    public R<List<RunningLogEquipmentDto>> equipmentList(@Validated @RequestBody RunningLogEquipmentListQueryParam param) {
        return R.success(runningLogService.equipmentList(param));
    }
    
    @Operation(summary = "对外接口--查询日志列表", tags = {VersionConstant.V5_8_0})
    @PostMapping("/queryListByParam")
    public R<List<RunningLogResDto>> queryListByParam(@Validated @RequestBody RunningLogPageQueryParam param) {
        return R.success(runningLogService.queryLogListByParam(param));
    }
    
    @Operation(summary = "日志下载", tags = {VersionConstant.V7_250402_0})
    @PostMapping("/export")
    public R<String> export(@Validated @RequestBody RunningLogPageQueryParam param) {
        return R.success(runningLogService.export(param));
    }

    
    //------------- 新版运行日志 --------
    
    
    @Operation(summary = "查询-值班首页运行播报统计", tags = {VersionConstant.V7_250801_0})
    @PostMapping("/todayLogSummary")
    public R<TodayLogSummaryVo> todayLogSummary() {
        return R.success(pageBroadcastMsgQueryService.todayLogSummary());
    }
    
    
    @Operation(summary = "查询日志播报的数据", tags = VersionConstant.V7_250801_0)
    @PostMapping("/listPageBroadcastMsg")
    public R<List<RunningLogVO>> listPageBroadcastMsg(@RequestBody @Valid NowPageBroadcastQueryParam param) {
        return R.success(nowRunningLogService.listPageBroadcastMsg(param));
    }
    
    
    @Operation(summary = "运行日志列表查询", tags = {VersionConstant.V7_250801_0})
    @PostMapping("/queryRunningLogListByPage")
    public R<Collection<RunningLogVO>> queryRunningLogListByPage(@Validated @RequestBody NowRunningLogQueryParam param) {
        return nowRunningLogService.queryRunningLogListByPage(param);
    }
    
    @Operation(summary = "新版日志下载", tags = {VersionConstant.V7_250801_0})
    @PostMapping("/downloadLog")
    public R<String> downloadLog(@Validated @RequestBody NowRunningLogQueryParam param) {
        return R.success(nowRunningLogService.downloadLog(param));
    }
    
    
    @Operation(summary = "运行日志分类查询", tags = {VersionConstant.V7_250801_0})
    @PostMapping("/logCategoryList")
    public R<List<RunningLogCategoryDto>> logCategoryList() {
        return R.success(runningLogService.getLogCategoryList());
    }
    
    @Operation(summary = "通过一级分类查询数据库中有的二级日志分类", tags = {VersionConstant.V7_250801_0})
    @PostMapping("/getSecondLevelRunningLogCategory")
    public R<List<RunningLogCategoryDto>> getSecondLevelRunningLogCategory(@Validated @RequestBody QueryLogCategoryParam param) {
        return R.success(nowRunningLogService.getSecondLevelRunningLogCategory(param));
    }
}
