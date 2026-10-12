package com.persagy.apccontrol.common.logtrace.assembler;


import com.persagy.apccontrol.common.logtrace.dto.LogInfoDto;
import com.persagy.apccontrol.common.logtrace.entity.LogNodeEntity;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import com.persagy.apccontrol.common.logtrace.vo.LogNodeInfoVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LogNodeAssembler {

   LogNodePo nodeEntityToNodePo(LogNodeEntity logNodeEntity);

   @Mapping(source ="logInfoDto.businessCode",target = "code")
   @Mapping(source ="logInfoDto.businessDesc",target = "description")
   LogNodeEntity nodeDtoToNodeEntities(LogInfoDto logInfoDto);

   @Mapping(source ="po.code",target = "businessCode")
//   @Mapping(source ="po.extend",target = "data")
   @Mapping(source ="po.description",target = "businessDesc")
   @Mapping(source ="po.id",target = "nodeId")
   LogNodeInfoVo nodePoToVo(LogNodePo po);
}
