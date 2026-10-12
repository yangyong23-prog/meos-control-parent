package com.persagy.apccontrol.basic.service.shellconfig;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.config.VelocityConfig;
import com.persagy.apccontrol.basic.controller.shellconfig.param.*;
import com.persagy.apccontrol.basic.model.base.BasicsConfigTypeEnum;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.base.QueryIOTEnum;
import com.persagy.apccontrol.basic.model.base.StateTypeEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFreezePumpDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.entity.RcBasicsConfig;
import com.persagy.apccontrol.basic.model.entity.ShellConfigGroup;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.system.SysResponseCode;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.WeatherStationVO;
import com.persagy.apccontrol.basic.outside.dto.AcccccGroupDTO;
import com.persagy.apccontrol.basic.outside.dto.ControlObjectDto;
import com.persagy.apccontrol.basic.outside.dto.EnergyDataResponse;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.response.knowledge.DtpKnowledgeObjectClassCodeResponse;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.RcBasicsConfigService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.utils.BeanUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.framework.common.exception.enums.ErrorCodeConstants;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.ValidationException;
import java.text.Collator;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShellConfigServiceImpl implements ShellConfigService {

    @Resource
    private BizDtpRwdService bizDtpRwdService;

    @Autowired
    private DtpRwdClientService dtpRwdClientService;

    @Autowired
    private RcBasicsConfigService rcBasicsConfigService;

    @Autowired
    private DtpKnowledgeClientService dtpKnowledgeClientService;

    @Autowired
    private AbstractIotDataCacheServer abstractIotDataCacheServer;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShellConfigServiceImpl.class);
    private static final String LOG_TAG = "壳配置";

    @Override
    public boolean changeAlgorithmConfig(String groupCode, String projectId, Boolean status) {
        RcBasicsConfig basicsConfig = new RcBasicsConfig();
        basicsConfig.setProjectId(projectId);
        basicsConfig.setType(BasicsConfigTypeEnum.ENABLE_ALGORITHM_CONFIG);
        basicsConfig.setSt(StateTypeEnum.VALID);
        basicsConfig.setResult(status == null ? null : String.valueOf(status));
        this.rcBasicsConfigService.saveOrUpdateByType(basicsConfig);
        return true;
    }

    @Override
    public boolean changeLtsmConfig(@NonNull LtsmConfigParam param) {
        RcBasicsConfig basicsConfig = new RcBasicsConfig();
        basicsConfig.setProjectId(ContextUtil.getProjectId());
        basicsConfig.setType(param.getType());
        basicsConfig.setSt(StateTypeEnum.VALID);
        basicsConfig.setResult(String.valueOf(param.getStatus()));
        this.rcBasicsConfigService.saveOrUpdateByType(basicsConfig);
        return true;
    }

    @Override
    public boolean getAlgorithmConfig(String groupCode, String projectId) {
        RcBasicsConfig basicsConfig = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, projectId)
                .eq(RcBasicsConfig::getType, BasicsConfigTypeEnum.ENABLE_ALGORITHM_CONFIG)
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
        return basicsConfig == null || StrUtil.isBlank(basicsConfig.getResult()) ? false : BooleanUtil.toBoolean(basicsConfig.getResult());
    }

    @Override
    public List<LtsmConfigParam> listLtsmConfig() {
        List<RcBasicsConfig> list = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, ContextUtil.getProjectId())
                .in(RcBasicsConfig::getType, Arrays.asList(BasicsConfigTypeEnum.ENABLE_LTSM_SPACE_CONFIG, BasicsConfigTypeEnum.ENABLE_LTSM_COOLING_CONFIG, BasicsConfigTypeEnum.ENABLE_LTSM_PUMP_CONFIG))
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .list();

        return list.stream().map(d -> {
            LtsmConfigParam param = new LtsmConfigParam();
            param.setType(d.getType());
            param.setStatus(BooleanUtil.toBoolean(d.getResult()));
            return param;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ShellConfigVO> query(String groupCode, QueryShellConfigParam param) {
        List<ShellConfigVO> resList = new ArrayList<>();
        // 获取项目下的壳配置数据
        RcBasicsConfig basicsConfig = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, param.getProjectId())
                .eq(RcBasicsConfig::getType, ShellConfigSceneEnum.convertToBasicConfigTypeByScene(param.getScene()))
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
        ShellConfigVO vo = new ShellConfigVO();
        // 获取默认值
        List<ShellConfigItem> items = ShellConfigItemEnum.getListByScene(param.getScene());
        // 按照分组和tab整理数据
        if (CollectionUtils.isNotEmpty(items) && CollectionUtils.isNotEmpty(param.getGroups())) {
            items = items.stream().filter(f -> param.getGroups().contains(f.getGroup())).collect(Collectors.toList());
        }
        List<ShellConfigGroup> groups = items.stream().collect(Collectors.groupingBy(d -> d.getGroup() + "," + d.getTab())).entrySet().stream().map(d -> {
            ShellConfigGroup group = new ShellConfigGroup();
            group.setGroup(d.getKey().split(",")[0]);
            group.setTab(d.getKey().split(",")[1]);
            group.setItems(d.getValue());
            return group;
        }).collect(Collectors.toList());
        if(ShellConfigSceneEnum.SCENE_MPC.getCode().equals(param.getScene())) {
            //复制一份
            ShellConfigGroup mpcAgentAcac = groups.stream().filter(g -> g.getGroup().equals(ShellConfigGroupEnum.GROUP_MPC_AGENT_ACAC.getCode())).findFirst().orElse(null);
            ShellConfigGroup mpcAgentAcai = new ShellConfigGroup();
            mpcAgentAcai.setGroup(ShellConfigGroupEnum.GROUP_MPC_AGENT_ACAI.getCode());
            List<ShellConfigItem> shellConfigItems = BeanUtil.listCopyProperties(mpcAgentAcac.getItems(), ShellConfigItem.class);
            shellConfigItems.stream().forEach(item -> item.setGroup(ShellConfigGroupEnum.GROUP_MPC_AGENT_ACAI.getCode()));
            mpcAgentAcai.setItems(shellConfigItems);
            groups.add(mpcAgentAcai);
        }
        vo.setDesc(ShellConfigSceneEnum.getByCode(param.getScene()).getDesc());
        // 组装数据
        vo.setGroups(getConfigValueByScene(groupCode, groups, param.getProjectId(), basicsConfig));
        resList.add(vo);
        return resList;
    }

    /**
     * 获取场景下的配置项数据
     *
     * @param groups
     * @param projectId
     * @return
     */
    public List<ShellConfigGroup> getConfigValueByScene(String groupCode, List<ShellConfigGroup> groups, String projectId, RcBasicsConfig basicsConfig) {
        if (CollectionUtils.isEmpty(groups)) {
            return new ArrayList<>();
        }
        List<ShellConfigGroup> values = new ArrayList<>();
        if (basicsConfig != null) {
            values = JSON.parseArray(basicsConfig.getResult(), ShellConfigGroup.class);
        }

        Set<ShellConfigGroup> deletedGroups = new ConcurrentHashSet<>();
        Map<List<String>, List<JSONObject>> classCodeObjectsCache = new HashMap<>();
        Map<String, Integer> acahcCountBySystemId = null;
        // 中央供冷系统参数配置分组下的数据需要查询系统数据进行重新组装
        for (ShellConfigGroup group : groups) {
            ShellConfigGroupEnum groupEnum = ShellConfigGroupEnum.getGroupEnumByCode(group.getGroup());
            List<ShellConfigGroup> shellConfigGroups = new ArrayList<>();
            try {
                shellConfigGroups = CollectionUtil.isEmpty(values) ? new ArrayList<>() :
                        values.stream().filter(x -> x.getGroup().equals(group.getGroup())).collect(Collectors.toList());
            } catch (Exception e) {
                log.error("获取场景下的配置项数据异常", e);
            }
            List<ShellConfigItem> shellConfigItems = CollectionUtils.isNotEmpty(shellConfigGroups) ? shellConfigGroups.get(0).getItems() : new ArrayList<>();

            if (StringUtils.isNotEmpty(groupEnum.getSystemType())) {
                // 查询系统列表，拼装数据
                List<ShellConfigItem> groupitemList = new ArrayList<>();

                // 根据对象id查询对应的系统
                List<JSONObject> queryObjectList = new ArrayList<>();
                if(groupEnum.getCode().equals(ShellConfigGroupEnum.GROUP_ALG_ACAH_PUMP_DIFF.getCode())){
                    queryObjectList = this.listObjectsByClassCodes(groupCode, projectId,
                            Lists.newArrayList(groupEnum.getSystemType().split(",")), classCodeObjectsCache);
                }else {
                    queryObjectList = this.listObjectsByClassCodes(groupCode, projectId,
                            Lists.newArrayList(groupEnum.getSystemType().split(",")[0]), classCodeObjectsCache);
                }
                if (CollectionUtils.isNotEmpty(queryObjectList)) {
                    if (groupEnum.getCode().equals(ShellConfigGroupEnum.GROUP_POLICY_SITE_PUMP.getCode()) || groupEnum.getCode().equals(ShellConfigGroupEnum.GROUP_ALG_DIFF.getCode())) {
                        // 需特殊处理，如果查询到有控制组设备数据，则显示标签一级一次泵组，再筛选末级泵组列表进行展示
                        List<ShellConfigItem> sysItems = BeanUtil.listCopyProperties(group.getItems(), ShellConfigItem.class);
                        for (ShellConfigItem item : sysItems) {
                            item.setObjectName(LocaleMessageUtil.getMessage("key_cce73d05545a42319b9ac11a1be4c4f9"));
                            List<ShellConfigItem> valueItem = shellConfigItems.stream().filter(x -> item.getObjectId() == null && item.getCode().equals(x.getCode())).collect(Collectors.toList());
                            item.setValue(CollectionUtil.isEmpty(valueItem) ? null : valueItem.get(0).getValue());
                        }
                        groupitemList.addAll(sysItems);
                        queryObjectList = queryObjectList.stream().filter(x -> "1".equals(x.getString("isTerminalPump"))).collect(Collectors.toList());
                    } else if(ShellConfigGroupEnum.GROUP_ALG_ACAC_FREQ.getCode().equals(groupEnum.getCode())
                    || ShellConfigGroupEnum.GROUP_ALG_ACCC_FREQ.getCode().equals(groupEnum.getCode())){
                        List<JSONObject> pumpObjectList = new ArrayList<>();
                        Set<String> queriedRelations = new HashSet<>();
                        List<String> objectIds = queryObjectList.stream()
                                .map(object -> object.getString(DtpRwdObjectDTO.Fields.id))
                                .collect(Collectors.toList());
                        for (ShellConfigItem item : group.getItems()) {
                            JSONObject valSource = JSONObject.parseObject(item.getValSource());
                            JSONObject relation = valSource.getJSONObject("relation");
                            // 相同关系的结果最终会按对象 ID 去重，无需因多个配置项重复访问 RWD。
                            if (relation == null || !queriedRelations.add(relation.toJSONString())) {
                                continue;
                            }
                            boolean from = CommonConst.FROM_RELATION.equals(relation.getString("relDirection"));
                            JSONArray classCodeCondition = relation.getJSONArray("classCode");
                            List<String> classCodes = classCodeCondition == null || classCodeCondition.size() == 0
                                    ? null : classCodeCondition.toJavaList(String.class);
                            List<JSONObject> instances = this.bizDtpRwdService.queryObjectList(
                                    groupCode, projectId, classCodes,
                                    relation.getString("graphCode"), relation.getString("relCode"),
                                    from ? objectIds : null, from ? null : objectIds);
                            pumpObjectList.addAll(instances);
                        }
                        // 根据 id 去重
                        pumpObjectList = pumpObjectList.stream()
                                .collect(Collectors.collectingAndThen(
                                        Collectors.toConcurrentMap(
                                                obj -> obj.getString("id"),
                                                obj -> obj,
                                                (existing, replacement) -> existing
                                        ),
                                        map -> new ArrayList<>(map.values())
                                ));
                        queryObjectList = pumpObjectList;
                    } else if (ShellConfigGroupEnum.GROUP_REPORT_CHAPTER.getCode().equals(groupEnum.getCode())){
                        //(monitoringSurveillanceStatus)=2或3
                        queryObjectList = queryObjectList.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))||"2".equals(x.getString("monitoringSurveillanceStatus"))).collect(Collectors.toList());

                        List<ShellConfigItem> sysItems = BeanUtil.listCopyProperties(group.getItems(), ShellConfigItem.class);
                        for (ShellConfigItem item : sysItems) {
                            if(ShellConfigItemEnum.REPORT_CHAPTER_COOLING_ENERGY.getCode().equals(item.getCode())
                            || ShellConfigItemEnum.REPORT_CHAPTER_COOLING_SYS_RUN.getCode().equals(item.getCode())){
                                if(CollectionUtil.isEmpty(queryObjectList)){
                                    continue;
                                }
                            }

                            if(ShellConfigItemEnum.REPORT_CHAPTER_COOLING_SYS_RUN_ITEM.getCode().equals(item.getCode())){
                                AtomicInteger cnt = new AtomicInteger(1);
                                queryObjectList.stream().forEach(x -> {
                                    ShellConfigItem sysItem = cn.hutool.core.bean.BeanUtil.copyProperties(item, ShellConfigItem.class);
                                    //生成json对象
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("name", x.getString(DtpRwdObjectDTO.Fields.localName));
                                    jsonObject.put("objectId", x.getString(DtpRwdObjectDTO.Fields.id));
                                    jsonObject.put("sort", cnt.getAndIncrement());
                                    jsonObject.put("parent",ShellConfigItemEnum.REPORT_CHAPTER_COOLING_SYS_RUN.getCode());
                                    sysItem.setDefaultValues(jsonObject.toJSONString());
                                    sysItem.setObjectId(x.getString(DtpRwdObjectDTO.Fields.id));
                                    sysItem.setObjectName(x.getString(DtpRwdObjectDTO.Fields.localName));

                                    // 匹配并设置配置项值
                                    for (ShellConfigItem shellConfigItem : shellConfigItems) {
                                        if (shellConfigItem == null || !sysItem.getCode().equals(shellConfigItem.getCode())) {
                                            continue;
                                        }

                                        String itemValue = shellConfigItem.getValue();
                                        if (StrUtil.isBlank(itemValue)) {
                                            continue;
                                        }

                                        JSONObject value = JSONObject.parseObject(itemValue);
                                        if (value == null) {
                                            continue;
                                        }

                                        String objectId = value.getString("objectId");
                                        if (sysItem.getObjectId() != null && sysItem.getObjectId().equals(objectId)) {
                                            sysItem.setValue(itemValue);
                                            break;
                                        }
                                    }

                                    //List<ShellConfigItem> valueItem = shellConfigItems.stream().filter(i -> item.getObjectId() == i.getObjectId() && item.getCode().equals(i.getCode())).collect(Collectors.toList());
                                    //sysItem.setValue(CollectionUtil.isEmpty(valueItem) ? null : valueItem.get(0).getValue());
                                    groupitemList.add(sysItem);
                                });
                            }else {
                                List<ShellConfigItem> valueItem = shellConfigItems.stream().filter(x -> item.getObjectId() == null && item.getCode().equals(x.getCode())).collect(Collectors.toList());
                                item.setValue(CollectionUtil.isEmpty(valueItem) ? null : valueItem.get(0).getValue());
                                groupitemList.add( item);
                            }
                        }
                        queryObjectList = new ArrayList<>();
                    } else if (ShellConfigGroupEnum.GROUP_CONTROL_COOLING.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_HOT.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_COLD_HOT_ALL.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_COLD_HOT.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_AIR_HOT.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_CONDITION.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COMMON_CONDITION.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAC_COLD_OUT_TEMP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAC_TEMP_PRESS_DIFF.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOL_FREE_CONDITION.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAH_OUT_TEMP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAH_HOT_SET.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAH_ADD.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAH_SUB.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAH_SECU_CALC.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAH_HOT_DIFF.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_MPC_AGENT_ACAC.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_MPC_AGENT_ACAI.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_UNIT_NUM.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_SUPPLY_TEMP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_SAFE_RUN.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_TEMP_PRESS_DIFF.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_SWITCH_SET.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_UNIT_NUM_HOT.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_SAFE_RUN_HOT.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_SUPPLY_TEMP_HOT.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_TEMP_PRESS_DIFF_HOT.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_COMMON_CONDITION.getCode().equals(groupEnum.getCode())
                    ) {
                        // 监测监控状态=3（可监可控）”的中央供冷系统实例、中央冷热源系统、中央供热系统实例。
                        queryObjectList = queryObjectList.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).collect(Collectors.toList());
                        /*
                    } else if (ShellConfigGroupEnum.GROUP_CONTROL_COOL_PUMP_GROUP.getCode().equals(groupEnum.getCode())) {
                        List<JSONObject> pumpObjectList = new ArrayList<>();
                        // 先取监测监控状态=3（可监可控）”的中央供冷系统实例、中央冷热源系统、中央供热系统实例。
                        List<JSONObject> systemRelation = bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList("ACCC", "ACCI", "ACCH", "ACAC"), null, null, null, null);
                        Set<String> systemIds = systemRelation.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).map(id -> id.getString("id")).collect(Collectors.toSet());
                        if (CollectionUtil.isNotEmpty(systemIds)) {
                            // 获取当前系统下所有的泵组
                            List<DtpRwdFreezePumpDTO> accpcGroups = this.dtpRwdClientService.listFreezePumpGroup(false, false, projectId, systemIds, null, null, true);
                            accpcGroups.forEach(item -> {
                                JSONObject pumpObject = new JSONObject();
                                pumpObject.put("id", item.getId());
                                pumpObject.put("localId", item.getLocalId());
                                pumpObject.put("localName", item.getLocalName());
                                pumpObjectList.add(pumpObject);
                            });
                        }
                        queryObjectList = pumpObjectList;

                         */
                    } else if (ShellConfigGroupEnum.GROUP_CONTROL_HOT_PUMP_GROUP.getCode().equals(groupEnum.getCode())
                    || ShellConfigGroupEnum.GROUP_CONTROL_COOL_HOT_PUMP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_COOL_PUMP_GROUP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_CHIL_PUMP_GROUP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_COOL_TOWER_GROUP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_CGLY_PUMP_GROUP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_CONTROL_COOL_HSLO_GROUP.getCode().equals(groupEnum.getCode())

                    ) {
                        List<JSONObject> pumpObjectList = new ArrayList<>();
                        String[] classCodes = groupEnum.getSystemType().split(",");
                        int length = classCodes.length;
                        String subClassCode = classCodes[length-1];
                        List<String> subList = CollectionUtil.sub(Lists.newArrayList(classCodes), 0, length - 1);
                        // 先取监测监控状态=3（可监可控）”的系统实例。
                        List<JSONObject> systemRelation = this.listObjectsByClassCodes(
                                groupCode, projectId, subList, classCodeObjectsCache);
                        Set<String> systemIds = systemRelation.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).map(id -> id.getString("id")).collect(Collectors.toSet());
                        if (CollectionUtil.isNotEmpty(systemIds)) {
                            // 获取当前系统下所有的泵组
                            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
                            param.setClassCode(subClassCode);
                            param.setRelationTo(new DtpRwdObjectRelationQueryParam(
                                        DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(systemIds));
                            List<DtpRwdObjectDTO> accpcGroups = this.dtpRwdClientService.listObject(param);
                            accpcGroups.forEach(item -> {
                                JSONObject pumpObject = new JSONObject();
                                pumpObject.put("id", item.getId());
                                pumpObject.put("localId", item.getLocalId());
                                pumpObject.put("localName", item.getLocalName());
                                pumpObjectList.add(pumpObject);
                            });
                        }
                        queryObjectList = pumpObjectList;
                    }else if (ShellConfigGroupEnum.GROUP_ALG_HOT_TEMP_SYSTEM.getCode().equals(groupEnum.getCode()) || ShellConfigGroupEnum.GROUP_ALG_HOT_DETA_TEMP_PRESS_SYSTEM.getCode().equals(groupEnum.getCode())) {
                        // 热源类型heatSoureType=“1-市政热水”
                        queryObjectList = queryObjectList.stream().filter(x -> {
                            Object heatSoureType = x.get("heatSoureType");
                            if (heatSoureType != null && heatSoureType instanceof List) {
                                List<String> heatSoureTypes = (List<String>) heatSoureType;
                                return heatSoureTypes.contains("1");
                            } else if (heatSoureType != null) {
                                return "1".equals(x.getString("heatSoureType"));
                            }
                            return false;
                        }).collect(Collectors.toList());
                    } else if (ShellConfigGroupEnum.GROUP_ALG_HOT_DETA_TEMP_PRESS_PUMP.getCode().equals(groupEnum.getCode())
                        || ShellConfigGroupEnum.GROUP_ALG_ACAH_PUMP_DIFF.getCode().equals(groupEnum.getCode())) {
                        // 非 一级一次泵 的供热水泵控制组
                        queryObjectList = queryObjectList.stream().filter(x -> !"1".equals(x.getString("netUseType"))).collect(Collectors.toList());
                    } else if (ShellConfigGroupEnum.GROUP_ALG_COOL_RELEASE_CONDITION.getCode().equals(groupEnum.getCode()) || ShellConfigGroupEnum.GROUP_ALG_COOL_STORE_CONDITION.getCode().equals(groupEnum.getCode())) {
                        queryObjectList = queryObjectList.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).collect(Collectors.toList());
                        boolean shouldDeleteGroup = queryObjectList.stream().noneMatch(
                                object -> "1".equals(object.getString("isCoolStore"))
                        );
                        if (shouldDeleteGroup) {
                            deletedGroups.add(group);
                        }
                    } else if(ShellConfigGroupEnum.GROUP_ALG_COOLING_PUMP_DIFF.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_PUMP_DIFF_HOT.getCode().equals(groupEnum.getCode())
                    ){
                        List<String> systemIds = queryObjectList.stream().map(id -> id.getString("id")).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(systemIds)) {
                            List<JSONObject> jsonObjects = bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.ACPPC_GROUP.getValue()), DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM.value().getLeft(), DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM.value().getRight(), systemIds, null);
                            queryObjectList = jsonObjects.stream().collect(Collectors.toList());
                        } else {
                            queryObjectList = new ArrayList<>();
                        }

                    }else if (ShellConfigGroupEnum.GROUP_ALG_ACAC_DIFF.getCode().equals(groupEnum.getCode())
                    ) {
                        List<String> systemIds = queryObjectList.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).map(id -> id.getString("id")).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(systemIds)) {
                            List<JSONObject> jsonObjects = bizDtpRwdService.queryObjectList(groupCode, projectId, Lists.newArrayList(DtpRwdClassCodeEnum.ACCPC_GROUP.getValue()), DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM.value().getLeft(), DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM.value().getRight(), systemIds, null);
                            queryObjectList = jsonObjects.stream().filter(x -> "2".equals(x.get("netUseType")) || "3".equals(x.get("netUseType"))).collect(Collectors.toList());
                        } else {
                            queryObjectList = new ArrayList<>();
                        }
                    }
                    for (JSONObject object : queryObjectList) {
                        String objectId = object.getString("id");

                        // 优先处理 valSource 字段
                        List<ShellConfigItem> sysItems = BeanUtil.listCopyProperties(group.getItems(), ShellConfigItem.class);
                        sysItems = this.getInstanceByValSource(sysItems, groupCode, projectId, objectId, object);

                        Map<String, Integer> cooler = null;
                        // 查询系统下的冷机台数，及其冷机实例
                        if (DtpRwdSystemClassCodeEnum.ACCC.value().equals(groupEnum.getSystemType()) && groupEnum.isResetParam()) {
                            cooler = this.getCooler(groupCode, projectId, objectId);
                        } else if (DtpRwdSystemClassCodeEnum.ACCI.value().equals(groupEnum.getSystemType()) && groupEnum.isResetParam()) {
                            if (acahcCountBySystemId == null) {
                                acahcCountBySystemId = new HashMap<>();
                                List<ControlObjectDto> objects = this.bizDtpRwdService.queryControlObjects(
                                        groupCode, projectId, "ACAHC");
                                if (CollectionUtils.isNotEmpty(objects)) {
                                    for (ControlObjectDto controlObject : objects) {
                                        String systemId = controlObject.getSystemId();
                                        if (systemId != null) {
                                            acahcCountBySystemId.merge(systemId, 1, Integer::sum);
                                        }
                                    }
                                }
                            }
                            cooler = new HashedMap();
                            cooler.put("count", acahcCountBySystemId.getOrDefault(objectId, 0));
                        }

                        for (ShellConfigItem item : sysItems) {
                            if (cooler != null) {
                                resetChillerLSNum(item, cooler);
                            }
                            item.setObjectId(object.getString("id"));
                            String localName = object.getString("localName");
                            item.setObjectName(StrUtil.isBlank(localName) ? object.getString("localId") : localName);
                            List<ShellConfigItem> valueItem = shellConfigItems.stream().filter(x -> item.getObjectId().equals(x.getObjectId()) && item.getCode().equals(x.getCode())).collect(Collectors.toList());
                            item.setValue(CollectionUtil.isEmpty(valueItem) ? null : valueItem.get(0).getValue());
                            if (CollectionUtil.isNotEmpty(item.getMulInstance()) && StrUtil.isNotBlank(item.getValue())) {
                                String[] objectIds = item.getValue().split(",");
                                for (JSONObject objectInfo : item.getMulInstance()) {
                                    objectInfo.put("select", Arrays.stream(objectIds).anyMatch(e -> e.equals(objectInfo.getString("objectId"))));
                                }
                            }
                        }
                        groupitemList.addAll(sysItems);
                    }
                    group.setItems(groupitemList);
                } else {
                    //查不到对应的系统实例
                    if (ShellConfigGroupEnum.GROUP_ALG_COOL_RELEASE_CONDITION.getCode().equals(groupEnum.getCode()) || ShellConfigGroupEnum.GROUP_ALG_COOL_STORE_CONDITION.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOLING_CONDITION.getCode().equals(groupEnum.getCode()) || ShellConfigGroupEnum.GROUP_ALG_COMMON_CONDITION.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_COOL_FREE_CONDITION.getCode().equals(groupEnum.getCode()) || ShellConfigGroupEnum.GROUP_ALG_ACAC_COLD_OUT_TEMP.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_ALG_ACAC_TEMP_PRESS_DIFF.getCode().equals(groupEnum.getCode()) || ShellConfigGroupEnum.GROUP_ALG_ACAC_DIFF.getCode().equals(groupEnum.getCode())
                            || ShellConfigGroupEnum.GROUP_MPC_AGENT_ACAC.getCode().equals(groupEnum.getCode()) || ShellConfigGroupEnum.GROUP_MPC_AGENT_ACAI.getCode().equals(groupEnum.getCode())
                    ) {
                        group.setItems(new ArrayList<>());
                    }else if(ShellConfigGroupEnum.GROUP_REPORT_CHAPTER.getCode().equals(groupEnum.getCode())){
                        if (CollectionUtils.isNotEmpty(shellConfigItems)) {
                            for (ShellConfigItem item : group.getItems()) {
                                if(ShellConfigItemEnum.REPORT_CHAPTER_COOLING_ENERGY.getCode().equals(item.getCode())
                                        ||ShellConfigItemEnum.REPORT_CHAPTER_COOLING_SYS_RUN_ITEM.getCode().equals(item.getCode())
                                        ||ShellConfigItemEnum.REPORT_CHAPTER_COOLING_SYS_RUN.getCode().equals(item.getCode())
                                ){
                                    if(CollectionUtil.isEmpty(queryObjectList)){
                                        continue;
                                    }
                                }
                                List<ShellConfigItem> s = shellConfigItems.stream().filter(x -> item.getCode().equals(x.getCode())).collect(Collectors.toList());
                                item.setValue(CollectionUtil.isEmpty(s) ? null : s.get(0).getValue());
                                groupitemList.add(item);
                            }
                            group.setItems(groupitemList);
                        }else{
                            for (ShellConfigItem item : group.getItems()) {
                                if(ShellConfigItemEnum.REPORT_CHAPTER_COOLING_ENERGY.getCode().equals(item.getCode())
                                        ||ShellConfigItemEnum.REPORT_CHAPTER_COOLING_SYS_RUN_ITEM.getCode().equals(item.getCode())
                                        ||ShellConfigItemEnum.REPORT_CHAPTER_COOLING_SYS_RUN.getCode().equals(item.getCode())
                                ){
                                    if(CollectionUtil.isEmpty(queryObjectList)){
                                        continue;
                                    }
                                }
                                //List<ShellConfigItem> s = shellConfigItems.stream().filter(x -> item.getCode().equals(x.getCode())).collect(Collectors.toList());
                                //item.setValue(CollectionUtil.isEmpty(s) ? null : s.get(0).getValue());
                                groupitemList.add(item);
                            }
                            group.setItems(groupitemList);
                        }
                    }
                }
            } else {
                if(ShellConfigGroupEnum.GROUP_MPC_AGENT_AIR_TERMINAL.getCode().equals(groupEnum.getCode())) {
                    List<ShellConfigItem> items = new ArrayList<>();
                    for (ShellConfigItem item : group.getItems()) {
                        List<JSONObject> jsonObjects = this.listObjectsByClassCodes(
                                groupCode, projectId, Lists.newArrayList(item.getValSource()), classCodeObjectsCache);
                        jsonObjects = jsonObjects.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).collect(Collectors.toList());
                        if (CollectionUtil.isNotEmpty(jsonObjects)) {
                            items.add(item);
                        }
                    }
                    group.setItems(items);
                }

                if (CollectionUtils.isNotEmpty(shellConfigItems)) {
                    for (ShellConfigItem item : group.getItems()) {
                        List<ShellConfigItem> s = shellConfigItems.stream().filter(x -> item.getCode().equals(x.getCode())).collect(Collectors.toList());
                        item.setValue(CollectionUtil.isEmpty(s) ? null : s.get(0).getValue());
                    }
                }

            }
        }
        if (CollectionUtil.isNotEmpty(deletedGroups)) {
            groups.removeAll(deletedGroups);
        }

        // 处理ALG_WEATHER_DATA_SOURCE参数，动态生成options（天气服务+气象站列表）
        this.handleWeatherDataSourceOptions(groups);

        // 处理通风控制"判断室外空气状态的方式"参数的静态选项（mulInstance）
        this.handleVentJudgeModeOptions(groups);

        return groups;
    }

    /**
     * 在一次场景组装中复用相同设备类查询，避免多个分组重复访问 RWD。
     *
     * <p>返回新的列表容器，调用方可以继续执行过滤和替换，不会污染后续分组使用的缓存结果。</p>
     *
     * @param groupCode 集团编码
     * @param projectId 项目ID
     * @param classCodes 设备类编码
     * @param cache 当前场景查询缓存
     * @return 当前设备类对应的对象列表
     * @since v7.260701.0
     */
    private @org.jspecify.annotations.NonNull List<JSONObject> listObjectsByClassCodes(
            @org.jspecify.annotations.NonNull String groupCode,
            @org.jspecify.annotations.NonNull String projectId,
            @org.jspecify.annotations.NonNull List<String> classCodes,
            @org.jspecify.annotations.NonNull Map<List<String>, List<JSONObject>> cache) {
        List<String> cacheKey = Collections.unmodifiableList(new ArrayList<>(classCodes));
        List<JSONObject> objects = cache.get(cacheKey);
        if (objects == null) {
            objects = this.bizDtpRwdService.queryObjectList(
                    groupCode, projectId, new ArrayList<>(cacheKey), null, null, null, null);
            objects = CollectionUtils.isEmpty(objects) ? Collections.emptyList() : objects;
            cache.put(cacheKey, objects);
        }
        return new ArrayList<>(objects);
    }

    /**
     * 处理ALG_WEATHER_DATA_SOURCE参数的动态选项生成
     * 动态组装：天气服务选项 + 气象站列表
     *
     * @param groups 壳配置分组列表
     */
    private void handleWeatherDataSourceOptions(List<ShellConfigGroup> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }

        List<ShellConfigItem> weatherDataSourceItems = new ArrayList<>();
        for (ShellConfigGroup group : groups) {
            if (CollectionUtils.isEmpty(group.getItems())) {
                continue;
            }
            for (ShellConfigItem item : group.getItems()) {
                if (ShellConfigItemEnum.ALG_WEATHER_DATA_SOURCE.getCode().equals(item.getCode())) {
                    weatherDataSourceItems.add(item);
                    break;
                }
            }
        }
        if (CollectionUtils.isEmpty(weatherDataSourceItems)) {
            return;
        }

        List<WeatherStationVO> weatherStations = this.queryWeatherStations();
        for (ShellConfigItem item : weatherDataSourceItems) {

            // 动态组装options：天气服务选项 + 气象站列表
            List<JSONObject> options = Lists.newArrayList();
            // 添加气象站选项（动态）
            if (CollectionUtil.isNotEmpty(weatherStations)) {
                for (WeatherStationVO station : weatherStations) {
                    JSONObject stationOption = new JSONObject();
                    stationOption.put("objectName", station.getName());
                    stationOption.put("objectId", station.getId());
                    options.add(stationOption);
                }
            }

            // 更新配置项的valSource
            item.setMulInstance(options);
            if(StringUtils.isEmpty(item.getValue())){
                //默认取第一个
                if(CollectionUtil.isNotEmpty(options)){
                    item.setValue(options.get(0).getString("objectId"));
                    item.setDefaultValues(options.get(0).getString("objectId"));
                }
            }else {
                //可选可选为空了
                if(CollectionUtils.isEmpty(options)){
                    item.setValue(null);
                    item.setDefaultValues(null);
                }else {
                    //可选列表不为空，判断是否在列表中,不在列表中，就选择列表中的第一个
                    String value = item.getValue();
                    Set<JSONObject> jsonObjects = options.stream().filter(e -> value.equals(e.getString("objectId"))).collect(Collectors.toSet());
                    if(CollectionUtil.isEmpty(jsonObjects)){
                        item.setValue(options.get(0).getString("objectId"));
                        item.setDefaultValues(options.get(0).getString("objectId"));
                    }
                }
            }
        }
    }

    /**
     * 处理通风控制"判断室外空气状态的方式"参数的静态选项生成。
     * <p>选项来源 {@link VentJudgeModeEnum}，输出形状 {code, name, select}，
     * select 根据当前 item.value 匹配设置。</p>
     *
     * @param groups 壳配置分组列表
     * @since v7.260802.2
     */
    private void handleVentJudgeModeOptions(List<ShellConfigGroup> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return;
        }

        Set<String> targetCodes = new HashSet<>(Arrays.asList(
                ShellConfigItemEnum.CONTROL_VENT_DOME_OFF_JUDGE_MODE.getCode(),
                ShellConfigItemEnum.CONTROL_VENT_DOME_ON_JUDGE_MODE.getCode()
        ));

        List<ShellConfigItem> judgeModeItems = new ArrayList<>();
        for (ShellConfigGroup group : groups) {
            if (CollectionUtils.isEmpty(group.getItems())) {
                continue;
            }
            for (ShellConfigItem item : group.getItems()) {
                if (targetCodes.contains(item.getCode())) {
                    judgeModeItems.add(item);
                }
            }
        }
        if (CollectionUtils.isEmpty(judgeModeItems)) {
            return;
        }

        for (ShellConfigItem item : judgeModeItems) {
            String currentValue = StringUtils.isNotEmpty(item.getValue()) ? item.getValue() : item.getDefaultValues();
            List<JSONObject> options = new ArrayList<>();
            for (VentJudgeModeEnum mode : VentJudgeModeEnum.values()) {
                JSONObject option = new JSONObject();
                option.put("code", mode.getCode());
                option.put("name", mode.getDesc());
                option.put("select", mode.getCode().equals(currentValue));
                options.add(option);
            }
            item.setMulInstance(options);
        }
    }

    /**
     * 根据 valSource 获取对应的mulInstance
     *
     * @param shellConfigItems 壳配置参数
     * @param groupCode
     * @param projectId
     * @param objectId
     * @return 返回处理后的 shellConfigItems，有可能数据会减少
     */
    private List<ShellConfigItem> getInstanceByValSource(List<ShellConfigItem> shellConfigItems, String groupCode, String projectId, String objectId, JSONObject object) {
        if (object == null && objectId != null) {
            List<JSONObject> jsonObjects = this.bizDtpRwdService.queryObjectList(groupCode, projectId, Arrays.asList(objectId));
            object = CollectionUtil.isEmpty(jsonObjects) ? null : jsonObjects.get(0);
        }
        List<ShellConfigItem> newShellConfigItems = new ArrayList<>();
        //判断实例化规则
        if (CollectionUtil.isNotEmpty(shellConfigItems) && ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING_EFF.getCode().equals(shellConfigItems.get(0).getGroup())) {
            Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
            classCodeAndObjectIds.put(ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING_EFF.getSystemType(), Sets.newHashSet(objectId));
            List<String> infoCodes = new ArrayList<>();
            shellConfigItems.stream().forEach(item -> {
                if (item.getValSource() != null) {
                    JSONObject valSource = JSONObject.parseObject(item.getValSource());
                    if (valSource.containsKey("infoCode")) {
                        String infoCode = valSource.getString("infoCode");
                        infoCodes.add(infoCode);
                    }
                }
            });
            //查实例化规则
            Map<String, Set<String>> ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodesByCloud(classCodeAndObjectIds, infoCodes);
            if (MapUtil.isNotEmpty(ruleSetMap)) {
                Set<String> infoCodeSet = ruleSetMap.get(objectId);
                shellConfigItems.stream().forEach(item -> {
                    if (item.getValSource() != null) {
                        JSONObject valSource = JSONObject.parseObject(item.getValSource());
                        if (valSource.containsKey("infoCode")) {
                            String infoCode = valSource.getString("infoCode");
                            if (infoCodeSet.contains(infoCode)) {
                                newShellConfigItems.add(item);
                            }
                        }
                    }
                });
            }
            return newShellConfigItems;
        }

        for (ShellConfigItem shellConfigItem : shellConfigItems) {
            if (ShellConfigItemEnum.ALG_AIR_COOLING_HOT_COLD_HOT_WATER_PUMP_MODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_HEATING_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_BRANCH_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
            ) {
                newShellConfigItems.add(shellConfigItem);
                setMulInstanceByInfoCode(shellConfigItem, object);
                continue;
            }
            if (StrUtil.isBlank(shellConfigItem.getValSource())) {
                newShellConfigItems.add(shellConfigItem);
                continue;
            }
            String group = shellConfigItem.getGroup();
            JSONObject valSource = null;
            try {
                valSource = JSONObject.parseObject(shellConfigItem.getValSource());
            } catch (Exception e) {

            }

            if(ShellConfigGroupEnum.GROUP_ALG_ACAC_FREQ.getCode().equals(group)
                    || ShellConfigGroupEnum.GROUP_ALG_ACCC_FREQ.getCode().equals(group)) {
                String classCode = valSource.getJSONObject("relation").getJSONArray("classCode").getString(0);
                if(!object.getString("classCode").equals(classCode)) {
                    continue;
                }
                //按照“冷冻水泵控制组（ACCPC）”实例配置，要求实例的管网使用类型（netUseType）= 1 一次一级泵或=2 一次二级泵。
                if (ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_1_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_COOLING_PUMP_1_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())) {
                    if ("1".equals(object.getString("netUseType")) || "2".equals(object.getString("netUseType"))) {
                        newShellConfigItems.add(shellConfigItem);
                    }
                } else if (ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_2_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_COOLING_PUMP_2_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())) {
                    if ("3".equals(object.getString("netUseType")) || "4".equals(object.getString("netUseType"))) {
                        newShellConfigItems.add(shellConfigItem);
                    }
                } else if (ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACOTC_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_COOLING_PUMP_ACOPC_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_COOLING_PUMP_ACOTC_FREQ_RANGE.getCode().equals(shellConfigItem.getCode())) {
                    newShellConfigItems.add(shellConfigItem);
                }
                continue;
            }

            // 判断对应的设备类下是否交付有设备数据
            if (ShellConfigGroupEnum.GROUP_CONTROL_COOLING.getCode().equals(group) || ShellConfigGroupEnum.GROUP_CONTROL_HOT.getCode().equals(group)
                    || ShellConfigGroupEnum.GROUP_CONTROL_COLD_HOT.getCode().equals(group)
            ) {
                String classCode = valSource.getString("classCode");
                String graphCode = valSource.getString("graphCode");
                String relCode = valSource.getString("relCode");
                String relDirection = valSource.getString("relDirection");
                boolean existsObjects = this.bizDtpRwdService.existsObjects(groupCode, projectId, objectId, classCode, graphCode, relCode, relDirection);
                if (existsObjects) {
                    newShellConfigItems.add(shellConfigItem);
                }
                continue;
            }

            // 判断对应的设备类下是否交付有设备数据
            if (ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING.getCode().equals(group)
                    || ShellConfigGroupEnum.GROUP_CONTROL_AIR_HOT.getCode().equals(group)
                    || ShellConfigGroupEnum.GROUP_CONTROL_COLD_HOT_ALL.getCode().equals(group)
            ) {
                JSONArray valSources = JSONObject.parseArray(shellConfigItem.getValSource());
                for (Object val : valSources) {
                    JSONObject valSourceObj = (JSONObject) val;
                    JSONArray classCodeCondition = valSourceObj.getJSONArray("classCode");
                    List<String> classCodes = classCodeCondition == null || classCodeCondition.size() == 0 ? null : classCodeCondition.toJavaList(String.class);

                    String graphCode = valSourceObj.getString("graphCode");
                    String relCode = valSourceObj.getString("relCode");
                    boolean from = CommonConst.FROM_RELATION.equals(valSourceObj.getString("relDirection"));
                    DtpRwdObjectQueryParam queryParam = new DtpRwdObjectQueryParam();
                    DtpRwdObjectRelationQueryParam relationQueryParam = new DtpRwdObjectRelationQueryParam().setGraphCode(graphCode).setRelCode(relCode);
                    queryParam.setClassCode(classCodes);
                    queryParam.setRelationFrom(from ? null:relationQueryParam.setObjTo(objectId));
                    queryParam.setRelationTo(from ? relationQueryParam.setObjFrom(objectId):null);
                    queryParam.setWithColumns(Arrays.asList("id"));
                    int count = this.dtpRwdClientService.countObject(queryParam);
                    if (count > 0) {
                        newShellConfigItems.add(shellConfigItem);
                        break;
                    }
                }
                continue;
            }

            //只有项目下的ACAC系统有一次泵系统（取系统的信息点冷冻泵形式chillPumpType=1 一次泵系统）才显示“间歇供冷加机”这个分类
            //ACAH一次泵系统（取系统的信息点供热泵形式heatPumpType=1 一次泵系统）
            if (ShellConfigGroupEnum.GROUP_ALG_COOLING_CONDITION.getCode().equals(group)
                    //|| ShellConfigGroupEnum.GROUP_ALG_ACAC_TEMP_PRESS_DIFF.getCode().equals(group)
                || ShellConfigGroupEnum.GROUP_ALG_ACAH_HOT_DIFF.getCode().equals(group)) {
                String key = valSource.getString("key");
                String value = valSource.getString("value");
                if (object != null && object.getString(key) != null && object.getString(key).equals(value)) {
                    newShellConfigItems.add(shellConfigItem);
                } else if (object != null && object.getString(key) == null) {
                    JSONObject relation = valSource.getJSONObject("relation");
                    if (relation != null) {
                        boolean from = CommonConst.FROM_RELATION.equals(relation.getString("relDirection"));
                        JSONArray classCodeCondition = relation.getJSONArray("classCode");
                        List<String> classCodes = classCodeCondition == null || classCodeCondition.size() == 0 ? null : classCodeCondition.toJavaList(String.class);
                        List<JSONObject> coolerInstances = this.bizDtpRwdService.queryObjectList(groupCode, projectId, classCodes, relation.getString("graphCode"), relation.getString("relCode"),
                                from ? Lists.newArrayList(objectId) : null, from ? null : Lists.newArrayList(objectId));
                        Map<String, Integer> classCodeMap = new HashMap<>();
                        JSONObject infoCode = valSource.getJSONObject("infoCode");
                        if (CollectionUtil.isNotEmpty(coolerInstances) && infoCode != null) {
                            for (JSONObject coolerInstance : coolerInstances) {
                                String classCode = coolerInstance.getString("classCode");
                                String infoCodeValue = coolerInstance.getString(infoCode.getString("key"));
                                if (infoCodeValue == null || !infoCodeValue.equals(infoCode.getString("value"))) {
                                    continue;
                                }
                                if (classCodeMap.containsKey(classCode)) {
                                    classCodeMap.put(classCode, classCodeMap.get(classCode) + Integer.parseInt(coolerInstance.getString("isDualCondition")));
                                } else {
                                    classCodeMap.put(classCode, Integer.parseInt(coolerInstance.getString("isDualCondition")));
                                }
                            }
                        }
                        if (ShellConfigItemEnum.ALG_AIR_COOLING_CHILLER_RATE_MAX.getCode().equals(shellConfigItem.getCode())) {
                            if(CollectionUtil.isNotEmpty(coolerInstances)){
                                newShellConfigItems.add(shellConfigItem);
                            }
                        }
                        if (classCodeMap.size() == 0) {
                            continue;
                        }
                        //冷机负载率最大值
                        //参数显示的判断条件：
                        //“是否双工况=0否”的离心式冷水机组（ACCCCC）的数量和“是否双工况=0否”的螺杆式冷水机组（ACCCSC）的数量只能其中一个有
                        if (ShellConfigItemEnum.ALG_AIR_COOLING_ELE_IPLR_MAX.getCode().equals(shellConfigItem.getCode())) {
                            if ((classCodeMap.size() == 1)) {
                                newShellConfigItems.add(shellConfigItem);
                            }
                        } else if (ShellConfigItemEnum.ALG_AIR_COOLING_CEN_IPLR_MAX.getCode().equals(shellConfigItem.getCode())
                                || ShellConfigItemEnum.ALG_AIR_COOLING_SCR_IPLR_MAX.getCode().equals(shellConfigItem.getCode())) {
                            if (classCodeMap.size() == 2) {
                                newShellConfigItems.add(shellConfigItem);
                            }
                        } else if (ShellConfigItemEnum.ALG_AIR_COOLING_DUA_IPLR_MAX.getCode().equals(shellConfigItem.getCode())) {
                            newShellConfigItems.add(shellConfigItem);
                        } else {
                            newShellConfigItems.add(shellConfigItem);
                        }
                    } else {
                        newShellConfigItems.add(shellConfigItem);
                    }
                }

                setMulInstanceByInfoCode(shellConfigItem, object);
                continue;
            }

            //泵组控制模式
            if ((ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode()))
                    || ShellConfigItemEnum.ALG_AIR_HOT_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
            ) {
                newShellConfigItems.add(shellConfigItem);
                setMulInstanceByInfoCode(shellConfigItem, object);
                continue;
            }

            // 冷站，不能同时开启的冷机数据来源处理
            if ((ShellConfigItemEnum.ALG_COOLING_UNABLE_SAME_TIME_COOL.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_OPEN_RULE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_WATER_RETURN_MAX.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.MPC_STAGE_UP_CLASS_CODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.MPC_STAGE_DAY_CLASS_CODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.MPC_STAGE_DOWN_CLASS_CODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.MPC_STAGE_NIGHT_CLASS_CODE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_START_INTERVAL_RULE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_COOLING_START_INTERVAL_RULE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_SUPPORT_BIG_SMALL_HOST.getCode().equals(shellConfigItem.getCode())
                    || ShellConfigItemEnum.ALG_AIR_COOLING_FLOW_DEMAND_CONF.getCode().equals(shellConfigItem.getCode())
            )
                    && StrUtil.isNotBlank(objectId)) {
                try {
                    JSONObject relation = valSource.getJSONObject("relation");
                    if (relation != null) {
                        boolean from = CommonConst.FROM_RELATION.equals(relation.getString("relDirection"));
                        JSONArray classCodeCondition = relation.getJSONArray("classCode");
                        List<String> classCodes = classCodeCondition == null || classCodeCondition.size() == 0 ? null : classCodeCondition.toJavaList(String.class);
                        List<JSONObject> coolerInstances = this.bizDtpRwdService.queryObjectList(groupCode, projectId, classCodes, relation.getString("graphCode"), relation.getString("relCode"),
                                from ? Lists.newArrayList(objectId) : null, from ? null : Lists.newArrayList(objectId));

                        List<JSONObject> resultList = new ArrayList<>();

                       if(ShellConfigItemEnum.ALG_AIR_COOLING_OPEN_RULE.getCode().equals(shellConfigItem.getCode())){
                            //查询系统下的空气源热泵控制组（ACAHC）、串联冷机组（ACSCG），需要用到关系“系统下的设备组”
                            List<DtpRwdEquipmentDTO> coolerGroups = this.dtpRwdClientService.listEquipmentGroupInBySystemIds(
                                    Sets.newHashSet(objectId), new DtpRwdObjectQueryParam()
                                            .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACAHC_GROUP,
                                                    DtpRwdClassCodeEnum.ACSCG_GROUP))
                                            .addWithColumn(DtpRwdObjectDTO.Fields.id)
                                            .addWithColumn(DtpRwdObjectDTO.Fields.localName)
                                            .addWithColumn(DtpRwdObjectDTO.Fields.classCode));
                            if(CollectionUtils.isNotEmpty(coolerGroups)){
                                coolerGroups.stream().forEach( coolerGroup -> {
                                            JSONObject result = new JSONObject();
                                            result.put("objectId", coolerGroup.getId());
                                            result.put("objectName", coolerGroup.getLocalName());
                                            result.put("select", false);
                                            resultList.add(result);
                                        });
                                
                                //串联冷机组中包含的冷机实例
                                List<String> ACSCGIds = coolerGroups.stream().filter(coolerGroup -> DtpRwdClassCodeEnum.ACSCG_GROUP.getValue().equals(coolerGroup.getClassCode()))
                                        .map(DtpRwdEquipmentDTO::getId).collect(Collectors.toList());

                                if(CollectionUtil.isNotEmpty(ACSCGIds)) {
                                    DtpRwdObjectQueryParam query = new DtpRwdObjectQueryParam();

                                    query.setRelationTo(new DtpRwdObjectRelationQueryParam(
                                            DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(ACSCGIds));
                                    query.setClassCodeEnum(Arrays.asList(DtpRwdClassCodeEnum.ACCCCC, DtpRwdClassCodeEnum.ACCCSC));
                                    query.addWithColumn(DtpRwdObjectDTO.Fields.id);
                                    List<DtpRwdObjectDTO> objectDTOS = dtpRwdClientService.listObject(false, false, projectId, query);
                                    //从coolerInstances中删除和objectDTOS中相同的id对象
                                    if (CollectionUtils.isNotEmpty(coolerInstances)) {
                                        coolerInstances.removeIf(coolerInstance -> objectDTOS.stream().anyMatch(objectDTO -> objectDTO.getId().equals(coolerInstance.getString("id"))));
                                    }
                                }
                            }
                        }else if(ShellConfigItemEnum.ALG_AIR_COOLING_HOT_SUPPORT_BIG_SMALL_HOST.getCode().equals(shellConfigItem.getCode())){
                           List<DtpRwdEquipmentDTO> coolerGroups = this.dtpRwdClientService.listEquipmentGroupInBySystemIds(
                                   Sets.newHashSet(objectId), new DtpRwdObjectQueryParam()
                                           .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACAHC_GROUP))
                                           .addWithColumn(DtpRwdObjectDTO.Fields.id)
                                           .addWithColumn(DtpRwdObjectDTO.Fields.localName)
                                           .addWithColumn(DtpRwdObjectDTO.Fields.classCode));

                           if(CollectionUtils.isNotEmpty(coolerGroups)){

                               Set<String> classCodeSet = coolerGroups.stream()
                                       .map(DtpRwdEquipmentDTO::getClassCode)
                                       .filter(Objects::nonNull)
                                       .collect(Collectors.toSet());

                               classCodeSet.stream().forEach(classCode -> {
                                   DtpRwdClassCodeEnum analyze = DtpRwdClassCodeEnum.analyze(classCode);
                                   JSONObject result = new JSONObject();
                                   result.put("code", analyze.value());
                                   result.put("name", analyze.getReasonPhrase());
                                   result.put("select", false);
                                   resultList.add(result);
                               });

                           }
                           if (CollectionUtils.isNotEmpty(coolerInstances)) {

                               for (JSONObject coolerInstance : coolerInstances) {
                                   String classCode = coolerInstance.getString("classCode");
                                   DtpRwdClassCodeEnum analyze = DtpRwdClassCodeEnum.analyze(classCode);
                                   JSONObject result = new JSONObject();
                                   result.put("code", analyze.value());
                                   result.put("name", analyze.getReasonPhrase());
                                   result.put("select", false);
                                   resultList.add(result);
                               }
                           }
                           shellConfigItem.setMulInstance(resultList);
                           newShellConfigItems.add(shellConfigItem);
                           continue;
                       } else if(ShellConfigItemEnum.ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE.getCode().equals(shellConfigItem.getCode())
                                || ShellConfigItemEnum.ALG_AIR_COOLING_START_INTERVAL_RULE.getCode().equals(shellConfigItem.getCode())
                                || ShellConfigItemEnum.ALG_COOLING_START_INTERVAL_RULE.getCode().equals(shellConfigItem.getCode())
                        ){
                            //过滤coolerGroups中monitoringSurveillanceStatus=3；
                            coolerInstances = coolerInstances.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).collect(Collectors.toList());
                            List<DtpRwdEquipmentDTO> coolerGroups = this.dtpRwdClientService.listEquipmentGroupInBySystemIds(
                                    Sets.newHashSet(objectId), new DtpRwdObjectQueryParam()
                                            .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACAHC_GROUP))
                                            .addWithColumn(DtpRwdObjectDTO.Fields.id)
                                            .addWithColumn(DtpRwdObjectDTO.Fields.localName)
                                            .addWithColumn("monitoringSurveillanceStatus")
                                            .addWithColumn(DtpRwdObjectDTO.Fields.classCode));
                            coolerGroups = coolerGroups.stream().filter(x -> "3".equals(x.analyzeProperties("monitoringSurveillanceStatus"))).collect(Collectors.toList());

                            if(CollectionUtils.isNotEmpty(coolerGroups)){
                                coolerGroups.stream().forEach( coolerGroup -> {
                                    JSONObject result = new JSONObject();
                                    result.put("objectId", coolerGroup.getId());
                                    result.put("objectName", coolerGroup.getLocalName());
                                    result.put("select", false);
                                    resultList.add(result);
                                });
                            }
                        } else if(ShellConfigItemEnum.ALG_AIR_COOLING_FLOW_DEMAND_CONF.getCode().equals(shellConfigItem.getCode())) {
                            // 过滤monitoringSurveillanceStatus=3的设备实例（ACCCCC/ACCCSC/ACCCAC/ACCCGH/ACCCWH/ACGEHE）
                            coolerInstances = coolerInstances.stream().filter(x -> "3".equals(x.getString("monitoringSurveillanceStatus"))).collect(Collectors.toList());
                            // 查询空气源热泵控制组（ACAHC），使用CombineSubset/Sy2Eg关系（系统下的设备组）
                            List<DtpRwdEquipmentDTO> acahcGroups = this.dtpRwdClientService.listEquipmentGroupInBySystemIds(
                                    Sets.newHashSet(objectId), new DtpRwdObjectQueryParam()
                                            .setClassCodeEnum(Lists.newArrayList(DtpRwdClassCodeEnum.ACAHC_GROUP))
                                            .addWithColumn(DtpRwdObjectDTO.Fields.id)
                                            .addWithColumn(DtpRwdObjectDTO.Fields.localName)
                                            .addWithColumn("monitoringSurveillanceStatus")
                                            .addWithColumn(DtpRwdObjectDTO.Fields.classCode));
                            acahcGroups = acahcGroups.stream().filter(x -> "3".equals(x.analyzeProperties("monitoringSurveillanceStatus"))).collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(acahcGroups)) {
                                acahcGroups.forEach(acahcGroup -> {
                                    JSONObject result = new JSONObject();
                                    result.put("objectId", acahcGroup.getId());
                                    result.put("objectName", acahcGroup.getLocalName());
                                    result.put("select", false);
                                    resultList.add(result);
                                });
                            }
                        }

                        if(ShellConfigItemEnum.MPC_STAGE_UP_CLASS_CODE.getCode().equals(shellConfigItem.getCode())
                                || ShellConfigItemEnum.MPC_STAGE_DAY_CLASS_CODE.getCode().equals(shellConfigItem.getCode())
                                || ShellConfigItemEnum.MPC_STAGE_DOWN_CLASS_CODE.getCode().equals(shellConfigItem.getCode())
                                || ShellConfigItemEnum.MPC_STAGE_NIGHT_CLASS_CODE.getCode().equals(shellConfigItem.getCode())){
                            //单独处理，查询冷却塔控制单元（ACOTU）需要用到查询关系—系统下的设备组
                            DtpRwdObjectQueryParam query = new DtpRwdObjectQueryParam();

                            query.setRelationTo(new DtpRwdObjectRelationQueryParam(
                                    DtpRwdRelationTypeEnum.EQUIP_GROUP_IN_SYSTEM).setObjFrom(objectId));
                            query.setClassCodeEnum(Arrays.asList(DtpRwdClassCodeEnum.ACOTU_GROUP));
                            query.addWithColumn(DtpRwdObjectDTO.Fields.id);
                            List<DtpRwdObjectDTO> objectDTOS = dtpRwdClientService.listObject(false, false, projectId, query);
                            if(CollectionUtils.isNotEmpty(objectDTOS)) {
                                coolerInstances.addAll(objectDTOS.stream().map(DtpRwdObjectDTO::getId).map(id -> {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("id", id);
                                    jsonObject.put("classCode", DtpRwdClassCodeEnum.ACOTU_GROUP);
                                    return jsonObject;
                                }).collect(Collectors.toList()));
                            }


                            Set<String> mpcClassCodes = new HashSet<>();
                            //从coolerInstances中获取所有classCode字段
                            coolerInstances.forEach(coolerInstance -> mpcClassCodes.add(coolerInstance.getString("classCode")));

                            List<DtpKnowledgeObjectClassCodeResponse> dtpKnowledgeObjectClassCodeResponses = dtpKnowledgeClientService.listClassCodes(mpcClassCodes);
                            coolerInstances = new  ArrayList<>();
                            List<JSONObject> finalCoolerInstances = coolerInstances;
                            dtpKnowledgeObjectClassCodeResponses.forEach(dtpKnowledgeObjectClassCodeResponse -> {
                                JSONObject coolerInstance = new JSONObject();
                                coolerInstance.put("id", dtpKnowledgeObjectClassCodeResponse.getCode());
                                coolerInstance.put("localName", dtpKnowledgeObjectClassCodeResponse.getName());
                                finalCoolerInstances.add(coolerInstance);
                            });

                        }
                        if (CollectionUtils.isNotEmpty(coolerInstances)) {
                            for (JSONObject coolerInstance : coolerInstances) {
                                JSONObject result = new JSONObject();
                                result.put("objectId", coolerInstance.getString("id"));
                                result.put("objectName", coolerInstance.getString("localName"));
                                result.put("select", false);
                                resultList.add(result);
                            }
                        }

                        shellConfigItem.setMulInstance(resultList);
                    }
                } catch (Exception e) {
                    log.error("壳配置--扩展数据源，加载异常，参数配置：{}", shellConfigItem, e);
                }
            }

            //单选，选项：冷机类型。冷机类型由bdtp提供查询接口。
            if (ShellConfigItemEnum.ALG_AIR_COOLING_MACHINE_MIX_ENABLED.getCode().equals(shellConfigItem.getCode())) {
                JSONObject sysGroupJson = this.bizDtpRwdService.querySysGroupBySystemId(groupCode, projectId, objectId);
                Set<String> sysGroups = sysGroupJson.keySet();
                List<JSONObject> resultList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(sysGroups)) {
                    for (String sysGroup : sysGroups) {
                        SysGroupEnum sysGroupEnum = SysGroupEnum.analyze(sysGroup);
                        if (sysGroupEnum == null) {
                            continue;
                        }
                        JSONObject result = new JSONObject();
                        result.put("code", sysGroupEnum.getCode());
                        result.put("name", sysGroupEnum.getDesc());
                        result.put("select", false);
                        resultList.add(result);
                    }
                }
                shellConfigItem.setMulInstance(resultList);
            }

            newShellConfigItems.add(shellConfigItem);
        }

        return newShellConfigItems;
    }

    private void setMulInstanceByInfoCode(ShellConfigItem shellConfigItem, JSONObject object) {
        if (shellConfigItem == null || object == null) {
            return;
        }
        //冷冻水泵控制模式，根据系统交付情况，哪个参数有实例化规则就显示哪个参数
        if ((ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode()))
                || ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                || ShellConfigItemEnum.ALG_AIR_HOT_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                || ShellConfigItemEnum.ALG_AIR_HOT_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_COLD_HOT_WATER_PUMP_MODE.getCode().equals(shellConfigItem.getCode())
                || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_HEATING_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_BRANCH_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
        ) {
            try {
                String id = object.getString("id");
                String classCode = object.getString("classCode");

                if (StringUtils.isBlank(id) || StringUtils.isBlank(classCode)) {
                    log.warn("Invalid object data: missing id or classCode");
                    return;
                }

                Map<String, Set<String>> classCodeAndObjectIds = new HashMap<>();
                classCodeAndObjectIds.put(classCode, Sets.newHashSet(id));
                //查实例化规则
                Map<String, Set<String>> ruleSetMap = null;
                if (ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())) {
                    ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, Arrays.asList(InfoCodeEnum.CHILL_WATER_DETA_TEMP_SET.getCode(), InfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode(), InfoCodeEnum.CHILL_WATER_DETA_PRESS_SET.getCode()));
                } else if (ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_AIR_HOT_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_PUMP_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_BRANCH_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                ) {
                    ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, Arrays.asList(InfoCodeEnum.BRANCH_DETA_TEMP_SET.getCode(), InfoCodeEnum.BRANCH_DETA_PRESS_SET.getCode(), InfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode()));
                }else if (ShellConfigItemEnum.ALG_AIR_HOT_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_COLD_HOT_WATER_PUMP_MODE.getCode().equals(shellConfigItem.getCode())
                        || ShellConfigItemEnum.ALG_AIR_COOLING_HOT_HEATING_PUMP_CONTROL_MODE.getCode().equals(shellConfigItem.getCode())
                ) {
                    ruleSetMap = abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(ContextUtil.getProjectId(), classCodeAndObjectIds, Arrays.asList(InfoCodeEnum.WATER_DETA_TEMP_SET.getCode(), InfoCodeEnum.WATER_DETA_PRESS_SET.getCode(), InfoCodeEnum.WORST_TERMINAL_DETA_PRESS_SET.getCode()));
                }
                if (MapUtil.isNotEmpty(ruleSetMap) && ruleSetMap.containsKey(id)) {
                    shellConfigItem.setMulInstance(ruleSetMap.get(id).stream().map(rule -> {
                        InfoCodeEnum infoCodeEnum = EnumUtil.getBy(InfoCodeEnum::getCode, rule);
                        if (infoCodeEnum == null) {
                            log.warn("Unknown info code: {}", rule);
                            return null;
                        }
                        JSONObject res = new JSONObject();
                        res.put("infoCode", rule);
                        res.put("name", infoCodeEnum.getDesc());
                        res.put("select", false);
                        return res;
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
            } catch (Exception e) {
                log.error("setMulInstance error:{}", e);
            }
        }
    }

    private static void resetChillerLSNum(ShellConfigItem item, Map<String, Integer> cooler) {
        if (cooler == null) {
            return;
        }
        try {
            item.setHint(VelocityConfig.getMessageByTemplate(cooler, item.getHint()));
            item.setDefaultValues(VelocityConfig.getMessageByTemplate(cooler, item.getDefaultValues()));
            if (item.getMax() == null && item.getMin() != null) {
                item.setMax(Integer.valueOf(item.getDefaultValues()));
            }
        } catch (JsonProcessingException ex) {
            log.error("VelocityConfig.getMessageByTemplate error:{}", ExceptionUtils.getStackTrace(ex));
        }
    }

    @Override
    public Boolean save(String groupCode, SaveShellConfigParam param) {
        BasicsConfigTypeEnum basicsConfigTypeEnum = ShellConfigSceneEnum.convertToBasicConfigTypeByScene(param.getScene());
        if (basicsConfigTypeEnum == null) {
            throw new ValidationException(LocaleMessageUtil.getMessage("key_7c8d283d60d542c2b24050c33d0af406"));
        }
        RcBasicsConfig basicsConfig = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, param.getProjectId())
                .eq(RcBasicsConfig::getType, basicsConfigTypeEnum)
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
        if (basicsConfig == null) {
            basicsConfig = new RcBasicsConfig();
            basicsConfig.setProjectId(param.getProjectId());
            basicsConfig.setType(basicsConfigTypeEnum);
        }
        basicsConfig.setResult(JSON.toJSONString(param.getGroups()));
        rcBasicsConfigService.saveOrUpdateBasicsConfig(basicsConfig);
        return true;
    }

    @Override
    public ShellConfigItem queryByInfoCode(String groupCode, QueryShellConfigByInfoCodeParam param) {
        param.check();
        // infocode转换为壳配置code
        // 查询项目下得配置，如果没有配置，则返回默认值
        String code = param.getCode();
        if (StringUtils.isEmpty(param.getCode())) {
            code = ShellConfigAndInfoCodeEnum.getCodeByInfoCode(param.getInfoCode(), param.getClassCode());
        }
        if (StringUtils.isEmpty(code)) {
            throw new BusinessException("A0400", LocaleMessageUtil.getMessage("key_23563689e471457eacc435a5f57f4cee"));
        }
        // 根据code获取配置项
        ShellConfigItemEnum itemEnum = ShellConfigItemEnum.getByCode(code);
        if (null == itemEnum) {
            // 兼容处理
            return new ShellConfigItem();
        }
        // 根据场景获取对应的配置数据
        RcBasicsConfig basicsConfig = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, param.getProjectId())
                .eq(RcBasicsConfig::getType, ShellConfigSceneEnum.convertToBasicConfigTypeByScene(itemEnum.getScene()))
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
        ShellConfigItem res = ShellConfigItemEnum.convert(itemEnum);
        List<ShellConfigGroup> groups = new ArrayList<>();
        if (basicsConfig == null) {
            res.setObjectId(param.getObjectId());
            groups.add(new ShellConfigGroup(res.getGroup(), "", Lists.newArrayList(res)));
        } else {
            groups = JSON.parseArray(basicsConfig.getResult(), ShellConfigGroup.class);
        }
        for (ShellConfigGroup group : groups) {
            for (ShellConfigItem basic : group.getItems()) {
                if (basic.getCode().equals(code)) {
                    if (basicsConfig != null && StringUtils.isNotEmpty(param.getObjectId()) && !param.getObjectId().equals(basic.getObjectId())) {
                        continue;
                    }
                    ShellConfigItem item = ShellConfigItemEnum.convert(ShellConfigItemEnum.getByCode(basic.getCode()));
                    ShellConfigGroupEnum groupEnum = ShellConfigGroupEnum.getGroupEnumByCode(group.getGroup());
                    if (StringUtils.isNotEmpty(param.getObjectId())) {
                        Map<String, Integer> cooler = null;
                        if (DtpRwdSystemClassCodeEnum.ACCC.value().equals(groupEnum.getSystemType()) && groupEnum.isResetParam()) {
                            cooler = this.getCooler(groupCode, param.getProjectId(), param.getObjectId());
                        } else if (DtpRwdSystemClassCodeEnum.ACCI.value().equals(groupEnum.getSystemType()) && groupEnum.isResetParam()) {
                            cooler = new HashMap<>(16);
                            List<ControlObjectDto> objects = bizDtpRwdService.queryControlObjects(groupCode, param.getProjectId(), "ACAHC");
                            cooler.put("count", CollectionUtils.isEmpty(objects) ? 0 : objects.stream().filter(x -> param.getObjectId().equals(x.getSystemId())).collect(Collectors.toList()).size());
                        }
                        resetChillerLSNum(item, cooler);
                    }
                    item.setValue(StringUtils.isNotEmpty(basic.getValue()) ? basic.getValue() : item.getDefaultValues());
                    item.setObjectId(basic.getObjectId());
                    item.setObjectName(basic.getObjectName());
                    List<ShellConfigItem> instanceByValSource = this.getInstanceByValSource(Lists.newArrayList(item), groupCode, param.getProjectId(), basic.getObjectId(), null);
                    if (CollectionUtils.isEmpty(instanceByValSource)) {
                        return new ShellConfigItem();
                    }
                    item = instanceByValSource.get(0);
                    if (CollectionUtil.isNotEmpty(item.getMulInstance()) && StrUtil.isNotBlank(item.getValue())) {
                        String[] objectIds = item.getValue().split(",");
                        for (JSONObject objectInfo : item.getMulInstance()) {
                            objectInfo.put("select", Arrays.stream(objectIds).anyMatch(e -> e.equals(objectInfo.getString("objectId"))));
                        }
                    }
                    return item;
                }
            }
        }
        return res;
    }

    @Override
    public List<ShellConfigItem> batchQueryConfigByObjectId(String groupCode, BatchQueryShellConfigParam param) {
        List<ShellConfigItem> itemByGroup = ShellConfigItemEnum.getListByGroup(param.getGroup());
        itemByGroup = CollectionUtils.isEmpty(itemByGroup) ? null : this.getInstanceByValSource(itemByGroup, groupCode, param.getProjectId(), param.getObjectId(), null);
        if (CollectionUtils.isEmpty(itemByGroup)) {
            throw new BusinessException(ErrorCodeConstants.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_f8a9da740e374821adcabdddf09f6760"));
        }
        for (ShellConfigItem item : itemByGroup) {
            item.setValue(item.getDefaultValues());
        }
        String scene = itemByGroup.get(0).getScene();
        // 根据场景获取对应的配置数据
        RcBasicsConfig basicsConfig = rcBasicsConfigService.lambdaQuery()
                .eq(RcBasicsConfig::getProjectId, param.getProjectId())
                .eq(RcBasicsConfig::getType, ShellConfigSceneEnum.convertToBasicConfigTypeByScene(scene))
                .orderByDesc(RcBasicsConfig::getUpdateTime)
                .last(CommonConst.LIMIT_ONE)
                .one();
        if (basicsConfig == null) {
            return itemByGroup;
        }
        List<ShellConfigGroup> groups = JSON.parseArray(basicsConfig.getResult(), ShellConfigGroup.class);
        List<ShellConfigGroup> groupList = groups.stream().filter(item -> item.getGroup().equals(param.getGroup())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(groupList) && CollectionUtils.isNotEmpty(groupList.get(0).getItems())) {
            Map<String, ShellConfigItem> configItemMap = groupList.get(0).getItems().stream().filter(item -> param.getObjectId().equals(item.getObjectId()))
                    .collect(Collectors.toMap(ShellConfigItem::getCode, Function.identity(), (v1, v2) -> v2));
            String objectName = groupList.get(0).getItems().get(0).getObjectName();

            itemByGroup.stream().forEach(item -> {
                if (configItemMap.containsKey(item.getCode())) {
                    item.setValue(configItemMap.get(item.getCode()).getValue());
                }
                item.setObjectId(param.getObjectId());
                item.setObjectName(objectName);

                if (CollectionUtil.isNotEmpty(item.getMulInstance()) && StrUtil.isNotBlank(item.getValue())) {
                    String[] objectIds = item.getValue().split(",");
                    for (JSONObject objectInfo : item.getMulInstance()) {
                        objectInfo.put("select", Arrays.stream(objectIds).anyMatch(e -> e.equals(objectInfo.getString("objectId"))));
                    }
                }
            });
        }
        return itemByGroup;
    }

    /**
     * 获取冷机数
     *
     * @param projectId
     * @param systemId
     * @return
     */
    private Map<String, Integer> getCooler(String groupCode, String projectId, String systemId) {
        // key: 大/小冷机  value: 数量
        Map<String, Integer> resultMap = new HashMap<>();
        try {
            // 查询系统下冷机分组
            AcccccGroupDTO acccacGroup = bizDtpRwdService.queryAcccccGroupBySystemId(groupCode, projectId, systemId);
            if (acccacGroup == null) {
                resultMap.put(CoolerRuleTypeEnum.LARGE_COOLER.getCode(), 0);
                resultMap.put(CoolerRuleTypeEnum.SMALL_COOLER.getCode(), 0);
                return resultMap;
            }
            resultMap.put(CoolerRuleTypeEnum.LARGE_COOLER.getCode(), CollectionUtil.isNotEmpty(acccacGroup.getLarge()) ? acccacGroup.getLarge().size() : 0);
            resultMap.put(CoolerRuleTypeEnum.SMALL_COOLER.getCode(), CollectionUtil.isNotEmpty(acccacGroup.getSmall()) ? acccacGroup.getSmall().size() : 0);
        } catch (Exception e) {
            log.error("设备信息获取失败", e);
            throw new BusinessException(SysResponseCode.FAIL_K0001.getCode(), LocaleMessageUtil.getMessage("key_aaf91f4ecfda4fa6890f21f61f96bae4"));
        }
        return resultMap;
    }

    /**
     * @author wxf
     * @description 查询效率指标数据
     * @date 11:03 2025/5/26
     * @param param
     * @return com.persagy.apccontrol.common.model.vo.runanalysis.ColdEfficiencyVO
     **/
    @Override
    public EfficiencyVO queryEfficiency(BasicsAnalysisParam param) {
        EfficiencyVO coldEfficiencyVO = new EfficiencyVO();
        String projectId = ContextUtil.getProjectId();
        String groupCode = ContextUtil.getGroupCode();
        if (RunningMonitorSecondTypeEnum.AIR_CONDITIONING_COLD_SOURCE.equals(param.getType())) {
            //获取壳配置数据
            List<String> instanceIds = param.getInstanceIds();
            if (CollectionUtil.isEmpty(instanceIds)) {
                coldEfficiencyVO.setDataList(Collections.emptyList());
                return coldEfficiencyVO;
            }
            String objectId = instanceIds.get(0);
            BatchQueryShellConfigParam shellConfigParam = new BatchQueryShellConfigParam();
            shellConfigParam.setGroup(ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING_EFF.getCode());
            shellConfigParam.setObjectId(objectId);
            shellConfigParam.setProjectId(projectId);

            List<ShellConfigItem> shellConfigItems = batchQueryConfigByObjectId(groupCode, shellConfigParam);
            if (CollectionUtil.isNotEmpty(shellConfigItems)) {
                List<InfoCodeValueVO> dataList = new ArrayList<>();
                //查持久化数据
                Set<String> infoCodes = new HashSet<>();
                Map<String, InfoCodeValueVO> infoCodeValueMap = new HashMap<>();
                shellConfigItems.stream().forEach(item -> {
                    if (item.getValSource() != null) {
                        JSONObject valSource = JSONObject.parseObject(item.getValSource());
                        if (valSource.containsKey("infoCode")) {
                            String infoCode = valSource.getString("infoCode");
                            infoCodes.add(infoCode);

                            String shellConfigValue = item.getShellConfigValue();
                            Map<String, String> shellConfigValueMap = Collections.singletonMap("shellConfigValue", shellConfigValue);

                            InfoCodeValueVO infoCodeValueVO = new InfoCodeValueVO();
                            infoCodeValueVO.setInfoCode(infoCode);
                            infoCodeValueVO.setObjectId(objectId);
                            infoCodeValueVO.setInfoCodeName(LocaleMessageUtil.getMessage("key_"+item.getCode()));
                            infoCodeValueVO.setAdditionalMapValue(shellConfigValueMap);
                            infoCodeValueMap.put(objectId + infoCode, infoCodeValueVO);
                            dataList.add(infoCodeValueVO);
                        }
                    }
                });

                LocalDate date = param.getDate();
                List<EnergyDataResponse> energyDataResponses = this.dtpRwdClientService.queryIPDataInPeriod(groupCode, projectId, Sets.newHashSet(objectId), infoCodes, date.atStartOfDay(), date.atStartOfDay().plusDays(1), "1d");

                if (CollectionUtil.isNotEmpty(energyDataResponses)) {
                    for (EnergyDataResponse energyDataResponse : energyDataResponses) {
                        String infoCode = energyDataResponse.getInfoCode();
                        String oId = energyDataResponse.getObjectId();
                        if (infoCodeValueMap.containsKey(oId + infoCode)) {
                            InfoCodeValueVO infoCodeValueVO = infoCodeValueMap.get(oId + infoCode);
                            infoCodeValueVO.setInfoCodeValue(energyDataResponse.getIpValue());
                        }
                    }
                }

                coldEfficiencyVO.setDataList(dataList);
            }
        }

        return coldEfficiencyVO;
    }
    
    @Override
    public void mpcAgentCfg(List<MpcAgentCfgParam> param) {
        RcBasicsConfig basicsConfig = new RcBasicsConfig();
        basicsConfig.setProjectId(ContextUtil.getProjectId());
        basicsConfig.setType(BasicsConfigTypeEnum.MPC_AGENT_CFG);
        basicsConfig.setSt(StateTypeEnum.VALID);
        basicsConfig.setResult(JSONObject.toJSONString(param));
        this.rcBasicsConfigService.saveOrUpdateByType(basicsConfig);
    }
    
    @Override
    public List<MpcAgentCfgParam> queryMpcAgentCfg() {
        RcBasicsConfig cfg = rcBasicsConfigService.lambdaQuery()
                                                  .eq(RcBasicsConfig::getProjectId, ContextUtil.getProjectId())
                                                  .eq(RcBasicsConfig::getType, BasicsConfigTypeEnum.MPC_AGENT_CFG)
                                                  .last(CommonConst.LIMIT_ONE)
                                                  .one();
        if (cfg == null) {
            return Collections.emptyList();
        }
        return JSONObject.parseArray(cfg.getResult(), MpcAgentCfgParam.class);
    }

    @Override
    public List<WeatherStationVO> queryWeatherStations() {
        try {
            String groupCode = ContextUtil.getGroupCode();
            String projectId = ContextUtil.getProjectId();

            // 查询WESEMS设备列表（气象监测站）
            List<JSONObject> objectList = this.bizDtpRwdService.queryObjectList(
                    groupCode,
                    projectId,
                    Lists.newArrayList(QueryIOTEnum.H.getDeviceCoding()),
                    null,
                    null,
                    null,
                    null
            );

            if (CollectionUtil.isEmpty(objectList)) {
                return Collections.emptyList();
            }

            // 组装WeatherStationVO并按设备名称汉字升序排序
            return objectList.stream()
                    .map(obj -> WeatherStationVO.builder()
                            .id(obj.getString("id"))
                            .name(obj.getString("localName"))
                            .build())
                    .sorted((o1, o2) -> {
                        if (o1.getName() == null && o2.getName() == null) {
                            return 0;
                        }
                        if (o1.getName() == null) {
                            return 1;
                        }
                        if (o2.getName() == null) {
                            return -1;
                        }
                        return Collator.getInstance(java.util.Locale.CHINA).compare(o1.getName(), o2.getName());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[{}] 查询气象监测站列表失败", LOG_TAG, e);
            return Collections.emptyList();
        }
    }
}
