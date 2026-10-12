package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.dto.intervention.InterventionConfigAddDto;
import com.persagy.apccontrol.basic.model.dto.intervention.InterventionInfoQueryListParamDto;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionAddEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfigEntity;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionInfoQueryParamEntity;
import com.persagy.apccontrol.basic.model.param.intervention.InterventionInfoQueryParam;
import com.persagy.apccontrol.basic.structure.intervention.infrastructure.param.InterventionConfigQueryParam;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = Constant.SPRING)
public interface InterventionConvert {

    @Mapping(source ="interventionConfig",target="configData" )
    InterventionConfig addEntityToPo(InterventionAddEntity addEntity);
    InterventionConfigEntity poToEntity(InterventionConfig config);
    List<InterventionConfigEntity> poToEntityList(List<InterventionConfig> config);
    InterventionInfoQueryParam queryParamToEntity(InterventionInfoQueryListParamDto queryParam);

    InterventionInfoQueryParamEntity queryParamToInfoQueryParamEntity(InterventionInfoQueryParam queryParam);

    InterventionAddEntity addConfigDtoToEntity(InterventionConfigAddDto addDto);
    @Mapping(source ="instanceIds",target="filterInstanceIds" )
    InterventionConfigQueryParam  paramToConfigQueryParam(InterventionInfoQueryListParamDto listParam);
}