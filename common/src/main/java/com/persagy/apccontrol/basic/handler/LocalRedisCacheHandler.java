package com.persagy.apccontrol.basic.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.MapUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.config.RedisCacheExpUtil;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.CustomFunctionDTO;
import com.persagy.apccontrol.basic.model.dto.RcInfoCodeGroupDTO;
import com.persagy.apccontrol.basic.model.dto.RwdObjectBusinessFeildDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.BasePropertyDTO;
import com.persagy.apccontrol.basic.model.entity.InfoCodeConfigExtendInfo;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.enumtype.DemandInfoCodeTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.TerminalEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdTagEnum;
import com.persagy.apccontrol.basic.model.enumtype.info.InfoTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.runningLog.RunningLogObjectCategpryEnum;
import com.persagy.apccontrol.basic.model.vo.systemgraph.PipelineRelationConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.SystemRunParamVo;
import com.persagy.apccontrol.basic.outside.dto.ClassCode;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.resource.RcInfoCodeCfgService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.structure.performance.StopWatch;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务相关的 redis 缓存处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/11/27 18:52
 */
@Slf4j
@Component
public class LocalRedisCacheHandler implements LogComponent {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private RedisCacheExpUtil redisCacheExpUtil;

    @Autowired
    private BizDtpRwdService bizDtpRwdService;

    @Autowired
    private CommonConfigService commonConfigService;

    @Autowired
    private RcInfoCodeCfgService infoCodeCfgService;

    @Lazy
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    @Lazy
    @Autowired
    private SystemRelationGraphHandler systemRelationGraphHandler;

    @Autowired
    private DtpKnowledgeClientService dtpKnowledgeClientService;

    /**
     * 获取本方业务判断所需要的扩展静态字段信息，并携带所属系统或模块（目前仅是为了记日志）
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param systemId  为了兼容系统图相关的控制接口，按系统ID进行日志记录，当前对象所属的真实系统ID，非模块化标识
     * @return
     */
    public RwdObjectBusinessFeildDTO getObjectBusinessFeildAndSystemInfo(String groupCode, String projectId, String objectId, String systemId) {
        if (StrUtil.isBlank(objectId)) {
            return null;
        }
        String modelType = ContextUtil.getString(CommonConst.MODEL_TYPE_HEADER);
        modelType = StrUtil.isBlank(modelType) || "null".equalsIgnoreCase(modelType) ? null : modelType;

        RwdObjectBusinessFeildDTO objectBusinessFeildDTO = LocalCacheConstant.OBJECT_DETAIL_MAP.getIfPresent(objectId);
        if (objectBusinessFeildDTO == null) {
            objectBusinessFeildDTO = this.getObjectBusinessFeild(groupCode, projectId, objectId);
            if (objectBusinessFeildDTO == null) {
                return null;
            }
            // 如果之前加载过一次数据，这里直接返回
            if (StrUtil.isNotBlank(objectBusinessFeildDTO.getSystemId()) && objectBusinessFeildDTO.getSystemType() != null) {
                return objectBusinessFeildDTO;
            }
            String classCode = objectBusinessFeildDTO.getClassCode();

            // 优先写入一次默认的所属系统模块
            objectBusinessFeildDTO.wirteObjectClassBelongModelType();

            // 新风机组需要特殊处理，因其有可能属于 厨房排油烟
            /*
            if ("ACATFU".equals(classCode)) {
                List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACKLSF"), "MechCtrl", "LinkCtrl", null, Lists.newArrayList(objectId));
                if (CollectionUtil.isNotEmpty(objectList)) {
                    // 不为空，说明属于 厨房排油烟
                    objectBusinessFeildDTO.setSystemId(DtpRwdSystemClassCodeEnum.KITCHEN_RANGE_HOOD.value());
                    objectBusinessFeildDTO.setSystemType(DtpRwdSystemClassCodeEnum.KITCHEN_RANGE_HOOD);
                    return objectBusinessFeildDTO;
                }
            }

             */

            // 优先取header
            if (StrUtil.isNotBlank(modelType)) {
                // header 不为空，就以传参为准，如果传参异常，自行纠正
                objectBusinessFeildDTO.setSystemId(modelType);
                objectBusinessFeildDTO.setSystemType(DtpRwdSystemClassCodeEnum.analyze(modelType));
            }

            // 如果为之前的冷站、冷热站、热站，或者系统ID的传参不为空，说明是以系统ID进行的日志区分，这里重新赋值，如果没能重新赋值，也不耽误日志记录，最多日志不是以系统进行区分的，出现再定位问题
            if (LocalCacheConstant.OBJECT_SYSTEM_CLASS.contains(classCode) || StrUtil.isNotBlank(systemId)) {
                List<JSONObject> systemObjectList = null;
                if (RunningLogObjectCategpryEnum.SYSTEM.getCode().equals(objectBusinessFeildDTO.getObjType())) {
                    objectBusinessFeildDTO.setSystemId(objectBusinessFeildDTO.getObjectId());
                    objectBusinessFeildDTO.setSystemType(DtpRwdSystemClassCodeEnum.analyze(objectBusinessFeildDTO.getClassCode()));
                } else if (StrUtil.isNotBlank(systemId)) {
                    systemObjectList = bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(systemId));
                }

                // 为空，查询一次关系数据
                if (CollectionUtil.isEmpty(systemObjectList)) {
                    if ("equipGroup".equals(objectBusinessFeildDTO.getObjType())) {
                        // 设备组和系统的关系
                        systemObjectList = bizDtpRwdService.queryObjectList(groupCode, projectId, null,
                                DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM.value().getLeft(), DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM.value().getRight(), null, Lists.newArrayList(objectId));
                    } else if (RunningLogObjectCategpryEnum.SPACE.getCode().equals(objectBusinessFeildDTO.getObjType())) {
                        // 空间与系统的关系
                        systemObjectList = bizDtpRwdService.queryObjectList(groupCode, projectId, null,
                                DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE.value().getLeft(), DtpRwdRelationTypeEnum.SYSTEM_SERVICE_SPACE.value().getRight(), null, Lists.newArrayList(objectId));
                    } else {
                        // 设备级别
                        systemObjectList = bizDtpRwdService.queryObjectList(groupCode, projectId, null,
                                DtpRwdRelationTypeEnum.EQ_IN_SYSTEM.value().getLeft(), DtpRwdRelationTypeEnum.EQ_IN_SYSTEM.value().getRight(), null, Lists.newArrayList(objectId));
                    }
                }

                if (CollectionUtil.isNotEmpty(systemObjectList)) {
                    // 末端后续不在区分系统
                    String systemClassCode = systemObjectList.get(0).getString("classCode");
                    if ("ACCCCP".equals(classCode) && DtpRwdSystemClassCodeEnum.ACAT.value().equals(systemClassCode)) {
                        objectBusinessFeildDTO.setSystemId(DtpRwdSystemClassCodeEnum.AIR_TERMINAL.value());
                        objectBusinessFeildDTO.setSystemType(DtpRwdSystemClassCodeEnum.AIR_TERMINAL);
                    } else {
                        objectBusinessFeildDTO.setSystemId(systemObjectList.get(0).getString("id"));
                        objectBusinessFeildDTO.setSystemType(DtpRwdSystemClassCodeEnum.analyze(systemClassCode));
                    }
                }
            }
        }
        // 不允许所属模块为空
        if (objectBusinessFeildDTO == null || StrUtil.isBlank(objectBusinessFeildDTO.getSystemId()) || objectBusinessFeildDTO.getSystemType() == null) {
            return null;
        }
        return objectBusinessFeildDTO;
    }

    /**
     * 获取本方业务判断所需要的扩展静态字段信息
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @return
     */
    public RwdObjectBusinessFeildDTO getObjectBusinessFeild(String groupCode, String projectId, String objectId) {
        if (StrUtil.isBlank(objectId)) {
            return null;
        }
        RwdObjectBusinessFeildDTO businessFeildDTO = LocalCacheConstant.OBJECT_DETAIL_MAP.getIfPresent(objectId);
        if (businessFeildDTO == null) {
            try {
                List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(objectId));
                if (CollectionUtil.isNotEmpty(objectList)) {
                    JSONObject jsonObject = objectList.get(0);
                    RwdObjectBusinessFeildDTO objectBusinessFeildDTO = new RwdObjectBusinessFeildDTO(objectId, jsonObject.getString("localId"), jsonObject.getString("localName"),
                            jsonObject.getString("classCode"), jsonObject.getString("objType"));
                    objectBusinessFeildDTO.setFanPos(jsonObject.getString("fanPos"));
                    objectBusinessFeildDTO.setFuncType(jsonObject.getString("funcType"));
                    objectBusinessFeildDTO.setCameraType(jsonObject.getString("cameraType"));
                    objectBusinessFeildDTO.setInstallType(jsonObject.getString("installType"));
                    LocalCacheConstant.OBJECT_DETAIL_MAP.put(objectId, objectBusinessFeildDTO);
                    return objectBusinessFeildDTO;
                }
            } catch (Exception e) {
                log.error("物理世界对象详情数据查询失败，对象ID-{}", objectId, e);
            }
        }

        return null;
    }

    /**
     * 根据对象及其类型，过滤绑点规则
     *
     * @param objectIds
     * @param groupCode
     * @param projectId
     * @param classCode    末端下的组空与吊空比较特殊，可以采用 EnumTEqClassCode 下的 localInfoCode
     * @param infoCodeType
     * @param control      control null-所有, true-仅控制，false-非控制
     * @return
     */
    public Map<String, List<RcInfoCodeCfg>> getInfoCodeConfigsByRule(List<String> objectIds, String groupCode, String projectId, String classCode, DemandInfoCodeTypeEnum infoCodeType, Boolean control) {
        // 获取信息点定义
        List<RcInfoCodeCfg> infoCodeConfigs = this.getInfoCodeConfigs(classCode, infoCodeType, control);
        if (CollectionUtil.isEmpty(infoCodeConfigs)) {
            return new HashMap<>();
        }

        // 过滤规则
        return this.filterRuleByInfoCodeConfigs(infoCodeConfigs, objectIds, groupCode, projectId, classCode);
    }

    /**
     * 根据对象及其类型，过滤绑点规则
     *
     * @param objectIds
     * @param groupCode
     * @param projectId
     * @param classCode 末端下的组空与吊空比较特殊，可以采用 EnumTEqClassCode 下的 localInfoCode
     * @return
     */
    public Map<String, List<RcInfoCodeCfg>> getInfoCodeConfigsByRule(List<String> objectIds, String groupCode, String projectId, String classCode) {
        // 获取信息点定义
        List<RcInfoCodeCfg> infoCodeConfigs = this.getInfoCodeConfigs(classCode);
        if (CollectionUtil.isEmpty(infoCodeConfigs)) {
            return new HashMap<>();
        }

        // 过滤规则
        return this.filterRuleByInfoCodeConfigs(infoCodeConfigs, objectIds, groupCode, projectId, classCode);
    }

    /**
     * 根据绑定得信息点列表找出最多的infocode返回
     *
     * @return
     */
    public List<String> getInfoCodeConfigsByRuleMaxInfoCode(Map<String, List<RcInfoCodeCfg>> infoCodeConfigsByRule) {
        List<List<RcInfoCodeCfg>> values = new ArrayList<>(infoCodeConfigsByRule.values());
        List<RcInfoCodeCfg> maxRcInfoCodeCfg = values.get(0);
        int max = values.get(0).size(); // 将第一个元素设为最大值

        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).size() > max) {
                max = values.get(i).size(); // 如果找到更大的值，则更新最大值
                maxRcInfoCodeCfg = values.get(i);
            }
        }
        List<String> infoCodeList = new ArrayList<>();
        for (RcInfoCodeCfg rcInfoCodeCfg : maxRcInfoCodeCfg) {
            infoCodeList.add(rcInfoCodeCfg.getInfoCode());
        }
        return infoCodeList;

    }

    /**
     * 根据对象及其类型，过滤绑点规则
     *
     * @param objectIds
     * @param groupCode
     * @param projectId
     * @param classCode 末端下的组空与吊空比较特殊，可以采用 EnumTEqClassCode 下的 localInfoCode
     * @return
     */
    public Map<String, List<RcInfoCodeCfg>> getControlInfoCodeConfigsByRule(List<String> objectIds, String groupCode, String projectId, String classCode) {
        // 获取信息点定义
        List<RcInfoCodeCfg> infoCodeConfigs = this.getControlInfoCodeConfigs(classCode);
        if (CollectionUtil.isEmpty(infoCodeConfigs)) {
            return new HashMap<>();
        }

        // 过滤规则
        return this.filterRuleByInfoCodeConfigs(infoCodeConfigs, objectIds, groupCode, projectId, classCode);
    }

    /**
     * 获取某功能定制信息点配置集合
     *
     * @param classCode，末端下的组空与吊空比较特殊，可以采用 EnumTEqClassCode 下的 localInfoCode
     * @param infoCodeType
     * @param control                      null-所有, true-仅控制，false-非控制
     * @return
     */
    public List<RcInfoCodeCfg> getInfoCodeConfigs(String classCode, DemandInfoCodeTypeEnum infoCodeType, Boolean control) {
        List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
        if (infoCodeType == null || StrUtil.isBlank(classCode)) {
            return rcInfoCodeCfgs;
        }
        try {
            Object value = this.redisCacheUtil.hGet(RedisKey.buildDemandTypeInfoCodeConfigStringKey(infoCodeType.getValue()), classCode);
            Set<String> infoCodes = value == null ? null : new HashSet<>((ArrayList<String>) value);
            if (CollectionUtil.isEmpty(infoCodes)) {
                return rcInfoCodeCfgs;
            }
            for (String infoCode : infoCodes) {
                Object o = this.redisCacheUtil.get(RedisKey.buildControlInfoCodeConfigStringKey(classCode, infoCode));
                if (o == null && !BooleanUtil.isTrue(control)) {
                    o = this.redisCacheUtil.get(RedisKey.buildInfoCodeConfigStringKey(classCode, infoCode));
                }
                RcInfoCodeCfg rcInfoCodeCfg = o == null ? null : JsonUtils.fromJson(JsonUtils.toJson(o), new TypeReference<RcInfoCodeCfg>() {
                });
                //if (rcInfoCodeCfg == null || "-1".equals(rcInfoCodeCfg.getView())) {
                if (rcInfoCodeCfg == null) {
                    continue;
                }
                if (control == null) {
                    rcInfoCodeCfgs.add(rcInfoCodeCfg);
                } else if (control && rcInfoCodeCfg.getExtendInfo() != null && BooleanUtil.isTrue(rcInfoCodeCfg.getExtendInfo().getControl())) {
                    rcInfoCodeCfgs.add(rcInfoCodeCfg);
                } else if (!control && (rcInfoCodeCfg.getExtendInfo() == null || !BooleanUtil.isTrue(rcInfoCodeCfg.getExtendInfo().getControl()))) {
                    rcInfoCodeCfgs.add(rcInfoCodeCfg);
                }
            }
            rcInfoCodeCfgs.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
        } catch (Exception e) {
            log.info("信息点配置加载异常，类型：{}", infoCodeType, e);
        }
        return rcInfoCodeCfgs;
    }

    /**
     * 获取控制信息点的配置数据
     *
     * @param classCode 末端下的组空与吊空比较特殊，可以采用 EnumTEqClassCode 下的 localInfoCode
     * @return
     */
    public List<RcInfoCodeCfg> getControlInfoCodeConfigs(String classCode) {
        List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
        try {
            Set<String> keys = this.redisCacheExpUtil.scanKeys(RedisKey.buildControlInfoCodeConfigStringKey(classCode, "*"));
            if (CollectionUtil.isEmpty(keys)) {
                return rcInfoCodeCfgs;
            }
            List<Object> objects = this.redisCacheUtil.mGet(keys);
            List<RcInfoCodeCfg> infoCodes = objects == null ? null : JsonUtils.fromJson(JsonUtils.toJson(objects), new TypeReference<List<RcInfoCodeCfg>>() {
            });
            if (CollectionUtil.isNotEmpty(infoCodes)) {
                for (RcInfoCodeCfg infoCode : infoCodes) {
                    //if (rcInfoCodeCfg != null && !"-1".equals(rcInfoCodeCfg.getView())) {
                    if (infoCode != null) {
                        rcInfoCodeCfgs.add(infoCode);
                    }
                }
            }
            rcInfoCodeCfgs.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
        } catch (Exception e) {
            log.info("信息点配置加载异常", e);
        }
        return rcInfoCodeCfgs;
    }

    /**
     * 获取信息点配置
     *
     * @param classCode
     * @return
     */
    public List<RcInfoCodeCfg> getInfoCodeConfigs(String classCode) {
        List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
        try {
            Set<String> keys = this.redisCacheExpUtil.scanKeys(RedisKey.buildInfoCodeConfigStringKey(classCode, "*"));
            if (CollectionUtil.isEmpty(keys)) {
                return rcInfoCodeCfgs;
            }
            List<Object> objects = this.redisCacheUtil.mGet(keys);
            List<RcInfoCodeCfg> infoCodes = objects == null ? null : JsonUtils.fromJson(JsonUtils.toJson(objects), new TypeReference<List<RcInfoCodeCfg>>() {
            });
            if (CollectionUtil.isNotEmpty(infoCodes)) {
                for (RcInfoCodeCfg infoCode : infoCodes) {
                    //if (rcInfoCodeCfg != null && !"-1".equals(rcInfoCodeCfg.getView())) {
                    if (infoCode != null) {
                        rcInfoCodeCfgs.add(infoCode);
                    }
                }
            }
            rcInfoCodeCfgs.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
        } catch (Exception e) {
            log.info("信息点配置加载异常", e);
        }
        return rcInfoCodeCfgs;
    }


    /**
     * 批量获取信息点配置
     *
     * @param classCodes classCode集合
     * @return {@link List }<{@link RcInfoCodeCfg }>
     */
    public List<RcInfoCodeCfg> listInfoCodeConfigs(Collection<String> classCodes) {
        List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
        try {
            Set<String> keys = new HashSet<>();
            classCodes.forEach(classCode -> keys.addAll(this.redisCacheExpUtil.scanKeys(RedisKey.buildInfoCodeConfigStringKey(classCode, "*"))));
            if (CollectionUtil.isEmpty(keys)) {
                return rcInfoCodeCfgs;
            }
            List<Object> objects = this.redisCacheUtil.mGet(keys);
            List<RcInfoCodeCfg> infoCodes = objects == null ? null : JsonUtils.fromJson(JsonUtils.toJson(objects), new TypeReference<List<RcInfoCodeCfg>>() {
            });
            if (CollectionUtil.isNotEmpty(infoCodes)) {
                for (RcInfoCodeCfg infoCode : infoCodes) {
                    //if (rcInfoCodeCfg != null && !"-1".equals(rcInfoCodeCfg.getView())) {
                    if (infoCode != null) {
                        rcInfoCodeCfgs.add(infoCode);
                    }
                }
            }
            rcInfoCodeCfgs.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
        } catch (Exception e) {
            log.info("信息点配置加载异常", e);
        }
        return rcInfoCodeCfgs;
    }

    /**
     * 批量获取信息点配置，按classCode分组
     *
     * @param classCodes classCode集合
     * @return
     */
    public Map<String, List<RcInfoCodeCfg>> mapListInfoCodeConfigs(Collection<String> classCodes) {
        List<RcInfoCodeCfg> rcInfoCodeCfgs = listInfoCodeConfigs(classCodes);
        return rcInfoCodeCfgs.stream().collect(Collectors.groupingBy(RcInfoCodeCfg::getClassCode, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 获取信息点配置
     *
     * @param classCode
     * @param dynamicParameter true IOT参数
     * @param settingParameter true 设定参数
     * @return
     */
    public List<RcInfoCodeCfg> getInfoCodeConfigs(String classCode, boolean dynamicParameter, boolean settingParameter) {
        List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
        try {
            Set<String> keys = this.redisCacheExpUtil.scanKeys(RedisKey.buildInfoCodeConfigStringKey(classCode, "*"));
            if (CollectionUtil.isEmpty(keys)) {
                return rcInfoCodeCfgs;
            }
            List<Object> objects = this.redisCacheUtil.mGet(keys);
            List<RcInfoCodeCfg> infoCodes = objects == null ? null : JsonUtils.fromJson(JsonUtils.toJson(objects), new TypeReference<List<RcInfoCodeCfg>>() {
            });
            if (CollectionUtil.isNotEmpty(infoCodes)) {
                for (RcInfoCodeCfg rcInfoCodeCfg : infoCodes) {
                    if (rcInfoCodeCfg == null) {
                        continue;
                    }
                    if (dynamicParameter && !rcInfoCodeCfg.isDynamicParameter()) {
                        continue;
                    }
                    if (settingParameter && !rcInfoCodeCfg.isSettingParameter()) {
                        continue;
                    }
                    rcInfoCodeCfgs.add(rcInfoCodeCfg);
                }
            }
            rcInfoCodeCfgs.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
        } catch (Exception e) {
            log.info("信息点配置加载异常", e);
        }
        return rcInfoCodeCfgs;
    }

    /**
     * 获取信息点配置
     *
     * @param classCode
     * @return
     */
    public List<RcInfoCodeCfg> getInfoCodeConfigs(String classCode, Set<String> infoCodes) {
        List<RcInfoCodeCfg> rcInfoCodeCfgs = new ArrayList<>();
        if (CollectionUtil.isEmpty(infoCodes)) {
            return rcInfoCodeCfgs;
        }
        try {
            Set<String> keys = new HashSet<>();
            for (String infoCode : infoCodes) {
                keys.add(RedisKey.buildInfoCodeConfigStringKey(classCode, infoCode));
            }
            List<Object> objects = this.redisCacheUtil.mGet(keys);
            List<RcInfoCodeCfg> rcInfoCodeCfgList = objects == null ? null : JsonUtils.fromJson(JsonUtils.toJson(objects), new TypeReference<List<RcInfoCodeCfg>>() {
            });
            if (CollectionUtil.isNotEmpty(rcInfoCodeCfgList)) {
                for (RcInfoCodeCfg rcInfoCodeCfg : rcInfoCodeCfgList) {
                    if (rcInfoCodeCfg != null) {
                        rcInfoCodeCfgs.add(rcInfoCodeCfg);
                    }
                }
            }
            rcInfoCodeCfgs.sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
        } catch (Exception e) {
            log.info("信息点配置加载异常", e);
        }
        return rcInfoCodeCfgs;
    }

    /**
     * 获取信息点配置Map
     *
     * @param classCode 类代码
     * @param infoCodes 信息代码
     * @return {@link Map }<<code>key:infoCode,value:RcInfoCodeCfg</code>>
     */
    public Map<String, RcInfoCodeCfg> mapInfoCodeConfigs(String classCode, Set<String> infoCodes) {
        return getInfoCodeConfigs(classCode, infoCodes).stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));
    }

    /**
     * 过滤出具有绑点规则的信息点
     *
     * @param infoCodeConfigs
     * @param objectIds
     * @param groupCode
     * @param projectId
     * @param classCode
     * @return
     */
    private Map<String, List<RcInfoCodeCfg>> filterRuleByInfoCodeConfigs(List<RcInfoCodeCfg> infoCodeConfigs, List<String> objectIds, String groupCode, String projectId, String classCode) {
        Map<String, List<RcInfoCodeCfg>> objectInfoCodeCfgMap = new HashMap<>();
        if (CollectionUtil.isEmpty(infoCodeConfigs)) {
            return objectInfoCodeCfgMap;
        }

        try {
            // 获取绑点规则
            Map<String, Set<String>> objectInfoCodes = this.abstractCommonServer.queryObjectsExistRuleMap(groupCode, projectId, Lists.newArrayList(new ClassCode(classCode)));
            if (CollectionUtil.isEmpty(objectInfoCodes)) {
                return objectInfoCodeCfgMap;
            }

            // 数据封装
            for (String objectId : objectIds) {
                List<RcInfoCodeCfg> list = new ArrayList<>();
                objectInfoCodeCfgMap.put(objectId, list);

                // 规则过滤
                Set<String> infoCodes = objectInfoCodes.get(objectId);
                for (RcInfoCodeCfg infoCodeConfig : infoCodeConfigs) {
                    // 动态信息点，不存在规则的，过滤掉
                    if (infoCodeConfig.isDynamicParameter() && (CollectionUtil.isEmpty(infoCodes) || !infoCodes.contains(infoCodeConfig.getInfoCode()))) {
                        continue;
                    }
                    RcInfoCodeCfg newCfg = JsonUtils.fromJson(JsonUtils.toJson(infoCodeConfig), new TypeReference<RcInfoCodeCfg>() {
                    });
                    list.add(newCfg);
                }
            }
        } catch (Exception e) {
            log.info("对象信息点配置，规则过滤异常", e);
        }
        return objectInfoCodeCfgMap;
    }

    /**
     * @return java.util.Map<java.lang.String, com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg>
     * @Author 薛涛
     * @Description 获取信息点配置MAP
     * @Date 16:53 2024/6/6
     * @Param [classCode]
     **/
    public Map<String, RcInfoCodeCfg> getInfoCodeConfigMap(String classCode) {
        List<RcInfoCodeCfg> infoCodeConfigs = getInfoCodeConfigs(classCode);
        Map<String, RcInfoCodeCfg> infoCodeConfigMap = new HashMap<>();
        if (CollectionUtil.isEmpty(infoCodeConfigs)) {
            return infoCodeConfigMap;
        }
        for (RcInfoCodeCfg infoCodeConfig : infoCodeConfigs) {
            infoCodeConfigMap.put(infoCodeConfig.getInfoCode(), infoCodeConfig);
        }
        return infoCodeConfigMap;
    }

    /**
     * 获取信息点配置
     *
     * @param classCode
     * @param infoCode
     * @return
     */
    public RcInfoCodeCfg getInfoCodeConfig(String classCode, String infoCode) {
        if (!StrUtil.isAllNotBlank(classCode, infoCode)) {
            return null;
        }
        RcInfoCodeCfg rcInfoCodeCfg = null;
        try {
            Object value = this.redisCacheUtil.get(RedisKey.buildInfoCodeConfigStringKey(classCode, infoCode));
            rcInfoCodeCfg = value == null ? null : JsonUtils.fromJson(JsonUtils.toJson(value), new TypeReference<RcInfoCodeCfg>() {
            });
        } catch (Exception e) {
            log.info("信息点配置加载异常", e);
        }
        return rcInfoCodeCfg;
    }

    /**
     * 批量获取信息点配置（根据 Redis Key）
     *
     * @param keys Redis Key 集合
     * @return Map<RedisKey, RcInfoCodeCfg>
     */
    public List<RcInfoCodeCfg> batchGetInfoCodeConfigs(Set<String> keys) {
        List<RcInfoCodeCfg> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(keys)) {
            return result;
        }
        try {
            List<Object> values = this.redisCacheUtil.mGet(keys.toArray(new String[0]));
            result = values.stream()
                    .filter(Objects::nonNull)
                    .map(value -> JsonUtils.fromJson(JsonUtils.toJson(value),
                            new TypeReference<RcInfoCodeCfg>() {
                            }))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("批量获取信息点配置异常", e);
        }

        return result;
    }

    /**
     * 获取信息点配置分组树
     *
     * @param topGroupId
     * @return
     */
    public RcInfoCodeGroupDTO getInfoCodeGroupDTO(String topGroupId) {
        RcInfoCodeGroupDTO rcInfoCodeGroupDTO = null;
        try {
            Object value = this.redisCacheUtil.hGet(RedisKey.buildInfoCodeConfigGroupHashKey(), topGroupId);
            rcInfoCodeGroupDTO = value == null ? null : JsonUtils.fromJson(JsonUtils.toJson(value), new TypeReference<RcInfoCodeGroupDTO>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rcInfoCodeGroupDTO;
    }

    /**
     * 获取某张系统图下所有的设备ID集合
     *
     * @param projectId
     * @param systemId
     * @param systemClassCode
     * @param graphTypeCode
     * @return
     */
    public Set<String> getObjectIdsBySystemGraph(String projectId, String systemId, String systemClassCode, String graphTypeCode) {
        return this.systemRelationGraphHandler.getObjectIdsBySystemGraph(projectId, systemId, systemClassCode, graphTypeCode);
    }

    /**
     * 根据系统ID，获取一键手自动切换的对象集
     *
     * @param projectId
     * @param systemGraphId
     * @param systemClassCode
     * @return
     */
    public Map<String, Set<String>> getObjectIdsBySystemId(String projectId, String systemId, Long systemGraphId,
                                                           String systemClassCode) {
        if (systemGraphId != null) {
            return this.systemRelationGraphHandler.getObjectIdsBySystemGraphClass(projectId, systemGraphId,
                    systemClassCode);
        }
        String graphTypeCode = "ACCC".equals(systemClassCode) ? "供水关系图" : "源端供水关系图";
        return this.systemRelationGraphHandler.getObjectIdsBySystemGraphClass(projectId, systemId, systemClassCode,
                graphTypeCode);
    }

    /**
     * 根据设备类编码，获取设备类名称
     *
     * @param classCode
     * @return
     */
    public String getClassName(String classCode) {
        //return this.dtpKnowledgeClientService.listTypeByCode(classCode);
        String o = this.redisCacheUtil.hGet(RedisKey.CLASS_NAME, classCode);
        return StrUtil.isBlank(o) ? StrUtil.EMPTY : o;
    }

    /**
     * 重置当前项目的所有与业务相关的本地或redis缓存
     */
    public synchronized void resetGlobalLocalRedisCache(TerminalEnum terminal, boolean updateCache) {
        if (updateCache) {
            LocalCacheConstant.CUSTOM_FUNCTION = new HashMap<>();
            LocalCacheConstant.SYSTEM_RUN_PARAM_MAP = new HashMap<>();
            LocalCacheConstant.NODE_TOP_ID = new HashMap<>();
            LocalCacheConstant.TOP_GROUP_TREE = new HashMap<>();
        }

        // 重置 信息点配置缓存
        this.resetInfoCodeConfigCache(terminal, updateCache);

        // 重置系统运行参数配置，必须在信息点配置缓存之后
        this.resetSystemRunParam(terminal);
    }

    /**
     * 重置当前项目的所有与业务相关的本地或redis缓存，先不考虑信息点删除的情况，更新或新增
     */
    private void resetInfoCodeConfigCache(TerminalEnum terminal, boolean updateCache) {
        if (!updateCache) {
            log.info("========= 本次不加载信息点配置 =========");
            return;
        }

        List<RcInfoCodeCfg> rcInfoCodeCfgs = this.infoCodeCfgService.queryAllInfoCodeConfigs(null);
        if (CollectionUtil.isEmpty(rcInfoCodeCfgs)) {
            return;
        }

        // 获取所有的信息点配置
        log.info("========= 信息点配置加载开始 =========");

        // 1.删除旧版控制点位数据
        this.redisCacheExpUtil.delKeys(RedisKey.INFO_CODE_CONFIG_CONTROL_PREFIX + "*");

        List<PipelineRelationConfigVo> pipelineRelationConfigVos = commonConfigService.pipelineRelationConfigVos();
        List<String> collect = pipelineRelationConfigVos.stream().map(PipelineRelationConfigVo::getPipelineCode).collect(Collectors.toList());
        collect.add(DtpRwdClassCodeEnum.ACPPWC.value());
        Map<String, String> classCodeAndName = dtpKnowledgeClientService.mapClassCodeAndName(false, true, collect);

        // 2.信息点配置初次解析，优先解析分组信息
        Map<String, String> classCodes = new HashMap<>(classCodeAndName);
        List<RcInfoCodeGroupDTO> rcInfoCodeGroupDTOS = new ArrayList<>();
        for (RcInfoCodeCfg rcInfoCodeCfg : rcInfoCodeCfgs) {
            if (StrUtil.isNotBlank(rcInfoCodeCfg.getClassCode())) {
                classCodes.put(rcInfoCodeCfg.getClassCode(), rcInfoCodeCfg.getClassName());
            }
            // 分组情况
            if (InfoTypeEnum.GROUP.equals(rcInfoCodeCfg.getDataType())) {
                rcInfoCodeGroupDTOS.add(new RcInfoCodeGroupDTO(rcInfoCodeCfg.getId(), rcInfoCodeCfg.getName(),
                        rcInfoCodeCfg.getGroupInfo() == null ? "0" : rcInfoCodeCfg.getGroupInfo(), rcInfoCodeCfg.getOrdered(), null, null));
            }
        }

        // 3.查询中台信息点定义
        List<String> allClassCodes = Lists.newArrayList(classCodes.keySet());
        List<List<String>> partition = Lists.partition(allClassCodes, 20);
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeAndInfoCodeMap = new HashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (List<String> codes : partition) {
            futures.add(CompletableFuture.runAsync(() -> {
                Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> temp = this.dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(false, true, codes);
                if (CollectionUtil.isNotEmpty(temp)) {
                    classCodeAndInfoCodeMap.putAll(temp);
                }
            }));
        }
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 4.分组信息转为树形结构，并将每一个组ID，都对应对应的分组树，写入到缓存中
        rcInfoCodeGroupDTOS.sort(Comparator.comparing(RcInfoCodeGroupDTO::getOrdered, Comparator.nullsFirst(Integer::compareTo)));
        List<RcInfoCodeGroupDTO> infoCodeGroupDTOS = RcInfoCodeGroupDTO.convertGroupInfoTree(rcInfoCodeGroupDTOS);
        RcInfoCodeGroupDTO.initLocalCaheGroupInfo(infoCodeGroupDTOS);

        // 5.解析当前项目的信息点配置数据，并填充扩展字段
        Map<String, Map<String, Set<String>>> class_info_code_demand_map = new HashMap<>();
        //拿到所有的classCode
        StopWatch stopWatch = StopWatch.create();
        Set<String> classCodeSet = rcInfoCodeCfgs.stream().map(RcInfoCodeCfg::getClassCode).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        stopWatch.start("删除信息点配置缓存");
        for (String classCode : classCodeSet) {
            Set<String> deleteKey = redisCacheExpUtil.scanKeys(RedisKey.buildInfoCodeConfigStringKey(classCode, "*"));
            logInstance().debug("删除信息点配置缓存", Arrays.asList(Pair.of("classCode", classCode), Pair.of("size", deleteKey.size())));
            if (CollectionUtils.isNotEmpty(deleteKey)) {
                this.redisCacheUtil.del(deleteKey);
            }
        }
        stopWatch.stop();
        stopWatch.start("解析信息点配置数据");
        //配置数据map
        Map<String, Object> rcInfoCodeCfgDataMap = new HashMap<>();
        //控制点位map
        Map<String, Object> controlDataMap = new HashMap<>();
        for (RcInfoCodeCfg rcInfoCodeCfg : rcInfoCodeCfgs) {
            if (InfoTypeEnum.GROUP.equals(rcInfoCodeCfg.getDataType())) {
                continue;
            }
            String infoCode = rcInfoCodeCfg.getInfoCode();
            String classCode = rcInfoCodeCfg.getClassCode();
            rcInfoCodeCfg.setTopGroupId(LocalCacheConstant.NODE_TOP_ID.get(rcInfoCodeCfg.getGroupInfo()));

            // 动态数据的赋值
            DtpKnowledgeInfoCodeDTO infoCodeDefine = classCodeAndInfoCodeMap.get(rcInfoCodeCfg.getClassCode()) == null ? null : classCodeAndInfoCodeMap.get(rcInfoCodeCfg.getClassCode()).get(rcInfoCodeCfg.getInfoCode());
            String unit = infoCodeDefine == null ? null : infoCodeDefine.getUnit();
            String dataType = infoCodeDefine == null ? null : infoCodeDefine.getDataType();
            Integer iotDecimal = infoCodeDefine == null ? null : infoCodeDefine.getScale();
            if ("BOOLEAN".equalsIgnoreCase(dataType) || "INTEGER".equalsIgnoreCase(dataType) || "ENUM".equalsIgnoreCase(dataType)) {
                iotDecimal = 0;
            }

            List<DtpKnowledgeInfoCodeDataSourceDTO> dataSource = infoCodeDefine == null ? null : infoCodeDefine.getDataSources();
            rcInfoCodeCfg.setDataSource(CollectionUtil.isNotEmpty(dataSource) ? dataSource : rcInfoCodeCfg.getDataSource());
            rcInfoCodeCfg.setDataType(rcInfoCodeCfg.getDataType() != null ? rcInfoCodeCfg.getDataType() : (dataSource == null || dataSource.size() == 0 ? InfoTypeEnum.STRING : InfoTypeEnum.ENUM));
            rcInfoCodeCfg.setUnit(Objects.nonNull(unit) ? unit : rcInfoCodeCfg.getUnit());
            rcInfoCodeCfg.setIotDecimal(rcInfoCodeCfg.getExtendInfo() == null || rcInfoCodeCfg.getExtendInfo().getIotDecimal() == null ? iotDecimal : rcInfoCodeCfg.getExtendInfo().getIotDecimal());
            rcInfoCodeCfg.setOriginDataType(infoCodeDefine != null ? infoCodeDefine.getOriginDataType() : null);
            rcInfoCodeCfg.setName(infoCodeDefine != null ? infoCodeDefine.getName() : null);
            // 设置参数类型
            if (DtpRwdTagEnum.RUN_PARAM.value().equals(rcInfoCodeCfg.getFirstTag()) || DtpRwdTagEnum.SETTING_PARAM.value().equals(rcInfoCodeCfg.getFirstTag())
                    || DtpRwdTagEnum.EVENT_PARAM.value().equals(rcInfoCodeCfg.getFirstTag()) || DtpRwdTagEnum.EVENT_RECORD.value().equals(rcInfoCodeCfg.getFirstTag())
                    || DtpRwdTagEnum.CONTROL_PARAM.value().equals(rcInfoCodeCfg.getFirstTag())) {
                rcInfoCodeCfg.setParameterType(DtpRwdTagEnum.RUN_PARAM.value());
            }

            // 处理扩展信息
            InfoCodeConfigExtendInfo extendInfo = rcInfoCodeCfg.getExtendInfo();
            if (extendInfo != null) {
                rcInfoCodeCfg.setView(extendInfo.getView());
                rcInfoCodeCfg.setForce(extendInfo.getForce());
                rcInfoCodeCfg.setImportanceLevel(extendInfo.getImportanceLevel());

                // 额外信息点表达式的缓存
                if (extendInfo.getTriggerFormulaInfo() != null) {
                    this.redisCacheUtil.hSet(RedisKey.buildInfoCodeConfigTriggerHashKey(classCode), rcInfoCodeCfg.getInfoCode(), extendInfo.getTriggerFormulaInfo());
                }

                // 设置需求类型缓存信息点
                if (CollectionUtil.isNotEmpty(extendInfo.getDemandType())) {
                    for (String demandType : extendInfo.getDemandType()) {
                        if (!class_info_code_demand_map.containsKey(demandType)) {
                            class_info_code_demand_map.put(demandType, new HashMap<>());
                        }
                        class_info_code_demand_map.get(demandType).computeIfAbsent(rcInfoCodeCfg.getClassCode(), e -> Sets.newHashSet()).add(rcInfoCodeCfg.getInfoCode());
                    }
                }

                // 控制信息点单独存储，如果当前属于控制信息点位，不放入纯展示的redis
                if (BooleanUtil.isTrue(extendInfo.getControl())) {
                    if ("ACATAHG".equals(rcInfoCodeCfg.getClassCode()) || "ACATAHZ".equals(rcInfoCodeCfg.getClassCode())) {
                        rcInfoCodeCfg.setClassCode(DtpRwdClassCodeEnum.ACATAH.getValue());
                    }
                    controlDataMap.put(RedisKey.buildControlInfoCodeConfigStringKey(classCode, infoCode), rcInfoCodeCfg);
                    continue;
                }
            }

            // 刷新信息点配置的缓存，非控制参数类, 如果是展示类，字典不存在则不放入缓存
            if (Objects.nonNull(infoCodeDefine)) {
                rcInfoCodeCfgDataMap.put(RedisKey.buildInfoCodeConfigStringKey(classCode, infoCode), rcInfoCodeCfg);
            }
        }
        //批量插入
        if (MapUtils.isNotEmpty(controlDataMap)) {
            logInstance().debug("刷新控制信息点配置缓存", Pair.of("controlDataMap.size", controlDataMap.size()));
            this.redisCacheUtil.mSet(controlDataMap);
        }
        if (MapUtils.isNotEmpty(rcInfoCodeCfgDataMap)) {
            logInstance().debug("刷新信息点配置缓存", Pair.of("rcInfoCodeCfgDataMap.size", rcInfoCodeCfgDataMap.size()));
            this.redisCacheUtil.mSet(rcInfoCodeCfgDataMap);
        }
        stopWatch.stop();

        stopWatch.start("刷新信息点分组");
        // 3.刷新信息点分组
        String topHashKey = RedisKey.buildInfoCodeConfigGroupHashKey();
        for (Map.Entry<String, RcInfoCodeGroupDTO> groupDTOEntry : LocalCacheConstant.TOP_GROUP_TREE.entrySet()) {
            this.redisCacheUtil.hSet(topHashKey, groupDTOEntry.getKey(), groupDTOEntry.getValue());
        }
        stopWatch.stop();

        stopWatch.start("需求类型下的信息点集合数据缓存");
        // 4.需求类型下的信息点集合数据缓存
        if (!class_info_code_demand_map.isEmpty()) {
            for (Map.Entry<String, Map<String, Set<String>>> stringMapEntry : class_info_code_demand_map.entrySet()) {
                String demandType = stringMapEntry.getKey();
                Map<String, Set<String>> classInfo = stringMapEntry.getValue();
                for (Map.Entry<String, Set<String>> entry : classInfo.entrySet()) {
                    this.redisCacheUtil.hSet(RedisKey.buildDemandTypeInfoCodeConfigStringKey(demandType), entry.getKey(), entry.getValue());
                }
            }
        }
        stopWatch.stop();

        // 5.名称映射
        stopWatch.start("名称映射");
        if (classCodes.size() > 0) {
            for (Map.Entry<String, String> stringEntry : classCodes.entrySet()) {
                this.redisCacheUtil.hSet(RedisKey.CLASS_NAME, stringEntry.getKey(), stringEntry.getValue());
            }
        }
        stopWatch.stop();
        logInstance().debug("信息点配置加载完成", Pair.of("stopWatch", stopWatch.prettyPrint()));
    }

    /**
     * 获取系统信息
     *
     * @param groupCode
     * @param projectId
     */
    public BasePropertyDTO getSystemPropertyDTO(String groupCode, String projectId, String systemId) {
        if (StrUtil.isBlank(systemId)) {
            return null;
        }
        BasePropertyDTO basePropertyDTO = LocalCacheConstant.SYSTEM_INFO.get(systemId);
        if (basePropertyDTO == null) {
            this.resetSystemCache(groupCode, projectId);
        }
        return LocalCacheConstant.SYSTEM_INFO.get(systemId);
    }

    /**
     * 根据系统ID，获取对应的系统编码枚举类型
     *
     * @param systemId
     * @return
     */
    public DtpRwdSystemClassCodeEnum getSystemType(String groupCode, String projectId, String systemId) {
        BasePropertyDTO systemPropertyDTO = this.getSystemPropertyDTO(groupCode, projectId, systemId);
        return systemPropertyDTO == null ? null : DtpRwdSystemClassCodeEnum.analyze(systemPropertyDTO.getClassCode());
    }

    /**
     * 重置系统缓存
     *
     * @param groupCode
     * @param projectId
     */
    public void resetSystemCache(String groupCode, String projectId) {
        // 1.获取所有系统
        List<JSONObject> systems = this.bizDtpRwdService.queryObjectList(groupCode, projectId, LocalCacheConstant.SYSTEM_CLASS, null, null, null, null);
        if (CollectionUtil.isEmpty(systems)) {
            return;
        }
        for (JSONObject system : systems) {
            String localName = system.getString("localName");
            BasePropertyDTO propertyDTO = new BasePropertyDTO();
            propertyDTO.setId(system.getString("id"));
            propertyDTO.setLocalId(system.getString("localId"));
            propertyDTO.setLocalName(StrUtil.isBlank(localName) ? system.getString("name") : localName);
            propertyDTO.setClassCode(system.getString("classCode"));
            LocalCacheConstant.SYSTEM_INFO.put(system.getString("id"), propertyDTO);
        }
    }

    /**
     * 重置系统运行参数配置
     *
     * @return
     */
    public void resetSystemRunParam(TerminalEnum terminal) {
        // 1.获取系统运行参数 与 图配置数据
        List<SystemRunParamVo> systemRunParam = this.commonConfigService.getSystemRunParam();
        List<GraphConfigVo> graphConfig = this.commonConfigService.getGraphConfig();
        if (CollectionUtil.isEmpty(systemRunParam) || CollectionUtil.isEmpty(graphConfig)) {
            return;
        }

        // 2. 图名 -> 唯一key
        Set<String> classCodes = new HashSet<>();
        Map<String, String> imageNameToCodeMap = new HashMap<>();
        for (GraphConfigVo graphConfigVo : graphConfig) {
            imageNameToCodeMap.put(graphConfigVo.getMapTitle(), getSystemRunParamUniqueKey(
                    graphConfigVo.getDiagramBaseObjectCode(), graphConfigVo.getDiagramTypeOfTheDiagram(), graphConfigVo.getMapTitle()));
            classCodes.add(graphConfigVo.getDiagramBaseObjectCode());
        }

        // 3.查询中台信息点定义
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeAndInfoCodeMap = this.dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(false, false, Lists.newArrayList(classCodes));

        // 4.扩展信息写入
        Map<String, Set<String>> classCodeTemp = new HashMap<>();
        Map<String, Set<String>> classInfoCode = new HashMap<>();
        Map<String, Map<String, RcInfoCodeGroupDTO>> groupDTOMap = new HashMap<>();
        for (int i = 0; i < systemRunParam.size(); i++) {
            SystemRunParamVo systemRunParamVo = systemRunParam.get(i);
            String systemRunParamUniqueKey = imageNameToCodeMap.get(systemRunParamVo.getMapTitle());
            if (StrUtil.isBlank(systemRunParamUniqueKey)) {
                continue;
            }
            String classCode = systemRunParamUniqueKey.split("#")[0];

            DtpKnowledgeInfoCodeDTO infoCodeDefine = classCodeAndInfoCodeMap.get(classCode) == null ? null : classCodeAndInfoCodeMap.get(classCode).get(systemRunParamVo.getParamCode());
            String unit = infoCodeDefine == null ? null : infoCodeDefine.getUnit();
            Integer scale = infoCodeDefine == null ? null : infoCodeDefine.getScale();
            List<DtpKnowledgeInfoCodeDataSourceDTO> dataSource = infoCodeDefine == null ? null : infoCodeDefine.getDataSources();

            String order = i + "";
            String group = systemRunParamVo.getGroup();
            String infoCode = systemRunParamVo.getParamCode();
            String infoName = systemRunParamVo.getParamName();
            if (!StrUtil.isAllNotBlank(classCode, group, infoCode, infoName)) {
                continue;
            }

            String isCustomize = systemRunParamVo.getIsCustomize();
            if ("1".equals(isCustomize) || "2".equals(isCustomize)) {
                if (!classCodeTemp.containsKey(systemRunParamUniqueKey) || CollectionUtil.isEmpty(classCodeTemp.get(systemRunParamUniqueKey)) || !classCodeTemp.get(systemRunParamUniqueKey).contains(infoCode)) {
                    LocalCacheConstant.CUSTOM_FUNCTION.computeIfAbsent(systemRunParamUniqueKey, e -> Lists.newArrayList()).add(new CustomFunctionDTO(order, infoCode, group, isCustomize));
                }
                classCodeTemp.computeIfAbsent(systemRunParamUniqueKey, e -> Sets.newHashSet()).add(infoCode);
                continue;
            }

            // 分组配置缓存
            if (!groupDTOMap.containsKey(systemRunParamUniqueKey)) {
                groupDTOMap.put(systemRunParamUniqueKey, new HashMap<>());
            }
            if (CollectionUtil.isEmpty(groupDTOMap.get(systemRunParamUniqueKey)) || !groupDTOMap.get(systemRunParamUniqueKey).containsKey(group)) {
                RcInfoCodeGroupDTO rcInfoCodeGroupDTO = new RcInfoCodeGroupDTO(order, group, "0", i, null, Lists.newArrayList());
                groupDTOMap.get(systemRunParamUniqueKey).put(group, rcInfoCodeGroupDTO);
                LocalCacheConstant.SYSTEM_RUN_PARAM_MAP.computeIfAbsent(systemRunParamUniqueKey, e -> Lists.newArrayList()).add(rcInfoCodeGroupDTO);
            }
            RcInfoCodeGroupDTO rcInfoCodeGroupDTO = groupDTOMap.get(systemRunParamUniqueKey).get(group);

            RcInfoCodeCfg infoCodeCfg = this.getInfoCodeConfig(classCode, infoCode);
            if (infoCodeCfg == null) {
                infoCodeCfg = new RcInfoCodeCfg();
                infoCodeCfg.setId(order);
                infoCodeCfg.setInfoCode(infoCode);
                infoCodeCfg.setName(infoName);
                infoCodeCfg.setClassCode(classCode);
                infoCodeCfg.setOrdered(i);
                infoCodeCfg.setGroupInfo(group);
                infoCodeCfg.setIotDecimal(scale);
                infoCodeCfg.setUnit(unit);
                infoCodeCfg.setFirstTag(systemRunParamVo.getInfoType());
                infoCodeCfg.setDataSource(CollectionUtil.isEmpty(dataSource) ? null : dataSource);
                if (DtpRwdTagEnum.RUN_PARAM.value().equals(infoCodeCfg.getFirstTag())) {
                    infoCodeCfg.setParameterType(DtpRwdTagEnum.RUN_PARAM.value());
                }
            } else {
                infoCodeCfg.setName(infoName);
                infoCodeCfg.setOrdered(i);
            }

            String temp = classCode + infoCode;
            if (!classInfoCode.containsKey(systemRunParamUniqueKey) || CollectionUtil.isEmpty(classInfoCode.get(systemRunParamUniqueKey)) || !classInfoCode.get(systemRunParamUniqueKey).contains(temp)) {
                rcInfoCodeGroupDTO.getInfoCodes().add(infoCodeCfg);
                classInfoCode.computeIfAbsent(systemRunParamUniqueKey, e -> Sets.newHashSet()).add(temp);
            }
        }
    }


    /**
     * 临时的，为了兼容补水监测图的配置，仅 系统运行参数 查询时使用，后续修改接口，前端传递 图名 即可
     *
     * @param classCode
     * @param graphTypeCode
     * @return
     */
    public static String getSystemRunParamUniqueKey(String classCode, String graphTypeCode, String imageOrProductName) {
        return classCode + "#" + graphTypeCode + "#" + imageOrProductName;
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "redis缓存操作类";
    }
}
