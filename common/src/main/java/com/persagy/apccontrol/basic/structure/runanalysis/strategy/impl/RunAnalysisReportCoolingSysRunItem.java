package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunAnalysisReportACDto;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportACConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy;
import com.persagy.framework.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportCoolingSysRunItem implements RunAnalysisReportStrategy<RunAnalysisReportACDto, RunAnalysisReportACConfig>, LogComponent  {

    @Override
    public Boolean isSupport(@NotNull RunAnalysisReportTypeEnum type) {
        return RunAnalysisReportTypeEnum.DAILY.equals(type) || RunAnalysisReportTypeEnum.MONTHLY.equals(type);
    }

    @Override
    public RunAnalysisReportACDto build(RunAnalysisReportBuildParam param, Map<RunAnalysisReportChapterEnum,JSONObject> allConfigData) {
        RunAnalysisReportACDto  dto = new RunAnalysisReportACDto();
        dto.setName("空调adf");
        dto.setDescription("1221");
        return dto;
    }

    @Override
    public RunAnalysisReportACDto buildWithConfig(RunAnalysisReportBuildParam param, RunAnalysisReportACConfig config, Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData) {
        RunAnalysisReportACDto  dto = new RunAnalysisReportACDto();
        dto.setName("空调adf");
        dto.setDescription("1221");
        return dto;
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_COOLING_SYS_RUN_ITEM;
    }

    @Override
    public void processChapterData(RunAnalysisReport runAnalysisReport) {
        String chapterData = runAnalysisReport.getChapterData();
        RunAnalysisReportACDto jsonObject = JSONObject.parseObject(chapterData, RunAnalysisReportACDto.class);
        jsonObject.setName("加工后1111");
        jsonObject.setDescription("加工后2222");
        runAnalysisReport.setChapterData(JSONObject.toJSONString(jsonObject));
    }

    @Override
    public String logTag()  {
        return "运行分析报告-空调";
    }
}
