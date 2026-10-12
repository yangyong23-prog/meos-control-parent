package com.persagy.apccontrol.basic.model.vo.runanalysis.energy;

import com.persagy.apccontrol.basic.structure.runanalysis.model.dto.RunAnalysisReportDataDto;
import lombok.Data;

/**
 　　* @author weiyizhong
    * @Company yushu.biz
　　 * @date 2026/1/22 13:47
　　 * @description: 能耗数据Vo
*/
@Data
public class RunAnalysisReportACDto extends RunAnalysisReportDataDto {

    String name;
    String description;

}