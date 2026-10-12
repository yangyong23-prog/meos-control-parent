package com.persagy.apccontrol.common.logtrace.assembler;


import com.persagy.apccontrol.common.logtrace.dto.LogNodeExtendDto;
import com.persagy.apccontrol.common.logtrace.po.LogNodeExtendPo;
import com.persagy.apccontrol.common.logtrace.vo.LogNodeExtendVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LogNodeExtendAssembler {

    List<LogNodeExtendPo> dtoToPoList(List<LogNodeExtendDto> dto);


    @Mapping(source = "extendCode", target = "key")
    @Mapping(source = "extendName", target = "name")
    @Mapping(source = "extendValue", target = "value")
    LogNodeExtendVo poToVo(LogNodeExtendPo po);

    List<LogNodeExtendVo>   poToVoList(List<LogNodeExtendPo> po);
}
