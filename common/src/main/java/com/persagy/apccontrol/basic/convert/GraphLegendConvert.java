package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphLegendVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 运维平台转换器
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GraphLegendConvert {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    GraphLegendVo clone(GraphLegendVo source);

}
