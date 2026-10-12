package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import com.persagy.apccontrol.basic.model.dto.JointNode;
import lombok.Data;

import java.util.List;

@Data
public class JointPoint {

    private JointNode jointNode;

    private List<CoordinateDto> jointOrientations;
}
