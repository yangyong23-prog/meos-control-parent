package com.persagy.apccontrol.basic.service.lighting;

import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdSpaceDTO;
import com.persagy.apccontrol.basic.model.param.dashboard.DashBoardQueryCountParam;
import com.persagy.apccontrol.basic.model.param.lighting.*;
import com.persagy.apccontrol.basic.model.vo.ProjectVO;
import com.persagy.apccontrol.basic.model.vo.dashboard.LightingCountDataVo;
import com.persagy.apccontrol.basic.model.vo.lighting.*;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.demand.strategy.lighting.DemandLightingStrategy;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 照明服务
 *
 * @author JiYinchuan
 * @since 5.240402.0
 */
public interface LightingService {

    /**
     * 物理世界是否查询缓存
     */
    boolean RWD_QUERY_CACHE = DemandLightingStrategy.RWD_QUERY_CACHE;

    /**
     * 查询需求目标
     *
     * @param param 需求目标查询参数
     * @return 查询+需求目标
     * @since 7.260101.0
     */
    LihgtingDemandTargetVO getDemandTarget(@Valid LightingDemandTargetQueryParam param);

    /**
     * 查询照明回路信息点
     *
     * @param param 查询参数
     * @return 照明回路信息点
     * @since 5.240402.0
     */
    LightingCircuitInfoCodeVO getCircuitInfoCode(@NonNull @Valid LightingCircuitInfoCodeQueryParam param);

    /**
     * 查询-需求变化
     *
     * @param instanceQueryParam 实例类型、id和需求模板
     * @return 需求变化信息
     */
    LightingSidebarDemandChangeVO getDemandChange(LightingInstanceQueryParam instanceQueryParam);

    Map<String, Map<String, Map<String, Long>>> groupByInfoValueResultMap(String projectId, Collection<String> objectIdList, List<String> infoCodeList);

    List<LightingSidebarLightLoopTotalVO> listLightLoopTotal(LightingInstanceQueryParam lightingInstanceQueryParam);

    /**
     * 根据查询参数获取手自动控制列表
     *
     * @param lightingInstanceQueryParam 照明实例查询参数对象
     * @return 照明设备侧边栏手自动控制列表
     */
    List<LightingSidebarManualAutomaticControlVO> listManualAutomaticControl(LightingInstanceQueryParam lightingInstanceQueryParam);

    /**
     * 侧边栏-当前设备状态
     *
     * @param instanceQueryParam 照明-实例查询参数
     * @return 当前设备状态（控制-离线，照明-自动、手动、控返、离线）
     */
    LightingSidebarDeviceStateVO countEquipmentStatus(LightingInstanceQueryParam instanceQueryParam);

    /**
     * 查询异常事件列表
     *
     * @param lightingInstanceQueryParam param
     * @return {@link List<ExceptionEventBaseInfoVO>}
     */
    List<ExceptionEventBaseInfoVO> getAbnormalDataList(LightingInstanceQueryParam lightingInstanceQueryParam);

    /**
     * 查询空间关联的飞行线
     *
     * @param lightingInstanceQueryParam 实例类型、id和需求模板
     * @return 飞行线对象信息
     */
    List<DtpRwdSpaceDTO> getLineOfFlight(LightingInstanceQueryParam lightingInstanceQueryParam);

    /**
     * hover空间查照明回路和照明控制模块的异常数据
     *
     * @param lightingHoverQueryParam 实例类型和id
     * @return 异常数据
     */
    LightingHoverSpaceAbnormalDataVO getSpaceOrLampFuncHoverInfo(LightingHoverQueryParam lightingHoverQueryParam);

    /**
     * 查询带状态的空间信息
     *
     * @param param 查询参数
     * @return 带状态的空间信息
     * @since 5.240402.0
     */
    Collection<LightingSpaceWithStatusVO> listSpaceWithStatus(LightingSpaceWithStatusQueryParam param);

    /**
     * 查询带状态的灯具功能类型
     *
     * @param param 查询参数
     * @return 带状态的灯具功能类型
     * @since 5.240501.0
     */
    LightingNightWithStatusVO getLampFuncTypeWithStatus(
            @NonNull @Valid LightingLampFuncTypeWithStatusQueryParam param);


    /**
     * 侧边栏-照度监测数据
     *
     * @param projectInfo 项目信息
     * @return 项目室外照度值和设备照度值
     */
    LightingSidebarIllumInfoVO getIllumInfo(ProjectVO projectInfo);

    /**
     * 查询照明控制模块详情
     *
     * @param param 查询参数
     * @return 照明控制模块详情
     * @since 5.240402.0
     */
    LightingControllerDetailVO getControllerDetail(@NonNull @Valid LightingControllerDetailQueryParam param);

    /**
     * 查询空间详情
     *
     * @param param 查询参数
     * @return 空间详情
     * @since 5.240402.0
     */
    LightingSpaceDetailVO getSpaceDetail(@NonNull @Valid LightingSpaceDetailQueryParam param);


    /**
     * dashboard-查询统计数据
     *
     * @param dashBoardQueryCountParam 参数
     * @return {@link LightingCountDataVo}
     * @author yuecaipu
     * @date 2024/12/6 14:28
     * @since v5.241201
     */
    LightingCountDataVo dashboard(@Valid DashBoardQueryCountParam dashBoardQueryCountParam);

}
