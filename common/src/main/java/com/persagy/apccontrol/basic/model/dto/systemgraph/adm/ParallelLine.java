package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.persagy.apccontrol.basic.outside.feign.model.response.BaseResponse;
import lombok.Data;

import java.util.List;

@Data
public class ParallelLine extends BaseResponse {

    private List<LineDto> linePaths;
    private List<JointPoint> jointPoints;
    private List<LineDto> groupLines;
    private String graphCode;
    private String relCode;

}