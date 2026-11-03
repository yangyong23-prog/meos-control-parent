package com.persagy.apccontrol.task.scheduled;

import com.persagy.apccontrol.basic.opt.OptSwitch;
import com.persagy.apccontrol.common.opt.OptTwinWriteGuard;
import com.persagy.apccontrol.opt.OptClassConfSnapshot;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.config.ProjectCfg;
import com.persagy.apccontrol.basic.config.RedisKey;
import com.persagy.apccontrol.basic.controller.shellconfig.param.ShellConfigVO;
import com.persagy.apccontrol.basic.executor.log.LogExecutor;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.entity.ShellConfigGroup;
import com.persagy.apccontrol.basic.model.entity.ShellConfigItem;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigGroupEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigItemEnum;
import com.persagy.apccontrol.basic.model.enumtype.ShellConfigSceneEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpTwinClientService;
import com.persagy.apccontrol.basic.service.BizDtpRwdService;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.util.json.JsonUtils;
import com.persagy.framework.redis.cache.RedisCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务内部定时任务，不影响业务使用
 * 壳配置同步孪生的任务
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/10/18 20:53
 */
@Slf4j
@Component
public class InnerSchdule {

    @Value("${local.sync.shell-config:true}")
    private Boolean syncShellConfig;
    @Autowired
    private ProjectCfg projectCfg;
    @Autowired
    private DtpTwinClientService dtpTwinClientService;
    @Autowired
    private RedisCacheUtil redisCacheUtil;
    @Autowired
    private BizDtpRwdService bizDtpRwdService;
    @Autowired
    private AbstractCommonServer abstractCommonServer;

    private static final Logger LOGGER = LoggerFactory.getLogger(InnerSchdule.class);
    private static final String LOG_TAG = "定时任务-壳配置同步孪生";

    private static final Map<String,String> INFO_CODE_MODE_MAP = new HashMap<String, String>() {
        {
            put("chillWaterDetaTempSet", "DT");
            put("chillWaterDetaPressSet", "DP");
            put("branchDetaTempSet", "DT");
            put("branchDetaPressSet", "DP");
            put("worstTerminalDetaPress", "WTDP");
        }
    };
    /**
     * 每 5min 同步一次
     * 壳配置同步孪生的任务
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void synShellConfigToTwin() {
        // opt B8：标记定时同步，switch.opt.twinWriteDiff 打开时 Twin 配置写入只写变化（OptTwinWriteGuard）
        OptTwinWriteGuard.runScheduledSync(this::doSynShellConfigToTwin);
    }

    private void doSynShellConfigToTwin() {
        ContextUtil.setGroupCode(projectCfg.getGroupCode());
        ContextUtil.setTenant(projectCfg.getGroupCode());
        ContextUtil.setProjectId(projectCfg.getProjectId());

        // 同步壳配置参数
        if (BooleanUtil.isTrue(syncShellConfig)) {
            //湿度参数
            this.saveSupplyRH();
            // 不能同时开启的冷机参数写入
            this.wirteEquipExclusions();

            // 设备频率设定参数写入
            this.saveClassConfs();

            // 累计运行时间参数写入
            //this.saveRunningTimeClassConfs(false);

            this.saveRunningTimeClassConfs(true);

            //冰蓄冷"冷冻水泵控制模式"、"泵组控制模式"写入
            this.savePumpControlMode();

            //冷机开启限制规则
            this.saveOpenRule();

            //流量需求配置
            this.saveFlowDemandConf();

            // 冷机间隔开启限制规则（新增）
            this.saveStartIntervals();

            // 屏蔽所有控制指令（新增）
            this.saveShieldControl();

            // 主机开启台数限制规则（新增）
            this.saveHostUnitLimit();

            // 频率调节范围配置
            this.saveFreqRangeConf();

            // 穹顶通风配置参数（项目级）
            this.saveVentDomeConf();
        }
    }


    /**
     * 不能同时开启的冷机参数写入
     */
    private void wirteEquipExclusions() {
        log.info("不能同时开启的冷机参数写入BDTP任务启动，启动时间：{}", LocalDateTime.now());

        try {
            // 1.获取所有的冷站系统
            List<JSONObject> systemList = this.bizDtpRwdService.queryObjectList(projectCfg.getGroupCode(), projectCfg.getProjectId(), Lists.newArrayList(DtpRwdClassCodeEnum.COLD_SYSTEM.getValue()), null, null, null, null);
            if (CollectionUtil.isEmpty(systemList)) {
                return;
            }
            List<JSONObject> updateParamList = new ArrayList<>();
            List<JSONObject> deleteParamList = new ArrayList<>();
            for (JSONObject systemInfo : systemList) {
                String systemId = systemInfo.getString("id");
                JSONObject paramInfo = new JSONObject();
                paramInfo.put("objectId", systemId);
                paramInfo.put("paramCode", "equipExclusions");
                paramInfo.put("paramLabel", ShellConfigItemEnum.ALG_COOLING_UNABLE_SAME_TIME_COOL.getName());

                // 2.获取壳配置参数
                String shellConfig = this.abstractCommonServer.getShellConfig(projectCfg.getGroupCode(), projectCfg.getProjectId(), systemId, ShellConfigItemEnum.ALG_COOLING_UNABLE_SAME_TIME_COOL.getCode(), null);
                //String shellConfig = "Eq990999000740332c0e18e14ba18119eb10d3ada497, Eq61011220101d80d62569b647db8d3ea4e02a542ab3";
                if (StrUtil.isNotBlank(shellConfig)) {
                    // 新增
                    String[] equipObjectIds = shellConfig.split(",");
                    int maxRuns = equipObjectIds.length - 1;

                    JSONObject objectInfo = new JSONObject();
                    objectInfo.put("objectIds", Lists.newArrayList(equipObjectIds));
                    objectInfo.put("maxRuns", maxRuns < 0 ? 0 : maxRuns);

                    paramInfo.put("paramArray", Lists.newArrayList(objectInfo));
                    updateParamList.add(paramInfo);
                } else {
                    // 删除
                    deleteParamList.add(paramInfo);
                }
            }

            // 3.数据写入
            if (updateParamList.size() > 0) {
                this.dtpTwinClientService.saveExclusions(updateParamList);
            }
            if (deleteParamList.size() > 0) {
                this.dtpTwinClientService.deleteByConf(deleteParamList);
            }
        } catch (Exception e) {
            log.error("不能同时开启的冷机参数写入BDTP异常", e);
        }
    }

    /**
     * 设备频率设定参数写入
     */
    private void saveClassConfs() {
        log.info("设备频率设定参数写入BDTP任务启动，启动时间：{}", LocalDateTime.now());

        try {
            String redisKey = RedisKey.APC + CommonConst.COLON + projectCfg.getProjectId() + CommonConst.COLON + "SAVE_CLASS_CONFS_PREFIX";
            String prefix = this.redisCacheUtil.get(redisKey);
            List<JSONObject> preList = StrUtil.isBlank(prefix) ? null : JSONArray.parseArray(prefix, JSONObject.class);
            Map<String, Map<String, String>> class_code_map = CollectionUtil.isEmpty(preList) ? new HashMap<>() : preList.stream().collect(Collectors.groupingBy(key -> key.getString("classCode"),
                    Collectors.toMap(code -> code.getString("paramCode"), value -> value.getString("paramValue"), (v1, v2) -> v2)));

            List<JSONObject> updateParamList = new ArrayList<>();
            List<JSONObject> deleteParamList = new ArrayList<>();

            // 1.数据组装（PRD-260801: 取消冷却塔风机分组ACOTC和冷冻水泵分组ACCPC的推送，仅保留冷热源泵组ACPPC）
            this.saveClassConfs(updateParamList, deleteParamList, class_code_map, ShellConfigItemEnum.POLICY_DEVICE_COLDHOT_FREQ);

            // 2.数据写入
            if (updateParamList.size() > 0) {
                this.dtpTwinClientService.saveClassConfs(updateParamList);
                // 将本次写入的数据，存入redis
                // opt B8：原来只存本轮写入的子集，导致另一项下轮又被判为变化、永久交替重写；打开开关后存合并后的全量快照
                this.redisCacheUtil.set(redisKey, JsonUtils.toJson(OptSwitch.on(OptSwitch.TWIN_WRITE_DIFF)
                        ? OptClassConfSnapshot.merge(preList, updateParamList) : updateParamList));
            }
            if (deleteParamList.size() > 0) {
                this.dtpTwinClientService.deleteByClassConf(deleteParamList);
                if (OptSwitch.on(OptSwitch.TWIN_WRITE_DIFF)) {
                    // opt B8：删除后快照里去掉对应项，配置恢复时会重新写入
                    this.redisCacheUtil.set(redisKey, JsonUtils.toJson(OptClassConfSnapshot.remove(
                            OptClassConfSnapshot.merge(preList, updateParamList), deleteParamList)));
                }
            }
        } catch (Exception e) {
            log.error("设备频率设定参数写入BDTP异常", e);
        }
    }

    /**
     * 累计运行时间参数配置写入
     */
    private void saveRunningTimeClassConfs(boolean isNewVersion) {
        log.info("累计运行时间配置参数写入BDTP任务启动，启动时间：{}", LocalDateTime.now());

        try {
            // 获取壳配置
            Set<String> groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_CONTROL_COOLING.getCode(), ShellConfigGroupEnum.GROUP_CONTROL_COLD_HOT.getCode(),
                    ShellConfigGroupEnum.GROUP_CONTROL_HOT.getCode());

            if(isNewVersion){
                groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_CONTROL_AIR_COOLING.getCode(), ShellConfigGroupEnum.GROUP_CONTROL_AIR_HOT.getCode(),
                            ShellConfigGroupEnum.GROUP_CONTROL_COOL_PUMP_GROUP.getCode(), ShellConfigGroupEnum.GROUP_CONTROL_CHIL_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_TOWER_GROUP.getCode(),ShellConfigGroupEnum.GROUP_CONTROL_CGLY_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_HSLO_GROUP.getCode(),ShellConfigGroupEnum.GROUP_CONTROL_HOT_PUMP_GROUP.getCode(),
                        ShellConfigGroupEnum.GROUP_CONTROL_COOL_HOT_PUMP.getCode());
            }
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(projectCfg.getGroupCode(), projectCfg.getProjectId(), ShellConfigSceneEnum.SCENE_CONTROL.getCode(), groups);
            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                return;
            }

            // 2.根据壳配置，返回对应的设备类及其所属对象
            List<JSONObject> updateParamList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(shellConfigGroups)) {
                    continue;
                }
                for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                    List<ShellConfigItem> items = shellConfigGroup.getItems();
                    if (CollectionUtil.isEmpty(items)) {
                        continue;
                    }
                    for (ShellConfigItem item : items) {
                       // String value = StrUtil.isBlank(item.getValue()) ? item.getDefaultValues() : item.getValue();
                        String value = item.getValue();
                        if (StrUtil.isAllNotBlank(value, item.getObjectId())) {
                            String classCode = item.getCode().substring(item.getCode().lastIndexOf("_") + 1);
                            JSONObject resultObject = new JSONObject();
                            resultObject.put("objectId", item.getObjectId());
                            resultObject.put("paramCode", classCode + "_sort");
                            if(isNewVersion){
                                resultObject.put("paramCode", "accRunTimeCode");
                            }
                            resultObject.put("paramValue", value);
                            resultObject.put("paramLabel", item.getName());
                            updateParamList.add(resultObject);
                        }
                    }
                }
            }

            // 3.数据写入
            if (updateParamList.size() > 0) {
                LogExecutor.project().info(LOGGER, LOG_TAG,"累计运行时间配置参数写入BDTP任务，数据："+updateParamList);
                this.dtpTwinClientService.saveList(updateParamList);
            }
        } catch (Exception e) {
            log.error("设备频率设定参数写入BDTP异常", e);
        }
    }

    /**
     * 数据组装
     *
     * @param updateParamList
     * @param deleteParamList
     * @param class_code_map
     * @param shellConfigItemEnum
     */
    private void saveClassConfs(List<JSONObject> updateParamList, List<JSONObject> deleteParamList, Map<String, Map<String, String>> class_code_map, ShellConfigItemEnum shellConfigItemEnum) {
        String classCode = ShellConfigItemEnum.POLICY_DEVICE_FAN_FREQ.equals(shellConfigItemEnum) ? "ACOTC" : ShellConfigItemEnum.POLICY_DEVICE_PUMP_FREQ.equals(shellConfigItemEnum) ? "ACCPC"
                : ShellConfigItemEnum.POLICY_DEVICE_COLDHOT_FREQ.equals(shellConfigItemEnum) ? "ACPPC" : null;
        String className = ShellConfigItemEnum.POLICY_DEVICE_FAN_FREQ.equals(shellConfigItemEnum) ? LocaleMessageUtil.getMessage("key_76de12b27cdb42608212a1416dbd5c9a") : ShellConfigItemEnum.POLICY_DEVICE_PUMP_FREQ.equals(shellConfigItemEnum) ? LocaleMessageUtil.getMessage("key_c05b16aa5fb14eeeb2f1271aabc3d3de")
                : ShellConfigItemEnum.POLICY_DEVICE_COLDHOT_FREQ.equals(shellConfigItemEnum) ? LocaleMessageUtil.getMessage("key_09667bc92c014583ae05632ba7966f60") : null;
        if (!StrUtil.isAllNotBlank(classCode, className)) {
            return;
        }

        // 获取设备频率设定
        String shellConfig = this.abstractCommonServer.getShellConfig(projectCfg.getGroupCode(), projectCfg.getProjectId(), null, shellConfigItemEnum.getCode(), null);
        if (StrUtil.isNotBlank(shellConfig)) {
            String[] min_max_value = shellConfig.split(",");

            Map<String, String> preMap = class_code_map.get(classCode);
            String vFDMinFreqCtrl = preMap == null ? null : preMap.get("vFDMinFreqCtrl");
            String vFDMaxFreqCtrl = preMap == null ? null : preMap.get("vFDMaxFreqCtrl");

            if (!min_max_value[0].equals(vFDMinFreqCtrl)) {
                JSONObject minParamInfo = new JSONObject();
                minParamInfo.put("classCode", classCode);
                minParamInfo.put("paramCode", "vFDMinFreqCtrl");
                minParamInfo.put("paramValue", min_max_value[0]);
                minParamInfo.put("paramLabel", className + LocaleMessageUtil.getMessage("key_87b0cc27d0b243cca2babe23e90f0a32"));
                updateParamList.add(minParamInfo);
            }

            if (!min_max_value[1].equals(vFDMaxFreqCtrl)) {
                JSONObject maxParamInfo = new JSONObject();
                maxParamInfo.put("classCode", classCode);
                maxParamInfo.put("paramCode", "vFDMaxFreqCtrl");
                maxParamInfo.put("paramValue", min_max_value[1]);
                maxParamInfo.put("paramLabel", className + LocaleMessageUtil.getMessage("key_433d06ed05204729af1b33abb8e43900"));
                updateParamList.add(maxParamInfo);
            }
        } else {
            JSONObject minParamInfo = new JSONObject();
            minParamInfo.put("classCode", classCode);
            minParamInfo.put("paramCode", "vFDMinFreqCtrl");

            JSONObject maxParamInfo = new JSONObject();
            maxParamInfo.put("classCode", classCode);
            maxParamInfo.put("paramCode", "vFDMaxFreqCtrl");

            deleteParamList.add(minParamInfo);
            deleteParamList.add(maxParamInfo);
        }
    }

    /**
     * 冰蓄冷“冷冻水泵控制模式”、”泵组控制模式“写入
     */
    private void savePumpControlMode() {
        LogExecutor.project().info(LOGGER, LOG_TAG,"冰蓄冷“冷冻水泵控制模式”、”泵组控制模式“写入BDTP任务启动");

        try {
            // 获取壳配置
            Set<String> groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_ALG_ACAC_TEMP_PRESS_DIFF.getCode(),ShellConfigGroupEnum.GROUP_ALG_ACAC_DIFF.getCode());
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(projectCfg.getGroupCode(), projectCfg.getProjectId(), ShellConfigSceneEnum.SCENE_ALG.getCode(), groups);

            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                LogExecutor.project().info(LOGGER, LOG_TAG,"冰蓄冷“冷冻水泵控制模式”、”泵组控制模式“写入BDTP任务，无壳配置");
                return;
            }

            // 2.根据壳配置，返回对应的设备类及其所属对象
            List<JSONObject> updateParamList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(shellConfigGroups)) {
                    LogExecutor.project().info(LOGGER, LOG_TAG,"冰蓄冷“冷冻水泵控制模式”、”泵组控制模式“写入BDTP任务，无壳配置分组"+shellConfigVO);
                    continue;
                }
                for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                    List<ShellConfigItem> items = shellConfigGroup.getItems();
                    if (CollectionUtil.isEmpty(items)) {
                        LogExecutor.project().info(LOGGER, LOG_TAG,"冰蓄冷“冷冻水泵控制模式”、”泵组控制模式“写入BDTP任务，无壳配置单项"+shellConfigGroup);
                        continue;
                    }
                    for (ShellConfigItem item : items) {
                        if(ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_CONTROL_MODE.getCode().equals(item.getCode())
                        || ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_PUMP_CONTROL_MODE.getCode().equals(item.getCode())) {
                            String value = StrUtil.isBlank(item.getValue()) ? item.getDefaultValues() : item.getValue();
                            if (StrUtil.isAllNotBlank(value, item.getObjectId())) {
                                String valSource = item.getValSource();
                                JSONObject jsonObject = JSONObject.parseObject(valSource);
                                String classCode = jsonObject.getString("classCode");
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("objectId", item.getObjectId());
                                resultObject.put("paramCode", classCode+"_sort");
                                resultObject.put("paramValue", value);
                                resultObject.put("paramLabel", item.getName());
                                resultObject.put("pidMode", INFO_CODE_MODE_MAP.get(value));
                                updateParamList.add(resultObject);
                            }
                        }
                    }
                }
            }

            // 3.数据写入
            if (updateParamList.size() > 0) {
                LogExecutor.project().info(LOGGER, LOG_TAG,"冰蓄冷“冷冻水泵控制模式”、”泵组控制模式“写入BDTP任务，数据："+updateParamList);
                this.dtpTwinClientService.saveList(updateParamList);
            }
        } catch (JSONException | ClassCastException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "冷机开启限制规则参数写入BDTP任务启动,JSON解析异常", e);
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG,"冰蓄冷“冷冻水泵控制模式”、”泵组控制模式“写入BDTP任务", e);
        }
    }

    /**
     * 流量需求配置参数写入BDTP
     * 参数：冷冻侧流量系数(chillFlowFactor)、冷却侧流量系数(coolingFlowFactor)、冷却塔散热系数(towerFlowFactor)
     * 按选中的冷机/换热器实例分条推送，值不为空时才推送
     */
    private void saveFlowDemandConf() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "流量需求配置参数写入BDTP任务启动");
        try {
            Set<String> groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_ALG_COMMON_CONDITION.getCode());
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(
                    projectCfg.getGroupCode(),projectCfg.getProjectId(),
                    ShellConfigSceneEnum.SCENE_ALG.getCode(), groups);

            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "流量需求配置参数写入BDTP任务，无壳配置");
                return;
            }

            List<JSONObject> updateParamList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(shellConfigGroups)) {
                    continue;
                }
                for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                    List<ShellConfigItem> items = shellConfigGroup.getItems();
                    if (CollectionUtil.isEmpty(items)) {
                        continue;
                    }
                    for (ShellConfigItem item : items) {
                        if (ShellConfigItemEnum.ALG_AIR_COOLING_FLOW_DEMAND_CONF.getCode().equals(item.getCode())) {
                            //[{"key":"2026-05-08T03:19:47.998Z","selected":["Eg4403050002a5da014c253e433b8f70a1999760c97e","Eq440305000218df39aab7354632948305a53e77e8e7"],"chillFlowFactor":1},{"key":"2026-05-08T05:15:31.207Z","selected":["Eq440305000243e2a526ea8643b0964ce164091a3d04"],"chillFlowFactor":3,"coolingFlowFactor":1,"towerFlowFactor":2}]
                            String value = item.getValue();
                            //全部清空的情况
                            List<JSONObject> mulInstance = CollectionUtil.isEmpty(item.getMulInstance())?Lists.newArrayList():item.getMulInstance();
                            if (StrUtil.isBlank(value)) {
                                //全部清空
                                mulInstance.forEach(jsonObject -> {
                                    JSONObject param = new JSONObject();
                                    String objectId = jsonObject.getString("objectId");
                                    param.put("objectId", objectId);
                                    param.put("paramCode", "chillFlowFactor");
                                    param.put("paramValue", "");
                                    param.put("paramLabel", "流量需求配置");
                                    updateParamList.add(param);
                                    JSONObject param1 = new JSONObject();
                                    param1.put("objectId", objectId);
                                    param1.put("paramCode", "coolingFlowFactor");
                                    param1.put("paramValue",  "");
                                    param1.put("paramLabel", "流量需求配置");
                                    updateParamList.add(param1);
                                    JSONObject param2 = new JSONObject();
                                    param2.put("objectId", objectId);
                                    param2.put("paramCode", "towerFlowFactor");
                                    param2.put("paramValue",  "");
                                    param2.put("paramLabel", "流量需求配置");
                                    updateParamList.add(param2);
                                });
                                continue;
                            }
                            List<JSONObject> updateParamListTmp = new ArrayList<>();
                            // value 为多组配置的 JSON 数组，每组含选中设备列表及三项系数
                            // 格式示例：[{"objectIds":["eq1","eq2"],"chillFlowFactor":"1.5","coolingFlowFactor":"1.2","towerFlowFactor":"0.8"}]
                            JSONArray configs = JSONObject.parseArray(value);
                            configs.forEach(configObj -> {
                                JSONObject config = (JSONObject) configObj;
                                JSONArray objectIds = config.getJSONArray("selected");
                                String chillFlowFactor = StringUtils.isEmpty(config.getString("chillFlowFactor"))?"":config.getString("chillFlowFactor");
                                String coolingFlowFactor = StringUtils.isEmpty(config.getString("coolingFlowFactor"))?"":config.getString("coolingFlowFactor");
                                String towerFlowFactor = StringUtils.isEmpty(config.getString("towerFlowFactor"))? "":config.getString("towerFlowFactor");
                                if (CollectionUtil.isEmpty(objectIds)) {
                                    return;
                                }
                                // 按选中的每个设备实例分条推送三项系数
                                for (int i = 0; i < objectIds.size(); i++) {
                                    String objectId = objectIds.getString(i);
                                    if (StrUtil.isBlank(objectId)) {
                                        continue;
                                    }
                                    JSONObject param = new JSONObject();
                                    param.put("objectId", objectId);
                                    param.put("paramCode", "chillFlowFactor");
                                    param.put("paramValue", chillFlowFactor);
                                    param.put("paramLabel", "流量需求配置");
                                    updateParamListTmp.add(param);

                                    JSONObject param1 = new JSONObject();
                                    param1.put("objectId", objectId); // Reusing the same param variable
                                    param1.put("paramCode", "coolingFlowFactor");
                                    param1.put("paramValue", coolingFlowFactor);
                                    param1.put("paramLabel", "流量需求配置");
                                    updateParamListTmp.add(param1);

                                    JSONObject param2 = new JSONObject();
                                    param2.put("objectId", objectId);
                                    param2.put("paramCode", "towerFlowFactor");
                                    param2.put("paramValue", towerFlowFactor);
                                    param2.put("paramLabel", "流量需求配置");
                                    updateParamListTmp.add(param2);
                                }
                            });
                            Map<String, List<JSONObject>> objIdToDemandMap = updateParamListTmp.stream().collect(Collectors.groupingBy(e -> e.getString("objectId")));
                            mulInstance.forEach(coolInstance -> {
                                String objectId = coolInstance.getString("objectId");
                                if(!objIdToDemandMap.containsKey(objectId)){
                                    JSONObject param = new JSONObject();
                                    param.put("objectId", objectId);
                                    param.put("paramCode", "chillFlowFactor");
                                    param.put("paramValue",  "");
                                    param.put("paramLabel", "流量需求配置");
                                    updateParamList.add(param);
                                    JSONObject param1 = new JSONObject();
                                    param1.put("objectId", objectId);
                                    param1.put("paramCode", "coolingFlowFactor");
                                    param1.put("paramValue",  "");
                                    param1.put("paramLabel", "流量需求配置");
                                    updateParamList.add(param1);
                                    JSONObject param2 = new JSONObject();
                                    param2.put("objectId", objectId);
                                    param2.put("paramCode", "towerFlowFactor");
                                    param2.put("paramValue",  "");
                                    param2.put("paramLabel", "流量需求配置");
                                    updateParamList.add(param2);
                                }else {
                                    updateParamList.addAll(objIdToDemandMap.get(objectId));
                                }
                            });
                        }
                    }
                }
            }
            if (!updateParamList.isEmpty()) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "流量需求配置参数写入BDTP任务，数据：" + updateParamList);
                this.dtpTwinClientService.saveList(updateParamList);
            }
        } catch (JSONException | ClassCastException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "流量需求配置参数写入BDTP，JSON解析异常", e);
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "流量需求配置参数写入BDTP异常", e);
        }
    }

    private void saveOpenRule() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "冷机开启限制规则参数写入BDTP任务启动");
        try{
            Set<String> groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_ALG_COMMON_CONDITION.getCode());
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(projectCfg.getGroupCode(), projectCfg.getProjectId(), ShellConfigSceneEnum.SCENE_ALG.getCode(), groups);

            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "冷机开启限制规则参数写入BDTP任务启动,无壳配置");
                return;
            }

            List<JSONObject> updateParamList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(shellConfigGroups)) {
                    LogExecutor.project().info(LOGGER, LOG_TAG, "冷机开启限制规则参数写入BDTP任务启动,无壳配置分组"+shellConfigVO);
                    continue;
                }
                for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                    List<ShellConfigItem> items = shellConfigGroup.getItems();
                    if (CollectionUtil.isEmpty(items)) {
                        LogExecutor.project().info(LOGGER, LOG_TAG, "冷机开启限制规则参数写入BDTP任务启动,无壳配置单项"+shellConfigGroup);
                        continue;
                    }
                    for (ShellConfigItem item : items) {
                        if(ShellConfigItemEnum.ALG_AIR_COOLING_OPEN_RULE.getCode().equals(item.getCode())) {
                            String value = item.getValue();
                            String itemName = ShellConfigItemEnum.ALG_AIR_COOLING_OPEN_RULE.getName();
                            if (StrUtil.isAllNotBlank(value, item.getObjectId())) {
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("objectId", item.getObjectId());
                                resultObject.put("paramCode", "equipExclusions");
                                resultObject.put("paramLabel", itemName);
                                JSONArray configs = JSONObject.parseArray(value);
                                ArrayList<JSONObject> paramArray = new ArrayList<>();
                                configs.stream().forEach(config -> {
                                    JSONObject param = new JSONObject();
                                    param.put("objectIds", ((JSONObject) config).getJSONArray("selected"));
                                    param.put("maxRuns", ((JSONObject) config).getString("countLimit"));
                                    paramArray.add(param);
                                });
                                resultObject.put("paramArray", paramArray);
                                updateParamList.add(resultObject);
                            }
                        }
                    }
                }
            }
            if (updateParamList.size() > 0) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "冷机开启限制规则参数写入BDTP任务启动,数据："+updateParamList);
                this.dtpTwinClientService.saveExclusions(updateParamList);
            }
        } catch (JSONException | ClassCastException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "冷机开启限制规则参数写入BDTP任务启动,JSON解析异常", e);
        } catch (Exception e){
            LogExecutor.project().error(LOGGER, LOG_TAG,"冷机开启限制规则参数写入BDTP异常", e);
        }
    }

    /**
     * 冷机间隔开启限制规则参数写入BDTP
     * 覆盖空调冷源（ACAC）和冷源（ACCC）系统
     * 配置值为空时，传paramArray: []
     */
    private void saveStartIntervals() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "冷机间隔开启限制规则参数写入BDTP任务启动");
        try {
            // 1. 查询ACAC（空调冷源）和ACCC（冷源）系统列表
            List<String> classCodes = Lists.newArrayList(
                DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue(),
                DtpRwdClassCodeEnum.COLD_SYSTEM.getValue()
            );
            List<JSONObject> systemList = this.bizDtpRwdService.queryObjectList(
                projectCfg.getGroupCode(), projectCfg.getProjectId(), classCodes, null, null, null, null
            );
            if (CollectionUtil.isEmpty(systemList)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "冷机间隔开启限制规则参数写入BDTP任务启动,无系统");
                return;
            }

            // 2. 组装参数列表
            List<JSONObject> updateParamList = new ArrayList<>();
            for (JSONObject systemInfo : systemList) {
                String systemId = systemInfo.getString("id");
                String classCode = systemInfo.getString("classCode");

                // 3. 根据系统类型获取对应的壳配置参数编码
                String configCode;
                String itemName;
                if (DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(classCode)) {
                    configCode = ShellConfigItemEnum.ALG_AIR_COOLING_START_INTERVAL_RULE.getCode();
                    itemName = ShellConfigItemEnum.ALG_AIR_COOLING_START_INTERVAL_RULE.getName();
                } else {
                    configCode = ShellConfigItemEnum.ALG_COOLING_START_INTERVAL_RULE.getCode();
                    itemName = ShellConfigItemEnum.ALG_COOLING_START_INTERVAL_RULE.getName();
                }

                // 4. 获取壳配置值
                String shellConfig = this.abstractCommonServer.getShellConfig(
                    projectCfg.getGroupCode(), projectCfg.getProjectId(), systemId, configCode, null
                );

                // 5. 组装BDTP参数
                JSONObject resultObject = new JSONObject();
                resultObject.put("objectId", systemId);
                resultObject.put("paramCode", "equipStartInterval");
                resultObject.put("paramLabel", itemName);

                // 6. 解析配置值并组装paramArray
                ArrayList<JSONObject> paramArray = new ArrayList<>();
                if (StrUtil.isNotBlank(shellConfig)) {
                    try {
                        JSONArray configs = JSONObject.parseArray(shellConfig);
                        configs.forEach(configObj -> {
                            JSONObject config = (JSONObject) configObj;
                            JSONObject param = new JSONObject();
                            param.put("objectIds", config.getJSONArray("selected"));
                            // 间隔时间字段，根据design.md中的open questions，暂时使用"interval"
                            param.put("interval", config.getString("intervalTime"));
                            paramArray.add(param);
                        });
                    } catch (JSONException e) {
                        LogExecutor.project().error(LOGGER, LOG_TAG, "冷机间隔开启限制规则参数写入BDTP任务启动,JSON解析异常,systemId:" + systemId, e);
                    }
                }
                resultObject.put("paramArray", paramArray);
                updateParamList.add(resultObject);
            }

            // 7. 调用BDTP接口推送数据（包括空数组场景）
            if (!updateParamList.isEmpty()) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "冷机间隔开启限制规则参数写入BDTP任务启动,数据：" + updateParamList);
                this.dtpTwinClientService.saveStartIntervals(updateParamList);
            }
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "冷机间隔开启限制规则参数写入BDTP异常", e);
        }
    }

    /**
     * 屏蔽所有控制指令参数写入BDTP
     * 覆盖空调冷源（ACAC）和冷源（ACCC）系统
     * 配置值为空时，跳过推送（不推送）
     */
    private void saveShieldControl() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "屏蔽所有控制指令参数写入BDTP任务启动");
        try {
            // 1. 查询ACAC（空调冷源）和ACCC（冷源）系统列表
            List<String> classCodes = Lists.newArrayList(
                DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue(),
                DtpRwdClassCodeEnum.COLD_SYSTEM.getValue()
            );
            List<JSONObject> systemList = this.bizDtpRwdService.queryObjectList(
                projectCfg.getGroupCode(), projectCfg.getProjectId(), classCodes, null, null, null, null
            );
            if (CollectionUtil.isEmpty(systemList)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "屏蔽所有控制指令参数写入BDTP任务启动,无系统");
                return;
            }

            // 2. 逐个系统处理并推送
            for (JSONObject systemInfo : systemList) {
                String systemId = systemInfo.getString("id");
                String classCode = systemInfo.getString("classCode");

                // 3. 根据系统类型获取对应的壳配置参数编码
                String configCode;
                if (DtpRwdClassCodeEnum.ACAC_SYSTEM.getValue().equals(classCode)) {
                    configCode = ShellConfigItemEnum.ALG_AIR_COOLING_SHIELD_ALL_ENABLED.getCode();
                } else {
                    configCode = ShellConfigItemEnum.ALG_COOLING_SHIELD_ALL_ENABLED.getCode();
                }

                // 4. 获取壳配置值
                String shellConfig = this.abstractCommonServer.getShellConfig(
                    projectCfg.getGroupCode(), projectCfg.getProjectId(), systemId, configCode, null
                );

                // 5. 空值处理：配置值为空时跳过推送（PRD要求：不为空时才推送）
                if (StrUtil.isBlank(shellConfig)) {
                    continue;
                }

                // 6. 配置值转换："true" → revise=1, "false" → revise=0
                Integer revise;
                if ("true".equalsIgnoreCase(shellConfig.trim())) {
                    revise = 1;
                } else if ("false".equalsIgnoreCase(shellConfig.trim())) {
                    revise = 0;
                } else {
                    // 非标准值时记录WARN日志并跳过
                    LogExecutor.project().warn(LOGGER, LOG_TAG, "屏蔽所有控制指令参数格式异常,systemId:" + systemId + ",value:" + shellConfig);
                    continue;
                }

                // 7. 组装BDTP参数并推送
                JSONObject param = new JSONObject();
                param.put("objectId", systemId);
                param.put("revise", revise);

                LogExecutor.project().info(LOGGER, LOG_TAG, "屏蔽所有控制指令参数写入BDTP任务,数据：" + param);
                try {
                    this.dtpTwinClientService.reviseObjectInSys(param);
                } catch (Exception e) {
                    LogExecutor.project().error(LOGGER, LOG_TAG, "屏蔽所有控制指令参数写入BDTP异常", e);
                }
            }
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "屏蔽所有控制指令参数查询BDTP实例数据异常", e);
        }
    }

    /**
     * 主机开启台数限制规则参数写入BDTP
     * 覆盖空调冷热源（ACAI）系统
     * 配置值为空时，传paramArray: []
     */
    private void saveHostUnitLimit() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "主机开启台数限制规则参数写入BDTP任务启动");
        try {
            // 1. 查询ACAI（空调冷热源）系统列表
            List<String> classCodes = Lists.newArrayList(
                DtpRwdClassCodeEnum.ACAI_SYSTEM.getValue()
            );
            List<JSONObject> systemList = this.bizDtpRwdService.queryObjectList(
                projectCfg.getGroupCode(), projectCfg.getProjectId(), classCodes, null, null, null, null
            );
            if (CollectionUtil.isEmpty(systemList)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "主机开启台数限制规则参数写入BDTP任务启动,无系统");
                return;
            }

            // 2. 组装参数列表
            List<JSONObject> updateParamList = new ArrayList<>();
            for (JSONObject systemInfo : systemList) {
                String systemId = systemInfo.getString("id");

                // 3. 获取壳配置值
                String shellConfig = this.abstractCommonServer.getShellConfig(
                    projectCfg.getGroupCode(), projectCfg.getProjectId(), systemId,
                    ShellConfigItemEnum.ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE.getCode(), null
                );

                // 4. 组装BDTP参数
                JSONObject resultObject = new JSONObject();
                resultObject.put("objectId", systemId);
                resultObject.put("paramCode", "equipExclusions");
                resultObject.put("paramLabel", ShellConfigItemEnum.ALG_AIR_COOLING_HOT_HOST_UNIT_LIMIT_RULE.getName());

                // 5. 解析配置值并组装paramArray
                ArrayList<JSONObject> paramArray = new ArrayList<>();
                if (StrUtil.isNotBlank(shellConfig)) {
                    try {
                        JSONArray configs = JSONObject.parseArray(shellConfig);
                        configs.forEach(configObj -> {
                            JSONObject config = (JSONObject) configObj;
                            JSONObject param = new JSONObject();
                            param.put("objectIds", config.getJSONArray("selected"));
                            param.put("maxRuns", config.getString("countLimit"));
                            paramArray.add(param);
                        });
                    } catch (JSONException e) {
                        LogExecutor.project().error(LOGGER, LOG_TAG, "主机开启台数限制规则参数写入BDTP任务启动,JSON解析异常,systemId:" + systemId, e);
                    }
                }
                resultObject.put("paramArray", paramArray);
                updateParamList.add(resultObject);
            }

            // 6. 调用BDTP接口推送数据（包括空数组场景）
            if (!updateParamList.isEmpty()) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "主机开启台数限制规则参数写入BDTP任务启动,数据：" + updateParamList);
                this.dtpTwinClientService.saveExclusions(updateParamList);
            }
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "主机开启台数限制规则参数写入BDTP异常", e);
        }
    }

    //湿度相关参数配置
    private void saveSupplyRH() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "湿度参数写入BDTP任务启动");
        try{
            Set<String> groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_ALG_DEMAND_ERROR.getCode(),ShellConfigGroupEnum.GROUP_ALG_OUT_WET_BALL_TEMP.getCode(),ShellConfigGroupEnum.GROUP_ALG_OUT_WET_BALL_TEMP_ACCC.getCode());
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(projectCfg.getGroupCode(), projectCfg.getProjectId(), ShellConfigSceneEnum.SCENE_ALG.getCode(), groups);

            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "湿度参数写入BDTP任务启动,无壳配置");
                return;
            }

            List<JSONObject> updateParamList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(shellConfigGroups)) {
                    LogExecutor.project().info(LOGGER, LOG_TAG, "湿度参数写入BDTP任务启动,无壳配置分组"+shellConfigVO);
                    continue;
                }
                for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                    List<ShellConfigItem> items = shellConfigGroup.getItems();
                    if (CollectionUtil.isEmpty(items)) {
                        LogExecutor.project().info(LOGGER, LOG_TAG, "湿度参数写入BDTP任务启动,无壳配置单项"+shellConfigGroup);
                        continue;
                    }
                    for (ShellConfigItem item : items) {
                        if(ShellConfigItemEnum.ALG_SPACE_RH_LOWER.getCode().equals(item.getCode())) {
                            String value = item.getValue();
                            String itemName = ShellConfigItemEnum.ALG_SPACE_RH_LOWER.getName();
                            if (StrUtil.isAllNotBlank(value)) {
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("paramCode", "supplyRHLowerLimLtm");
                                resultObject.put("paramValue", value);
                                resultObject.put("paramLabel", itemName);
                                resultObject.put("objectId", ContextUtil.getProjectId());
                                updateParamList.add(resultObject);
                            }
                        }else if(ShellConfigItemEnum.ALG_SPACE_RH_THRESHOLD.getCode().equals(item.getCode())) {
                            String value = item.getValue();
                            String itemName = ShellConfigItemEnum.ALG_SPACE_RH_THRESHOLD.getName();
                            if (StrUtil.isAllNotBlank(value)) {
                                String[] split = value.split(",");
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("paramCode", "supplyRHLowerLim");
                                resultObject.put("paramValue", split[0]);
                                resultObject.put("paramLabel", itemName);
                                resultObject.put("objectId", ContextUtil.getProjectId());
                                updateParamList.add(resultObject);

                                if(StrUtil.isAllNotBlank(split[1])){
                                    JSONObject resultObject1 = new JSONObject();
                                    resultObject1.put("paramCode", "supplyRHUpperLim");
                                    resultObject1.put("paramValue", split[1]);
                                    resultObject1.put("paramLabel", itemName);
                                    resultObject1.put("objectId", ContextUtil.getProjectId());
                                    updateParamList.add(resultObject1);
                                }
                            }
                        }else if(ShellConfigItemEnum.ALG_SPACE_ADJUST_TIME.getCode().equals(item.getCode())) {
                            String value = item.getValue();
                            String itemName = ShellConfigItemEnum.ALG_SPACE_ADJUST_TIME.getName();
                            if (StrUtil.isAllNotBlank(value)) {
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("paramCode", "adjustmentLtm");
                                resultObject.put("paramValue", value);
                                resultObject.put("paramLabel", itemName);
                                resultObject.put("objectId", ContextUtil.getProjectId());
                                updateParamList.add(resultObject);
                            }
                        }else if(ShellConfigItemEnum.ALG_AIR_COOLING_WET_BULB_TEMP_RANGE.getCode().equals(item.getCode())
                                || ShellConfigItemEnum.ALG_COOLING_WET_BULB_TEMP_RANGE.getCode().equals(item.getCode())
                        ){
                            String value = item.getValue();
                            String itemName = item.getName();
                            if (StrUtil.isAllNotBlank(value)) {
                                JSONObject resultObject = new JSONObject();
                                resultObject.put("paramCode", item.getCode());
                                resultObject.put("paramValue", value);
                                resultObject.put("paramLabel", itemName);
                                resultObject.put("objectId", ContextUtil.getProjectId());
                                updateParamList.add(resultObject);
                            }
                        }
                    }
                }
            }
            if (updateParamList.size() > 0) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "湿度参数写入BDTP任务启动,数据："+updateParamList);
                this.dtpTwinClientService.saveList(updateParamList);
            }
        } catch (JSONException | ClassCastException e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "湿度参数写入BDTP任务启动,JSON解析异常", e);
        } catch (Exception e){
            LogExecutor.project().error(LOGGER, LOG_TAG,"湿度参数写入BDTP异常", e);
        }
    }

    /**
     * 频率调节范围配置参数写入BDTP
     * 覆盖空调冷源（ACAC）和冷站（ACCC）系统的8个FREQ_RANGE壳配置参数
     * 将范围值（如"40,50"）拆分为vFDMinFreqCtrl和vFDMaxFreqCtrl两个独立参数推送
     * 按控制组实例（objectId）推送，调用saveList接口
     */
    private void saveFreqRangeConf() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "频率调节范围配置参数写入BDTP任务启动");
        try {
            Set<String> groups = Sets.newHashSet(
                    ShellConfigGroupEnum.GROUP_ALG_ACAC_FREQ.getCode(),
                    ShellConfigGroupEnum.GROUP_ALG_ACCC_FREQ.getCode()
            );
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(
                    projectCfg.getGroupCode(), projectCfg.getProjectId(),
                    ShellConfigSceneEnum.SCENE_ALG.getCode(), groups);

            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "频率调节范围配置参数写入BDTP任务，无壳配置");
                return;
            }

            // 9个FREQ_RANGE壳配置枚举码集合
            Set<String> freqRangeCodes = Sets.newHashSet(
                    ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_1_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_2_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACOPC_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACOTC_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_AIR_COOLING_PUMP_ACPPC_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_COOLING_PUMP_1_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_COOLING_PUMP_2_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_COOLING_PUMP_ACOPC_FREQ_RANGE.getCode(),
                    ShellConfigItemEnum.ALG_COOLING_PUMP_ACOTC_FREQ_RANGE.getCode()
            );

            List<JSONObject> updateParamList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(shellConfigGroups)) {
                    continue;
                }
                for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                    List<ShellConfigItem> items = shellConfigGroup.getItems();
                    if (CollectionUtil.isEmpty(items)) {
                        continue;
                    }
                    for (ShellConfigItem item : items) {
                        // 匹配8个FREQ_RANGE枚举码
                        if (!freqRangeCodes.contains(item.getCode())) {
                            continue;
                        }

                        // objectId为空时跳过
                        if (StrUtil.isBlank(item.getObjectId())) {
                            continue;
                        }

                        String value = item.getValue();
                        String paramLabel = item.getName();

                        // 值为空时推送空值
                        if (StrUtil.isBlank(value)) {
                            JSONObject minParam = new JSONObject();
                            minParam.put("objectId", item.getObjectId());
                            minParam.put("paramCode", "vFDMinFreqCtrl");
                            minParam.put("paramValue", "");
                            minParam.put("paramLabel", paramLabel);
                            updateParamList.add(minParam);

                            JSONObject maxParam = new JSONObject();
                            maxParam.put("objectId", item.getObjectId());
                            maxParam.put("paramCode", "vFDMaxFreqCtrl");
                            maxParam.put("paramValue", "");
                            maxParam.put("paramLabel", paramLabel);
                            updateParamList.add(maxParam);
                            continue;
                        }

                        // 值格式校验：按逗号拆分
                        String[] parts = value.split(",");
                        if (parts.length < 2) {
                            LogExecutor.project().warn(LOGGER, LOG_TAG,
                                    "频率调节范围配置值格式异常，code:" + item.getCode() + ",value:" + value);
                            continue;
                        }

                        // 拆分为vFDMinFreqCtrl和vFDMaxFreqCtrl两条推送记录
                        JSONObject minParam = new JSONObject();
                        minParam.put("objectId", item.getObjectId());
                        minParam.put("paramCode", "vFDMinFreqCtrl");
                        minParam.put("paramValue", parts[0]);
                        minParam.put("paramLabel", paramLabel);
                        updateParamList.add(minParam);

                        JSONObject maxParam = new JSONObject();
                        maxParam.put("objectId", item.getObjectId());
                        maxParam.put("paramCode", "vFDMaxFreqCtrl");
                        maxParam.put("paramValue", parts[1]);
                        maxParam.put("paramLabel", paramLabel);
                        updateParamList.add(maxParam);
                    }
                }
            }

            // 调用BDTP接口推送数据
            if (!updateParamList.isEmpty()) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "频率调节范围配置参数写入BDTP任务，数据：" + updateParamList);
                this.dtpTwinClientService.saveList(updateParamList);
            }
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "频率调节范围配置参数写入BDTP异常", e);
        }
    }

    /**
     * 穹顶通风配置参数写入BDTP
     * 将「穹顶通风」（group_control_vent_dome）分组下全部项目级壳配置参数同步至BDTP，
     * objectId取当前项目ID，值为空时取默认值，调用saveList接口
     */
    private void saveVentDomeConf() {
        LogExecutor.project().info(LOGGER, LOG_TAG, "穹顶通风配置参数写入BDTP任务启动");
        try {
            Set<String> groups = Sets.newHashSet(ShellConfigGroupEnum.GROUP_CONTROL_VENT_DOME.getCode());
            List<ShellConfigVO> shellConfigVOS = this.abstractCommonServer.queryShellConfigByScene(
                    projectCfg.getGroupCode(), projectCfg.getProjectId(),
                    ShellConfigSceneEnum.SCENE_CONTROL.getCode(), groups);

            if (CollectionUtil.isEmpty(shellConfigVOS)) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "穹顶通风配置参数写入BDTP任务，无壳配置");
                return;
            }

            List<JSONObject> updateParamList = new ArrayList<>();
            for (ShellConfigVO shellConfigVO : shellConfigVOS) {
                List<ShellConfigGroup> shellConfigGroups = shellConfigVO.getGroups();
                if (CollectionUtil.isEmpty(shellConfigGroups)) {
                    continue;
                }
                for (ShellConfigGroup shellConfigGroup : shellConfigGroups) {
                    List<ShellConfigItem> items = shellConfigGroup.getItems();
                    if (CollectionUtil.isEmpty(items)) {
                        continue;
                    }
                    for (ShellConfigItem item : items) {
                        // 穹顶通风为项目级配置（无对象实例），objectId取当前项目ID；值为空时取默认值
                        String paramValue = item.getShellConfigValue();
                        JSONObject resultObject = new JSONObject();
                        resultObject.put("objectId", ContextUtil.getProjectId());
                        resultObject.put("paramCode", item.getCode());
                        resultObject.put("paramValue", StrUtil.isBlank(paramValue) ? "" : paramValue);
                        resultObject.put("paramLabel", item.getName());
                        updateParamList.add(resultObject);
                    }
                }
            }

            if (!updateParamList.isEmpty()) {
                LogExecutor.project().info(LOGGER, LOG_TAG, "穹顶通风配置参数写入BDTP任务，数据：" + updateParamList);
                this.dtpTwinClientService.saveList(updateParamList);
            }
        } catch (Exception e) {
            LogExecutor.project().error(LOGGER, LOG_TAG, "穹顶通风配置参数写入BDTP异常", e);
        }
    }
}
