package com.persagy.apccontrol.basic.outside.feign.client;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.persagy.apccontrol.basic.model.dto.rwd.BaseQueryParamDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFractionalPrecisionRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdQueryPointListDTO;
import com.persagy.apccontrol.basic.outside.dto.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.*;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCommonParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.common.DtpRwdCriteriaQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.kitchen.KitchenExhaustResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.*;
import com.persagy.apccontrol.basic.structure.control.enumeration.ChillerTypeSub;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuyao
 * 物理世界 查询关系对象 获取动态数据
 */
@FeignClient(name = "dtp-rwd-server",
        url = "${inner-url.bdtp.dtp-rwd.service:}"
        // url = "https://qa.ysbdtp.com/dtp-rwd-server"
)
@RefreshScope
@Validated
@Primary
public interface DtpRwdClient {

    // ------------------------- 对象查询 -------------------------

    /**
     * 查询物理世界对象实例
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 物理世界对象实例
     * @since v5.1.0
     */
    @PostMapping("/rwd/instance/object/query")
    CustomerResponse<List<DtpRwdObjectResponse>> listObject(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody DtpRwdCriteriaQueryParam<DtpRwdObjectQueryParam> body);

    /**
     * 根据空间功能类型查询空间
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 物理世界对象实例
     * @since 5.240702.0
     */
    @PostMapping("/rwd/instance/object/querySpaceListByRoomFuncType")
    CustomerResponse<List<DtpRwdObjectResponse>> listSpacesByRoomFuncType(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody DtpRwdSpaceByRoomFuncTypeQueryParam body);

    /**
     * 查询物理世界对象关系
     *
     * @param param 公共参数
     * @param body  请求体
     * @return 物理世界对象关系
     * @since v5.1.0
     */
    @Deprecated
    @PostMapping("/rwd/instance/object/v3/queryObjectListByGraphCodeAndRelCode")
    CustomerResponse<List<DtpRwdRelationResponse>> listRelationObject(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody DtpRwdRelationQueryParam body);

    // ------------------------- 持久化查询 -------------------------

    /**
     * 持久化规则查询
     *
     * @param param 公共查询参数
     * @return 持久化规则, 当持久化规则不存在时将抛出异常
     * @since v5.7.0
     */
    @GetMapping("/rwd/instantiate/query/objId")
    CustomerResponse<ObjectInstantiateResponse> getInstantiate(
            @SpringQueryMap ObjectInstantiateQueryParam param);

    /**
     * 批量持久化规则查询
     *
     * @param param 公共查询参数
     * @return 持久化规则
     * @since v6.0.0
     */
    @PostMapping("/rwd/instantiate/query/objId/batch")
    CustomerResponse<List<ObjectInstantiateResponse>> listInstantiate(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody ObjectInstantiateBatchQueryParam body);

    // ------------------------- 孪生数据查询 -------------------------

    /**
     * 孪生对象规则根据对象ID查询参数
     *
     * @param param 公共参数
     * @param body  查询参数
     * @return 孪生对象规则
     * @since 7.250602.0
     */
    @PostMapping("/rwd/twin/queryInfoPointExistRule")
    CustomerResponse<List<DtpRwdTwinObjectRuleByIdResponse>> listTwinObjectRuleById(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody DtpRwdTwinObjectRuleByIdQueryParam body);

    /**
     * 孪生对象规则查询
     *
     * @param param 公共参数
     * @param body  查询参数
     * @return 孪生对象规则
     * @since v5.6.0
     */
    @PostMapping("/rwd/twin/queryObjectsExistRule")
    CustomerResponse<List<DtpRwdTwinObjectRuleResponse>> listTwinObjectRule(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody Collection<DtpRwdTwinObjectRuleQueryParam> body);

    /**
     * 孪生控制对象查询
     *
     * @param param      公共参数
     * @param classCodes 设备类编码
     * @return 孪生对象规则
     * @since 5.240601.0
     */
    @PostMapping("/rwd/twc/object/queryControlObjects")
    CustomerResponse<List<DtpRwdTwinControlObjectResponse>> listTwinControlObjects(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody Collection<String> classCodes);

    // ------------------------- 其他查询 -------------------------

    @Deprecated
    @PostMapping({"/rwd/instance/object/query"})
    DmpResult<List<JSONObject>> queryObjectList(@RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId, @RequestBody BaseQueryParamDTO criteria);

    /**
     * 查询 RWD 对象数据
     *
     * @param groupCode
     * @param projectId
     * @param appId
     * @param userId
     * @param criteria
     * @return
     * @see #listObject(DtpRwdCommonParam, DtpRwdCriteriaQueryParam)
     */
    @Deprecated
    @PostMapping({"/rwd/instance/object/query"})
    DmpResult<ArrayNode> query(@RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId, @RequestParam("appId") String appId, @RequestParam("userId") String userId, @RequestBody BaseQueryParamDTO criteria);

    /**
     * @description: 查询物理时间对象关系
     * @author zyc
     * @date 2023/3/13 15:11
     * @version 1.0
     */
    @PostMapping({"/rwd/instance/relation/query"})
    DmpResult<List<JSONObject>> queryRelation(@RequestParam("groupCode") String groupCode,
                                              @RequestParam("projectId") String projectId,
                                              @RequestParam("appId") String appId,
                                              @RequestParam("userId") String userId,
                                              @RequestBody DtpRwdRelationQueryParam criteria);

    @Deprecated
    @PostMapping({"/rwd/instance/object/queryObjectListByGraphCodeAndRelCode"})
    DmpResult<List<JSONObject>> queryObjectListByGraphCodeAndRelCode(@RequestParam("groupCode") String groupCode,
                                                                     @RequestParam("projectId") String projectId,
                                                                     @RequestParam("appId") String appId,
                                                                     @RequestParam("userId") String userId,
                                                                     @RequestBody Object criteria);

    /**
     * E1.2查找持久化有值的对象
     *
     * @param groupCode
     * @param projectId
     * @param userId
     * @param criteria
     * @return
     */
    @PostMapping({"/rwd/ipcal/point/queryObjectsOfExistRule"})
    DmpResult<List<ObjectsExistRule>> queryObjectsOfExistRule(@RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
                                                              @RequestParam("userId") String userId,
                                                              @RequestBody Object criteria);

    /**
     * 查找持久化有值的对象
     *
     * @param param 参数
     * @param body 查询参数
     * @return {@code CustomerResponse<List<ObjectsExistRule>> }
     * @since 7.250801.0
     */
    @PostMapping({"/rwd/ipcal/point/queryObjectsOfExistRule"})
    CustomerResponse<List<ObjectsExistRule>> listObjectsOfExistRule(@SpringQueryMap DtpRwdCommonParam param,@RequestBody Collection<DtpRwdObjectsOfExistRuleQueryParam> body);

    /**
     * F1.2 查找孪生有值的对象  云端接口
     *
     * @param groupCode
     * @param projectId
     * @param userId
     * @param criteria
     * @return
     */
    @PostMapping({"/rwd/twin/queryObjectsExistRule"})
    DmpResult<List<ObjectsExistRule>> queryObjectsExistRule(@RequestParam("groupCode") String groupCode, @RequestParam("projectId") String projectId,
                                                            @RequestParam("userId") String userId, @RequestBody List<ClassCode> criteria);


    /**
     * 查询孪生对象接口
     *
     * @param param 参数
     * @param body 查询条件
     * @return {@code CustomerResponse<List<ObjectsExistRule>> }
     * @since 7.250801.0
     */
    @PostMapping({"/rwd/twin/queryObjectsExistRule"})
    CustomerResponse<List<ObjectsExistRule>> listTwinObjectsOfExistRule(@SpringQueryMap DtpRwdCommonParam param,@RequestBody Collection<DtpRwdObjectsOfExistRuleQueryParam> body);

    /**
     * 根据系统id获取冷机分组
     *
     * @param groupCode 集团编码
     * @param projectId 项目id
     */
    @GetMapping({"/rwd/twc/group/acccccGroup"})
    DmpResult<AcccccGroupDTO> queryAcccccGroupBySystemId(@RequestParam("groupCode") String groupCode,
                                                         @RequestParam("projectId") String projectId,
                                                         @RequestParam("systemId") String systemId,
                                                         @RequestParam("appId") String appId,
                                                         @RequestParam("userId") String userId);

    /**
     * 根据系统id获取冷机分组，包含冰蓄冷
     *
     * @param groupCode 集团编码
     * @param projectId 项目id
     */
    @GetMapping({"/rwd/twc/group/sysGroup"})
    CustomerResponse<JSONObject> querySysGroupBySystemId(@RequestParam("groupCode") String groupCode,
                                                         @RequestParam("projectId") String projectId,
                                                         @RequestParam("systemId") String systemId,
                                                         @RequestParam("appId") String appId,
                                                         @RequestParam("userId") String userId);

    /**
     * 根据系统id获取冷机分组
     *
     * @param groupCode 集团编码
     * @param projectId 项目id
     */
    @Deprecated
    @PostMapping({"/rwd/twc/object/queryControlObjects"})
    DmpResult<List<ControlObjectDto>> queryControlObjects(@RequestParam("groupCode") String groupCode,
                                                          @RequestParam("projectId") String projectId,
                                                          @RequestBody String[] classCodeList);

    /**
     * 指定起点，查询指定类型的终点
     * 入参onlyId为true时，内部Object类型为String; false时，为JSONObject类型
     * @author wangPing
     * @date 2023/10/23
     */
    /**
     * R4 指定起点，查询指定类型的终点
     */
    @PostMapping(value = "/rwd/route/queryClassCodeFromIds")
    CustomerResponse<List<List<DtpRwdObjectResponse>>> queryClassCodeFromIds(@RequestBody RwdRouteFromIdsParam rwdRouteParam);

    /**
     * 厨房油烟感知聚合接口
     */
    @PostMapping(value = "/rwd/aggregate/exhaust/fumes/query")
    CustomerResponse<List<KitchenExhaustResponse>> queryKitchenExhaust(@SpringQueryMap DtpRwdCommonParam param);

    /**
     * 持久化分精度规则
     */
    @PostMapping(value = "/rwd/ipcal/point/queryInfoPointPeroid")
    CustomerResponse<List<DtpRwdFractionalPrecisionRuleDTO>> queryInfoPointPeroid(@SpringQueryMap DtpRwdCommonParam param,
                                                                                  @RequestBody Collection<String> objects);

    /**
     * 查询对象信息点绑点状态，如果信息点返回则为已绑点，否则为未绑点
     * http://172.19.0.143:9099/project/116/interface/api/35604
     *
     * @param param    rwd查询通用请求参数
     * @param criteria json入参详见文档
     * @return
     */
    @PostMapping(value = "/rwd/iot/server/v2/point/queryPointList")
    CustomerResponse<List<DtpRwdQueryPointListDTO>> queryPointList(@SpringQueryMap DtpRwdCommonParam param,
                                                                   @RequestBody JSONObject criteria);

    /**
     * 根据系统id获取冷机分组
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param systemId  系统ID
     * @param appId     应用标识
     * @param userId    用户ID
     * @return 冷机类型 -> 冷机大小 -> 冷机ID
     */
    @GetMapping("/rwd/twc/group/sysGroup")
    CustomerResponse<Map<String, Map<String, List<String>>>> querySystemGroup(
            @RequestParam("groupCode") String groupCode,
            @RequestParam("projectId") String projectId,
            @RequestParam("systemId") String systemId,
            @RequestParam("appId") String appId,
            @RequestParam("userId") String userId);

    /**
     * 根据系统id获取冷机分组
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param systemId  系统ID
     * @param appId     应用标识
     * @param userId    用户ID
     * @return {@code  DmpResult<Map< ChillerTypeSub ,Map< ChillerSize ,List<String>>>>}
     */
    @GetMapping({"/rwd/twc/group/sysGroup"})
    DmpResult<Map<ChillerTypeSub, Map<String, List<String>>>> querySysGroup(
            @RequestParam("groupCode") String groupCode,
            @RequestParam("projectId") String projectId,
            @RequestParam("systemId") String systemId,
            @RequestParam("appId") String appId,
            @RequestParam("userId") String userId);

    /**
     * 根据系统id获取冷机分组
     *
     * @param groupCode   集团编码
     * @param projectId   项目ID
     * @param pumpGroupId 泵组ID
     * @param appId       应用标识
     * @param userId      用户ID
     * @return {@code  DmpResult<Map< ChillerTypeSub ,Map< ChillerSize ,List<String>>>>}
     */
    @GetMapping({"/rwd/twc/group/pumpGroup"})
    DmpResult<Map<String, List<String>>> queryPumpGroup(
            @RequestParam("groupCode") String groupCode,
            @RequestParam("projectId") String projectId,
            @RequestParam("pumpGroupId") String pumpGroupId,
            @RequestParam("appId") String appId,
            @RequestParam("userId") String userId);

    /**
     * 根据系统id获取冷机分组
     *
     * @param body   查询参数，泵组的主键数组
     * @return {@code  DmpResult<Map< ChillerTypeSub ,Map< ChillerSize ,List<String>>>>}
     */
    @PostMapping("/rwd/twc/group/pumpGroups")
    CustomerResponse<Map<String, Map<String, List<String>>>> queryPumpGroups(
            @SpringQueryMap DtpRwdCommonParam param,
            @RequestBody Set<String> body);

}
