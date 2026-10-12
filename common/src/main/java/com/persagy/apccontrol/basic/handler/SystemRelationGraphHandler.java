package com.persagy.apccontrol.basic.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.CommonConst;
import com.persagy.apccontrol.basic.model.dto.DirectRelationDTO;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdSystemClassCodeEnum;
import com.persagy.apccontrol.basic.model.vo.systemgraph.EquipmentNodeVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.GraphGroupVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.SystemGraphInstanceVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.ExceptionEventConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.ModelDiagramPrimitivesVo;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.impl.ConsumptionSystemGraphService;
import com.persagy.apccontrol.basic.utils.CommUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 系统图数据处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/15 15:03
 */
@Slf4j
@Component
public class SystemRelationGraphHandler {

    @Lazy
    @Autowired
    private ConsumptionSystemGraphService systemGraphService;
    @Autowired
    private CommonConfigService commonConfigService;

    /**
     * 获取模型图图元，配置的动态规则
     *
     * @return key: 图名， value: 扩展规则
     */
    public List<DirectRelationDTO> getPrimitivesConfig(String systemClassCode) {
        String imageName = this.getMapTitleByClassCode(systemClassCode);
        if (StrUtil.isBlank(imageName)) {
            return null;
        }

        Map<String, List<DirectRelationDTO>> primitivesConfig = this.getPrimitivesConfig();
        return primitivesConfig.get(imageName);
    }

    /**
     * 获取图名
     *
     * @param classCode
     */
    public String getMapTitleByClassCode(String classCode) {
        if (StrUtil.isBlank(classCode)) {
            return null;
        }
        try {
            List<GraphConfigVo> graphConfig = this.commonConfigService.getGraphConfig();
            if (CollectionUtil.isEmpty(graphConfig)) {
                return null;
            }
            for (GraphConfigVo graphConfigVo : graphConfig) {
                String diagramBaseObjectCode = graphConfigVo.getDiagramBaseObjectCode();
                if (classCode.equalsIgnoreCase(diagramBaseObjectCode)) {
                    return graphConfigVo.getMapTitle();
                }
            }
        } catch (Exception e) {
            log.error("系统图图名配置数据加载异常", e);
        }
        return null;
    }

    /**
     * 获取模型图图元，配置的动态规则
     *
     * @return key: 图名， value: 扩展规则
     */
    public Map<String, List<DirectRelationDTO>> getPrimitivesConfig() {
        Map<String, List<DirectRelationDTO>> resultRelationMap = new HashMap<>();

        try {
            List<ModelDiagramPrimitivesVo> modelDiagramPrimitives = this.commonConfigService.getModelDiagramPrimitives();
            if (CollectionUtil.isEmpty(modelDiagramPrimitives)) {
                return resultRelationMap;
            }
            for (ModelDiagramPrimitivesVo modelDiagramPrimitive : modelDiagramPrimitives) {
                String imageOrProductName = modelDiagramPrimitive.getMapTitle();
                String addRule = modelDiagramPrimitive.getAddRule();
                if (!StrUtil.isAllNotBlank(imageOrProductName, addRule)) {
                    continue;
                }
                String[] split = addRule.split("\n");
                for (String rule : split) {
                    //如果包含Equipments，则表明要通过系统图设备查询
                    if (rule.contains("Equipments")) {
                        continue;
                    }
                    DirectRelationDTO directRelationDTO = CommUtils.parseRule(rule);
                    if (directRelationDTO != null) {
                        resultRelationMap.computeIfAbsent(imageOrProductName, e -> Lists.newArrayList()).add(directRelationDTO);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取模型图图元信息异常", e);
        }
        return resultRelationMap;
    }

    /**
     * 获取异常事件配置数据，并解析
     *
     * @param classCode
     * @return
     */
    public List<DirectRelationDTO> getRiskEventConfig(String classCode) {
        List<DirectRelationDTO> resultList = new ArrayList<>();
        try {
            // 获取图名
            String imageName = this.getMapTitleByClassCode(classCode);
            if (StrUtil.isBlank(imageName)) {
                return resultList;
            }

            // 获取异常事件配置
            List<ExceptionEventConfigVo> exceptionEventConfigs = this.commonConfigService.getExceptionEventConfig();
            if (CollectionUtil.isEmpty(exceptionEventConfigs)) {
                return resultList;
            }
            for (ExceptionEventConfigVo exceptionEventConfig : exceptionEventConfigs) {
                String addRule = exceptionEventConfig.getAddRule();
                if (StrUtil.isBlank(addRule) || !imageName.equals(exceptionEventConfig.getMapTitle())) {
                    continue;
                }
                // 解析规则
                DirectRelationDTO directRelationDTO = CommUtils.parseRule(addRule);
                if (directRelationDTO == null) {
                    continue;
                }
                if (CommonConst.FROM_RELATION.equals(directRelationDTO.getDirect())) {
                    if (classCode.equals(directRelationDTO.getToClassCode())) {
                        resultList.add(directRelationDTO);
                    }
                } else if (CommonConst.TO_RELATION.equals(directRelationDTO.getDirect())) {
                    if (classCode.equals(directRelationDTO.getFromClassCode())) {
                        resultList.add(directRelationDTO);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

    /**
     * 实时解析系统图数据，获取对应的设备、设备组、系统等等数据
     *
     * @param projectId
     * @param systemId
     * @param systemClassCode
     * @param graphTypeCode
     * @return
     */
    public Set<String> getObjectIdsBySystemGraph(String projectId, String systemId, String systemClassCode, String graphTypeCode) {
        Set<String> result = new HashSet<>();
        try {
            if (StrUtil.isBlank(ContextUtil.getProjectId())) {
                ContextUtil.setProjectId(projectId);
            }
            // 1.获取系统图的图ID
            List<SystemGraph> systemGraphs = this.systemGraphService.getNoDataPoBySystemObjectIdAndClassCode(systemId, systemClassCode, graphTypeCode);
            if (CollectionUtil.isEmpty(systemGraphs)) {
                return result;
            }
            // 2.根据图ID，获取对应的系统图
            for (SystemGraph systemGraph : systemGraphs) {
                SystemGraphInstanceVo systemGraphInstanceVo = this.systemGraphService.querySystemGraphDataNoProcess(systemGraph.getId());
                if (systemGraphInstanceVo == null) {
                    continue;
                }
                if (DtpRwdSystemClassCodeEnum.ACAT.getValue().equals(systemClassCode)) {
                    this.wirteACAT2DSystemCache(systemGraphInstanceVo, null, result);
                } else {
                    this.wirteACCC2DSystemCache(systemGraphInstanceVo, null, result);
                }
            }
        } catch (Exception e) {
            log.error("系统图数据加载异常", e);
        }
        return result;
    }

    /**
     * 实时解析系统图数据，获取对应的设备、设备组、系统等等数据
     *
     * @param projectId
     * @param systemGraphId
     * @param systemClassCode
     * @return
     */
    public Map<String, Set<String>> getObjectIdsBySystemGraphClass(String projectId, Long systemGraphId,
                                                                   String systemClassCode) {
        Map<String, Set<String>> systemGraphAllObjectMap = new HashMap<>();
        try {
            if (StrUtil.isBlank(ContextUtil.getProjectId())) {
                ContextUtil.setProjectId(projectId);
            }
            
            SystemGraphInstanceVo systemGraphInstanceVo =
                    this.systemGraphService.querySystemGraphDataNoProcess(systemGraphId);
            if (systemGraphInstanceVo == null) {
                return systemGraphAllObjectMap;
            }
            if (DtpRwdSystemClassCodeEnum.ACAT.getValue().equals(systemClassCode)) {
                this.wirteACAT2DSystemCache(systemGraphInstanceVo, systemGraphAllObjectMap, null);
            } else {
                this.wirteACCC2DSystemCache(systemGraphInstanceVo, systemGraphAllObjectMap, null);
            }
            
        } catch (Exception e) {
            log.error("系统图数据加载异常", e);
        }
        return systemGraphAllObjectMap;
    }
    
    public Map<String, Set<String>> getObjectIdsBySystemGraphClass(String projectId, String systemId,
                                                                   String systemClassCode, String graphTypeCode) {
        Map<String, Set<String>> systemGraphAllObjectMap = new HashMap<>();
        try {
            if (StrUtil.isBlank(ContextUtil.getProjectId())) {
                ContextUtil.setProjectId(projectId);
            }
            // 1.获取系统图的图ID
            List<SystemGraph> systemGraphs = this.systemGraphService.getNoDataPoBySystemObjectIdAndClassCode(systemId
                    , systemClassCode, graphTypeCode);
            if (CollectionUtil.isEmpty(systemGraphs)) {
                return systemGraphAllObjectMap;
            }
            // 2.根据图ID，获取对应的系统图
            for (SystemGraph systemGraph : systemGraphs) {
                SystemGraphInstanceVo systemGraphInstanceVo =
                        this.systemGraphService.querySystemGraphDataNoProcess(systemGraph.getId());
                if (systemGraphInstanceVo == null) {
                    continue;
                }
                if (DtpRwdSystemClassCodeEnum.ACAT.getValue().equals(systemClassCode)) {
                    this.wirteACAT2DSystemCache(systemGraphInstanceVo, systemGraphAllObjectMap, null);
                } else {
                    this.wirteACCC2DSystemCache(systemGraphInstanceVo, systemGraphAllObjectMap, null);
                }
            }
        } catch (Exception e) {
            log.error("系统图数据加载异常", e);
        }
        return systemGraphAllObjectMap;
    }

    /**
     * 2D系统图数据缓存写入--冷站、冷热站
     *
     * @param instanceObject
     * @param systemGraphAllObjectMap
     * @param objectIds
     */
    private void wirteACCC2DSystemCache(SystemGraphInstanceVo instanceObject, Map<String, Set<String>> systemGraphAllObjectMap, Set<String> objectIds) {
        if (instanceObject == null) {
            return;
        }
        // 所有的设备详情
        List<EquipmentNodeVo> equipmentNodeVos = instanceObject.getEquipmentNodeVos();
        if (CollectionUtil.isNotEmpty(equipmentNodeVos)) {
            for (EquipmentNodeVo equipmentNode : equipmentNodeVos) {
                JSONObject dataObject = equipmentNode.getDataObject();
                if (dataObject != null) {
                    String objectId = dataObject.getString("id");
                    String classCode = dataObject.getString("classCode");
                    if (StrUtil.isNotBlank(objectId)) {
                        if (systemGraphAllObjectMap != null) {
                            systemGraphAllObjectMap.computeIfAbsent(classCode, v -> Sets.newHashSet()).add(objectId);
                        }
                        if (objectIds != null) {
                            objectIds.add(objectId);
                        }
                    }
                }
            }
        }

        // 所有的控制单元组信息
        List<GraphGroupVo> groupVos = instanceObject.getGroupVos();
        if (CollectionUtil.isNotEmpty(groupVos)) {
            for (GraphGroupVo groupVo : groupVos) {
                JSONObject dataObject = groupVo.getDataObject();
                if (dataObject != null) {
                    String objectId = dataObject.getString("id");
                    String classCode = dataObject.getString("classCode");
                    if (StrUtil.isNotBlank(objectId)) {
                        if (systemGraphAllObjectMap != null) {
                            systemGraphAllObjectMap.computeIfAbsent(classCode, v -> Sets.newHashSet()).add(objectId);
                        }
                        if (objectIds != null) {
                            objectIds.add(objectId);
                        }
                    }
                }
            }
        }
    }

    /**
     * 2D系统图数据缓存写入--末端
     *
     * @param instanceObject
     * @param systemGraphAllObjectMap
     * @param objectIds
     */
    private void wirteACAT2DSystemCache(SystemGraphInstanceVo instanceObject, Map<String, Set<String>> systemGraphAllObjectMap, Set<String> objectIds) {
        if (instanceObject == null) {
            return;
        }

        // 所有的设备详情
        List<EquipmentNodeVo> equipmentNodeVos = instanceObject.getEquipmentNodeVos();
        if (CollectionUtil.isNotEmpty(equipmentNodeVos)) {
            for (EquipmentNodeVo equipmentNode : equipmentNodeVos) {
                JSONObject dataObject = equipmentNode.getDataObject();
                JSONArray items = dataObject == null ? null : dataObject.getJSONArray("items");
                if (items == null) {
                    continue;
                }
                for (int k = 0; k < items.size(); k++) {
                    JSONObject objectInfo = items.getJSONObject(k);
                    String objectId = objectInfo.getString("id");
                    String classCode = objectInfo.getString("classCode");
                    if (StrUtil.isNotBlank(objectId)) {
                        if (systemGraphAllObjectMap != null) {
                            systemGraphAllObjectMap.computeIfAbsent(classCode, v -> Sets.newHashSet()).add(objectId);
                        }
                        if (objectIds != null) {
                            objectIds.add(objectId);
                        }
                    }
                }
            }
        }

        // 所有的控制单元组信息
        List<GraphGroupVo> groupVos = instanceObject.getGroupVos();
        if (CollectionUtil.isNotEmpty(groupVos)) {
            for (GraphGroupVo groupVo : groupVos) {
                JSONObject dataObject = groupVo.getDataObject();
                if (dataObject != null) {
                    String objectId = dataObject.getString("id");
                    String classCode = dataObject.getString("classCode");
                    if (StrUtil.isNotBlank(objectId)) {
                        if (systemGraphAllObjectMap != null) {
                            systemGraphAllObjectMap.computeIfAbsent(classCode, v -> Sets.newHashSet()).add(objectId);
                        }
                        if (objectIds != null) {
                            objectIds.add(objectId);
                        }
                    }
                }
            }
        }
    }

}
