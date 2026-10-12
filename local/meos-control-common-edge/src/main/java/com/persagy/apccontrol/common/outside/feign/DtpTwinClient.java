//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.persagy.apccontrol.common.outside.feign;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.QueryTwinDataDTO;
import com.persagy.apccontrol.basic.model.dto.twin.*;
import com.persagy.apccontrol.basic.model.vo.kitchen.KitChenRangeHoodEquipStatisticsVo;
import com.persagy.apccontrol.basic.outside.dto.lighting.LightingStatisticsResponse;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinManualSend;
import com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.common.outside.model.dto.MqttSubscribe;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 本地端 MQTT订阅服务
 *
 * @author shaohongbo
 * @date 2023/3/27 21:14
 */
@FeignClient(name = "dtp-twin-server-edge", url = "${inner-url.bdtp.dtp-twin.service:}")
@RefreshScope
public interface DtpTwinClient {

    // ------------------------- 孪生计算时间表 -------------------------

    /**
     * 注册MQTT 接口
     *
     * @param projectId         项目ID
     * @param groupCode
     * @param mqttSubscribeList
     * @return
     */
    @PostMapping({"/subscribe/rule/addComputeSubscribe"})
    DtpTwinR<Object> addComputeSubscribe(@RequestHeader String projectId, @RequestHeader String groupCode,
                                         @RequestBody List<MqttSubscribe> mqttSubscribeList);



    /**
     * 保存冷机间隔开启限制规则
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param paramList 参数列表 [{"objectId": "xxx", "paramCode": "equipStartInterval", "paramArray": [{"objectIds": [], "interval": 30}]}]
     * @return 响应结果
     * @since 7.260501.0
     */
    @PostMapping({"/twc/conf/saveStartIntervals"})
    DtpTwinR<Object> saveStartIntervals(@RequestHeader String groupCode, @RequestHeader String projectId, List<JSONObject> paramList);

    /**
     * 修订系统对象屏蔽状态
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param param     参数 {"objectId": "xxx", "revise": 1}
     * @return 响应结果
     * @since 7.260501.0
     */
    @PostMapping({"/twc/revise/reviseObjectInSys"})
    DtpTwinR<Object> reviseObjectInSys(@RequestHeader String groupCode, @RequestHeader String projectId, JSONObject param);





    /***
     * 切换设备锁定状态
     * @author shaohongbo
     * @date 2023/3/30 10:07
     * @param objectId 设备id
     * @param lock 0:为解锁设备，1:锁定设备
     * @return com.persagy.outside.feign.dto.DtpTwinR<java.lang.Object>
     */
    @PostMapping({"/twc/manual/switchEquipLock"})
    DtpTwinR<Object> switchEquipLock(@RequestHeader String projectId, @RequestHeader String groupCode, @RequestParam("objectId") String objectId, @RequestParam("lock") Integer lock);

    /***
     * 发送手动指令或者设置运行参数
     * @author shaohongbo
     * @date 2023/3/30 10:18
     * @param dtpTwinManualSend
     * @return com.persagy.outside.feign.dto.DtpTwinR<java.lang.Object>
     */
    @PostMapping({"/twc/manual/send"})
    DtpTwinR<Object> send(@RequestHeader String projectId, @RequestHeader String groupCode, @RequestHeader String userId, @RequestBody DtpTwinManualSend dtpTwinManualSend);


    /***
     * 查看当前控制量孪生
     * @author shaohongbo
     * @date 2023/3/30 10:18
     * @param queryTwinDataDTO
     * @return com.persagy.outside.feign.dto.DtpTwinR<java.lang.Object>
     */
    @PostMapping({"/data/queryTwinDataOnMoment"})
    DtpTwinR<DtpTwinIotData> queryTwinDataOnMoment(@RequestParam("groupCode") String groupCode,
                                                   @RequestParam("projectId") String projectId,
                                                   @RequestParam("userId") String userId,
                                                   @RequestBody QueryTwinDataDTO queryTwinDataDTO);


    /**
     * @Description: 查询近两小时孪生值
     * @Author: laixingqi
     * @Date: 2024/1/8 10:44
     * @param: [groupCode, projectId, userId, queryTwinDataDTO]
     * @return: com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinR<com.persagy.apccontrol.basic.outside.feign.model.dto.DtpTwinIotData>
     **/
    @PostMapping({"/data/queryTwinDataHistory"})
    DtpTwinR<DtpTwinIotData> queryTwinDataHistory(@RequestParam("groupCode") String groupCode,
                                                  @RequestParam("projectId") String projectId,
                                                  @RequestParam("userId") String userId,
                                                  @RequestBody QueryTwinDataDTO queryTwinDataDTO);

    /***
     * 自控参数下发接口
     */
    @PostMapping({"/twc/auto/send"})
    DtpTwinR<Object> autoSend(@RequestHeader String groupCode, @RequestHeader String projectId, @RequestHeader String userId, @RequestBody DtpTwinAutoSendDTO dtpTwinAutoSendDTO);

    /**
     * 自动控制指令批量下发
     *
     * @param params 下发参数
     * @return 响应结果
     * @since 5.240902.0
     */
    @PostMapping("/twc/auto/send/batch")
    CustomerResponse<Object> autoCommandBatchSend(@RequestBody List<DtpTwinAutoBatchSendDTO> params);


    /***
     * 自控参数下发接口 批量下发
     */
    @PostMapping({"/twc/auto/send/batch"})
    DtpTwinR<Object> autoSendBatch(@RequestHeader String groupCode, @RequestHeader String projectId, @RequestHeader String userId, @RequestBody List<DtpTwinAutoBatchSendDTO> dtpTwinAutoSendDTOList);

    /**
     * 保存壳配置参数--按对象ID进行保存
     *
     * @param groupCode
     * @param projectId
     * @param paramList
     * [
     *     {
     *         "objectId": "Sy61011220101538727783620608002",
     *         "paramCode": "ACCCCC_sort",
     *         "paramValue": "autoCtrlAccRunTime",
     *         "paramLabel": "累计运行时间"
     *     }
     * ]
     * @return
     */
    @PostMapping({"/twc/conf/saveList"})
    DtpTwinR<Object> saveList(@RequestHeader String groupCode, @RequestHeader String projectId, List<JSONObject> paramList);

    /**
     * 保存壳配置参数--按对象类进行保存
     *
     * @param groupCode
     * @param projectId
     * @param paramList
     * @return
     */
    @PostMapping({"/twc/conf/saveClassConfs"})
    DtpTwinR<Object> saveClassConfs(@RequestHeader String groupCode, @RequestHeader String projectId, List<JSONObject> paramList);

    /**
     * 删除壳配置参数--按对象类进行删除
     *
     * @param groupCode
     * @param projectId
     * @param paramList
     * @return
     */
    @PostMapping({"/twc/conf/deleteByClassConf"})
    DtpTwinR<Object> deleteByClassConf(@RequestHeader String groupCode, @RequestHeader String projectId, List<JSONObject> paramList);

    /**
     * 保存壳配置参数--不能同时开启的冷机
     *
     * @param groupCode
     * @param projectId
     * @param paramList [{
     *                  "objectId": "Sy99099900071567042985003003906",
     *                  "paramCode": "equipExclusions",
     *                  "paramLabel": "不能同时开启的冷机",
     *                  "paramArray": [
     *                  {
     *                  "objectIds": [
     *                  "Eq990999000740332c0e18e14ba18119eb10d3ada497",
     *                  "Eq61011220101d80d62569b647db8d3ea4e02a542ab3"
     *                  ],
     *                  "maxRuns": 1
     *                  }
     *                  ]
     *                  }]
     * @return
     */
    @PostMapping({"/twc/conf/saveExclusions"})
    DtpTwinR<Object> saveExclusions(@RequestHeader String groupCode, @RequestHeader String projectId, List<JSONObject> paramList);

    /**
     * 保存可配置参数
     *
     * @param groupCode
     * @param projectId
     * @param paramList [
     *                  {
     *                  "objectId": "Sy99099900071567042985003003906",
     *                  "paramCode": "equipExclusions"
     *                  }
     *                  ]
     * @return
     */
    @PostMapping({"/twc/conf/deleteByConf"})
    DtpTwinR<Object> deleteByConf(@RequestHeader String groupCode, @RequestHeader String projectId, List<JSONObject> paramList);

    /**
     * 保存冷机间隔开启限制规则
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param paramList 参数列表 [{"objectId": "xxx", "paramCode": "equipStartInterval", "paramArray": [{"objectIds": [], "interval": 30}]}]
     * @return 响应结果
     * @since 7.260501.0
     */

    /**
     * 重置系统下设备类信息点
     *
     * @param groupCode
     * @param projectId
     * @param param     {
     *                  "objectId": "Sy61011220101538727783620608002",
     *                  "classCode": "ACCCC",
     *                  "infoCode": "autoCtrlAccRunTime"
     *                  }
     * @return
     */
    @PostMapping({"/reset/resetEquipInfoCodeInSystem"})
    DtpTwinR<Object> resetEquipInfoCodeInSystem(@RequestHeader String groupCode, @RequestHeader String projectId, JSONObject param);

    /**
     * 重置控制组下设备类信息点
     *
     * @param groupCode
     * @param projectId
     * @param param     {
     *                  "objectId": "Eg990999000705c0ed1f032145b880459188ccda7a95",
     *                  "classCode": "ACCCCP",
     *                  "infoCode": "autoCtrlAccRunTime"
     *                  }
     * @return
     */
    @PostMapping({"/reset/resetEquipInfoCodeInGroup"})
    DtpTwinR<Object> resetEquipInfoCodeInGroup(@RequestHeader String groupCode, @RequestHeader String projectId, JSONObject param);

    /**
     * 通风设备状态统计查询
     *
     * @param queryParam
     * @param param
     */
    @PostMapping({"/state/statis/manage/fanner"})
    CustomerResponse<Map<String, KitChenRangeHoodEquipStatisticsVo>> ventilationEquipStatistics(@SpringQueryMap DtpRwdCommonParam queryParam, Map<String, Set<String>> param);

    /**
     * 油烟机设备状态统计查询
     *
     * @param queryParam
     * @param param
     */

    @PostMapping({"/state/statis/manage/acklsf"})
    CustomerResponse<Map<String, KitChenRangeHoodEquipStatisticsVo>> kitChenRangeHoodEquipStatistics(@SpringQueryMap DtpRwdCommonParam queryParam, Map<String, Set<String>> param);


    //-----------------------------------------值班首页聚合接口开始------------------------------------------------

    /**
     * 空调末端对象的状态统计
     */
    @PostMapping({"/home/watch/agg/terminal"})
    CustomerResponse<DutyHomePageGroupDTO> terminal(@RequestHeader String groupCode,
                                                    @RequestHeader String projectId, Map<String, ObjectIdAndForceCodeQuery> queryParam);

    /**
     * 照明对象的状态统计
     */
    @PostMapping({"/home/watch/agg/ltltcc"})
    CustomerResponse<DutyHomePageGroupDTO> ltltcc(@RequestHeader String groupCode,
                                                  @RequestHeader String projectId, Map<String, ObjectIdAndForceCodeQuery> queryParam);

    /**
     * 通风设备的状态统计
     */
    @PostMapping({"/home/watch/agg/acrex"})
    CustomerResponse<DutyHomePageGroupDTO> acrex(@RequestHeader String groupCode,
                                                 @RequestHeader String projectId, Map<String, ObjectIdAndForceCodeQuery> queryParam);

    /**
     * 厨房排油烟的状态统计
     */
    @PostMapping({"/home/watch/agg/acklsf"})
    CustomerResponse<DutyHomePageGroupDTO> acklsf(@RequestHeader String groupCode,
                                                  @RequestHeader String projectId, Map<String, ObjectIdAndForceCodeQuery> queryParam);

    /**
     * 冷热源状态统计
     */
    @PostMapping({"/home/watch/agg/coolHeatSys"})
    CustomerResponse<DutyHomePageGroupDTO> coolHeatSys(@RequestHeader String groupCode,
                                                       @RequestHeader String projectId, ObjectIdAndForceCodeQuery queryParam);

    //-----------------------------------------值班首页聚合接口结束------------------------------------------------

    /**
     * 空调末端对象的状态统计
     *
     * @param groupCode
     * @param projectId
     * @param queryParam 分组条件+ 分组设备 ID
     * @return Map key 分组条件 ,val 状态统计
     */
    @PostMapping({"/run/watch/agg/terminal"})
    CustomerResponse<Map<String, StatisManageTerminalDTO>> statisManageTerminal(@RequestHeader String groupCode,
                                                                   @RequestHeader String projectId, Map<String, ObjectIdAndForceCodeQuery> queryParam);

    /**
     * 照明Dashboard设备统计
     */
    @PostMapping({"/run/watch/agg/ltltcc"})
    CustomerResponse<Map<String, Map<String, LightingStatisticsResponse>>> runLtltcc(@RequestParam("groupCode") String groupCode,
                                                                                     @RequestParam("projectId") String projectId, Map<String, Map<String, Map<String, List<String>>>> queryParam);

}
