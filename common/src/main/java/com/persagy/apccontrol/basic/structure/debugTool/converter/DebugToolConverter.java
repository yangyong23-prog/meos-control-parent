package com.persagy.apccontrol.basic.structure.debugTool.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolInfoCodeConfigStatusVO;
import com.persagy.apccontrol.basic.model.vo.security.SecuritySpaceStatusVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 异常事件参数转换
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 20:00
 * @since v5.241002.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DebugToolConverter {

    /**
     * 将kafka推送的消息转换为通用异常事件
     *
     * @param source 源
     * @return {@link SecuritySpaceStatusVO }
     */
    @Mapping(target = "infoCode", ignore = true)
    @Mapping(target = "infoCodeName", ignore = true)
    @Mapping(target = "bindingPointStatus", ignore = true)
    @Mapping(target = "instantiationRuleStatus", ignore = true)
    @Mapping(target = "minuteDisplay", ignore = true)
    @Mapping(target = "showStyle", ignore = true)
    DebugToolInfoCodeConfigStatusVO msgDtoToVo(InfoCodeValueVO source);

}
