package com.persagy.apccontrol.common.service.lighting;

import com.persagy.apccontrol.basic.model.dto.lighting.LightingLampFuncTypeTagDTO;
import com.persagy.apccontrol.basic.model.dto.lighting.LightingSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdCircuitDTO;
import com.persagy.apccontrol.basic.model.param.lighting.LightingControllerWithCircuitQueryParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingInstanceQueryParam;
import com.persagy.apccontrol.basic.model.param.lighting.LightingOperatingParametersSetParam;
import com.persagy.apccontrol.basic.model.vo.demand.template.DemandTemplateVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingCircuitTagVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingControllerTagVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarDeviceStateVO;
import com.persagy.apccontrol.basic.model.vo.lighting.LightingSidebarLightLoopTotalVO;
import com.persagy.apccontrol.basic.model.vo.rules.SetOperatingParametersVO;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingDataDto;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.common.model.dto.RunningModeLightingDTO;
import com.persagy.framework.common.entity.R;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LightingService {

    /**
     * 侧边栏-当前设备状态
     *
     * @param instanceQueryParam 照明-实例查询参数
     * @return 当前设备状态（控制-离线，照明-自动、手动、控返、离线）
     */
    LightingSidebarDeviceStateVO countEquipmentStatus(LightingInstanceQueryParam instanceQueryParam);

    /**
     * 回路、控制模块对象统计
     *
     * @param projectId    项目ID
     * @param objectIdList 回路、控制模块对象集合
     * @return {@link LightingSidebarDeviceStateVO}
     * @author yuecaipu
     * @date 2024/8/31 15:48
     * @since v5.240802
     */
    LightingSidebarDeviceStateVO countEquipmentStatus(String projectId, List<String> objectIdList);

    List<DtpRwdCircuitDTO> listLightLoopByInstance(LightingInstanceQueryParam lightingInstanceQueryParam);

    /**
     * 侧边栏-当前需求满足情况
     *
     * @param instanceQueryParam 实例类型、id和需求模板
     * @return 当前需求满足情况
     */
    List<LightingSidebarLightLoopTotalVO> listLightLoopTotal(LightingInstanceQueryParam instanceQueryParam);

    /**
     * 查询当前运行模式
     *
     * @param templateType 模版类型
     * @return 当前需求模板
     */
    DemandTemplateVO queryCurrentDemandTemplate(DemandTemplateTypeEnum templateType);

    /**
     * 查询空间标签
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param spaceIds     空间ID
     * @return 空间标签
     * @since 5.240402.0
     */
    List<LightingSpaceTagDTO> listSpaceTags(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> spaceIds);

    /**
     * 查询灯具功能类型标签
     *
     * @param projectId     项目ID
     * @param templateType  模版类型
     * @param lampFuncTypes 灯具功能类型
     * @return 灯具功能类型标签
     * @since 5.240501.0
     */
    List<LightingLampFuncTypeTagDTO> listLampFuncTypeTags(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> lampFuncTypes);

    /**
     * 查询空间ID和空间标签映射
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param spaceIds     空间ID
     * @return 空间ID -> 空间标签
     * @since 5.240402.0
     */
    Map<String, LightingSpaceTagDTO> mapSpaceIdAndTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> spaceIds);

    /**
     * 查询灯具功能类型和标签映射
     *
     * @param projectId     项目ID
     * @param templateType  模版类型
     * @param lampFuncTypes 灯具功能类型
     * @return 空间ID -> 灯具功能类型标签
     * @since 5.240501.0
     */
    Map<String, LightingLampFuncTypeTagDTO> mapLampFuncTypeAndTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Collection<String> lampFuncTypes);

    /**
     * 查询照明回路ID和照明回路标签映射
     *
     * @param projectId    项目ID
     * @param templateType 模版类型
     * @param circuitIds   照明回路ID
     * @return 照明回路ID -> 照明回路标签
     * @since 5.240402.0
     */
    Map<String, LightingCircuitTagVO> mapCircuitIdAndTag(
            @NonNull String projectId,
            @NonNull DemandTemplateTypeEnum templateType,
            @NonNull Set<String> circuitIds);

    /**
     * 查询照明控制模块标签
     *
     * @param projectId     项目ID
     * @param controllerIds 照明控制模块ID, 为空时查询所有
     * @return 空间标签
     * @since 5.240402.0
     */
    Map<String, LightingControllerTagVO> mapControllerIdAndTag(
            @NonNull String projectId, @NonNull Collection<String> controllerIds);

    List<ExceptionEventBaseInfoVO> getAbnormalDataList(LightingInstanceQueryParam lightingInstanceQueryParam);

    /**
     * 设置操作参数
     *
     * @param param 设置参数
     * @return 是否成功
     * @since 5.240402.0
     */
    R<Boolean> setOperatingParameters(@NonNull @Valid LightingOperatingParametersSetParam param);

    LightingDataDto pageCircuitDetails(LightingControllerWithCircuitQueryParam param);

    LightingDataDto pageControllerDetails(LightingControllerWithCircuitQueryParam param);

    SetOperatingParametersVO setOperatingParametersReturnInfo(LightingOperatingParametersSetParam param);

    SetOperatingParametersVO changeManualAutoStatusAndEquipSwitchSetByObjectIds(RunningModeLightingDTO param);
    /**
     * 获取需要推送孪生数据的对象集合
     * key 为对象类
     * value 为对象实例id集合
     *
     * @param projectId    项目ID
     * @param demandTemplateType    需求模板类型
     * @param instanceId   实例ID
     * @param instanceType 实例类型
     * @return {@link Map<String,Set<String>>}
     * @author yuecaipu
     * @date 2024/8/22
     * @since v5.240802
     */
    Map<String, Set<String>> mapClaasCode2objectIds(String projectId, DemandTemplateTypeEnum demandTemplateType, String instanceId, DemandInstanceTypeEnum instanceType);
}
