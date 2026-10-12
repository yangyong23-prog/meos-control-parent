package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.base.InstanceTypeEnum;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 通风需求管理配置转换器
 *
 * @author ShaoHongbo
 * @since v6.0.0
 */
@Mapper(
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = InstanceTypeEnum.class
)
public interface TagConverter extends BaseConvert {

    // ------------------------- To VO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.240502.0
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "localId", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "localName", ignore = true)
    @Mapping(target = "floorId", ignore = true)
    @Mapping(target = "floorLocalId", ignore = true)
    @Mapping(target = "floorLocalName", ignore = true)
    @Mapping(target = "abnormal", expression = "java(toCommonTag(source.getAbnormal()))")
    @Mapping(target = "manualAutoStatus", ignore = true)
    @Mapping(target = "localRemote", ignore = true)
    @Mapping(target = "onOffState", ignore = true)
    @Mapping(target = "onlineStatusEnum", ignore = true)
    @Mapping(target = "powerStatus", ignore = true)
    TagInfoVO toCommonTag(AirTerminalSpaceTagDTO source);

}
