package com.persagy.apccontrol.basic.model.param.lighting;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Set;

/**
 * @Author 薛涛
 * @Description 
 * @Date 14:36 2024/5/16
 * @Param 
 * @return 
 **/
@Data
public class LightingControllerWithCircuitQueryParam {

    @NotBlank(message = "{key_79ae698d8ca34f22a0784247c6543796}")
    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "照明回路名称|编码")
    private String keyword;
    @Schema(description = "在线状态")
    private String onlineStatus;
    @Schema(description = "手自动状态")
    private String manualAutoStatus;
    @Schema(description = "运行状态")
    private String runStatus;

    @Schema(description = "运行需求类型")
    private String runningDemandType;
    @Schema(description = "运行偏差类型")
    private List<String> runningDeviationType;

    @Schema(description = "异常状态")
    private String abnormalStatus;
    @Schema(description = "是否有异常事件")
    private Boolean hasAbnormalEvent;

    @Schema(description = "控反不一致")
    private String orderFailAlarm;


    @Schema(description = "需求实例类型枚举")
    private DemandInstanceTypeEnum instanceType;
    @Schema(description = "关联ID")
    private String instanceId;
    @Schema(description = "服务空间功能类型, 仅回路列表使用")
    private Set<String> serviceRoomFuncTypes;
    @Schema(description = "所在空间功能类型, 仅控制模块列表使用")
    private Set<String> inRoomFuncTypes;

    @Schema(description = "关联功能类型")
    private List<String> funcType;
    @Schema(description = "关联灯具类型")
    private List<String> lampFuncType;
    @Schema(description = "模版类型")
    private DemandTemplateTypeEnum templateType;


    @Schema(description = "页码")
    private Integer page = 1;
    @Schema(description = "每页条数")
    private Integer pageSize = 10000;

}
