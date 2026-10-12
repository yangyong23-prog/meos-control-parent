package com.persagy.apccontrol.basic.structure.demand.converter.instance.group;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.demand.DemandInstanceGroup;
import com.persagy.apccontrol.basic.model.vo.demand.DemandInstanceGroupVO;
import com.persagy.apccontrol.basic.resource.demand.po.instance.group.DemandInstanceGroupPO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 需求实例分组转换器
 *
 * @author JiYinchuan
 * @since 7.250602.0
 */
@Mapper(
        componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = DemandInstanceTypeEnum.class
)
public interface DemandInstanceGroupConverter extends BaseConvert {

    // ------------------------- Do To PO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since 7.250602.0
     */
    @Mapping(target = "details", ignore = true)
    DemandInstanceGroupPO toPo(DemandInstanceGroup source);

    /**
     * 转换模型
     *
     * @param source 来源
     * @return {@code DemandInstanceGroupVO }
     * @since 7.250602.0
     */
    @Mapping(target = "relObjects", ignore = true)
    @Mapping(target = "belongInstance", ignore = true)
    DemandInstanceGroupVO toVO(DemandInstanceGroupPO source);

    /**
     * 转换模型
     *
     * @param source 来源
     * @return {@code DemandInstanceGroupVO }
     * @since 7.250602.0
     */
    @Mapping(target = "relObjects", ignore = true)
    @Mapping(target = "belongInstance", ignore = true)
    @Mapping(target = "details", ignore = true)
    DemandInstanceGroupVO toVO(DemandInstanceGroup source);

    /**
     * 转换模型List
     *
     * @param source 来源
     * @return {@code List<DemandInstanceGroupVO> }
     * @since 7.250602.0
     */
    List<DemandInstanceGroupVO> toVO(List<DemandInstanceGroupPO> source);

}
