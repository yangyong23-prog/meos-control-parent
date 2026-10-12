package com.persagy.apccontrol.basic.structure.demand.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.demand.DemandScene;
import com.persagy.apccontrol.basic.model.vo.demand.scene.DemandSceneVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.model.scene.domain.DemandSceneEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 需求管理转换器
 *
 * @author JiYinchuan
 * @since 5.240902.0
 */
@Mapper(
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = DemandInstanceTypeEnum.class
)
public interface DemandSceneConverter extends BaseConvert {

    @Mapping(target = "demandType", expression = "java(source.getDemandTemplateType().toDemandTypeEnum())")
    DemandSceneVO toSceneVo(DemandSceneEntity source);

    List<DemandSceneVO> toSceneVo(List<DemandSceneEntity> source);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    DemandScene toScene(DemandSceneEntity source);
}
