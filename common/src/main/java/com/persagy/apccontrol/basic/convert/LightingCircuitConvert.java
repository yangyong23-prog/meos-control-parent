package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.dto.lighting.LightingCircuitDetailDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 照明回路转换器
 *
 * @author wxf
 * @since v7.250601.0
 */
@Mapper(componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface LightingCircuitConvert extends BaseConvert {

    @Mapping(target = "relSpace", source = "serviceSpaces")
    @Mapping(target = "relFloor", source = "serviceFloors")
    @Mapping(target = "relController", ignore = true)
    @Mapping(target = "relDDC", ignore = true)
    @Mapping(target = "funcTypeName", ignore = true)
    @Mapping(target = "lampFuncTypeName", ignore = true)
    @Mapping(target = "serviceAreaTypeName", ignore = true)
    @Mapping(target = "bimLocation", ignore = true)
    @Mapping(target = "bimId", ignore = true)
    LightingCircuitDetailDTO toDetailDTO(DtpRwdCircuitDTO source);

}
