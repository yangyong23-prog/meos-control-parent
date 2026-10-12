package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphIconVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 运维平台转换器
 *
 * @author JiYinchuan
 * @since v5.7.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface GraphIconConvert {

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 模型
     * @since v6.0.0
     */
    GraphIconVo clone(GraphIconVo source);

    /**
     * 列表克隆
     *
     * @param source 来源
     * @return {@link List }<{@link GraphIconVo }>
     */
    List<GraphIconVo> listClone(List<GraphIconVo> source);

}
