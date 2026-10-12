package com.persagy.apccontrol.basic.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractRunningMonitorServer;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.RcInfoCodeGroupDTO;
import com.persagy.apccontrol.basic.model.entity.InfoCodeSetExtend;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.outside.dto.DtpPointInfo;
import com.persagy.apccontrol.basic.outside.vo.riskevent.ExceptionEventBaseInfoVO;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 对象信息点处理
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2022/12/12 16:01
 */
@Slf4j
public class ObjectInfoCodeGroupTask implements Callable<Boolean> {

    private Boolean riskEvent;
    private JSONObject objectInfo;
    private JSONObject resultObject;
    private String runModelSet;
    private String systemClassCode;
    private Map<String, Set<String>> object_infoCode;
    private AbstractRunningMonitorServer abstractRunningMonitorServer;

    public ObjectInfoCodeGroupTask(JSONObject objectInfo, JSONObject resultObject, String runModelSet, String systemClassCode,
                                   Boolean riskEvent, Map<String, Set<String>> object_infoCode,
                                   AbstractRunningMonitorServer abstractRunningMonitorServer) {
        this.riskEvent = riskEvent;
        this.objectInfo = objectInfo;
        this.resultObject = resultObject;
        this.runModelSet = runModelSet;
        this.systemClassCode = systemClassCode;
        this.object_infoCode = object_infoCode;
        this.abstractRunningMonitorServer = abstractRunningMonitorServer;
    }

    @Override
    public Boolean call() {
        String objectId = objectInfo.getString("id");
        String groupCode = objectInfo.getString("groupCode");
        String projectId = objectInfo.getString("projectId");
        String classCode = objectInfo.getString("classCode");
        String installType = objectInfo.getString("installType");
        try {
            String tempCode = null;
            if ("ACATAH".equals(classCode)) {
                tempCode = "1".equals(installType) ? "ACATAHG" : "2".equals(installType) ? "ACATAHZ" : null;
            }

            List<RcInfoCodeCfg> allInfoCodeConfigs = this.abstractRunningMonitorServer.getInfoCodesByClassCode(classCode);
            List<RcInfoCodeCfg> controlInfoCodeConfigs = this.abstractRunningMonitorServer.getControlInfoCodesByClassCode(StrUtil.isBlank(tempCode) ? classCode : tempCode);

            // 信息点数据分组处理
            Map<String, RcInfoCodeCfg> controlInfoCodeMap = CollectionUtil.isEmpty(controlInfoCodeConfigs) ? new HashMap<>()
                    : controlInfoCodeConfigs.stream().collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));

            Map<String, RcInfoCodeCfg> infoCodeCfgMap = CollectionUtil.isEmpty(allInfoCodeConfigs) ? new HashMap<>() : allInfoCodeConfigs.stream()
                    .filter(item -> {
                        if (StrUtil.isNotBlank(item.getMergeCode()) && (item.getExtendInfo() == null || CollectionUtil.isEmpty(item.getExtendInfo().getMergeInfoCodes()))) {
                            return false;
                        }
                        return StrUtil.isNotBlank(item.getGroupInfo());
                    })
                    .collect(Collectors.toMap(RcInfoCodeCfg::getInfoCode, Function.identity(), (v1, v2) -> v2));

            // 1.获取动态信息点的IOT数据
            Map<String, DtpPointInfo> pointInfoMap = this.queryIotRealDataList(groupCode, projectId, objectId, allInfoCodeConfigs, controlInfoCodeConfigs);


            // 2.写入动态信息点数据
            Map<String, RcInfoCodeGroupDTO> allGroupTree = new HashMap<>();
            Map<String, RcInfoCodeGroupDTO> controlGroupTree = new HashMap<>();
            boolean heatGroup = "ACAHC".equals(classCode) || "ACCCAH".equals(classCode);

            Set<String> existRules = object_infoCode.get(objectId);
            //existRules = CollectionUtil.isEmpty(existRules) ? new HashSet<>() : existRules;
            //existRules.addAll(Sets.newHashSet("orderFailAlarm", "manualAutoSet", "manualAutoStatus"));
            if (CollectionUtil.isNotEmpty(existRules)) {
                for (String infoCode : existRules) {
                    if (heatGroup && InfoCodeEnum.RUN_STATUS.getCode().equals(infoCode)) {
                        // 空气源热泵的和泵组的单独处理
                        continue;
                    }
                    if ("ACCI".equals(systemClassCode)) {
                        if (StrUtil.isBlank(runModelSet) && ("chillWaterOutTempSet".equals(infoCode) || "heatWaterSupplyTempSet".equals(infoCode))) {
                            continue;
                        } else if ("1".equals(runModelSet) && "heatWaterSupplyTempSet".equals(infoCode)) {
                            continue;
                        } else if ("2".equals(runModelSet) && "chillWaterOutTempSet".equals(infoCode)) {
                            continue;
                        }
                    }

                    // 写入动态数据，也就是 paramType 为 运行的参数（包含设定，只要是IOT数据，均为运行参数）
                    this.abstractRunningMonitorServer.loadInfoCodeGroupTree(allGroupTree, infoCodeCfgMap.get(infoCode), objectInfo, pointInfoMap.get(infoCode), true, false);
                    this.abstractRunningMonitorServer.loadInfoCodeGroupTree(controlGroupTree, controlInfoCodeMap.get(infoCode), objectInfo, pointInfoMap.get(infoCode), true, false);
                }
            }

            // 3.额外逻辑处理
            if (heatGroup) {
                this.abstractRunningMonitorServer.loadInfoCodeGroupTree(allGroupTree, infoCodeCfgMap.get(InfoCodeEnum.RUN_STATUS.getCode()), objectInfo,
                        pointInfoMap.get(InfoCodeEnum.RUN_STATUS.getCode()), true, false);
            }

            // 4.写入一次静态数据
            if (CollectionUtil.isNotEmpty(allInfoCodeConfigs)) {
                for (RcInfoCodeCfg infoCodeConfig : allInfoCodeConfigs) {
                    this.abstractRunningMonitorServer.loadInfoCodeGroupTree(allGroupTree, infoCodeConfig, objectInfo, null, false, false);

                    // 如果存在聚合点位数据，赋值聚合数据
                    infoCodeConfig.setLinkCodes(this.abstractRunningMonitorServer.getLinkInfoCodes(groupCode, projectId, objectId, infoCodeConfig, objectInfo));
                }
            }

            // 5.控制数据响应数据封装
            List<RcInfoCodeGroupDTO> controlGroup = new ArrayList<>(controlGroupTree.values());
            RcInfoCodeGroupDTO.removeEmptyGroupInfo(controlGroup);
            Collections.sort(controlGroup);
            // 5.1补充扩展配置
            List<InfoCodeSetExtend> infoCodeSetExtends = this.abstractRunningMonitorServer.listInfoCodeSetExtend(objectId);
            if (CollUtil.isNotEmpty(infoCodeSetExtends)) {
                // 信息点和配置映射
                Map<String, InfoCodeSetExtend> infoCodeSetExtendMap = infoCodeSetExtends.stream().collect(Collectors.toMap(InfoCodeSetExtend::getInfoCode, Function.identity(), (v1, v2) -> v2));
                for (RcInfoCodeGroupDTO rcInfoCodeGroup : controlGroup) {
                    for (RcInfoCodeCfg tcInfoCodeCfg : rcInfoCodeGroup.getInfoCodes()) {
                        if (!infoCodeSetExtendMap.containsKey(tcInfoCodeCfg.getInfoCode())) {
                            continue;
                        }
                        tcInfoCodeCfg.setExtendSetting(infoCodeSetExtendMap.get(tcInfoCodeCfg.getInfoCode()).getExtend());
                    }
                }
            }
            resultObject.put("controlInfoCode", controlGroup);

            // 6.右侧弹窗数据响应数据封装
            List<RcInfoCodeGroupDTO> allGroup = new ArrayList<>(allGroupTree.values());
            RcInfoCodeGroupDTO.removeEmptyGroupInfo(allGroup);
            Collections.sort(allGroup);
            resultObject.put("allInfoCode", allGroup);

            // 7.写入异常事件
            this.wirteRiskEventData(groupCode, projectId, objectId, classCode);

        } catch (Exception e) {
            log.error("对象ID：{}，信息点分类组装失败", objectId, e);
        }
        return true;
    }

    /**
     * 获取IOT数据
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param allInfoCodeConfigs
     * @param controlInfoCodeConfigs
     * @return
     */
    private Map<String, DtpPointInfo> queryIotRealDataList(String groupCode, String projectId, String objectId, List<RcInfoCodeCfg> allInfoCodeConfigs, List<RcInfoCodeCfg> controlInfoCodeConfigs) {
        allInfoCodeConfigs = CollectionUtil.isEmpty(allInfoCodeConfigs) ? new ArrayList<>() : allInfoCodeConfigs;
        if (CollectionUtil.isNotEmpty(controlInfoCodeConfigs)) {
            allInfoCodeConfigs.addAll(controlInfoCodeConfigs);
        }
        List<DtpPointInfo> pointInfos = this.abstractRunningMonitorServer.queryCommonIotRealDataList(groupCode, projectId, objectId, allInfoCodeConfigs);
        return CollectionUtil.isEmpty(pointInfos) ? new HashMap<>() : pointInfos.stream().collect(Collectors.toMap(DtpPointInfo::getInfoCode, Function.identity(), (v1, v2) -> v2));
    }

    /**
     * 写入异常事件等数据
     *
     * @param groupCode
     * @param projectId
     * @param objectId
     * @param classCode
     */
    private void wirteRiskEventData(String groupCode, String projectId, String objectId, String classCode) {
        if (!BooleanUtil.isTrue(riskEvent)) {
            return;
        }

        try {
            // 获取异常事件
            Set<String> objectIds = Sets.newHashSet(objectId);
            if ("ACOTU".equals(classCode)) {
                // 如果是冷却塔控制单元，获取下面的风机和阀门的异常事件
                List<JSONObject> relationObjects = this.abstractRunningMonitorServer.queryRelationObjectList(groupCode, projectId, Lists.newArrayList(objectId), Lists.newArrayList("ACCCOF", "ACVVEO"),
                        "CombineSubset", "Eg2Eq", CommonConst.TO_RELATION);
                if (CollectionUtil.isNotEmpty(relationObjects)) {
                    relationObjects.forEach(item -> objectIds.add(item.getString("id")));
                }
            }

            // 查询出对应的异常事件
            List<ExceptionEventBaseInfoVO> exceptEventInfoDTOS = new ArrayList<>();
            Map<String, List<ExceptionEventBaseInfoVO>> listMap = CollectionUtil.isEmpty(objectIds) ? null : this.abstractRunningMonitorServer.queryRiskEventList(Lists.newArrayList(objectIds), null);
            if (CollectionUtil.isNotEmpty(listMap)) {
                listMap = ExceptionEventBaseInfoVO.duplicateRemoval(listMap);
                listMap.forEach((key, value) -> {
                    if (CollectionUtil.isNotEmpty(value)) {
                        exceptEventInfoDTOS.addAll(value);
                    }
                });
            }

            exceptEventInfoDTOS.sort(Comparator.nullsFirst(Comparator.comparing(ExceptionEventBaseInfoVO::getExceptionStartTime)).reversed());

            resultObject.put("exceptionEventInfoList", exceptEventInfoDTOS);
        } catch (Exception e) {
            log.error("查询异常事件失败", e);
        }
    }

    public static List<Boolean> joinAll(List<Future<Boolean>> futureList) {
        List<Boolean> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(futureList)) {
            return result;
        }
        try {
            for (Future<Boolean> future : futureList) {
                if (future == null) {
                    continue;
                }
                result.add(future.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
