package com.persagy.apccontrol.basic.structure.runanalysis.strategy;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.param.runanalysis.RunAnalysisReportBuildParam;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportChapterEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.enumeration.RunAnalysisReportTypeEnum;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.valueobject.RunAnalysisReportConfig;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface RunAnalysisReportStrategy<T extends RunAnalysisReportDataDto, C extends RunAnalysisReportConfig> extends BizComponent<RunAnalysisReportStrategy> {


    /**取值精度*/
    int NEW_SCALE = 10;
    /**
     * 是否支持某种类型的报告
     */
    Boolean isSupport(@NonNull RunAnalysisReportTypeEnum type);

    /**
     * 构建报告，数据入库
     */
    default T build(RunAnalysisReportBuildParam param, Map<RunAnalysisReportChapterEnum,JSONObject> allConfigData){
        return null;
    }

    /**
     * 构建报告，数据入库
     */
    default T buildWithConfig(RunAnalysisReportBuildParam param, C config, Map<RunAnalysisReportChapterEnum, List<RunAnalysisReportConfig>> allConfigData) throws ExecutionException, InterruptedException {
        return null;
    }

    /**
     * 获取报告章节
     */
    RunAnalysisReportChapterEnum getChapter();


    /**
     * 处理章节详情数据，以返回给前端展示
     * @param runAnalysisReport 保存的数据
    **/
    default void processChapterData(RunAnalysisReport runAnalysisReport) {
    }
}
