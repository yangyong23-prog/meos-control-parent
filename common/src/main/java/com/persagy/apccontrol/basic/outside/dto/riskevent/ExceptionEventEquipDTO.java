package com.persagy.apccontrol.basic.outside.dto.riskevent;

import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdTagEnum;
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.IotInfoCodeFieldVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.MapEquipVo;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 类功能描述
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/17 14:25
 */
@Getter
@Setter
@ToString
public class ExceptionEventEquipDTO {

    @Schema(description = "对象ID")
    private String id;

    @Schema(description = "设备本地名称")
    private String localName;

    @Schema(description = "Iot数据")
    private List<IotInfoCodeFieldVo> riskEventIotFieldVos;

    @Schema(description = "当前位置")
    private String location;

    @Schema(description = "巡检信息")
    private TaskInfoDTO taskInfo;

    @ApiModelProperty("异常事件信息")
    private List<ExceptionEventBaseInfoVO> exceptionEventInfoList;

    @ApiModelProperty("标签信息")
    private TagInfoVO tagInfo;

    public static ExceptionEventEquipDTO of(DtpRwdObjectDTO rwdEquipment, List<ExceptionEventBaseInfoVO> exceptionEventObjVos,
                                            List<IotInfoCodeFieldVo> iotInfoCodeFieldVos, TaskInfoDTO task) {
        ExceptionEventEquipDTO vo = new ExceptionEventEquipDTO();
        MapEquipVo mapEquipVo = MapEquipVo.of(rwdEquipment);
        BeanUtils.copyProperties(mapEquipVo, vo);
        // 假设iotInfoCodeFieldVos和rwdEquipment已经初始化
        Set<String> techAndBasicParams = Sets.newHashSet(DtpRwdTagEnum.TECH_PARAM.value(), DtpRwdTagEnum.BASIC_PARAMETER.value());

        if (CollectionUtils.isNotEmpty(iotInfoCodeFieldVos)) {
            iotInfoCodeFieldVos.removeIf(x -> {
                if (techAndBasicParams.contains(x.getInfoType())) {
                    Map<String, Object> undefinedProperties = rwdEquipment.getUndefinedProperties();
                    String code = x.getCode();
                    if (DtpRwdObjectDTO.Fields.localName.equals(code)) {
                        x.setDataValue(rwdEquipment.getLocalName());
                    } else if (undefinedProperties.containsKey(code)) {
                        x.setDataValue(String.valueOf(undefinedProperties.get(code)));
                    }
                    return StringUtils.isBlank(x.getDataValue());
                }
                return false;
            });
        }

        vo.setRiskEventIotFieldVos(iotInfoCodeFieldVos);
        vo.setExceptionEventInfoList(exceptionEventObjVos);
        if (Objects.nonNull(task)) {
            vo.setTaskInfo(task);
        }
        return vo;
    }

    public static ExceptionEventEquipDTO of(DtpRwdObjectDTO rwdEquipment, List<ExceptionEventBaseInfoVO> exceptionEventObjVos,
                                            List<IotInfoCodeFieldVo> iotInfoCodeFieldVos) {
        ExceptionEventEquipDTO vo = new ExceptionEventEquipDTO();
        MapEquipVo mapEquipVo = MapEquipVo.of(rwdEquipment);
        BeanUtils.copyProperties(mapEquipVo, vo);

        vo.setRiskEventIotFieldVos(iotInfoCodeFieldVos);
        vo.setExceptionEventInfoList(exceptionEventObjVos);
        return vo;
    }

}
