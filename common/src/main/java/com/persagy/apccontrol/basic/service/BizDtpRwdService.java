package com.persagy.apccontrol.basic.service;

import com.alibaba.fastjson.JSONObject;
import com.persagy.apccontrol.basic.model.dto.EnergyDataQueryParam;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdBuildingDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.QueryDTO;
import com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdTwinControlObjectVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.outside.dto.ControlObjectDto;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.CustomerResponse;
import com.persagy.apccontrol.basic.outside.feign.model.response.rwd.DtpRwdRelationResponse;
import com.persagy.framework.common.exception.BusinessException;
import feign.FeignException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 物理世界业务服务
 *
 * <p>------------------------- 查询相关 -------------------------
 * <ul>
 *     <li><b>listPublicCircuit</b> - 查询公共照明回路</li>
 *     <li><b>mapFloorRelationSpacesBySystem</b> - 查询项目下供冷系统服务的空间</li>
 *     <li><b>treeBuildingFloorSpace</b> - 查询项目下建筑楼层空间树</li>
 * </ul>
 *
 * @author JiYinchuan
 * @since v5.6.0
 */
public interface BizDtpRwdService {

    // ------------------------- 查询相关 -------------------------

    /**
     * 获取对象详情信息
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @return
     */
    List<JSONObject> queryObjectList(String groupCode, String projectId, List<String> objectIds);

    /**
     * 获取对象详情信息--根据关系获取
     *
     * @param groupCode
     * @param projectId
     * @param classCode
     * @param graphCode
     * @param relCode
     * @param objFrom
     * @param objTo
     * @return
     */
    List<JSONObject> queryObjectList(String groupCode, String projectId, List<String> classCode, String graphCode, String relCode, List<String> objFrom, List<String> objTo);

    /**
     * 查询项目下供冷系统服务的楼层空间
     *
     * @param queryCache 是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows   是否在查询失败|数据为空时抛出异常
     * @param projectId  项目ID
     * @param systemId   系统ID
     * @param floorIds   楼层ID
     * @param param      查询参数
     * @return 项目下供冷系统服务的楼层空间
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到楼层
     * @since v5.1.0
     */
    Map<String, List<DtpRwdRelationResponse>> mapFloorRelationSpacesBySystem(
            boolean queryCache, boolean isThrows, @NonNull String projectId,
            @NonNull String systemId, @NonNull Collection<String> floorIds,
            @Nullable DtpRwdRelationQueryParam param) throws FeignException, BusinessException;

    /**
     * 查询项目下供冷系统服务的建筑楼层空间树
     *
     * @param queryCache    是否查询缓存, 如缓存不存在则会自动添加并使用
     * @param isThrows      是否在查询失败|数据为空时抛出异常
     * @param projectId     项目ID
     * @param systemId      中央供冷系统ID
     * @param buildingParam 查询建筑参数
     * @param floorParam    查询楼层参数
     * @param spaceParam    查询空间参数
     * @param isQuerySpace  是否查询空间
     * @return 项目下供冷系统服务的建筑楼层空间树
     * @throws FeignException    RPC调用异常
     * @throws BusinessException 未找到建筑|未找到楼层|未找到空间
     * @since v5.1.0
     */
    List<DtpRwdBuildingDTO> treeBuildingFloorSpace(
            boolean queryCache, boolean isThrows,
            @NonNull String projectId, @Nullable Collection<String> systemId,
            @Nullable DtpRwdObjectQueryParam buildingParam,
            @Nullable DtpRwdObjectQueryParam floorParam,
            @Nullable DtpRwdObjectQueryParam spaceParam, Boolean isQuerySpace)
            throws FeignException, BusinessException;

    // ------------------------- 其他查询 -------------------------

    /**
     * @return List<Map>
     * @descrption 远程调用查询本地Iot数据
     * @author tangzy
     * @since 2023/3/22 9:20
     */
    List<DtpRwdObjectDTO> querySystemDiagramData(QueryDTO params);

    /**
     * 查询rwd 关系/对应数据接口
     * 对应的HBASE数据
     * 查找指定时间段的持久化数据
     *
     * @param param 查询参数
     * @return 能耗分精度
     * @since v5.1.0
     */
    CustomerResponse<List<EnergyDataResponse>> listIpDataInPeriod(EnergyDataQueryParam param, String projectId, String groupCode);

    /**
     * 查持久化数据
     *
     * @param param 支持时间，对象id等
     * @return iot数据
     */
    List<EnergyDataResponse> queryIpDataInPeriod(EnergyDataQueryParam param);

    /**
     * 获取当前对象、信息点的，最近的一条数据
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @param infoCodes
     * @return
     */
    List<EnergyDataResponse> queryIpDataOnMoment(String groupCode, String projectId, List<String> objectIds, List<String> infoCodes);

    /**
     * 查询对象关系数据
     *
     * @param groupCode
     * @param projectId
     * @param graphCode
     * @param relCode
     * @param relValue
     * @param objFroms
     * @param objTos
     * @return
     */
    List<JSONObject> queryRelation(String groupCode, String projectId, String graphCode, String relCode, String relValue, List<String> objFroms, List<String> objTos);

    /**
     * 一个小时之内的最新数据
     *
     * @param objectIds 对象id集合
     * @param infoCodes 信息点
     * @return 一个小时之内的最新数据
     */
    List<EnergyDataResponse> queryIpDataOnMomentInOneHour(Set<String> objectIds, Set<String> infoCodes);


    /**
     * 查询冷机分组
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @return
     */
    AcccccGroupDTO queryAcccccGroupBySystemId(String groupCode, String projectId, String systemId);

    /**
     * 查询冷机类型
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @return
     */
    JSONObject querySysGroupBySystemId(String groupCode, String projectId, String systemId);

    /**
     * 查询项目下控制对象列表
     *
     * @param groupCode
     * @param projectId
     * @param classCodeList
     * @return
     */
    List<ControlObjectDto> queryControlObjects(String groupCode, String projectId, String... classCodeList);

    /**
     * 查询是否有交付对象信息
     *
     * @param groupCode
     * @param projectId
     * @param objectId 查询条件的对象ID
     * @param classCode 需要判断是否存在的设备类编码
     * @param graphCode
     * @param relCode
     * @param relDirection from objectId -> objFrom
     * @return
     */
    boolean existsObjects(String groupCode, String projectId, String objectId, String classCode, String graphCode, String relCode, String relDirection);

    /**
     * 查询空气源热泵控制组
     *
     * @param projectId
     * @param sysId
     * @return
     */
    List<DtpRwdObjectDTO> getAcahcObj(String projectId, String sysId);


    /**
     * @return java.util.Map<java.lang.String, com.persagy.apccontrol.basic.model.vo.rwd.DtpRwdTwinControlObjectVO>
     * @Author 薛涛
     * @Description 查询管控对象
     * @Date 20:38 2024/8/21
     * @Param [allClassCodes]
     **/
    Map<String, DtpRwdTwinControlObjectVO> queryControlObjects(@NonNull Set<String> allClassCodes);


    /**
     * @return
     * @Author 薛涛
     * @Description 判断此对象是否为管控对象
     * @Date 20:42 2024/8/21
     * @Param [objectId, InfoCode, dtpRwdTwinControlObjectVOMap]
     **/
    Boolean booleanControlObject(String objectId, String InfoCode, Map<String, DtpRwdTwinControlObjectVO> dtpRwdTwinControlObjectVOMap);


}
