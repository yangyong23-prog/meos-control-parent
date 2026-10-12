package com.persagy.apccontrol.basic.outside.feign.model.param.energyresearch;

import com.persagy.apccontrol.basic.outside.enums.EnergyModelTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class EnergyModelItemParam {

    @NotNull
    private String userId;
    @NotNull
    private String projectId;
    /**
     * 能量模型类型
     */
    @NotNull
    private EnergyModelTypeEnum energyModelType;

    private String energyTypeId;
    /**
     * 标签名称
     */
    @NotNull
    private List<String> tagNames;

}
