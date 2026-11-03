package com.persagy.apccontrol.basic.abstracts;

import com.persagy.apccontrol.basic.opt.OptRwdObjectsById;
import com.persagy.apccontrol.basic.opt.OptSwitch;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.config.NacosSwitchConfig;
import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.ColdHeatGroupObjectInfo;
import com.persagy.apccontrol.basic.model.dto.DirectRelationDTO;
import com.persagy.apccontrol.basic.model.dto.ObjectInfoCodeConfigDTO;
import com.persagy.apccontrol.basic.model.dto.RcInfoCodeGroupDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.InfoCodeSetExtend;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.enumtype.calendar.MidCalendarAcSeasonTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdPersistenceTimeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdTagEnum;
import com.persagy.apccontrol.basic.model.vo.RoomTempRangeVO;
import com.persagy.apccontrol.basic.model.vo.RoomTempStatisticsVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.ModelDiagramPrimitivesVo;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import com.persagy.apccontrol.basic.outside.enums.abnormalevent.RiskMeasureAimLevel2;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectsOfExistRuleQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.InfoCodeSetExtendService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.task.BusinessThreadPoolExecutor;
import com.persagy.apccontrol.basic.task.ObjectInfoCodeGroupTask;
import com.persagy.apccontrol.basic.utils.CommUtils;
import com.persagy.apccontrol.basic.utils.IotDataUtil;
import com.persagy.apccontrol.basic.utils.Obj2JSONObject;
import com.persagy.apccontrol.basic.utils.dateUtil.DateUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 运行监测处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/14 15:30
 */
@Slf4j
public abstract class AbstractRunningMonitorServer {


    @Autowired
    protected BizDtpRwdService bizDtpRwdService;

    @Autowired
    protected NacosSwitchConfig nacosSwitchConfig;

    @Autowired
    protected RcBasicsConfigService rcBasicsConfigService;

    @Lazy
    @Autowired
    protected AbstractCommonServer abstractCommonServer;

    @Lazy
    @Autowired
    protected LocalRedisCacheHandler localRedisCacheHandler;

    @Autowired
    protected CommonConfigService commonConfigService;

    @Autowired
    protected AbstractIotDataCacheServer abstractIotDataCacheServer;
    @Autowired
    @Lazy
    protected SystemGraphService systemGraphService;
    @Autowired
    protected DtpRwdClientService dtpRwdClientService;

    @Resource
    InfoCodeSetExtendService infoCodeSetExtendService;

    /** opt B10：按 id 取空间（switch.opt.monitorSpaceById） */
    @Autowired(required = false)
    protected OptRwdObjectsById optRwdObjectsById;

    public JSONObject queryObjectGroupInfoBySystemId(String groupCode, String projectId, String systemId, String systemClassCode, String graphTypeCode) throws Exception {
        JSONObject resultObject = new JSONObject();

        // 1.优先加载一次管网数据
        CompletableFuture<List<JSONObject>> future = CompletableFuture.supplyAsync(() -> this.abstractCommonServer.getExtraRuleObjects(groupCode, projectId, systemId, systemClassCode), BusinessThreadPoolExecutor.ttlExecutor());

        // 2.在获取该系统图下所有的设备
        Set<String> equipmentIds = localRedisCacheHandler.getObjectIdsBySystemGraph(projectId, systemId, systemClassCode, graphTypeCode);
        List<String> objectIds = CollectionUtil.isEmpty(equipmentIds) ? null : Lists.newArrayList(equipmentIds);
        List<JSONObject> allEquipObjects = CollectionUtil.isEmpty(objectIds) ? null : this.bizDtpRwdService.queryObjectList(groupCode, projectId, objectIds);
        // 3.设置设备所在空间的空间ID，以及对应的空间数据
        List<JSONObject> spaceList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(allEquipObjects)) {
            allEquipObjects = this.convertEquipmentInfo(groupCode, projectId, spaceList, objectIds, allEquipObjects);
        }
        List<JSONObject> meshesObjects = future.get();
        if (CollectionUtil.isNotEmpty(equipmentIds)) {
            List<JSONObject> ruleObjects = handleRule(systemClassCode, graphTypeCode, systemId, equipmentIds);
            if (CollectionUtil.isNotEmpty(ruleObjects)) {
                if (meshesObjects == null) {
                    allEquipObjects = ruleObjects;
                } else {
                    meshesObjects.addAll(ruleObjects);
                }
            }
        }
        if (CollectionUtil.isNotEmpty(meshesObjects)) {
            Set<String> mesheClassCodes = new HashSet<>();
            meshesObjects.forEach(item -> {
                mesheClassCodes.add(item.getString("classCode"));
                this.changeOutLine(item);
            });
            // 管网类名称返回
            List<JSONObject> classDefines = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(mesheClassCodes)) {
                for (String mesheClassCode : mesheClassCodes) {
                    if (isMatch(mesheClassCode)) {
                        JSONObject classDefine = new JSONObject();
                        classDefine.put("classCode", mesheClassCode);
                        classDefine.put("className", this.localRedisCacheHandler.getClassName(mesheClassCode));
                        classDefines.add(classDefine);
                    }
                }
            }
            resultObject.put("classDefines", classDefines);
        }

        resultObject.put("spaces", spaceList);
        resultObject.put("equipments", allEquipObjects == null ? Lists.newArrayList() : allEquipObjects);
        resultObject.put("meshes", meshesObjects == null ? Lists.newArrayList() : meshesObjects);

        return resultObject;
    }

    /**
     * 检查字符串是否满足六位字母且第三四位为PP的条件，意思是仅为官网数据
     *
     * @param str 待检查的字符串
     * @return 如果满足条件返回true，否则返回false
     */
    public static boolean isMatch(String str) {
        // 正则表达式匹配六位字母，且第三四位为PP
        String regex = "^[A-Za-z]{2}PP[A-Za-z]{2}$";
        return str.matches(regex);
    }

    private List<JSONObject> handleRule(String systemClassCode, String graphTypeCode, String systemId, Set<String> equipmentIds) {
        List<ModelDiagramPrimitivesVo> modelDiagramPrimitives = commonConfigService.getModelDiagramPrimitives();
        List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
        if (CollectionUtil.isEmpty(modelDiagramPrimitives)) {
            return Collections.emptyList();
        }
        String graphName = systemGraphService.getGraphName(systemClassCode, graphTypeCode, graphConfig, systemId);
        Map<String, ModelDiagramPrimitivesVo> graphNameMap = modelDiagramPrimitives.stream().collect(Collectors.toMap(ModelDiagramPrimitivesVo::getMapTitle, Function.identity()));
        ModelDiagramPrimitivesVo modelDiagramPrimitivesVo = graphNameMap.get(graphName);
        if (modelDiagramPrimitivesVo == null) {
            return Collections.emptyList();
        }
        String addRule = modelDiagramPrimitivesVo.getAddRule();
        if (StringUtils.isBlank(addRule) || !addRule.contains("Equipments") || !graphTypeCode.equals(modelDiagramPrimitivesVo.getMapTitle())) {
            return Collections.emptyList();
        }
        List<List<DirectRelationDTO>> allRules = new LinkedList<>();
        String[] rules = addRule.split("\n");
        for (String rule : rules) {
            List<DirectRelationDTO> directRelations = new ArrayList<>();
            if (rule.contains(",")) {
                String[] split = rule.split(",");
                for (String s : split) {
                    directRelations.add(CommUtils.parseRule(s));
                }
            } else {
                directRelations.add(CommUtils.parseRule(rule));
            }
            allRules.add(directRelations);
        }
        List<DtpRwdObjectDTO> allEquipments = new ArrayList<>();
        for (List<DirectRelationDTO> ruleList : allRules) {
            if (CollectionUtil.isEmpty(ruleList)) {
                continue;
            }
            Iterator<DirectRelationDTO> iterator = ruleList.iterator();
            if (iterator.hasNext()) {
                DirectRelationDTO directRelation = iterator.next();
                List<DtpRwdObjectDTO> objects = executeRule(directRelation, equipmentIds, iterator);
                if (CollectionUtil.isNotEmpty(objects)) {
                    allEquipments.addAll(objects);
                }
            }
        }
        return Obj2JSONObject.convertList(allEquipments);

    }

    private List<DtpRwdObjectDTO> executeRule(DirectRelationDTO
                                                      directRelation, Set<String> objectIds, Iterator<DirectRelationDTO> iterator) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        String direct = directRelation.getDirect();
        DtpRwdObjectRelationQueryParam relationQueryParam = new DtpRwdObjectRelationQueryParam(directRelation.getGraphCode(), directRelation.getRelCode());
        if (CommonConst.FROM_RELATION.equals(direct)) {
            param.setClassCode(directRelation.getFromClassCode());
            relationQueryParam.setObjTo(objectIds);
            param.setRelationFrom(relationQueryParam);
        } else {
            param.setClassCode(directRelation.getToClassCode());
            relationQueryParam.setObjFrom(objectIds);
            param.setRelationTo(relationQueryParam);
        }
        List<DtpRwdObjectDTO> rwdObjects = dtpRwdClientService.listObject(false, false, ContextUtil.getProjectId(), param);
        Set<String> objectIdSet = rwdObjects.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (iterator.hasNext()) {
            DirectRelationDTO newDirectRelation = iterator.next();
            return executeRule(newDirectRelation, objectIdSet, iterator);
        }
        return rwdObjects;
    }

    /**
     * 获取系统运行参数
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @param systemClassCode
     * @param graphTypeCode
     * @return
     */
    public JSONObject querySystemRunningParam(String groupCode, String projectId, String systemId,
                                              String systemClassCode, String graphTypeCode, String imageOrProductName) {
        JSONObject result = new JSONObject();

        try {
            // 获取当前 系统运行参数配置
            List<JSONObject> systemInfo = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(systemId));

            //没查询做一次补偿
            if (CollectionUtil.isEmpty(systemInfo)) {
                log.error("查询系统失败，重试获取-systemId:{}-graphTypeCode:{}", projectId, systemId);
                systemInfo = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(systemId));
            }


            JSONObject extraInfo = CollectionUtil.isEmpty(systemInfo) ? null : systemInfo.get(0);

            String systemRunParamUniqueKey = LocalRedisCacheHandler.getSystemRunParamUniqueKey(systemClassCode, graphTypeCode, imageOrProductName);

            List<RcInfoCodeGroupDTO> rcInfoCodeGroupDTOS = LocalCacheConstant.SYSTEM_RUN_PARAM_MAP.get(systemRunParamUniqueKey);

            //查询失败重刷缓存
            if (CollectionUtil.isEmpty(rcInfoCodeGroupDTOS)) {
                synchronized (AbstractRunningMonitorServer.class) {
                    rcInfoCodeGroupDTOS = LocalCacheConstant.SYSTEM_RUN_PARAM_MAP.get(systemRunParamUniqueKey);
                    if (CollectionUtil.isEmpty(rcInfoCodeGroupDTOS)) {
                        log.error("查询系统运行参数失败，重刷缓存-systemId:{}-graphTypeCode:{}", systemId, graphTypeCode);
                        this.localRedisCacheHandler.resetGlobalLocalRedisCache(TerminalEnum.CLOUD, nacosSwitchConfig.getUpdateSystemGraph());
                        rcInfoCodeGroupDTOS = LocalCacheConstant.SYSTEM_RUN_PARAM_MAP.get(systemRunParamUniqueKey);
                    }
                }
            }

            if (extraInfo == null || CollectionUtil.isEmpty(rcInfoCodeGroupDTOS)) {
                return result;
            }
            result.put("extraInfo", extraInfo);
            result.put("customFunction", LocalCacheConstant.CUSTOM_FUNCTION.get(systemRunParamUniqueKey));

            // 获取绑点规则
            List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(groupCode, projectId, Lists.newArrayList(new ClassCode(systemClassCode)));
            if (CollectionUtil.isEmpty(objectsExistRules)) {
                return result;
            }
            Set<String> infoCodes = objectsExistRules.stream().filter(item -> systemId.equals(item.getObjectId())).map(ObjectsExistRule::getInfoCode).collect(Collectors.toSet());

            //查询持久化规则 用于过滤效率指标
            List<ObjectsExistRule> cloudObjectsExistRules = this.dtpRwdClientService.listObjectsOfExistRuleByClassCode(Lists.newArrayList(DtpRwdObjectsOfExistRuleQueryParam.of(systemClassCode, null)));
            Set<String> cloudInfoCodes = new HashSet<>();
            if (CollectionUtil.isNotEmpty(cloudObjectsExistRules)) {
                cloudInfoCodes =
                        cloudObjectsExistRules.stream().filter(item -> systemId.equals(item.getObjectId())).map(ObjectsExistRule::getInfoCode).collect(Collectors.toSet());
            }
            // 运行参数写入
            List<RcInfoCodeGroupDTO> infoCodeGroupDTOS = JsonUtils.parse(JsonUtils.toJson(rcInfoCodeGroupDTOS), new TypeReference<List<RcInfoCodeGroupDTO>>() {
            });
            Collections.sort(infoCodeGroupDTOS);
            for (RcInfoCodeGroupDTO rcInfoCodeGroupDTO : infoCodeGroupDTOS) {
                List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
                List<RcInfoCodeCfg> orgInfoCodeCfg = rcInfoCodeGroupDTO.getInfoCodes();
                for (RcInfoCodeCfg infoCodeCfg : orgInfoCodeCfg) {
                    infoCodeCfg.setDataValue(null);
                    if (infoCodeCfg.isDynamicParameter()) {
                        if (RcInfoCodeGroupDTO.GROUP_NAME_EFFICIENCY.contains(rcInfoCodeGroupDTO.getGroupName())) {
                            if (!cloudInfoCodes.contains(infoCodeCfg.getInfoCode())) {
                                continue;
                            }
                            //如果是效率指标 改成查询日纬度的分精度数据
                            List<EnergyDataResponse> dataVal =
                                    dtpRwdClientService.queryIpDataOnMoment(Collections.singleton(systemId),
                                            Collections.singleton(infoCodeCfg.getInfoCode()),
                                            DtpRwdPersistenceTimeTypeEnum.analyze(nacosSwitchConfig.getBdtpTimeTypeEfficiency()),
                                            LocalDateTime.of(DateUtils.now().toLocalDate(), LocalTime.MIDNIGHT), null);
                            if (CollectionUtil.isNotEmpty(dataVal)) {
                                infoCodeCfg.setDataValue(String.valueOf(dataVal.get(0).getIpValue()));
                            }
                        } else {
                            if (!infoCodes.contains(infoCodeCfg.getInfoCode())) {
                                continue;
                            }
                            infoCodeCfg.setDataValue(this.abstractIotDataCacheServer.queryIotRealData(groupCode,
                                    projectId, systemId, systemClassCode, infoCodeCfg.getInfoCode()));
                        }

                    } else {
                        infoCodeCfg.setDataValue(extraInfo.getString(infoCodeCfg.getInfoCode()));
                    }
                    rcInfoCodeCfgs.add(infoCodeCfg);
                }

                rcInfoCodeCfgs.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Integer::compare));
                rcInfoCodeGroupDTO.setInfoCodes(rcInfoCodeCfgs);
            }

            // 控制参数写入
            Map<String, RcInfoCodeGroupDTO> controlGroupTree = new HashMap<>();
            List<RcInfoCodeCfg> controlInfoCodeConfigs = this.getControlInfoCodesByClassCode(systemClassCode);
            if (CollectionUtil.isNotEmpty(controlInfoCodeConfigs)) {
                for (RcInfoCodeCfg infoCodeConfig : controlInfoCodeConfigs) {
                    if (infoCodes.contains(infoCodeConfig.getInfoCode())) {
                        this.loadInfoCodeGroupTree(controlGroupTree, infoCodeConfig, extraInfo, null, true, true);
                    }
                }
            }

            List<RcInfoCodeGroupDTO> controlGroup = CollectionUtil.isEmpty(controlGroupTree.values()) ? new ArrayList<>() : new ArrayList<>(controlGroupTree.values());
            Collections.sort(controlGroup);
            result.put("controlInfoCode", controlGroup);

            result.put("allInfoCode", infoCodeGroupDTOS);
        } catch (Exception e) {
            log.error("系统运行参数查询异常，systemId:{},systemClassCode:{}", systemId, systemClassCode, e);
        }

        return result;
    }

    /**
     * 获取信息点配置及其对应的IOT数据
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @param objectIds
     * @return
     */
    public Map<String, JSONObject> queryInfoCodeConfigByObjectIds(String groupCode, String projectId,
                                                                  String systemId, Set<String> objectIds, Boolean riskEvent) {
        List<JSONObject> systemInfo = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(systemId));
        String systemClassCode = CollectionUtil.isEmpty(systemInfo) ? null : systemInfo.get(0).getString("classCode");

        // 1.获取设备对象详情数据
        List<String> objIds = Lists.newArrayList(objectIds);
        List<JSONObject> objectInfoList = bizDtpRwdService.queryObjectList(groupCode, projectId, objIds);
        if (CollectionUtil.isEmpty(objectInfoList)) {
            return new HashMap<>();
        }

        // 2.获取当前系统的季节
        String runModelSet = null;
        try {
            runModelSet = this.getRunModelSet(groupCode, projectId, systemId);
        } catch (Exception e) {
            log.warn("运行模式查询异常，项目：{}，系统：{}", projectId, systemId);
        }

        // 3.写入IOT数据
        return this.groupInfoCodeConfigs(groupCode, projectId, objectInfoList, systemClassCode, runModelSet, riskEvent);
    }

    /**
     * 获取信息点配置及其对应的IOT数据
     *
     * @param groupCode
     * @param projectId
     * @return
     */
    public Map<String, JSONObject> queryInfoCodeConfigByObjectId(String groupCode, String projectId,
                                                                 String systemClassCode, String objectId, Boolean riskEvent) {
        // 1.获取设备对象详情数据
        List<String> objIds = Lists.newArrayList(objectId);
        List<JSONObject> objectInfoList = bizDtpRwdService.queryObjectList(groupCode, projectId, objIds);
        if (CollectionUtil.isEmpty(objectInfoList)) {
            return new HashMap<>();
        }

        // 2.获取当前系统的季节
        //   String runModelSet = this.getRunModelSet(groupCode, projectId, systemId);

        // 3.写入IOT数据
        return this.groupInfoCodeConfigs(groupCode, projectId, objectInfoList, systemClassCode, null, riskEvent);
    }

    /**
     * 查询信息点配置及其IOT数据
     * 已与前端沟通确认 rcRunStatus 前端只会使用到值为2 或 3 的情况，设备染色不靠这个
     * 所以目前为null，多查询一次 runStatus ，一是旧版逻辑，防止C端需要，二是实际就不应该为null，防止数据清空后，存在问题
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param classCode
     * @param infoCodes
     * @return
     */
    public List<RcInfoCodeCfg> queryInfoCodeConfigByObjectId(String groupCode, String projectId,
                                                             String objectId, String classCode, Set<String> infoCodes) {
        List<RcInfoCodeCfg> resultList = new ArrayList<>();

        if (StrUtil.isBlank(classCode)) {
            // 1.获取设备对象详情数据
            List<JSONObject> objectInfoList = StrUtil.isBlank(objectId) ? null : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectId));
            if (CollectionUtil.isEmpty(objectInfoList)) {
                return resultList;
            }
            classCode = objectInfoList.get(0).getString("classCode");
        }

        // 2.获取信息点配置
        List<RcInfoCodeCfg> infoCodeConfigs = null;
        if (CollectionUtil.isEmpty(infoCodes)) {
            infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode);
        } else {
            infoCodes.add(InfoCodeEnum.RC_RUN_STATUS.getCode());
            infoCodeConfigs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode, infoCodes);
        }
        if (CollectionUtil.isNotEmpty(infoCodeConfigs)) {
            for (RcInfoCodeCfg infoCodeConfig : infoCodeConfigs) {
                if (infoCodeConfig != null && (infoCodeConfig.isDynamicParameter() || InfoCodeEnum.RC_RUN_STATUS.getCode().equals(infoCodeConfig.getInfoCode()))) {
                    this.setDataValueAndTime(groupCode, projectId, objectId, infoCodeConfig, null);
                    resultList.add(infoCodeConfig);
                }
            }
        }
        return resultList;
    }

    public Map<String, List<RcInfoCodeCfg>> batchQueryInfoCodeConfigByObjectId(
            String groupCode, String projectId, List<ObjectInfoCodeConfigDTO> objects) throws Exception {
        Map<String, List<RcInfoCodeCfg>> resultMap = new HashMap<>();
        if (CollectionUtil.isEmpty(objects)) {
            return resultMap;
        }
        Map<String, Set<String>> infoCodeConfigDTOMap = objects.stream().collect(Collectors.toMap(ObjectInfoCodeConfigDTO::getObjectId, ObjectInfoCodeConfigDTO::getInfoCodes, (v1, v2) -> v1));
        List<JSONObject> objectInfoList = CollectionUtil.isEmpty(infoCodeConfigDTOMap) ? null : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(infoCodeConfigDTOMap.keySet()));
        if (CollectionUtil.isEmpty(objectInfoList)) {
            return resultMap;
        }

        Map<String, CompletableFuture<List<RcInfoCodeCfg>>> futureMap = new HashMap<>();
        Set<String> classCodes = new HashSet<>();
        for (JSONObject jsonObject : objectInfoList) {
            String id = jsonObject.getString("id");
            String classCode = jsonObject.getString("classCode");
            classCodes.add(classCode);
            CompletableFuture<List<RcInfoCodeCfg>> future = CompletableFuture.supplyAsync(() -> this.queryInfoCodeConfigByObjectId(groupCode, projectId, id, classCode, infoCodeConfigDTOMap.get(id)),
                    BusinessThreadPoolExecutor.ttlExecutor());
            futureMap.put(id, future);
        }
        if (futureMap.size() > 0) {
            Map<String, Set<String>> idAndInfoCodeMap = abstractCommonServer.queryObjectsExistRuleMap(groupCode, projectId, classCodes);
            for (Map.Entry<String, CompletableFuture<List<RcInfoCodeCfg>>> futureEntry : futureMap.entrySet()) {
                String id = futureEntry.getKey();
                if (idAndInfoCodeMap.containsKey(id)) {
                    Set<String> infoCodeSet = idAndInfoCodeMap.get(id);
                    //查询了rcRunStatus 追加这个信息点
                    infoCodeSet.add(InfoCodeEnum.RC_RUN_STATUS.getCode());
                    List<RcInfoCodeCfg> rcInfoCodeCfgs = futureEntry.getValue().get();
                    List<RcInfoCodeCfg> rcInfoCodes = Lists.newArrayList();
                    if (CollectionUtil.isNotEmpty(rcInfoCodeCfgs)) {
                        rcInfoCodes = rcInfoCodeCfgs.stream().filter(x -> infoCodeSet.contains(x.getInfoCode())).collect(Collectors.toList());
                    }
                    resultMap.put(id, rcInfoCodes);
                }
            }
        }
        return resultMap;
    }

    /**
     * 查询设备的运行状态
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @return
     */
    public JSONObject queryRunningStatusByObjectIds(String groupCode, String projectId, Set<String> objectIds) {
        JSONObject result = new JSONObject();

        // 获取中台所有对象详情
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectIds));
        if (CollectionUtil.isEmpty(objectList)) {
            return result;
        }

        // 封装IOT数据
        for (JSONObject object : objectList) {
            String objectId = object.getString("id");
            String classCode = object.getString("classCode");
            DtpPointInfo infoCodeValue = this.getGroupObjectInfoCodeValue(groupCode, projectId, objectId, classCode, InfoCodeEnum.RUN_STATUS.getCode());
            result.put(objectId, infoCodeValue == null ? null : infoCodeValue.getDataValue());
        }

        return result;
    }

    /**
     * 根据对象ID，返回所有的信息点配置的IOT数据，仅返回运行、控制参数
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @return
     */
    public JSONObject queryInfoCodeValueByObjectIds(String groupCode, String projectId, Set<String> objectIds) {
        JSONObject result = new JSONObject();

        // 获取中台所有对象详情
        List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectIds));
        if (CollectionUtil.isEmpty(objectList)) {
            return result;
        }

        // 封装IOT数据
        for (JSONObject object : objectList) {
            String objectId = object.getString("id");
            String classCode = object.getString("classCode");
            Map<String, String> infoCodeValueMap = new HashMap<>();
            result.put(objectId, infoCodeValueMap);

            List<RcInfoCodeCfg> rcInfoCodeCfgs = this.localRedisCacheHandler.getInfoCodeConfigs(classCode);
            if (CollectionUtil.isEmpty(rcInfoCodeCfgs)) {
                continue;
            }
            for (RcInfoCodeCfg rcInfoCodeCfg : rcInfoCodeCfgs) {
                if (rcInfoCodeCfg.isDynamicParameter()) {
                    infoCodeValueMap.put(rcInfoCodeCfg.getInfoCode(), this.abstractIotDataCacheServer.queryIotRealData(groupCode, projectId, objectId, classCode, rcInfoCodeCfg.getInfoCode()));
                }
            }
        }

        return result;
    }

    /**
     * 通过风机查询冷却塔控制单元
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @param objectId
     * @return
     */
    public JSONObject queryCoolingTowerByFanId(String groupCode, String projectId, String systemId, String objectId) {
        List<JSONObject> systemInfo = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(systemId));
        String systemClassCode = CollectionUtil.isEmpty(systemInfo) ? null : systemInfo.get(0).getString("classCode");
        if (StrUtil.isBlank(systemClassCode)) {
            return new JSONObject();
        }

        // 1.根据风机，获取对应的冷却塔控制单元
        List<JSONObject> relationObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACOTU"), "CombineSubset", "Eg2Eq", null, Lists.newArrayList(objectId));
        if (CollectionUtil.isEmpty(relationObjects)) {
            return new JSONObject();
        }
        Map<String, JSONObject> objectMap = this.groupInfoCodeConfigs(groupCode, projectId, relationObjects, systemClassCode, null, nacosSwitchConfig.getYihengServerSwitch());
        JSONObject result = objectMap.get(relationObjects.get(0).getString("id"));
        return result == null ? new JSONObject() : result;
    }

    /**
     * 查询泵对应的泵组信息
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @return
     */
    public RcInfoCodeGroupDTO queryPumpGroupByWaterPump(String groupCode, String projectId, String objectId) {
        // 1.验证当前对象是否存在
        /*List<JSONObject> objects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectId));
        if (CollectionUtil.isEmpty(objects)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), "对象不存在");
        }
        JSONObject objectInfo = objects.get(0);
        String classCode = objectInfo.getString("classCode");
        if (!"ACCCCP".equals(classCode)) {
            // 第一版，非冷冻泵，直接返回
            return null;
        }
        // 2.获取分组信息，写死了，后续根据产品在变
        RcInfoCodeGroupDTO rcInfoCodeGroupDTO = new RcInfoCodeGroupDTO("GPBZ001", "泵组信息", "0", 1, null, null);

        // 3.获取此水泵对应的泵组信息
        List<JSONObject> relationObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.ACCPC_GROUP.getValue()), "CombineSubset", "Eg2Eq", null, Lists.newArrayList(objectId));
        if (CollectionUtil.isEmpty(relationObjects)) {
            return rcInfoCodeGroupDTO;
        }

        // 4.获取泵组存在的规则，并与信息点配置取交集
        RcInfoCodeCfg localName = this.localRedisCacheHandler.getInfoCodeConfig(DtpRwdClassCodeEnum.ACCPC_GROUP.getValue(), "localName");
        localName.setDataValue(relationObjects.get(0).getString("localName"));
        rcInfoCodeGroupDTO.setInfoCodes(Lists.newArrayList(localName));

        List<ObjectsExistRule> objectsExistRules = this.bizDtpRwdService.queryObjectsExistRule(projectId, Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.ACCPC_GROUP.getValue())));
        if (CollectionUtil.isEmpty(objectsExistRules)) {
            return rcInfoCodeGroupDTO;
        }
        String relationId = relationObjects.get(0).getString("id");
        Map<String, Set<String>> object_infoCode = objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId, Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));
        Set<String> infoCodes = object_infoCode.get(relationId);
        if (CollectionUtil.isEmpty(infoCodes)) {
            return rcInfoCodeGroupDTO;
        }

        // 5.规则信息点返回
        if (infoCodes.contains("branchDetaTempSet")) {
            // 只要包含，当前版本就仅返回 供回水温差设定
            RcInfoCodeCfg branchDetaTempSet = this.localRedisCacheHandler.getInfoCodeConfig(DtpRwdClassCodeEnum.ACCPC_GROUP.getValue(), "branchDetaTempSet");
            this.abstractIotDataCacheServer.setDataValueAndTime(groupCode, projectId, relationId, branchDetaTempSet, null);
            rcInfoCodeGroupDTO.getInfoCodes().add(branchDetaTempSet);
        } else if (infoCodes.contains("branchDetaPressSet")) {
            RcInfoCodeCfg branchDetaPressSet = this.localRedisCacheHandler.getInfoCodeConfig(DtpRwdClassCodeEnum.ACCPC_GROUP.getValue(), "branchDetaPressSet");
            this.abstractIotDataCacheServer.setDataValueAndTime(groupCode, projectId, relationId, branchDetaPressSet, null);
            rcInfoCodeGroupDTO.getInfoCodes().add(branchDetaPressSet);
        }
       return rcInfoCodeGroupDTO;*/
        return new RcInfoCodeGroupDTO();
    }

    /**
     * 空间温度分布
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @return
     */
    public List<RoomTempStatisticsVO> queryRoomTempStatistics(String groupCode, String projectId, String systemId) {
        //查询范围配置
        RcBasicsConfig basicsConfig = rcBasicsConfigService.lambdaQuery().eq(RcBasicsConfig::getType, BasicsConfigTypeEnum.ROOM_TEMP_RANGE).last(CommonConst.LIMIT_ONE).one();
        if (basicsConfig == null || StrUtil.isBlank(basicsConfig.getResult())) {
            return new ArrayList<>();
        }
        List<RoomTempRangeVO> rangeVOS = JSONObject.parseArray(basicsConfig.getResult(), RoomTempRangeVO.class);

        //获取空调季，过渡季，不返回数据
        MidCalendarAcSeasonTypeEnum projectSeason = this.abstractCommonServer.getProjectSeason(groupCode, projectId, null);
        if (MidCalendarAcSeasonTypeEnum.SEASON_TRANSITION.equals(projectSeason)) {
            return new ArrayList<>();
        }

        //查询系统下的所有空间
        List<JSONObject> spaceObjects = bizDtpRwdService.queryObjectList(groupCode, projectId,
                Lists.newArrayList(DtpRwdClassCodeEnum.SPACE.value()),
                DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE.value().getLeft(), DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE.value().getRight(),
                Lists.newArrayList(systemId), null);
        if (CollectionUtil.isEmpty(spaceObjects)) {
            return new ArrayList<>();
        }

        //获取绑点限制
        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(groupCode, projectId, Lists.newArrayList(new ClassCode(DtpRwdClassCodeEnum.SPACE.value())));
        if (CollectionUtil.isEmpty(objectsExistRules)) {
            return new ArrayList<>();
        }
        Set<String> spaceRuleIds = objectsExistRules.stream().filter(item -> InfoCodeEnum.SPACE_TDB.getCode().equals(item.getInfoCode())).map(ObjectsExistRule::getObjectId).collect(Collectors.toSet());

        //查询空间下得数据
        Set<String> spaceIds = spaceObjects.stream().filter(space -> spaceRuleIds.contains(space.getString("id"))).map(space -> space.getString("id")).collect(Collectors.toSet());

        List<BigDecimal> spaceTdbValueList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(spaceIds)) {
            Map<String, Map<String, String>> spaceTempValueMap = this.abstractIotDataCacheServer.queryIotRealDataMap(groupCode, projectId, spaceIds, Sets.newHashSet("tdb"));
            spaceIds.forEach(spaceId -> {
                String tdbValue = spaceTempValueMap.get(spaceId) == null ? null : spaceTempValueMap.get(spaceId).get(InfoCodeEnum.SPACE_TDB.getCode());
                spaceTdbValueList.add(StrUtil.isBlank(tdbValue) ? null : new BigDecimal(tdbValue));
            });
        }

        return statisticsRoomTemp(rangeVOS, spaceTdbValueList);
    }

    /**
     * 查询室内温度范围配置
     *
     * @param projectId
     * @return
     */
    public RcBasicsConfig queryTempRange(String projectId) {
        RcBasicsConfig basicsConfig = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, projectId)
                .eq(RcBasicsConfig::getType, BasicsConfigTypeEnum.ROOM_TEMP_RANGE)
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
        return basicsConfig;
    }

    /**
     * 统计温度值在区间范围内的数量
     *
     * @param rangeVOS
     * @param tempValueList
     * @return
     */
    private List<RoomTempStatisticsVO> statisticsRoomTemp
    (List<RoomTempRangeVO> rangeVOS, List<BigDecimal> tempValueList) {
        List<RoomTempStatisticsVO> resList = new ArrayList<>();
        for (RoomTempRangeVO range : rangeVOS) {
            List<BigDecimal> selectedValueList = tempValueList.stream().collect(Collectors.toList());
            if (range.getValueMin() != null && range.getIsIncludeMin()) {
                selectedValueList = selectedValueList.stream().filter(d -> d != null && d.compareTo(range.getValueMin()) >= 0).collect(Collectors.toList());
            }
            if (range.getValueMin() != null && !range.getIsIncludeMin()) {
                selectedValueList = selectedValueList.stream().filter(d -> d != null && d.compareTo(range.getValueMin()) > 0).collect(Collectors.toList());
            }
            if (range.getValueMax() != null && range.getIsIncludeMax()) {
                selectedValueList = selectedValueList.stream().filter(d -> d != null && d.compareTo(range.getValueMax()) <= 0).collect(Collectors.toList());
            }
            if (range.getValueMax() != null && !range.getIsIncludeMax()) {
                selectedValueList = selectedValueList.stream().filter(d -> d != null && d.compareTo(range.getValueMax()) < 0).collect(Collectors.toList());
            }
            if (range.getValueMin() == null && range.getValueMax() == null) {
                selectedValueList = selectedValueList.stream().filter(d -> d == null).collect(Collectors.toList());
            }
            RoomTempStatisticsVO vo = new RoomTempStatisticsVO();
            vo.setRange(range.getRange());
            vo.setCount(selectedValueList.size());
            resList.add(vo);
        }
        return resList;
    }

    /**
     * 查询某图纸下所有对象的异常事件
     *
     * @param groupCode
     * @param projectId
     * @param systemObjectId
     * @param classCode
     * @param graphTypeCode
     * @return
     */
    public List<ExceptionEventBaseInfoVO> queryAbnormalEventList(String groupCode, String projectId,
                                                                 String systemObjectId,
                                                                 String classCode, String graphTypeCode) {
        List<ExceptionEventBaseInfoVO> exceptEventInfoDTOS = new ArrayList<>();
        if (!BooleanUtil.isTrue(nacosSwitchConfig.getYihengServerSwitch())) {
            return exceptEventInfoDTOS;
        }

        // 根据图纸找到图中所有的设备
        Set<String> objectIds = this.localRedisCacheHandler.getObjectIdsBySystemGraph(projectId, systemObjectId, classCode, graphTypeCode);
        // 冷站需要获取一次 ACAC
        List<DirectRelationDTO> relationDTOs = this.abstractCommonServer.getRiskEventConfig(classCode);
        if (CollectionUtil.isNotEmpty(relationDTOs)) {
            for (DirectRelationDTO relationDTO : relationDTOs) {
                boolean from = CommonConst.FROM_RELATION.equals(relationDTO.getDirect());
                List<JSONObject> jsonObjects = this.queryRelationObjectList(groupCode, projectId, Lists.newArrayList(systemObjectId), Lists.newArrayList(from ? relationDTO.getFromClassCode() : relationDTO.getToClassCode()),
                        relationDTO.getGraphCode(), relationDTO.getRelCode(), relationDTO.getDirect());
                if (CollectionUtil.isNotEmpty(jsonObjects)) {
                    jsonObjects.forEach(item -> objectIds.add(item.getString("id")));
                }
            }
        }

        return getAbnormalEvents(exceptEventInfoDTOS, objectIds);
    }

    /**
     * 查询单个对象的异常事件信息
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @return
     */
    public List<ExceptionEventBaseInfoVO> querySingleAbnormalEventList(String groupCode, String projectId,
                                                                       String objectId) {
        List<ExceptionEventBaseInfoVO> exceptEventInfoDTOS = new ArrayList<>();
        if (!BooleanUtil.isTrue(nacosSwitchConfig.getYihengServerSwitch())) {
            return exceptEventInfoDTOS;
        }
        List<JSONObject> objectInfos = StrUtil.isBlank(objectId) ? null : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectId));
        if (CollectionUtil.isEmpty(objectInfos)) {
            return exceptEventInfoDTOS;
        }
        String classCode = objectInfos.get(0).getString("classCode");

        // 获取异常事件
        Set<String> objectIds = Sets.newHashSet(objectId);
        if ("ACOTU".equals(classCode)) {
            // 如果是冷却塔控制单元，获取下面的风机和阀门的异常事件
            List<JSONObject> relationObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACCCOF", "ACVVEO"), "CombineSubset", "Eg2Eq", Lists.newArrayList(objectId), null);
            if (CollectionUtil.isNotEmpty(relationObjects)) {
                relationObjects.forEach(item -> objectIds.add(item.getString("id")));
            }
        }
        return getAbnormalEvents(exceptEventInfoDTOS, objectIds);
    }

    /**
     * 获取异常事件
     *
     * @param exceptEventInfos 响应异常事件列表
     * @param objectIds        对象id
     * @return 异常事件数组
     */
    private List<ExceptionEventBaseInfoVO> getAbnormalEvents(List<ExceptionEventBaseInfoVO> exceptEventInfos, Set<String> objectIds) {
        if (CollectionUtils.isEmpty(objectIds)) {
            return exceptEventInfos;
        }
        Map<String, List<ExceptionEventBaseInfoVO>> mapRiskEvent =
                abstractCommonServer.mapRiskEvent(objectIds, null);
        if (CollectionUtil.isEmpty(mapRiskEvent)) {
            return exceptEventInfos;
        }
        mapRiskEvent = ExceptionEventBaseInfoVO.duplicateRemoval(mapRiskEvent);
        mapRiskEvent.forEach((key, value) -> {
            if (CollectionUtil.isNotEmpty(value)) {
                exceptEventInfos.addAll(value);
            }
        });
        exceptEventInfos.sort(Comparator.nullsFirst(Comparator.comparing(ExceptionEventBaseInfoVO::getExceptionStartTime)).reversed());
        return exceptEventInfos;
    }

    private Map<String, JSONObject> groupInfoCodeConfigs(String groupCode, String
            projectId, List<JSONObject> objectInfoList, String systemClassCode, String runModelSet, Boolean riskEvent) {
        // key-objectId, value -> 响应数据
        Map<String, JSONObject> result = new HashMap<>();

        // 1.获取所有存在持久化数据的IOT点位信息
        List<ClassCode> classCodes = new ArrayList<>();
        objectInfoList.forEach(item -> {
            classCodes.add(new ClassCode(item.getString("classCode")));
            JSONObject objectGroupInfo = new JSONObject();
            String objectId = item.getString("id");
            objectGroupInfo.put("extraInfo", item);
            result.put(objectId, objectGroupInfo);
        });
        List<ObjectsExistRule> objectsExistRules = abstractCommonServer.queryObjectsExistRule(groupCode, projectId, classCodes);

        // 2.写入IOT数据，以及强控信息object_infoCode = {HashMap@20828}  size = 2
        Map<String, Set<String>> object_infoCode = CollectionUtil.isEmpty(objectsExistRules) ? new HashMap<>()
                : objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId, Collectors.mapping(ObjectsExistRule::getInfoCode, Collectors.toSet())));

        int size = objectInfoList.size();
        List<Future<Boolean>> futureList = new ArrayList<>();
        for (JSONObject objectInfo : objectInfoList) {
            String objectId = objectInfo.getString("id");

            JSONObject resultObject = result.get(objectId);
            ObjectInfoCodeGroupTask infoCodeGroupTask = new ObjectInfoCodeGroupTask(objectInfo, resultObject, runModelSet, systemClassCode,
                    riskEvent == null ? size == 1 : riskEvent, object_infoCode, this);
            if (size > 1) {
                Future<Boolean> submit = BusinessThreadPoolExecutor.submitCommon(infoCodeGroupTask);
                futureList.add(submit);
            } else {
                infoCodeGroupTask.call();
            }
        }
        ObjectInfoCodeGroupTask.joinAll(futureList);
        return result;
    }

    public List<InfoCodeSetExtend> listInfoCodeSetExtend(String objectId) {
        return infoCodeSetExtendService.listByObjectId(objectId);
    }

    /**
     * 将信息点分配至对应的组信息中
     *
     * @param resultMap    分组树，key -> topGroupId，value-groupTree
     * @param infoCodeCfg  本次要填写的信息点
     * @param objectInfo   中台交付的数据全集
     * @param dtpPointInfo IOT数据，当 iot 参数为 true时，这里为空，会主动查询一次
     * @param iot          true-填写IOT数据，否则，填写中台交付的动态数据
     * @param resultMap    分组树，key -> topGroupId，value-groupTree
     * @param infoCodeCfg  本次要填写的信息点
     * @param objectInfo   中台交付的数据全集
     * @param iot          true-填写IOT数据，否则，填写中台交付的动态数据
     */
    public void loadInfoCodeGroupTree(Map<String, RcInfoCodeGroupDTO> resultMap,
                                      RcInfoCodeCfg infoCodeCfg, JSONObject objectInfo, DtpPointInfo dtpPointInfo, boolean iot, boolean queryIot) {
        if (resultMap == null || infoCodeCfg == null || !StrUtil.isAllNotBlank(infoCodeCfg.getGroupInfo(), infoCodeCfg.getTopGroupId())) {
            return;
        }

        // 当前信息点为动态点位
        boolean dynamic = infoCodeCfg.isDynamicParameter();

        if (iot && dynamic) {
            // 运行和控制数据处理
            this.loadRealInfoCodeGroupTree(infoCodeCfg, objectInfo, dtpPointInfo, queryIot);
        } else if (!iot && !dynamic) {
            // 静态数据写入，当一级标签为技术参数，且数据未交付时，过滤掉此信息点
            String staticValue = objectInfo.getString(infoCodeCfg.getInfoCode());
            if (StrUtil.isBlank(staticValue) && DtpRwdTagEnum.TECH_PARAM.getValue().equals(infoCodeCfg.getFirstTag())) {
                return;
            }
            infoCodeCfg.setDataValue(staticValue);
        } else {
            return;
        }

        try {
            // 写入数据的时候，组装出对应的树形数据
            if (!resultMap.containsKey(infoCodeCfg.getTopGroupId())) {
                RcInfoCodeGroupDTO infoCodeGroupDTO = this.localRedisCacheHandler.getInfoCodeGroupDTO(infoCodeCfg.getTopGroupId());
                resultMap.put(infoCodeCfg.getTopGroupId(), infoCodeGroupDTO);
            }
            RcInfoCodeGroupDTO.loadInfoCodeGroupTree(infoCodeCfg, resultMap.get(infoCodeCfg.getTopGroupId()));
        } catch (Exception e) {
            log.error("信息点分组异常，当前信息点:{}", JsonUtils.toJson(infoCodeCfg), e);
        }
    }

    /**
     * 获取聚合信息点集合，包含对应的IOT数据
     *
     * @param groupCode
     * @param projectId
     * @param infoCodeCfg
     * @return
     */
    public List<RcInfoCodeCfg> getLinkInfoCodes(String groupCode, String projectId, String objectId,
                                                RcInfoCodeCfg infoCodeCfg, JSONObject objectInfo) {
        if (StrUtil.isBlank(infoCodeCfg.getMergeCode()) || infoCodeCfg.getExtendInfo() == null || CollectionUtil.isEmpty(infoCodeCfg.getExtendInfo().getMergeInfoCodes())) {
            return null;
        }
        // 修改为聚合信息点配置
        infoCodeCfg.setInfoCode(infoCodeCfg.getMergeCode());
        infoCodeCfg.setName(infoCodeCfg.getMergeName());

        List<RcInfoCodeCfg> linkCodes = new ArrayList<>();
        for (String mergeInfoCode : infoCodeCfg.getExtendInfo().getMergeInfoCodes()) {
            RcInfoCodeCfg codeConfig = this.localRedisCacheHandler.getInfoCodeConfig(infoCodeCfg.getClassCode(), mergeInfoCode);
            if (codeConfig != null) {
                codeConfig.setDataValue(this.abstractIotDataCacheServer.queryIotRealData(groupCode, projectId, objectId, infoCodeCfg.getClassCode(), mergeInfoCode));
                if (objectInfo != null) {
                    objectInfo.put(codeConfig.getInfoCode(), codeConfig.getDataValue());
                }
                if (StrUtil.isNotBlank(codeConfig.getAbbreviationName())) {
                    codeConfig.setName(codeConfig.getAbbreviationName());
                }
                linkCodes.add(codeConfig);
            }
        }
        return linkCodes;
    }

    /**
     * 判断泵组与旗下的所有热泵是否存在规则
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param infoCode
     * @param group
     * @return
     */
    public ColdHeatGroupObjectInfo checkGroupEquipmentRule(String groupCode, String projectId,
                                                           String objectId, String infoCode, boolean group) {
        ColdHeatGroupObjectInfo groupObjectInfo = new ColdHeatGroupObjectInfo(false, false, null, null);

        // 1.根据 空气源热泵控制组，获取其对应的空气源热泵
        String groupObjectId = null;
        List<JSONObject> equipments = null;
        if (group) {
            groupObjectId = objectId;
            equipments = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACCCAH"), "CombineSubset", "Eg2Eq", Lists.newArrayList(objectId), null);
        } else {
            List<JSONObject> equipGroups = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACAHC"), "CombineSubset", "Eg2Eq", null, Lists.newArrayList(objectId));
            if (CollectionUtil.isNotEmpty(equipGroups)) {
                groupObjectId = equipGroups.get(0).getString("id");
                equipments = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACCCAH"), "CombineSubset", "Eg2Eq", Lists.newArrayList(groupObjectId), null);
            }
        }
        groupObjectInfo.setGroupObjectId(groupObjectId);
        groupObjectInfo.setEquipObjectIds(CollectionUtil.isEmpty(equipments) ? null : equipments.stream().map(item -> item.getString("id")).collect(Collectors.toSet()));

        // 2.获取所有的绑点规则，并判断泵组本身是否存在规则
        List<ObjectsExistRule> objectsExistRules = this.abstractCommonServer.queryObjectsExistRule(groupCode, projectId, Lists.newArrayList(new ClassCode("ACAHC"), new ClassCode("ACCCAH")));
        Map<String, List<ObjectsExistRule>> existRuleMap = CollectionUtil.isEmpty(objectsExistRules) ? null : objectsExistRules.stream().collect(Collectors.groupingBy(ObjectsExistRule::getObjectId));
        List<ObjectsExistRule> groupExistRule = existRuleMap == null ? null : existRuleMap.get(groupObjectId);
        if (CollectionUtil.isNotEmpty(groupExistRule)) {
            for (ObjectsExistRule existRule : groupExistRule) {
                if (existRule.getInfoCode().equals(infoCode)) {
                    groupObjectInfo.setGroupRule(true);
                    break;
                }
            }
        }

        // 3.判断热泵是否全部存在规则
        if (CollectionUtil.isEmpty(groupObjectInfo.getEquipObjectIds()) || existRuleMap == null) {
            return groupObjectInfo;
        }
        for (String equipId : groupObjectInfo.getEquipObjectIds()) {
            List<ObjectsExistRule> equipExistRule = existRuleMap.get(equipId);
            List<ObjectsExistRule> exists = CollectionUtil.isEmpty(equipExistRule) ? null
                    : equipExistRule.stream().filter(rule -> rule.getInfoCode().equals(infoCode)).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(exists)) {
                return groupObjectInfo;
            }
        }
        groupObjectInfo.setAllEquipRule(true);
        return groupObjectInfo;
    }

    /**
     * 获取设备信息，填充空间ID与楼层ID，并设置运行状态信息
     *
     * @param groupCode
     * @param projectId
     * @param objectIds
     * @param allEquipObjects
     * @return
     */
    private List<JSONObject> convertEquipmentInfo(String groupCode,
                                                  String projectId,
                                                  List<JSONObject> spaceList,
                                                  List<String> objectIds,
                                                  List<JSONObject> allEquipObjects) {
        List<JSONObject> resultList = new ArrayList<>();
        if (CollectionUtil.isEmpty(allEquipObjects)) {
            return resultList;
        }

        // 非设备类信息，不返回
        final Set<String> noEquipmentTypes = Sets.newHashSet("building", "floor", "project", "shaft", "space", "system");

        // opt B10（switch.opt.monitorSpaceById）：先查设备所在空间关系，再只按这些空间 id 取空间，不再查全项目空间
        boolean spaceById = OptSwitch.on(OptSwitch.MONITOR_SPACE_BY_ID) && optRwdObjectsById != null;

        // 获取当前所有的空间数据
        List<JSONObject> allSpaceList = spaceById ? null : this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.SPACE.getValue()), null, null, null, null);

        // 获取当前所有设备所对应的物业分区
        List<JSONObject> jsonObjects = this.bizDtpRwdService.queryRelation(groupCode, projectId, "MechInArch", "Eq2Sp", DtpRwdClassCodeEnum.SPACE.getValue(), objectIds, null);
        Map<String, String> object_space = CollectionUtil.isEmpty(jsonObjects) ? new HashMap<>() : jsonObjects.stream().collect(Collectors.toMap(key -> key.getString("objFrom"), value -> value.getString("objTo"), (v1, v2) -> v1));
        if (spaceById) {
            allSpaceList = optRwdObjectsById.list(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.SPACE.getValue()),
                    object_space.values().stream().filter(StrUtil::isNotBlank).collect(Collectors.toList()));
        }
        Map<String, JSONObject> space_info = CollectionUtil.isEmpty(allSpaceList) ? new HashMap<>()
                : allSpaceList.stream().collect(Collectors.toMap(key -> key.getString("id"), Function.identity(), (v1, v2) -> v1));

        // 数据封装
        Set<String> tempSpace = new HashSet<>();
        for (JSONObject equipObject : allEquipObjects) {
            String id = equipObject.getString("id");
            String objType = equipObject.getString("objType");
            if (noEquipmentTypes.contains(objType)) {
                continue;
            }

            resultList.add(equipObject);
            this.changeOutLine(equipObject);

            // 设置空间与楼层ID
            if (object_space.containsKey(id)) {
                String spaceId = object_space.get(id);
                if (StrUtil.isNotBlank(spaceId)) {
                    equipObject.put("spaceId", spaceId);
                    if (!tempSpace.contains(spaceId) && space_info.containsKey(spaceId)) {
                        JSONObject spaceObject = space_info.get(spaceId);
                        this.changeOutLine(spaceObject);
                        spaceList.add(spaceObject);
                        tempSpace.add(spaceId);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 获取该设备类型下所有的展示信息点配置集
     *
     * @param classCode
     * @return
     */
    public List<RcInfoCodeCfg> getInfoCodesByClassCode(String classCode) {
        return this.localRedisCacheHandler.getInfoCodeConfigs(classCode);
    }

    /**
     * 获取该设备类型下所有的控制信息点配置集
     *
     * @param classCode
     * @return
     */
    public List<RcInfoCodeCfg> getControlInfoCodesByClassCode(String classCode) {
        return this.localRedisCacheHandler.getControlInfoCodeConfigs(classCode);
    }

    /**
     * 获取信息点配置分组定义
     *
     * @param topGroupId
     * @return
     */
    public RcInfoCodeGroupDTO getInfoCodeGroupDTO(String topGroupId) {
        return this.localRedisCacheHandler.getInfoCodeGroupDTO(topGroupId);
    }

    /**
     * 注意边缘端的响应
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param infoCodeCfgs
     * @return
     */
    public List<DtpPointInfo> queryCommonIotRealDataList(String groupCode, String projectId, String
            objectId, List<RcInfoCodeCfg> infoCodeCfgs) {
        return this.abstractIotDataCacheServer.queryCommonIotRealDataList(groupCode, projectId, objectId, infoCodeCfgs);
    }

    public List<JSONObject> queryRelationObjectList(String groupCode, String projectId, List<String> objectIds,
                                                    List<String> classCodes, String graphCode, String relCode,
                                                    String relDirection) {
        // 获取关系对象详情
        return this.abstractCommonServer.queryRelationObjectList(groupCode, projectId, objectIds, classCodes, graphCode, relCode, relDirection);
    }

    /**
     * 获取异常事件、巡检信息
     *
     * @param objectIds
     * @return
     */
    public Map<String, List<ExceptionEventBaseInfoVO>> queryRiskEventList(List<String> objectIds, Collection<RiskMeasureAimLevel2> riskMeasureAimLevel2s) {
        return this.abstractCommonServer.mapRiskEvent(objectIds, riskMeasureAimLevel2s);
    }

    /**
     * 改变 outline 线
     *
     * @param objectList
     */
    private void changeOutLine(List<JSONObject> objectList) {
        if (CollectionUtil.isEmpty(objectList)) {
            return;
        }
        for (JSONObject jsonObject : objectList) {
            changeOutLine(jsonObject);
        }
    }

    /**
     * 改变 outline 线
     *
     * @param objectInfo
     */
    private void changeOutLine(JSONObject objectInfo) {
        try {
            String outline = objectInfo.getString("outline");
            if (StrUtil.isNotBlank(outline)) {
                objectInfo.put("outline", JSONArray.parseArray(outline));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置IOT数据及其对应的时间
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param rcInfoCodeCfg
     * @param objectInfo
     */
    private void setDataValueAndTime(String groupCode, String projectId, String objectId,
                                     RcInfoCodeCfg rcInfoCodeCfg, JSONObject objectInfo) {
        DtpPointInfo pointInfo = this.getGroupObjectInfoCodeValue(groupCode, projectId, objectId, rcInfoCodeCfg.getClassCode(), rcInfoCodeCfg.getInfoCode());
        if (pointInfo != null) {
            pointInfo.setDataValue(IotDataUtil.overrideDataValue(rcInfoCodeCfg.getInfoCode(), pointInfo.getDataValue(), rcInfoCodeCfg));
            rcInfoCodeCfg.setDataValue(pointInfo.getDataValue());
            rcInfoCodeCfg.setDataTime(pointInfo.getDataTime());
            rcInfoCodeCfg.setDataIndate(pointInfo.getDataIndate());
            if (objectInfo != null) {
                objectInfo.put(rcInfoCodeCfg.getInfoCode(), pointInfo.getDataValue());
            }
        }
    }

    /**
     * 获取系统运行模式
     *
     * @param groupCode
     * @param projectId
     * @param systemId
     * @return
     */
    public abstract String getRunModelSet(String groupCode, String projectId, String systemId);

    /**
     * 加载运行、控制信息点
     *
     * @param infoCodeCfg
     * @param objectInfo
     * @param dtpPointInfo
     */
    public abstract void loadRealInfoCodeGroupTree(RcInfoCodeCfg infoCodeCfg, JSONObject objectInfo, DtpPointInfo
            dtpPointInfo, boolean queryIot);

    public abstract DtpPointInfo getGroupObjectInfoCodeValue(String groupCode, String projectId, String
            objectId, String classCode, String infoCode);

}
