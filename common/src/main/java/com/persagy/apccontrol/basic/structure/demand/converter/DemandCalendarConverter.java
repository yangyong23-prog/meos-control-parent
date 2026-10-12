package com.persagy.apccontrol.basic.structure.demand.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.demand.DemandTemplate;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.resource.demand.param.DemandTemplateSaveDaoParam;
import org.mapstruct.*;

import java.util.List;

/**
 * 需求管理-需求日历模版转换器
 *
 * @author JiYinchuan
 * @since v6.0.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DemandCalendarConverter {

    // ------------------------- Do To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @Mappings({
            @Mapping(target = "templateId", source = "id"),
            @Mapping(target = "templateName", source = "demandTemplateName"),
            @Mapping(target = "demandType", source = "demandType"),
            @Mapping(target = "templateType", source = "demandTemplateType"),
            @Mapping(target = "templateTag", source = "demandTemplateTag")
    })
    DemandTemplateVO toTemplateVo(DemandTemplate source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    @InheritConfiguration
    List<DemandTemplateVO> toTemplateVo(List<DemandTemplate> source);

    // ------------------------- To Dao Param -------------------------

    // ------------------------- Copy -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    DemandTemplateSaveDaoParam copy(DemandTemplateSaveDaoParam source);

}
