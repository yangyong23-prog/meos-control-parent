package com.persagy.apccontrol.basic.structure.runanalysis.strategy.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunAnalysisReportACDto;
import com.persagy.apccontrol.basic.model.vo.runanalysis.energy.RunAnalysisReportEnergyBudgetDto;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.BenchmarkEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import com.persagy.apccontrol.basic.structure.runanalysis.strategy.RunAnalysisReportStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/22 13:41
　　 * @description: 能耗预算相关实现
*/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunAnalysisReportAC implements RunAnalysisReportStrategy<RunAnalysisReportACDto, RunAnalysisReportConfig>, LogComponent  {

    @Override
    public Boolean isSupport(@NotNull RunAnalysisReportTypeEnum type) {
        return RunAnalysisReportTypeEnum.DAILY.equals(type) || RunAnalysisReportTypeEnum.MONTHLY.equals(type);
    }

    @Override
    public RunAnalysisReportChapterEnum getChapter() {
        return RunAnalysisReportChapterEnum.REPORT_CHAPTER_AC;
    }

    @Override
    public String logTag()  {
        return "运行分析报告-空调";
    }
}
