package com.persagy.apccontrol.basic.structure.system;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphEditorInfoCodeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 系统图转换器
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/6/27 15:57
 * @since v5.240502.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface SystemGraphConverter extends BaseConvert {

    // ------------------------- To Vo -------------------------

    /**
     * 转换模型
     *
     * @param source 数据源
     * @return 目标源
     * @since 5.240602.0
     */
    @Mapping(target = "ipValue", ignore = true)
    SystemGraphEditorInfoCodeVO toSystemGraphVo(RcInfoCodeCfg source);

}