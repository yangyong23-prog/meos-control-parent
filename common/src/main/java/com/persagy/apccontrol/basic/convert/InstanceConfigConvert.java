package com.persagy.apccontrol.basic.convert;

import com.persagy.apccontrol.basic.model.entity.instance.config.InstanceConfig;
import com.persagy.apccontrol.basic.model.vo.instance.InstanceConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface InstanceConfigConvert {

    /**
     * 转换模型
     *
     * @param source 来源
     * @return {@code InstanceConfigVO }
     * @since 7.250601.0
     */
    InstanceConfigVO toVO(InstanceConfig source);

    /**
     * 列表转换模型
     *
     * @param sources 来源
     * @return {@code List<InstanceConfigVO> }
     * @since 7.250601.0
     */
    List<InstanceConfigVO> toListVO(List<InstanceConfig> sources);
}
