package com.persagy.apccontrol.basic.outside.feign.service;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoBatchSendDTO;
import com.persagy.apccontrol.basic.model.dto.twin.DtpTwinAutoSendDTO;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import org.springframework.lang.NonNull;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description: 调用物理世界
 * @author: yancheng
 * @date: 2023/4/1
 * @version: V1.0
 */
public interface DtpTwinClientService {

    // ------------------------- 孪生计算时间表 -------------------------

    /**
     * 查看当前控制量孪生
     *
     * @param groupCode        集团编码
     * @param projectId        项目id
     * @param userId           用户id
     * @param queryTwinDataDTO 查询dto
     * @return
     */
    List<DtpTwinIotData> queryTwinDataOnMoment(String groupCode,
                                               String projectId,
                                               String userId,
                                               QueryTwinDataDTO queryTwinDataDTO);

    void manualSend(DtpTwinManualSend dtpTwinManualSend);

    /**
     * 发送自动需求指令
     */
    void autoSend(DtpTwinAutoSendDTO dtpTwinAutoSendDTO);

    /**
     * 自动需求指令批量下发
     *
     * @param params 下发参数
     * @since 5.240902.0
     */
    void autoCommandBatchSend(@NonNull List<DtpTwinAutoBatchSendDTO> params);

    /**
     * opt B8：与 {@link #autoCommandBatchSend} 相同，但返回是否下发成功（调用失败或 Twin 返回失败码为 false），
     * 供照明“相同指令不重发”在失败时下一轮重发。默认实现无法感知失败，恒为 true。
     */
    default boolean autoCommandBatchSendChecked(@NonNull List<DtpTwinAutoBatchSendDTO> params) {
        autoCommandBatchSend(params);
        return true;
    }

    /**
     * 发送自动需求指令 批量下发
     */
    void autoSendBatch(List<DtpTwinAutoSendDTO> dtpTwinAutoSendDTOList);

    /**
     * 通风设备状态统计查询
     *
     * @param param 查询参数
     * @since 7.2507.1.0
     */
    Map<String, KitChenRangeHoodEquipStatisticsVo> ventilationEquipStatistics(@NonNull Map<String, Set<String>> param);

    /**
     * 切换设备锁定状态
     *
     * @param projectId
     * @param groupCode
     * @param objectId
     * @param lock
     * @author shaohongbo
     * @date 2023/4/10 14:22
     */
    void switchEquipLock(String projectId, String groupCode, String objectId, Integer lock);

    /**
     * 指令下发，下发的值，受信息点配置的影响
     *
     * @param objectId
     * @param infoCode
     * @param iotValue
     * @param infoCodeCfg
     * @return {@link String} 实际设置的值 iotValue
     */
    String setOperatingParameters(String objectId, String infoCode, String iotValue, RcInfoCodeCfg infoCodeCfg);

    /**
     * 按对象ID保存壳配置参数
     *
     * @param paramList
     */
    void saveList(List<JSONObject> paramList);

    /**
     * 按对象类保存壳配置参数
     *
     * @param paramList
     */
    void saveClassConfs(List<JSONObject> paramList);

    /**
     * 删除壳配置参数
     *
     * @param paramList
     */
    void deleteByClassConf(List<JSONObject> paramList);

    /**
     * 保存壳配置参数
     *
     * @param paramList
     */
    void saveExclusions(List<JSONObject> paramList);

    /**
     * 保存冷机间隔开启限制规则
     *
     * @param paramList 参数列表
     * @since 7.260501.0
     */
    void saveStartIntervals(List<JSONObject> paramList);


    /**
     * 修订系统对象屏蔽状态
     *
     * @param param 参数 {objectId, revise}
     * @since 7.260501.0
     */
    void reviseObjectInSys(JSONObject param);



    /**
     * 删除壳配置参数
     *
     * @param paramList
     */
    void deleteByConf(List<JSONObject> paramList);


    /**
     * 重置系统下设备类信息点
     *
     * @param objectId
     * @param classCode
     * @param infoCode
     */
    void resetEquipInfoCodeInSystem(String objectId, String classCode, String infoCode);

    /**
     * 重置控制组下设备类信息点
     *
     * @param objectId
     * @param classCode
     * @param infoCode
     */
    void resetEquipInfoCodeInGroup(String objectId, String classCode, String infoCode);

    /**
     * @param request
     * @return java.util.List<com.persagy.apccontrol.common.outside.model.dto.DtpTwinIotData>
     * @author wxf
     * @description 统一处理孪生数据给前端
     * @date 14:47 2024/7/24
     **/
    List<DtpTwinIotData> getTwinDataOnMoment(HttpServletRequest request);

    /**
     * @param request
     * @author wxf
     * @description 清理统一缓存的孪生数据
     * @date 15:35 2024/7/24
     **/
    void removeTwinDataOnMoment(HttpServletRequest request);


}
