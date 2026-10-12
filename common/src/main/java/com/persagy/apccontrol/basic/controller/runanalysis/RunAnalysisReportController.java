package com.persagy.apccontrol.basic.controller.runanalysis;

import com.persagy.apccontrol.basic.constant.VersionConstant;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportDownloadParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportListParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportQueryParam;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportRemarkParam;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportChapterVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportRemarkVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportVO;
import com.persagy.apccontrol.basic.service.runanalysis.RunAnalysisReportService;
import com.persagy.framework.common.entity.R;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/runAnalysis/report")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Tag(name = "运行分析报告", description = VersionConstant.V7_260102_0)
public class RunAnalysisReportController {

    private final RunAnalysisReportService runAnalysisReportService;

    @Operation(summary = "查询-获取报告列表", tags = VersionConstant.V7_260102_0)
    @PostMapping("/list")
    public R<Collection<RunAnalysisReportVO>> getList(@Valid @RequestBody RunAnalysisReportListParam param) {
        List<RunAnalysisReportVO> list = runAnalysisReportService.getList(param);
        Long total = (long) list.size();
        if(param.getPageNo() != null && param.getPageSize() != null && param.getPageNo() > 0){
            list =  list.stream().skip((param.getPageNo() - 1) * param.getPageSize()).limit(param.getPageSize()).collect(Collectors.toList());
        }
        return R.successMulti(list,total);
    }

    @Operation(summary = "查询-获取章节目录树", tags = VersionConstant.V7_260102_0)
    @PostMapping("/chapterTree")
    public R<List<RunAnalysisReportChapterVO>> getChapterTree(@Valid @RequestBody RunAnalysisReportQueryParam param) {
        return R.success(runAnalysisReportService.getChapterTree(param));
    }

    @Operation(summary = "查询-获取报告详情", tags = VersionConstant.V7_260102_0)
    @PostMapping("/detail")
    public R<List<RunAnalysisReportVO>> getDetail(@Valid @RequestBody List<Long> ids) {
        return R.success(runAnalysisReportService.getDetail(ids));
    }

    @Operation(summary = "创建报告", tags = VersionConstant.V7_260102_0)
    @PostMapping("/build")
    public R<Boolean> build(@Valid @RequestBody RunAnalysisReportBuildParam param) {
        return R.success(runAnalysisReportService.build(param));
    }

    @GetMapping("/buildAuto")
    public R<Boolean> buildAuto() {
        return R.success(runAnalysisReportService.buildAuto());
    }

    @Operation(summary = "创建报告备注", tags = VersionConstant.V7_260102_0)
    @PostMapping("/remark/save")
    public R<Boolean> saveRemark(@Valid @RequestBody List<RunAnalysisReportRemarkParam> param) {
        return R.success(runAnalysisReportService.saveRemark(param));
    }

    @Operation(summary = "获取报告备注", tags = VersionConstant.V7_260102_0)
    @PostMapping("/remark/list")
    public R<List<RunAnalysisReportRemarkVO>> listRemark(@Valid @RequestBody List<Long> reportIdList) {
        return R.success(runAnalysisReportService.listRemark(reportIdList));
    }

    @Operation(summary = "下载运行分析报告CSV", tags = VersionConstant.V7_260302_0)
    @PostMapping("/download")
    public R<Boolean> downloadReport(@Valid @RequestBody RunAnalysisReportDownloadParam param, HttpServletResponse response) {
        try {
            runAnalysisReportService.downloadReport(param, response);
            return R.success(true);
        } catch (Exception e) {
            log.error("运行分析报告-下载失败", e);
            return R.fail( ErrorCodeConstants.DEF_ERROR.getCode(),"报告下载失败：" + e.getMessage());
        }
    }
}
