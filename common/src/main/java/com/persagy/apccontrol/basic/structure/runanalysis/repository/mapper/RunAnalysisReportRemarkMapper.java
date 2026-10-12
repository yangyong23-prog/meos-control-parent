package com.persagy.apccontrol.basic.structure.runanalysis.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReport;
import com.persagy.apccontrol.basic.structure.runanalysis.model.entity.RunAnalysisReportRemark;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RunAnalysisReportRemarkMapper extends BaseMapper<RunAnalysisReportRemark> {
}
