package com.persagy.apccontrol.basic.structure.instance.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.vo.instance.SpaceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SpaceConverter extends BaseConvert {

    // ------------------------- To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 7.250201.0
     */
    @Mapping(target = "roomFuncTypeName", ignore = true)
    @Mapping(target = "equipments", ignore = true)
    SpaceVO toSpaceVo(DtpRwdSpaceDTO source);

}
