package com.persagy.apccontrol.basic.structure.event.converter;

import com.persagy.apccontrol.basic.convert.BaseConvert;
import com.persagy.apccontrol.basic.convert.Constant;
import com.persagy.apccontrol.basic.model.vo.security.SecuritySpaceStatusVO;
import com.persagy.apccontrol.basic.outside.dto.riskevent.ExceptionEventMsgDTO;
import com.persagy.apccontrol.basic.outside.feign.model.response.exceptionevent.ExceptionEventOriginalProblemResponse;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 异常事件参数转换
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 20:00
 * @since v5.241002.0
 */
@Mapper(componentModel = Constant.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        imports = DateUtils.class
)
public interface AbnormalEventConverter extends BaseConvert {

    /**
     * 将kafka推送的消息转换为通用异常事件
     *
     * @param source 源
     * @return {@link SecuritySpaceStatusVO }
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "localName", ignore = true)
    @Mapping(target = "objectType", ignore = true)
    @Mapping(target = "inSpaceIds", ignore = true)
    @Mapping(target = "exceptionEventId", source = "source.id")
    @Mapping(target = "instancesName", ignore = true)
    @Mapping(target = "problemsNumber", ignore = true)
    @Mapping(target = "problemsName", source = "source.originalProblem.nodeName")
    ExceptionEventBaseInfoVO msgDtoToVo(ExceptionEventMsgDTO source);

    @Mapping(target = "localName", ignore = true)
    @Mapping(target = "objectType", ignore = true)
    @Mapping(target = "inSpaceIds", ignore = true)
    @Mapping(target = "exceptionEndTime", ignore = true)
    @Mapping(target = "allProblemCount", ignore = true)
    @Mapping(target = "resolvedProblemCount", ignore = true)
    @Mapping(target = "instances", ignore = true)
    @Mapping(target = "problems", ignore = true)
    @Mapping(target = "riskMeasureAimLevel1Collect", ignore = true)
    @Mapping(target = "riskMeasureAimLevel2Collect", ignore = true)
    @Mapping(target = "originalProblem", ignore = true)
    @Mapping(target = "instancesName", ignore = true)
    @Mapping(target = "problemsNumber", ignore = true)
    @Mapping(target = "exceptionEventId", source = "source.exceptionEvent.id")
    @Mapping(target = "eventNumber", source = "source.exceptionEvent.eventNumber")
    @Mapping(target = "exceptionEventStatus", source = "source.exceptionEvent.exceptionEventStatus")
    @Mapping(target = "exceptionStartTime", source = "source.exceptionEvent.exceptionStartTime")
    @Mapping(target = "importanceDegree", source = "source.exceptionEvent.importanceDegree")
    @Mapping(target = "overdue", source = "source.exceptionEvent.overdue")
    @Mapping(target = "solveProcess", source = "source.exceptionEvent.solveProcess")
    @Mapping(target = "minRequireSolveTime", expression = "java(source.getExceptionEvent() != null ? this.toLocalDateTime(source.getExceptionEvent().getMinRequireSolveTime()) : null)")
    @Mapping(target = "maxRequireSolveTime", expression = "java(source.getExceptionEvent() != null ? this.toLocalDateTime(source.getExceptionEvent().getMaxRequireSolveTime()) : null)")
    @Mapping(target = "problemsName", source = "source.eeProblem.problemDesc")
    @Mapping(target = "duration", source = "source.exceptionEvent.duration")
    @Mapping(target = "id", source = "source.eeProblem.rwdInstanceId")
    @Mapping(target = "relatedRiskEvents", source = "source.eeProblem.riskEventList")
    ExceptionEventBaseInfoVO toVO(ExceptionEventOriginalProblemResponse source);

    List<ExceptionEventBaseInfoVO> toVO(List<ExceptionEventOriginalProblemResponse> source);

}
