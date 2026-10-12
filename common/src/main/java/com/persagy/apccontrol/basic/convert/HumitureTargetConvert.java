package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.enumtype.HumidityUpperTypeEnum;
import com.persagy.apccontrol.basic.model.vo.HumidityUpperTypeVO;
import org.mapstruct.*;

import java.util.List;

/**
 * 温湿度指标转换器
 *
 * @author JiYinchuan
 * @since v5.1.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface HumitureTargetConvert {

    // ------------------------- To VO -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @Mappings({
            @Mapping(target = "key", source = "value"),
            @Mapping(target = "value", source = "reasonPhrase"),
            @Mapping(target = "upperNumber", source = "upperNumber")
    })
    HumidityUpperTypeVO toVo(HumidityUpperTypeEnum source);

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v5.1.0
     */
    @InheritConfiguration
    List<HumidityUpperTypeVO> toVo(HumidityUpperTypeEnum[] source);

    // ------------------------- To DTO -------------------------

    // ------------------------- To Context -------------------------

}
