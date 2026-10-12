package com.persagy.apccontrol.basic.model.dto;

import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.CoordinateDto;
import lombok.Data;

@Data
public class JointNode {

    private String id;
    private CoordinateDto location;
}
