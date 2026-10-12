package com.persagy.apccontrol.common.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.PointInfoDTO;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.entity.ForceInfoCodeInfo;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.common.model.dto.ExecSend;
import com.persagy.apccontrol.common.model.dto.RunningModeDTO;
import com.persagy.apccontrol.basic.outside.feign.model.param.dtpmeos.TwcInstructionSend;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 运行监测
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 15:29
 */
public interface RunningMonitorService {

    TreeMap<String, String> getManualAutoChangeReason(String groupCode, String projectId, String systemId);

    /**
     * 手动进行指令下发
     *
     * @param dto
     * @param logFlag 是否下发日志，避免重复
     * @return
     */
    Boolean setOperatingParameters(TwcInstructionSend dto, boolean logFlag);

    /**
     * 手自动动进行指令下发
     *
     * @param projectId    项目ID
     * @param systemId     系统ID
     * @param objectId     下发对象ID
     * @param businessFeildDTO   下发对象信息，来源于 getObjectBusinessFeildAndSystemInfo()
     * @param pointInfoDTO 下发信息点信息
     * @param logFlag 是否下发日志，避免重复
     * @author yuecaipu
     * @date 2024/8/6 17:49
     * @since v5.240801
     */
    void sendInstruction(String projectId, String systemId, String objectId, RwdObjectBusinessFeildDTO businessFeildDTO, PointInfoDTO pointInfoDTO, boolean logFlag);
    
    /**
     * 解除设定点位的人工干预信息
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @param infoCode
     * @return
     */
    String releaseIntervention(String groupCode, String projectId, Set<String> objectIds, String infoCode);

    /**
     * 获取一键手自动切换，需要的设备类及其设备
     *
     * @param systemGraphId
     * @param projectId
     * @param systemId
     * @return
     */
    List<JSONObject> queryManualAutoClassList(Long systemGraphId, String projectId, String systemId);

    /**
     * 查询手自动状态
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @return
     */
    JSONObject queryManualAutoStatusByObjectId(String groupCode, String projectId, String objectId);

    /**
     * 获取冷却塔控制单元下的所有冷却塔风机、电动通断水阀
     * @param groupCode
     * @param projectId
     * @param coolingTowerId 冷却塔控制单元对象ID
     * @return
     */
    List<JSONObject> queryFanAndWaterValveListByCoolingTowerId(String groupCode, String projectId, String systemId, String coolingTowerId);

    /**
     * 改变联动、单点控制状态位
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @param objectId
     * @param infoCode
     * @param model 0-联动控制，1-单点控制
     * @return 切换后的设备信息点集合信息
     */
    List<RcInfoCodeCfg> changeLinkForceControlStatus(String groupCode, String projectId, String systemId, String objectId, String infoCode, String model);

    /**
     * @description: 获取绑定规则
     * @author zyc
     * @param objectList 设备对象List
     * @param projectId 项目ID
     * @date 2023/12/5 17:45
     * @version 1.0
     */
    Map<String, Set<String>> getBindRule(String groupCode, String projectId, List<JSONObject> objectList);


    /**
     * 根据半剖图配置，返回设备对应的信息点配置数据
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @return
     */
    List<JSONObject> queryClassPositionCodeConfigs(String groupCode, String projectId, String objectId);

    /**
     * 获取可用的强控信息
     *
     * @param groupCode
     * @param masterObjectId
     * @param masterClassCode
     * @param forceInfoCodeInfo
     * @return
     */
    JSONArray getAvailableForceInfo(String groupCode, String projectId, String masterObjectId, String masterClassCode, List<ForceInfoCodeInfo> forceInfoCodeInfo);

    ExecSend changeManualAutoStatusByObjectIds(String groupCode, RunningModeDTO dto, boolean b);


    /**
     * 获取累计运行时间的配置
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @param systemClassCode
     * @return
     */
    List<JSONObject> listAccRuntimeClass(String groupCode, String projectId, String systemId, String systemClassCode);

    /**
     * 清空累计运行时间的配置
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param classCode
     * @return
     */
    void clearAccRuntimeClass(String groupCode, String projectId, String objectId, String classCode);

}
