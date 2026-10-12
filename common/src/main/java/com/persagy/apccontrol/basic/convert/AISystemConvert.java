package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.vo.AI.AISystemVo;
import org.mapstruct.Mapper;

@Mapper(componentModel = Constant.SPRING)
public interface AISystemConvert {

    AISystemVo toAISystemVo(DtpRwdEquipmentDTO Equipment);

}
