package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportChapterVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportRemarkVO;
import com.persagy.apccontrol.basic.model.vo.runanalysis.RunAnalysisReportVO;
import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportChapterDto;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReportRemark;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

@Mapper(
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RunAnalysisReportConvert {
    @Mapping(target = "chapterData", expression = "java(toJSONObject(runAnalysisReport.getChapterData()))")
    @Mapping(target = "chapterConfig", expression = "java(toJSONObject(runAnalysisReport.getChapterConfig()))")
    RunAnalysisReportVO toRunAnalysisReportVO(RunAnalysisReport runAnalysisReport);

    @InheritConfiguration
    List<RunAnalysisReportVO> toRunAnalysisReportVOList(List<RunAnalysisReport> source);

    @Mapping(target = "children", ignore = true)
    RunAnalysisReportChapterVO toRunAnalysisReportChapterVO(RunAnalysisReportChapterDto dto);

    List<RunAnalysisReportChapterVO> toRunAnalysisReportChapterVOList(List<RunAnalysisReportChapterDto> source);

    RunAnalysisReportRemarkVO toRunAnalysisReportRemarkVO(RunAnalysisReportRemark source);

    @InheritConfiguration
    List<RunAnalysisReportRemarkVO> toRunAnalysisReportRemarkVOList(List<RunAnalysisReportRemark> source);

    default JSONObject toJSONObject(String source) {
        return JSONObject.parseObject(source);
    }

}
