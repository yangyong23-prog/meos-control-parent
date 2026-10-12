package com.persagy.apccontrol.basic.structure.intervention.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.intervention.InterventionConfig;
import com.persagy.apccontrol.basic.structure.intervention.model.domain.identifier.InterventionConfigIdentifier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 干预配置转换器
 *
 * @author JiYinchuan
 * @since 5.240702.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InterventionConfigConverter {

    // ------------------------- To Model -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 5.240702.0
     */
    @Mapping(target = "interventionConfigId", source = "id")
    InterventionConfigIdentifier toIdentifier(InterventionConfig source);

}
