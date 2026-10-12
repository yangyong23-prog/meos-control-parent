package com.persagy.apccontrol.common.logtrace.assembler;


import com.persagy.apccontrol.common.logtrace.entity.LogNodeRelationEntity;
import com.persagy.apccontrol.common.logtrace.po.LogNodeRelationPo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LogNodeRelationAssembler {

    LogNodeRelationPo entityToPo(LogNodeRelationEntity entity);
}
