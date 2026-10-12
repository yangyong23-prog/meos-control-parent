package com.persagy.apccontrol.basic.model.vo.ventilation;

import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningCO2QualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 通风DtpRwdSpaceVO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VentilationDtpRwdSpaceVO extends DtpRwdSpaceVO {
    
    @Schema(description = "关联对象")
    private List<VentilationDtpRwdEquipmentVO> relObjects;
    
    @Schema(description = "温度品质对象")
    private DemandConfigAirconditioningTdbSeasonValueObject tdbQuality;
    
    @Schema(description = "CO2品质对象")
    private DemandConfigAirconditioningCO2QualityValueObject co2Quality;
    
    @Schema(description = "二氧化碳过高上限")
    private BigDecimal o2Target;
    
    @Schema(description = "二氧化碳过高上限")
    private BigDecimal coTarget;
    
    @Schema(description = "二氧化碳过高上限")
    private BigDecimal nh3Target;
    
    @Schema(description = "二氧化碳过高上限")
    private BigDecimal h2sTarget;
    
}
