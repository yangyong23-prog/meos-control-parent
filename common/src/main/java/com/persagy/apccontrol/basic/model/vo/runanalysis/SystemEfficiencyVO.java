package com.persagy.apccontrol.basic.model.vo.runanalysis;

import lombok.Data;

import java.util.List;

@Data
public class SystemEfficiencyVO {

    private String classCode;

    private List<ColdEfficiencyVO> efficiencyDataList;

    private Integer order;
}
