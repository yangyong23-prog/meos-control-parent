package com.persagy.apccontrol.basic.structure.event.converter;

import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.vo.dangerevent.DangerEventVO;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerEventInstance;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.DangerInstanceTreeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * 风险事件参数转换
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/02/14 16:00
 * @since 7.250201.0
 */
@Mapper(componentModel = Constant.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DangerEventConverter {


    @Mapping(target = "symptomaticValueLevel", ignore = true)
    @Mapping(target = "strategyCoverRatio", ignore = true)
    @Mapping(target = "ordinary", ignore = true)
    @Mapping(target = "monitoringSituations", ignore = true)
    @Mapping(target = "localName", ignore = true)
    @Mapping(target = "exceptionValueLevel", ignore = true)
    @Mapping(target = "dangerEventId", source = "id")
    @Mapping(target = "objectId", source = "rwdInstanceId")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "currentStatus")
    @Mapping(target = "updateTime", source = "statusUpdateTime")
    DangerEventVO toDangerEventVO(DangerEventInstance source);

    @Mapping(target = "dangerEventId", source = "source.id")
    @Mapping(target = "objectId", source = "source.rwdInstanceId")
    @Mapping(target = "status", source = "source.currentStatus")
    @Mapping(target = "name", source = "source.name")
    @Mapping(target = "updateTime", source = "source.statusUpdateTime")
    @Mapping(target = "strategyCoverRatio", source = "tree.strategyCoverRatio")
    @Mapping(target = "localName", ignore = true)
    @Mapping(target = "monitoringSituations", source = "tree.monitoringSituations")
    @Mapping(target = "symptomaticValueLevel", source = "tree.symptomaticValueLevel")
    @Mapping(target = "exceptionValueLevel", source = "tree.exceptionValueLevel")
    @Mapping(target = "followMark", source = "tree.followMark")
    @Mapping(target = "ordinary", source = "tree.ordinary")
    DangerEventVO toDangerEventVO(DangerEventInstance source, DangerInstanceTreeResponse tree);

}
