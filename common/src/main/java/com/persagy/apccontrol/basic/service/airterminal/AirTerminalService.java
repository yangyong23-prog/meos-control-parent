package com.persagy.apccontrol.basic.service.airterminal;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.context.airterminal.AirTerminalQualityStatisticsContext;
import com.persagy.apccontrol.basic.model.dto.SingleObjectDTO;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalEquipmentTagDTO;
import com.persagy.apccontrol.basic.model.dto.airteminal.AirTerminalSpaceTagDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalCarbonDioxideIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalInstanceTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.airterminal.AirTerminalTemperatureIntervalEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.param.airterminal.*;
import com.persagy.apccontrol.basic.model.param.demand.DemandSpaceByGroupFloorQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.SpaceInfoQueryParam;
import com.persagy.apccontrol.basic.model.param.demand.airconditioning.TagInfoQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.TagInfoVO;
import com.persagy.apccontrol.basic.model.vo.airterminal.*;
import com.persagy.apccontrol.basic.model.vo.airterminal.statistics.DashboardEquipmentVO;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningCO2QualityValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningTdbSeasonValueObject;
import com.persagy.apccontrol.basic.structure.demand.model.valueobject.airconditioning.DemandConfigAirconditioningValueObject;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;

/**
 * 空调末端服务
 *
 * @author JiYinchuan
 * @since 5.240502.0
 */
public interface AirTerminalService {

    /**
     * RPC查询数据分割条数
     */
    int RPC_QUERY_PARTITION_SIZE = 100;

    /**
     * 供冷季温度需求状态枚举
     */
    List<AirTerminalDemandStatusEnum> COOLING_TEMPERATURE_DEMAND_STATUS_ENUMS = Lists.newArrayList(
            AirTerminalDemandStatusEnum.COLD_STORAGE_VALUE,
            AirTerminalDemandStatusEnum.UPPER_LIMIT_VALUE,
            AirTerminalDemandStatusEnum.TARGET_VALUE);

    /**
     * 供暖季二氧化碳需求状态枚举
     */
    List<AirTerminalDemandStatusEnum> HEATING_CARBON_DIOXIDE_DEMAND_STATUS_ENUMS = Lists.newArrayList(
            AirTerminalDemandStatusEnum.HEAT_STORAGE_VALUE,
            AirTerminalDemandStatusEnum.LOWER_LIMIT_VALUE,
            AirTerminalDemandStatusEnum.TARGET_VALUE);

    /**
     * 供冷季二氧化碳需求状态枚举
     */
    List<AirTerminalDemandStatusEnum> DIOXIDE_DEMAND_STATUS_ENUMS = Lists.newArrayList(
            AirTerminalDemandStatusEnum.UPPER_LIMIT_VALUE, AirTerminalDemandStatusEnum.TARGET_VALUE);

    /**
     * 所有末端设备类编码 有顺序 注意
     */
    List<DtpRwdClassCodeEnum> ALL_EQUIPMENT_CLASS_CODES = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACATDH,
            DtpRwdClassCodeEnum.ACATAH,
            DtpRwdClassCodeEnum.ACATFC,
            DtpRwdClassCodeEnum.ACVRVI,
            DtpRwdClassCodeEnum.ACCCWP,
            DtpRwdClassCodeEnum.ACATFA,
            DtpRwdClassCodeEnum.ACATVA,
            DtpRwdClassCodeEnum.ACATFU,
            DtpRwdClassCodeEnum.ACATRA,
            DtpRwdClassCodeEnum.ACVTAC,
            DtpRwdClassCodeEnum.ACAHG_GROUP,
            DtpRwdClassCodeEnum.ACATHT,
            DtpRwdClassCodeEnum.ACVTSF,
            DtpRwdClassCodeEnum.ACVVER,
            DtpRwdClassCodeEnum.ACVVEA,
            DtpRwdClassCodeEnum.FFSCAF,
            DtpRwdClassCodeEnum.ACVVEJ);

    /**
     * 所有管道设备类编码
     */
    List<DtpRwdClassCodeEnum> ALL_PIPELINE_CLASS_CODES = Lists.newArrayList(
            DtpRwdClassCodeEnum.ACPPCS, DtpRwdClassCodeEnum.ACPPCR, DtpRwdClassCodeEnum.ACPPCB,
            DtpRwdClassCodeEnum.ACPPAS, DtpRwdClassCodeEnum.ACPPAR, DtpRwdClassCodeEnum.ACPPAB,
            DtpRwdClassCodeEnum.ACPPRE, DtpRwdClassCodeEnum.ACPPRA, DtpRwdClassCodeEnum.ACPPEA,
            DtpRwdClassCodeEnum.ACPPSA, DtpRwdClassCodeEnum.ACPPFS);


    /**
     * 类型
     */
    String[] ID_ADN_RELOBJIDS = new String[]{DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.relObjIds};

    /**
     * 查询品质区间
     *
     * @param param 查询参数
     * @return 品质区间
     * @since 5.240502.0
     */
    AirTerminalQualityIntervalVO getQualityInterval(@NonNull @Valid AirTerminalQualityIntervalQueryParam param);

    /**
     * 查询需求状态
     *
     * @param param 查询参数
     * @return 需求状态
     * @since 5.240502.0
     */
    AirTerminalDemandStatusVO getDemandStatus(@NonNull @Valid AirTerminalDemandStatusQueryParam param);

    /**
     * 查询室外天气
     *
     * @param param 查询参数
     * @return 室外天气
     * @since 5.240502.0
     */
    AirTerminalOutdoorWeatherVO getOutdoorWeather(@NonNull @Valid AirTerminalOutdoorWeatherQueryParam param);

    /**
     * 查询空间品质统计
     *
     * @param param 查询参数
     * @return 空间品质统计
     * @since 5.240502.0
     */
    AirTerminalSpaceQualityStatisticsVO getSpaceQualityStatistics(
            @NonNull @Valid AirTerminalSpaceQualityStatisticsQueryParam param);

    /**
     * 查询空间品质统计上下文
     *
     * @param param 查询参数
     * @return 空间品质统计上下文
     * @since 5.240601.0
     */
    AirTerminalQualityStatisticsContext getSpaceQualityStatisticsContext(
            @NonNull @Valid AirTerminalSpaceQualityStatisticsQueryParam param);

    /**
     * 查询设备类型列表
     *
     * @param param 查询参数
     * @return 设备类型列表
     * @since 5.240502.0
     */
    Set<AirTerminalEquipmentTypeVO> listEquipmentTypes(@NonNull @Valid AirTerminalEquipmentTypeQueryParam param);

    /**
     * 查询空间带状态
     *
     * @param param 查询参数
     * @return 空间带状态
     * @since 5.240502.0
     */
    AirTerminalSpaceWithStatusVO listSpaceWithStatus(@NonNull @Valid AirTerminalSpaceWithStatusQueryParam param);

    /**
     * 查询空间设备业务数据。
     *
     * <p>空间范围由已保存的壳配置选择白名单或项目全量；末端缓存就绪时读取新版缓存，
     * 未就绪时按相同范围实时查询物理世界。</p>
     *
     * @param param 查询参数
     * @return 空间设备数据；空间和设备均为空时返回 {@code null}
     * @since 7.250101.0
     */
    @Nullable AirTerminalSpaceWithStatusVO listSpaceEquipment(
            @NonNull @Valid AirTerminalSpaceWithStatusQueryParam param);

    /**
     * 查询新版缓存构建使用的项目全量末端空间和设备。
     *
     * <p>该入口始终实时查询物理世界，不读取末端业务缓存或白名单范围；结果只填充
     * {@code rwdSpaces} 和 {@code rwdEquipments}，供新版缓存写入使用。</p>
     *
     * @param param 项目级查询参数
     * @return 项目全量空间设备原始数据；空间和设备均为空时返回 {@code null}
     * @since v7.260701.0
     */
    @Nullable AirTerminalSpaceWithStatusVO listFullSpaceEquipmentForCacheBuild(
            @NonNull @Valid AirTerminalSpaceWithStatusQueryParam param);

    /**
     * 查询楼下下管道列表
     *
     * @param param 查询参数
     * @return 楼下下管道列表
     * @since 5.240502.0
     */
    List<DtpRwdObjectDTO> listPipelineByFloor(@NonNull @Valid AirTerminalPipelineByFloorQueryParam param);

    /**
     * 查询设备状态统计
     *
     * @param param 查询参数
     * @return 设备状态统计
     * @since 5.240502.0
     */
    List<AirTerminalFloorEquipmentTypeStatisticsVO> listEquipmentStatusStatistics(
            @NonNull @Valid AirTerminalEquipmentStatusStatisticsQueryParam param);

    /**
     * Dashboard设备统计显示
     *
     * @return 是否显示
     * @since 7.251101.0
     */
    boolean statisticDashboardEquipmentVisible();

    /**
     * 统计Dashboard设备
     *
     * @return 设备统计
     * @since 7.251101.0
     */
    List<DashboardEquipmentVO> statisticDashboardEquipment();

    /**
     * 查询空间ID和空间标签映射
     *
     * @param projectId      项目ID
     * @param filterSpaceIds 筛选空间ID, 为空时查询所有
     * @return 空间ID -> 空间标签
     * @since 5.240502.0
     */
    Map<String, AirTerminalSpaceTagDTO> mapSpaceIdAndTag(
            @NonNull String projectId, @Nullable Collection<String> filterSpaceIds);

    /**
     * 查询设备ID和设备标签映射
     *
     * @param projectId          项目ID
     * @param filterEquipmentIds 筛序设备ID, 为空时查询所有
     * @return 设备ID -> 设备标签
     * @since 5.240502.0
     */
    Map<String, AirTerminalEquipmentTagDTO> mapEquipmentIdAndTag(
            @NonNull String projectId, @Nullable Set<String> filterEquipmentIds);

    /**
     * 查询空间ID和空间温度值映射
     *
     * @param spaceIds 空间ID
     * @return 空间ID -> 空间温度值
     * @since 5.240502.0
     */
    Map<String, InfoCodeValueVO> mapSpaceIdAndTemperature(@Nullable Collection<String> spaceIds);

    /**
     * 查询空间ID和空间二氧化碳浓度值映射
     *
     * @param spaceIds 空间ID
     * @return 空间ID -> 空间二氧化碳浓度值
     * @since 5.240502.0
     */
    Map<String, InfoCodeValueVO> mapSpaceIdAndCarbonDioxide(@Nullable Collection<String> spaceIds);

    Object listEquipmentControllerDetails(SpaceInfoQueryParam param);

    List<ExceptionEventBaseInfoVO> getAbnormalDataList(AirTerminalAbnormalQueryParam instanceQueryParam);

    TagInfoVO listEquipmentTag(SpaceInfoQueryParam param);

    /**
     * 获取对象的标签
     *
     * @param tagInfoQueryParam 标签查询入参，空间或设备
     * @return 标签信息
     */
    TagInfoVO getObjectTag(TagInfoQueryParam tagInfoQueryParam);

    AirTerminalEquipManualAutoStatusVo queryManualAutoStatusByObjectId(SingleObjectDTO dto);

    Map<String, Set<String>> mapClaasCode2objectIds(String projectId, String instanceId, AirTerminalInstanceTypeEnum instanceType);

    /**
     * 历史回放
     *
     * @param groupCode
     * @param param
     * @return
     */
    TreeMap<LocalTime, AirTerminalHistoryPlaybackVO> historyPlaybackByTime(String groupCode, AirTerminalHistoryPlaybackParam param) throws Exception;

    AirTerminalSpaceTemperatureStatisticsVO listSpaceTemperatureStatistics(AirTerminalSpaceWithStatusQueryParam param);

    /**
     * 计算二氧化碳区间
     *
     * @param value      IOT值
     * @param co2Quality 二氧化碳品质配置
     * @return 二氧化碳区间
     * @since 5.241102.0
     */
    AirTerminalCarbonDioxideIntervalEnum calculateCarbonDioxideInterval(
            @NonNull BigDecimal value,
            @Nullable DemandConfigAirconditioningCO2QualityValueObject co2Quality);

    /**
     * 计算温度区间
     *
     * @param value            IOT值
     * @param tdbSeasonQuality 温度季节品质配置
     * @return 温度区间
     * @since 5.241102.0
     */
    AirTerminalTemperatureIntervalEnum calculateTemperatureInterval(
            @NonNull BigDecimal value,
            @Nullable DemandConfigAirconditioningTdbSeasonValueObject tdbSeasonQuality);

    /**
     * 查询项目全局需求配置
     *
     * @return 项目全局需求配置
     * @since 5.241102.0
     */
    @Nullable
    DemandConfigAirconditioningValueObject queryDemandProjectGlobalConfig();

    /**
     * 查询弹框继续加载
     *
     * @return 是否继续加载弹框
     */
    Boolean getDialogContinueLoad(DemandSpaceByGroupFloorQueryParam param);

}
