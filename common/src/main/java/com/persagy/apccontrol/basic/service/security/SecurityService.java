package com.persagy.apccontrol.basic.service.security;

import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.SecurityDtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.AccessDemandStatusEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.CameraEnum;
import com.persagy.apccontrol.basic.model.enumtype.security.SecurityCameraTypeEnum;
import com.persagy.apccontrol.basic.model.param.security.SecurityDashboardQueryParam;
import com.persagy.apccontrol.basic.model.param.security.SecurityModelViewParam;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceRelObjVO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdSpaceVO;
import com.persagy.apccontrol.basic.model.vo.security.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.service.BizComponent;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandInstanceTypeEnum;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 安防空间
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2024/8/4 18:53
 * @since v5.240801.0
 */
public interface SecurityService extends BizComponent<SecurityService> {

    /**
     * 安防-监控摄像头-是否有布撤防功能 hasArmingFunc 字段也是静态信息点
     */
    String HAS_ARMING_FUNC_CODE = InfoCodeEnum.HAS_ARMING_FUNC.value();
    /**
     * 安防-监控摄像头-是否有布撤防功能 hasArmingFunc 字段也是静态信息点
     */
    String CAMERA_TYPE_CODE = InfoCodeEnum.CAMERA_TYPE.getCode();
    /**
     * 可监可控
     */
    String MONITORING_SURVEILLANCE_STATUS_CODE = InfoCodeEnum.MONITORING_SURVEILLANCE_STATUS.value();
    /**
     * 安防设备类门相关，并且带有部分过滤逻辑
     */
    ArrayList<DtpRwdClassCodeEnum> DOOR_EQUIPMENT_CLASS = Lists.newArrayList(
            DtpRwdClassCodeEnum.CFCSDR,
            DtpRwdClassCodeEnum.CFCSFR,
            DtpRwdClassCodeEnum.SPEGFG
    );
    /**
     * 安防设备类所有设备类，并且带有部分过滤逻辑
     */
    ArrayList<DtpRwdClassCodeEnum> SECURITY_EQUIPMENT_CLASS = Lists.newArrayList(
            DtpRwdClassCodeEnum.SPIACU,
            DtpRwdClassCodeEnum.SPVSCM,
            DtpRwdClassCodeEnum.WEGMCM,
            DtpRwdClassCodeEnum.SPVSVE,
            DtpRwdClassCodeEnum.SPVSDR,
            DtpRwdClassCodeEnum.SPIAGD,
            DtpRwdClassCodeEnum.SPIAID,
            DtpRwdClassCodeEnum.SPIAMA,
            DtpRwdClassCodeEnum.SPEGCU,
            DtpRwdClassCodeEnum.CFCSDR,
            DtpRwdClassCodeEnum.CFCSFR,
            DtpRwdClassCodeEnum.SPEGFG
    );
    /**
     * 安防设备类所有设备类，并且带有部分过滤逻辑
     */
    ArrayList<String> SECURITY_EQUIPMENT_CLASS_VALUE = Lists.newArrayList(
            DtpRwdClassCodeEnum.SPIACU.value(),
            DtpRwdClassCodeEnum.SPVSCM.value(),
            DtpRwdClassCodeEnum.WEGMCM.value(),
            DtpRwdClassCodeEnum.SPVSVE.value(),
            DtpRwdClassCodeEnum.SPVSDR.value(),
            DtpRwdClassCodeEnum.SPIAGD.value(),
            DtpRwdClassCodeEnum.SPIAID.value(),
            DtpRwdClassCodeEnum.SPIAMA.value(),
            DtpRwdClassCodeEnum.SPEGCU.value(),
            DtpRwdClassCodeEnum.CFCSDR.value(),
            DtpRwdClassCodeEnum.CFCSFR.value(),
            DtpRwdClassCodeEnum.SPEGFG.value()
    );
    /**
     * 安防一类小图标
     */
    ArrayList<String> FIRST_CLASS_SMALL_ICON_EQUIPMENT_CLASS = Lists.newArrayList(
            DtpRwdClassCodeEnum.SPVSCM.value(),
            DtpRwdClassCodeEnum.WEGMCM.value()
    );

    /**
     * 安防二类小图标设备类
     */
    ArrayList<String> SECOND_CLASS_SMALL_ICON_EQUIPMENT_CLASS = Lists.newArrayList(
            DtpRwdClassCodeEnum.SPIACU.value(),
            DtpRwdClassCodeEnum.SPIAGD.value(),
            DtpRwdClassCodeEnum.SPIAID.value(),
            DtpRwdClassCodeEnum.SPIAMA.value()
    );

    /**
     * SPVSCM摄像头类型
     */
    ArrayList<String> SPVSCM_CAMERA_TYPE = Lists.newArrayList(
            CameraEnum.GUN_CAMERA.value(),
            CameraEnum.BALL_CAMERA.value(),
            CameraEnum.HEMISPHERICAL_CAMERA.value()
    );

    /**
     * 摄像头设备类
     */
    List<String> CAMERA_CLASS_CODE_LIST = Lists.newArrayList(
            DtpRwdClassCodeEnum.WESEPC.value(),
            DtpRwdClassCodeEnum.SPVSCM.value(),
            DtpRwdClassCodeEnum.WEGMCM.value()
    );

    /**
     * 撤布防能力的摄像头定义
     */
    String HAS_ARMING_FUNC = "1";

    /**
     * 安防设备类门相关，并且带有部分过滤逻辑
     */
    ArrayList<String> DOOR_EQUIPMENT_CLASS_STRING = Lists.newArrayList(
            DtpRwdClassCodeEnum.CFCSDR.value(),
            DtpRwdClassCodeEnum.CFCSFR.value(),
            DtpRwdClassCodeEnum.SPEGFG.value()
    );

    ArrayList<String> MONITORING_SURVEILLANCE_STATUS_LIST = Lists.newArrayList("2", "3");
    // 常量提取（类级别）
    String SPVSCM_CODE = DtpRwdClassCodeEnum.SPVSCM.value();
    InfoCodeEnum ONLINE_STATUS = InfoCodeEnum.ONLINE_STATUS;
    Triple<String, String, SecurityCameraTypeEnum> DEFAULT_KEY =
            Triple.of("UNDEFINED", "UNDEFINED_CONFIG", SecurityCameraTypeEnum.UNKNOWN);


    /**
     * 查询Dashboard数据
     *
     * @param param 查询参数
     * @return Dashboard数据
     * @since 5.241201.0
     */
    SecurityDashboardVO dashboard(@NonNull @Valid SecurityDashboardQueryParam param);

    /**
     * 查询撤布防空间
     *
     * @return 安防的空间和设备信息
     */
    List<DtpRwdSpaceVO> listDeploymentSpace();

    /**
     * 查询通行空间
     *
     * @return 安防的空间和设备信息
     */
    List<DtpRwdSpaceRelObjVO> listAccessSpace();

    /**
     * 查询设备信息
     *
     * @return 设备信息
     */
    List<SecurityEquipmentStatusVO> listEquipment();

    /**
     * 查询空间安防信息
     *
     * @param viewParam 项目建筑楼层视角所在
     * @return 安防的空间和设备信息
     */
    SecurityModelViewVO querySecurityModelView(SecurityModelViewParam viewParam);

    /**
     * 获取当前视角下安防设备类所有设备类
     *
     * @param projectId              项目id
     * @param needRelation           是否需要关联关系
     * @param dtpRwdObjectQueryParam rwd入参指定字段，减少查询数量，对象类为全部已在接口中包含无需传递
     * @return rwd对象带有安防特有的判断字段
     */
    List<SecurityDtpRwdEquipmentDTO> getSecureAllEquipmentList(String projectId, Boolean needRelation,
                                                               DtpRwdObjectQueryParam dtpRwdObjectQueryParam);

    /**
     * 空间通行状态
     *
     * @param projectId           项目id
     * @param spaceIdList         空间id
     * @param dtpRwdEquipmentList 设备信息
     * @param equipmentAccessMap  设备通行状态仅用于模型图
     * @return 空间->通行状态枚举
     */
    Map<String, AccessDemandStatusEnum> mapSpaceAccessState(
            @NonNull String projectId,
            @NonNull List<String> spaceIdList,
            @Nullable List<SecurityDtpRwdEquipmentDTO> dtpRwdEquipmentList,
            @Nullable Map<String, AccessDemandStatusEnum> equipmentAccessMap);

    /**
     * 空间安防异常事件空间叠加一级风控目标，设备不进行叠加
     *
     * @param param 通风入参
     * @return 异常事件列表
     */
    List<ExceptionEventBaseInfoVO> getAbnormalDataList(SecurityModelViewParam param);

    /**
     * 布放空间统计
     *
     * @param param 安防不同视角入参
     * @return 布放空间统计
     */

    SecuritySpaceStatisticsVo getArmingSpaceStatistics(SecurityModelViewParam param);

    /**
     * 通行空间统计
     *
     * @param param 安防不同视角入参
     * @return 通行空间统计
     */

    SecurityAccessSpaceStatisticsVo getAccessSpaceStatistics(SecurityModelViewParam param);

    /**
     * 统计安防设别列表
     *
     * @param param 安防不同视角入参
     * @return 设备统计列表
     */

    List<SecurityEquipmentTypeStatisticsVo> getEquipmentTypeStatistics(SecurityModelViewParam param);
    /**
     * 获取需要推送孪生数据的对象集合
     * key 为对象类
     * value 为对象实例id集合
     *
     * @param projectId    项目ID
     * @param instanceId   实例ID
     * @param instanceType 实例类型
     * @return {@link Map<String,Set<String>>}
     * @author yuecaipu
     * @date 2024/8/12 10:46
     * @since v5.240801
     */
    Map<String, Set<String>> mapClaasCode2objectIds(String projectId, String instanceId, DemandInstanceTypeEnum instanceType);

    /**
     * 查询-安防设备实例数量统计(安防功能最低要求)
     *
     * @return 功能开关
     */
    SecurityFunctionVO querySecurityFunction();

}
