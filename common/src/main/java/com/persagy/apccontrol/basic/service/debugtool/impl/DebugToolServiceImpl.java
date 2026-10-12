package com.persagy.apccontrol.basic.service.debugtool.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.executor.log.LogComponent;
import com.persagy.apccontrol.basic.model.base.YesNoEnum;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdFractionalPrecisionRuleDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdQueryPointListDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.model.param.debugtool.DebugToolObjectByClassCodeQueryParam;
import com.persagy.apccontrol.basic.model.param.debugtool.DebugToolObjectInfoCodesQueryParam;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolInfoCodeConfigStatusVO;
import com.persagy.apccontrol.basic.model.vo.debugtool.DebugToolTemplateTypeAndClassCodeVO;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.infocodeconfig.param.InfoCodeTemplateTypeQueryDaoParam;
import com.persagy.apccontrol.basic.service.debugtool.DebugToolService;
import com.persagy.apccontrol.basic.structure.debugTool.converter.DebugToolConverter;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.InfoCodeGateway;
import com.persagy.apccontrol.basic.structure.infocodeconfig.gateway.param.InfoCodeInitByObjectParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeAggregate;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeConfigModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.validate.utility.executor.MapExecutor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 调试工具服务实现
 *
 * @author JiYinchuan
 * @since 7.250102.0
 */
@Service
@Validated
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DebugToolServiceImpl implements DebugToolService, LogComponent {

    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final DtpRwdClientService dtpRwdClientService;
    private final InfoCodeGateway infoCodeGateway;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final DebugToolConverter debugToolConverter;

    @Override
    public Collection<DebugToolTemplateTypeAndClassCodeVO> listTemplateTypeAndClassCodes() {
        List<InfoCodeTemplateTypeModel> templateTypeModels = this.infoCodeGateway.listTemplateTypes(
                new InfoCodeTemplateTypeQueryDaoParam());
        if (CollectionUtils.isEmpty(templateTypeModels)) {
            return Lists.newArrayList();
        }
        Map<RunningMonitorTypeEnum, List<InfoCodeTemplateTypeModel>> runningMonitorTypeAndModels =
                templateTypeModels.stream().collect(Collectors.groupingBy(
                        InfoCodeTemplateTypeModel::getRunningMonitorType));
        Set<String> classCodes = new HashSet<>();
        for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
            RunningMonitorTypeEnum runningMonitorType = templateTypeModel.getRunningMonitorType();
            String classCode = templateTypeModel.getClassCode();

            classCodes.add(classCode);
            MapExecutor.dynamicPut(runningMonitorTypeAndModels, runningMonitorType, templateTypeModel, ArrayList::new);
        }
        // 查询设备类编码和名称
        Map<String, String> classCodeAndName = this.dtpKnowledgeClientService
                .mapClassCodeAndName(false, false, classCodes);

        Set<RunningMonitorTypeEnum> associatedDebugToolRunningMonitorTypes =
                this.listAssociatedDebugToolRunningMonitorTypes();
        List<DebugToolTemplateTypeAndClassCodeVO> result = new ArrayList<>();
        for (RunningMonitorTypeEnum runningMonitorType : RunningMonitorTypeEnum.values()) {
            if (associatedDebugToolRunningMonitorTypes.contains(runningMonitorType)) {
                continue;
            }
            List<InfoCodeTemplateTypeModel> models = runningMonitorTypeAndModels.get(runningMonitorType);
            if (CollectionUtils.isEmpty(models)) {
                continue;
            }
            // 将模版类型根据设备类分组添加
            Map<String, DebugToolTemplateTypeAndClassCodeVO.ClassCode> classCodeAndVOs = new LinkedHashMap<>();
            for (InfoCodeTemplateTypeModel model : models) {
                Long templateTypeId = model.getTemplateTypeId();
                String classCode = model.getClassCode();
                DtpRwdObjectTypeEnum objectType = model.getObjectType();

                DebugToolTemplateTypeAndClassCodeVO.ClassCode classCodeVO = classCodeAndVOs.get(classCode);
                if (Objects.isNull(classCodeVO)) {
                    classCodeVO = new DebugToolTemplateTypeAndClassCodeVO.ClassCode();
                    classCodeVO.setObjectType(objectType);
                    classCodeVO.setObjectTypeName(objectType.getReasonPhrase());
                    classCodeVO.setClassCode(classCode);
                    classCodeVO.setClassCodeName(classCodeAndName.get(classCode));
                    classCodeVO.setRelTemplateIds(Sets.newHashSet());
                }
                Set<Long> relTemplateIds = classCodeVO.getRelTemplateIds();
                relTemplateIds.add(templateTypeId);
                classCodeVO.setRelTemplateIds(relTemplateIds);
                classCodeAndVOs.put(classCode, classCodeVO);
            }
            DebugToolTemplateTypeAndClassCodeVO templateTypeAndClassCodeVO = new DebugToolTemplateTypeAndClassCodeVO();
            templateTypeAndClassCodeVO.setRunningMonitorType(runningMonitorType);
            templateTypeAndClassCodeVO.setRunningMonitorTypeName(runningMonitorType.getReasonPhrase());
            templateTypeAndClassCodeVO.setClassCodes(new ArrayList<>(classCodeAndVOs.values()));
            result.add(templateTypeAndClassCodeVO);
        }
        return result;
    }

    /**
     * 查询所有被关联的调试工具运行监测类型
     *
     * <p>调试工具类型只作为后端合并信息点配置的承载类型，不直接暴露给模板类型列表接口，避免前端重复展示。</p>
     *
     * @return 被关联的调试工具运行监测类型
     * @since v7.260602.0
     */
    private @NonNull Set<RunningMonitorTypeEnum> listAssociatedDebugToolRunningMonitorTypes() {
        Set<RunningMonitorTypeEnum> result = new LinkedHashSet<>();
        for (RunningMonitorTypeEnum runningMonitorType : RunningMonitorTypeEnum.values()) {
            RunningMonitorTypeEnum debugToolRunningMonitorType = runningMonitorType.toDebugToolRunningMonitorType();
            if (Objects.nonNull(debugToolRunningMonitorType)) {
                result.add(debugToolRunningMonitorType);
            }
        }
        return result;
    }

    @Override
    public Collection<DtpRwdObjectDTO> listObjectsByClassCode(@NonNull DebugToolObjectByClassCodeQueryParam param) {
        String classCode = param.getClassCode();
        return this.dtpRwdClientService.listObject(new DtpRwdObjectQueryParam().setClassCode(classCode)
                .addWithColumn(DtpRwdObjectDTO.Fields.id, DtpRwdObjectDTO.Fields.localName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DebugToolInfoCodeConfigStatusVO> queryObjectInfoCodes(DebugToolObjectInfoCodesQueryParam queryParam) {
        String objectId = queryParam.getObjectId();
        Set<String> objectIds = Collections.singleton(objectId);
        DtpRwdObjectDTO object = this.dtpRwdClientService.getObject(new DtpRwdObjectQueryParam()
                .setId(objectId)
                .addWithColumn(DtpRwdObjectDTO.Fields.classCode));
        if (Objects.isNull(object)) {
            return Lists.newArrayList();
        }
        String classCode = object.getClassCode();
        List<InfoCodeTemplateTypeModel> templateTypeModels =
                this.listTemplateTypeModels(queryParam.getTemplateTypeIds());
        RunningMonitorTypeEnum debugToolRunningMonitorType =
                this.getDebugToolRunningMonitorType(templateTypeModels);
        Set<Long> templateTypeIds = this.listMergedTemplateTypeIds(
                queryParam.getTemplateTypeIds(), templateTypeModels,
                debugToolRunningMonitorType, classCode);

        InfoCodeInitByObjectParam param = new InfoCodeInitByObjectParam();
        param.setObjects(Collections.singletonList(object));
        param.setNotFilterIotInfoCodeRule(true);
        param.setTemplateTypeIds(templateTypeIds);
        param.setInfoCodeTypes(Lists.newArrayList(InfoCodeTypeEnum.IOT_RUNNING, InfoCodeTypeEnum.IOT_SETTING));
        List<InfoCodeAggregate> aggregates = this.infoCodeGateway.of(param);
        if (CollectionUtils.isEmpty(aggregates)) {
            return Lists.newArrayList();
        }
        Map<String, String> infoCodeAndNameMap = this.mapInfoCodeAndName(aggregates, classCode);
        Map<String, String> infoCodeAndShowStyleMap =
                this.mapInfoCodeAndShowStyle(aggregates, debugToolRunningMonitorType);
        Set<String> infoCodes = infoCodeAndNameMap.keySet();
        // 云端持久化边缘端孪生规则
        Set<String> existsRuleInfoCodes = this.getInfoCodeRules(classCode, objectId, infoCodes);
        // iot数值
        List<InfoCodeValueVO> infoCodeValues = this.abstractIotDataCacheServer.listInfoCodeValue(
                ContextUtil.getProjectId(), null, objectIds, infoCodes);

        // 查询信息点分精度规则，仅限云端有边缘端直接为空 <信息点,精度>
        Map<String, String> infoCodeAndPrecisionRuleMap =
                this.mapInfoCodeAndPrecisionRule(objectIds, infoCodes);

        // 查询对象信息点绑点规则
        List<String> pointList = this.listInfoCodePoint(objectIds, infoCodes);

        return this.dataAssembly(infoCodeAndNameMap,
                infoCodeAndShowStyleMap,
                existsRuleInfoCodes, infoCodeValues,
                infoCodeAndPrecisionRuleMap, pointList);
    }

    /**
     * 查询模板类型
     *
     * @param templateTypeIds 模板类型ID
     * @return 模板类型
     * @since v7.260602.0
     */
    private @NonNull List<InfoCodeTemplateTypeModel> listTemplateTypeModels(@NonNull Set<Long> templateTypeIds) {
        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryDaoParam.setTemplateTypeIds(templateTypeIds);
        return this.infoCodeGateway.listTemplateTypes(queryDaoParam);
    }

    /**
     * 获取本次查询关联的调试工具运行监测类型
     *
     * <p>调试工具查询一次只会关联一个调试工具运行监测类型，后续新增关联关系时只需要扩展枚举映射。</p>
     *
     * @param templateTypeModels 本次查询的原始模板类型
     * @return 关联的调试工具运行监测类型，不存在关联时返回空
     * @since v7.260602.0
     */
    private @Nullable RunningMonitorTypeEnum getDebugToolRunningMonitorType(
            @NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels) {
        if (CollectionUtils.isEmpty(templateTypeModels)) {
            return null;
        }
        for (InfoCodeTemplateTypeModel templateTypeModel : templateTypeModels) {
            RunningMonitorTypeEnum debugToolRunningMonitorType =
                    templateTypeModel.getRunningMonitorType().toDebugToolRunningMonitorType();
            if (Objects.nonNull(debugToolRunningMonitorType)) {
                return debugToolRunningMonitorType;
            }
        }
        return null;
    }

    /**
     * 合并原模板类型和关联调试工具模板类型ID
     *
     * <p>调试工具模板固定查询默认模板类型，并按原模板的对象类和对象类型约束，避免把其他场景的配置合并进来。</p>
     *
     * @param templateTypeIds             原模板类型ID
     * @param templateTypeModels          原模板类型
     * @param debugToolRunningMonitorType 关联调试工具运行监测类型
     * @param classCode                   对象类编码
     * @return 合并后的模板类型ID
     * @since v7.260602.0
     */
    private @NonNull Set<Long> listMergedTemplateTypeIds(
            @NonNull Set<Long> templateTypeIds,
            @NonNull Collection<InfoCodeTemplateTypeModel> templateTypeModels,
            @Nullable RunningMonitorTypeEnum debugToolRunningMonitorType,
            @NonNull String classCode) {
        Set<Long> result = new LinkedHashSet<>(templateTypeIds);
        if (CollectionUtils.isEmpty(templateTypeModels) || Objects.isNull(debugToolRunningMonitorType)) {
            return result;
        }
        List<InfoCodeTemplateTypeModel> associatedTemplateTypeModels = templateTypeModels.stream()
                .filter(item -> Objects.equals(debugToolRunningMonitorType,
                        item.getRunningMonitorType().toDebugToolRunningMonitorType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(associatedTemplateTypeModels)) {
            return result;
        }
        Set<DtpRwdObjectTypeEnum> objectTypes = associatedTemplateTypeModels.stream()
                .map(InfoCodeTemplateTypeModel::getObjectType)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        InfoCodeTemplateTypeQueryDaoParam queryDaoParam = new InfoCodeTemplateTypeQueryDaoParam();
        queryDaoParam.setRunningMonitorType(debugToolRunningMonitorType);
        queryDaoParam.setTemplateType(InfoCodeTemplateTypeEnum.DEFAULT);
        queryDaoParam.setObjectTypes(objectTypes);
        queryDaoParam.setClassCode(classCode);
        List<InfoCodeTemplateTypeModel> debugToolTemplateTypeModels =
                this.infoCodeGateway.listTemplateTypes(queryDaoParam);
        if (CollectionUtils.isEmpty(debugToolTemplateTypeModels)) {
            return result;
        }
        debugToolTemplateTypeModels.stream()
                .map(InfoCodeTemplateTypeModel::getTemplateTypeId)
                .forEach(result::add);
        return result;
    }

    /**
     * 查询信息点名称，优先使用数据字典名称
     *
     * @param aggregates 信息点聚合根
     * @param classCode  对象类编码
     * @return 信息点编码和名称
     * @since v7.260602.0
     */
    private @NonNull Map<String, String> mapInfoCodeAndName(
            @NonNull List<InfoCodeAggregate> aggregates,
            @NonNull String classCode) {
        Map<String, String> knowledgeInfoCodeAndName =
                this.dtpKnowledgeClientService.mapInfoCodeAndName(classCode);
        Map<String, String> result = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeConfigModel configModel = aggregate.getConfigModel();
            String infoCode = configModel.getInfoCode();
            String infoCodeName = knowledgeInfoCodeAndName.get(infoCode);
            if (StringUtils.isBlank(infoCodeName)) {
                infoCodeName = configModel.getInfoCodeName();
            }
            if (!result.containsKey(infoCode) || StringUtils.isBlank(result.get(infoCode))) {
                result.put(infoCode, infoCodeName);
            }
        }
        return result;
    }

    /**
     * 合并信息点展示样式
     *
     * <p>原运行监测类型和调试工具类型都配置展示样式时，会先做结构化合并；重复项以调试工具类型为准。</p>
     *
     * @param aggregates                  信息点聚合根
     * @param debugToolRunningMonitorType 关联调试工具运行监测类型
     * @return 信息点编码和展示样式
     * @since v7.260602.0
     */
    private @NonNull Map<String, String> mapInfoCodeAndShowStyle(
            @NonNull List<InfoCodeAggregate> aggregates,
            @Nullable RunningMonitorTypeEnum debugToolRunningMonitorType) {
        Map<String, String> originalShowStyles = new LinkedHashMap<>();
        Map<String, String> debugToolShowStyles = new LinkedHashMap<>();
        for (InfoCodeAggregate aggregate : aggregates) {
            InfoCodeConfigModel configModel = aggregate.getConfigModel();
            String showStyle = configModel.getShowStyle();
            if (StringUtils.isBlank(showStyle)) {
                continue;
            }
            String infoCode = configModel.getInfoCode();
            boolean debugToolType = Objects.equals(debugToolRunningMonitorType,
                    aggregate.getTemplateTypeModel().getRunningMonitorType());
            Map<String, String> target = debugToolType ? debugToolShowStyles : originalShowStyles;
            target.merge(infoCode, showStyle, this::mergeShowStyle);
        }

        Map<String, String> result = new LinkedHashMap<>(originalShowStyles);
        debugToolShowStyles.forEach((infoCode, showStyle) ->
                result.put(infoCode, this.mergeShowStyle(result.get(infoCode), showStyle)));
        return result;
    }

    /**
     * 合并两个展示样式配置
     *
     * <p>JSON对象按字段合并，JSON数组按元素关键字段合并；无法识别为同类JSON时，使用调试工具配置兜底。</p>
     *
     * @param originalShowStyle  原运行监测类型展示样式
     * @param debugToolShowStyle 调试工具类型展示样式
     * @return 合并后的展示样式
     * @since v7.260602.0
     */
    private String mergeShowStyle(@Nullable String originalShowStyle, @Nullable String debugToolShowStyle) {
        if (StringUtils.isBlank(debugToolShowStyle)) {
            return originalShowStyle;
        }
        if (StringUtils.isBlank(originalShowStyle)) {
            return debugToolShowStyle;
        }
        JsonNode originalNode = this.parseShowStyleJsonNode(originalShowStyle);
        JsonNode debugToolNode = this.parseShowStyleJsonNode(debugToolShowStyle);
        if (Objects.isNull(originalNode) || Objects.isNull(debugToolNode)) {
            return debugToolShowStyle;
        }
        if (originalNode.isObject() && debugToolNode.isObject()) {
            ObjectNode mergedNode = originalNode.deepCopy();
            this.mergeShowStyleObjectNode(mergedNode, (ObjectNode) debugToolNode);
            return this.writeShowStyleJsonNode(mergedNode, debugToolShowStyle);
        }
        if (originalNode.isArray() && debugToolNode.isArray()) {
            return this.writeShowStyleJsonNode(
                    this.mergeShowStyleArrayNode((ArrayNode) originalNode, (ArrayNode) debugToolNode),
                    debugToolShowStyle);
        }
        return debugToolShowStyle;
    }

    /**
     * 解析展示样式JSON
     *
     * @param showStyle 展示样式
     * @return 展示样式JSON节点，非JSON时返回空
     * @since v7.260602.0
     */
    private @Nullable JsonNode parseShowStyleJsonNode(String showStyle) {
        try {
            return ObjectMapperUtils.getObjectMapper().readTree(showStyle);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 合并展示样式JSON对象
     *
     * @param originalNode  原运行监测类型展示样式节点
     * @param debugToolNode 调试工具类型展示样式节点
     * @since v7.260602.0
     */
    private void mergeShowStyleObjectNode(@NonNull ObjectNode originalNode, @NonNull ObjectNode debugToolNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = debugToolNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode debugToolValue = field.getValue();
            JsonNode originalValue = originalNode.get(fieldName);
            if (Objects.nonNull(originalValue) && originalValue.isObject() && debugToolValue.isObject()) {
                ObjectNode childNode = originalValue.deepCopy();
                this.mergeShowStyleObjectNode(childNode, (ObjectNode) debugToolValue);
                originalNode.set(fieldName, childNode);
                continue;
            }
            originalNode.set(fieldName, debugToolValue);
        }
    }

    /**
     * 合并展示样式JSON数组
     *
     * <p>数组元素优先使用 infoCode、code、key、name 作为重复判断字段；没有稳定字段时使用元素JSON作为唯一键。</p>
     *
     * @param originalNode  原运行监测类型展示样式节点
     * @param debugToolNode 调试工具类型展示样式节点
     * @return 合并后的数组节点
     * @since v7.260602.0
     */
    private @NonNull ArrayNode mergeShowStyleArrayNode(@NonNull ArrayNode originalNode,
                                                       @NonNull ArrayNode debugToolNode) {
        Map<String, JsonNode> keyAndNode = new LinkedHashMap<>();
        originalNode.forEach(item -> keyAndNode.put(this.getShowStyleArrayItemKey(item), item));
        debugToolNode.forEach(item -> keyAndNode.put(this.getShowStyleArrayItemKey(item), item));

        ArrayNode result = ObjectMapperUtils.getObjectMapper().createArrayNode();
        keyAndNode.values().forEach(result::add);
        return result;
    }

    /**
     * 获取展示样式数组元素的合并键
     *
     * @param item 展示样式数组元素
     * @return 合并键
     * @since v7.260602.0
     */
    private @NonNull String getShowStyleArrayItemKey(@NonNull JsonNode item) {
        if (item.isObject()) {
            for (String fieldName : Arrays.asList("infoCode", "code", "key", "name")) {
                JsonNode fieldNode = item.get(fieldName);
                if (Objects.nonNull(fieldNode) && !fieldNode.isNull()) {
                    return fieldName + ":" + fieldNode.asText();
                }
            }
        }
        return item.toString();
    }

    /**
     * 将展示样式JSON节点写回字符串
     *
     * @param jsonNode     展示样式JSON节点
     * @param defaultValue 序列化失败时返回的默认值
     * @return 展示样式字符串
     * @since v7.260602.0
     */
    private String writeShowStyleJsonNode(@NonNull JsonNode jsonNode, @Nullable String defaultValue) {
        String showStyle = ObjectMapperUtils.writeValueAsStringFailSafe(jsonNode);
        return StringUtils.defaultIfBlank(showStyle, defaultValue);
    }


    /**
     * 数据组装
     *
     * @param infoCodeAndShowStyleMap     信息点展示样式
     * @param existsRuleInfoCodes         云端持久化边缘端孪生规则
     * @param infoCodeValues              iot数值
     * @param infoCodeAndPrecisionRuleMap 查询信息点分精度规则
     * @param pointList                   查询对象信息点绑点规则
     * @return 信息点配置的bdtp交付信息状态
     */
    private List<DebugToolInfoCodeConfigStatusVO> dataAssembly(
            Map<String, String> infoCodeAndNameMap,
            Map<String, String> infoCodeAndShowStyleMap,
            Set<String> existsRuleInfoCodes,
            List<InfoCodeValueVO> infoCodeValues,
            Map<String, String> infoCodeAndPrecisionRuleMap,
            List<String> pointList) {
        List<DebugToolInfoCodeConfigStatusVO> resultInfoCodeConfigStatusList = new ArrayList<>();
        Map<String, InfoCodeValueVO> groupByInfoCodeMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(infoCodeValues)) {
            groupByInfoCodeMap = InfoCodeValueVO.groupByInfoCodeAndOneInfoCodeValueMap(infoCodeValues);
        }
        Map<String, InfoCodeValueVO> finalGroupByInfoCodeMap = groupByInfoCodeMap;
        infoCodeAndNameMap.forEach((infoCode, infoCodeName) -> {
            DebugToolInfoCodeConfigStatusVO debugToolInfoCodeConfigStatusVO = new DebugToolInfoCodeConfigStatusVO();
            if (finalGroupByInfoCodeMap.containsKey(infoCode)) {
                InfoCodeValueVO infoCodeValueVO = finalGroupByInfoCodeMap.get(infoCode);
                debugToolInfoCodeConfigStatusVO = this.debugToolConverter.msgDtoToVo(infoCodeValueVO);
            }
            debugToolInfoCodeConfigStatusVO.setInfoCode(infoCode);
            debugToolInfoCodeConfigStatusVO.setInfoCodeName(infoCodeName);
            debugToolInfoCodeConfigStatusVO.setBindingPointStatus(pointList.contains(infoCode) ? YesNoEnum.YES : YesNoEnum.NO);
            debugToolInfoCodeConfigStatusVO.setInstantiationRuleStatus(existsRuleInfoCodes.contains(infoCode) ? YesNoEnum.YES : YesNoEnum.NO);
            debugToolInfoCodeConfigStatusVO.setMinuteDisplay(infoCodeAndPrecisionRuleMap.getOrDefault(infoCode, null));
            debugToolInfoCodeConfigStatusVO.setShowStyle(infoCodeAndShowStyleMap.get(infoCode));
            resultInfoCodeConfigStatusList.add(debugToolInfoCodeConfigStatusVO);
        });
        return resultInfoCodeConfigStatusList;
    }

    /**
     * 查信息点精度仅云端有
     *
     * @param objectIds 对象id
     * @param infoCodes 信息点
     * @return 已绑点的信息点
     */
    private List<String> listInfoCodePoint(Set<String> objectIds, Set<String> infoCodes) {
        // iot数值
        List<DtpRwdQueryPointListDTO> dtpRwdQueryPointLists =
                this.dtpRwdClientService.queryPointList(objectIds, infoCodes);
        if (CollectionUtils.isEmpty(dtpRwdQueryPointLists)) {
            return Lists.newArrayList();
        }
        return dtpRwdQueryPointLists.stream().map(DtpRwdQueryPointListDTO::getInfoCode).collect(Collectors.toList());
    }

    /**
     * 查信息点精度仅云端有
     *
     * @param objectIds 对象id
     * @param infoCodes 信息点
     * @return <信息点,精度>
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> mapInfoCodeAndPrecisionRule(Set<String> objectIds, Set<String> infoCodes) {
        // iot数值
        List<DtpRwdFractionalPrecisionRuleDTO> infoPointPeroids =
                this.abstractIotDataCacheServer.queryInfoPointPeroid(objectIds, infoCodes);
        if (CollectionUtils.isEmpty(infoPointPeroids)) {
            return new HashMap<>();
        }
        return infoPointPeroids.stream().collect(
                Collectors.toMap(DtpRwdFractionalPrecisionRuleDTO::getInfoCode, DtpRwdFractionalPrecisionRuleDTO::getMinuteDisplay));
    }

    /**
     * 云端查持久化规则，边缘端查孪生规则
     *
     * @param classCode 对象类
     * @param objectId  对象id
     * @param infoCodes 信息点
     * @return 有规则的信息点
     */
    @SuppressWarnings("unchecked")
    private Set<String> getInfoCodeRules(String classCode, String objectId, Collection<String> infoCodes) {
        Map<String, Set<String>> classCodeAndObjectIds = Collections.singletonMap(classCode, Collections.singleton(objectId));
        Map<String, Set<String>> objectIdAndExistsRuleInfoCodes =
                this.abstractIotDataCacheServer.mapObjectIdAndExistsRuleInfoCodes(classCodeAndObjectIds, infoCodes);
        // 双重判断，边缘端返回是按对象类查询，当前对象是可以不存在的
        if (MapUtils.isNotEmpty(objectIdAndExistsRuleInfoCodes)
                && objectIdAndExistsRuleInfoCodes.containsKey(objectId)) {
            return objectIdAndExistsRuleInfoCodes.get(objectId);
        }
        return new HashSet<>();
    }

    @Override
    public String logTag() {
        return "调试工具";
    }
}
