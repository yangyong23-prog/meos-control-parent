package com.persagy.apccontrol.basic.service.systemgraph.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.abstracts.AbstractGraphLegendServer;
import com.persagy.apccontrol.basic.abstracts.AbstractIotDataCacheServer;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdEquipmentDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.*;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.*;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.systemgraph.*;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdRelationTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumGraphType;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumSystemGraphStatus;
import com.persagy.apccontrol.basic.model.vo.InfoCodeValueVO;
import com.persagy.apccontrol.basic.model.vo.knowledge.DtpKnowledgeInfoCdeDataSourceVO;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.MenuConfigVo;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRelationalGraphClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.*;
import com.persagy.apccontrol.basic.structure.system.SystemGraphConverter;
import com.persagy.apccontrol.basic.utils.CompressUtil;
import com.persagy.apccontrol.basic.utils.IotDataUtil;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统图编辑器服务impl
 *
 * @author MAF
 * @date 2024/06/26
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphEditorServiceImpl implements SystemGraphEditorService {

    private final SystemGraphDataService systemGraphDataService;
    private final SystemGraphService systemGraphService;
    private final SystemGraphDetailService systemGraphDetailService;
    private final SystemGraphDuplicateService systemGraphDuplicateService;
    private final SystemGraphTemplateStatusService systemGraphTemplateStatusService;
    private final SystemGraphGenerateParameterService systemGraphGenerateParameterService;
    private final DtpRwdClientService dtpRwdClientService;
    private final DtpRelationalGraphClientService dtpRelationalGraphClientService;
    private final LocalRedisCacheHandler localRedisCacheHandler;
    private final AbstractIotDataCacheServer abstractIotDataCacheServer;
    private final SystemGraphConverter systemGraphConverter;
    private final CommonConfigService commonConfigService;
    private final AbstractGraphLegendServer abstractGraphLegendServer;


    private final DtpKnowledgeClientService dtpKnowledgeClientService;

    /**
     * 列表系统模板
     *
     * @return {@link List }<{@link TemplateTreeVO }>
     */
    @Override
    public List<TemplateTreeVO> listSystemTemplate() {
        List<GraphConfigVo> graphConfig = this.commonConfigService.getGraphConfig();
        List<MenuConfigVo> menuConfigs = this.commonConfigService.getMenuConfig();
        if (CollectionUtils.isEmpty(graphConfig) || CollectionUtils.isEmpty(menuConfigs)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "配置缺失，请检查", "配置缺失，请检查");
        }
        Map<String, MenuConfigVo> menuConfigMap = menuConfigs.stream().collect(Collectors.toMap(MenuConfigVo::getImageOrProductName, Function.identity(), (v1, v2) -> v1));
        //获取基准对象名称和CodeMap
        Map<String, String> codeNameMap = graphConfig.stream().filter(e -> StringUtils.isNotEmpty(e.getDiagramBaseObjectCode())).collect(Collectors.toMap
                (GraphConfigVo::getDiagramBaseObjectCode, GraphConfigVo::getDiagramBaseObjectName, (k, v) -> v));
        //按照基准对象code分组
        Map<String, List<GraphConfigVo>> codeObjectMap = graphConfig.stream().filter(e -> StringUtils.isNotEmpty(e.getDiagramBaseObjectCode()))
                .collect(Collectors.groupingBy(GraphConfigVo::getDiagramBaseObjectCode));
        //查询基准对象
        Set<String> classCodeSet = codeNameMap.keySet();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setClassCode(classCodeSet);
        List<DtpRwdObjectDTO> dtpRwdObjectList = dtpRwdClientService.listObject(param);
        Map<String, List<DtpRwdObjectDTO>> classCodeRwdObjectMap = dtpRwdObjectList.stream().collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode));
        //查询已上架系统图
        GraphTemplateSelectFilterDto dto = new GraphTemplateSelectFilterDto();
        dto.setTemplateStatus("已上架");
        List<GraphTemplateSelectDto> templateSelectDtoList = dtpRelationalGraphClientService.graphTemplateSelect(dto);
        if (CollectionUtils.isEmpty(templateSelectDtoList)) {
            return Collections.emptyList();
        }
        GraphTemplatesDTO graphTemplatesDTO = new GraphTemplatesDTO();
        Map<String, Set<String>> classCodeToGraphTypeMap = templateSelectDtoList.stream().filter(e -> classCodeSet.contains(e.getReferenceObjectTypeCode()))
                .collect(Collectors.groupingBy(GraphTemplateSelectDto::getReferenceObjectTypeCode,
                        Collectors.mapping(GraphTemplateSelectDto::getGraphTypeCode, Collectors.toSet())));
        List<TemplateTreeVO> result = new ArrayList<>();
        List<GraphTemplateFilterDto> items = new ArrayList<>();
        for (Map.Entry<String, String> entry : codeNameMap.entrySet()) {
            String key = entry.getKey();
            List<GraphConfigVo> graphConfigVos = codeObjectMap.get(key);
            if (CollectionUtils.isEmpty(graphConfigVos)) {
                continue;
            }
            for (GraphConfigVo graphConfigVo : graphConfigVos) {
                List<DtpRwdObjectDTO> dtpRwdObjectDTOS = classCodeRwdObjectMap.get(key);
                if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
                    continue;
                }
                for (DtpRwdObjectDTO obj : dtpRwdObjectDTOS) {
                    GraphTemplateFilterDto graphTemplateFilterDto = new GraphTemplateFilterDto();
                    graphTemplateFilterDto.setGraphTypeCode(graphConfigVo.getDiagramTypeOfTheDiagram());
                    graphTemplateFilterDto.setReferenceObjectId(obj.getId());
                    items.add(graphTemplateFilterDto);
                }
            }
        }
        graphTemplatesDTO.setItems(items);
        //查询所有模板
        GraphTemplateLegendSelectListDTO graphTemplateLegendSelectListDTO = dtpRelationalGraphClientService.getTemplates(graphTemplatesDTO);
        List<GraphTemplateLegendSelectDto> selectTemplateItems = graphTemplateLegendSelectListDTO.getItems();
        if (CollectionUtils.isEmpty(selectTemplateItems)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "获取关系图模版为空", "获取关系图模版为空");
        }
        //移除没有模版Id的记录
        selectTemplateItems.removeIf(e -> StringUtils.isBlank(e.getTemplateOriId()));
        Map<String, GraphTemplateLegendSelectDto> templateMap = selectTemplateItems.stream()
                .collect(Collectors.toMap(e -> e.getReferenceObjectId() + e.getGraphTypeCode(), Function.identity(), (k, v) -> v));
        //查询所有模板状态
        List<SystemGraphTemplateStatus> allTemplates = systemGraphTemplateStatusService.list();
        if (CollectionUtils.isEmpty(allTemplates)) {
            //如果为空，则初始化数据
            systemGraphTemplateStatusService.init(selectTemplateItems);
            allTemplates = systemGraphTemplateStatusService.list();
        }
        Map<String, SystemGraphTemplateStatus> templateStatusMap = allTemplates.stream().collect(Collectors.toMap(e -> e.getObjectId() + e.getGraphTypeCode(),
                Function.identity(), (v1, v2) -> v1));
        for (Map.Entry<String, String> entry : codeNameMap.entrySet()) {
            TemplateTreeVO templateTreeVO = buildTree(entry, codeObjectMap,
                    classCodeRwdObjectMap, classCodeToGraphTypeMap, graphConfig, templateMap, templateStatusMap, menuConfigMap);
            if (templateTreeVO != null) {
                result.add(templateTreeVO);
            }
        }
        updateFlag(result);
        return result;
    }

    private TemplateTreeVO buildTree(Map.Entry<String, String> entry, Map<String, List<GraphConfigVo>> codeObjectMap, Map<String,
                                             List<DtpRwdObjectDTO>> classCodeRwdObjectMap, Map<String, Set<String>> classCodeToGraphTypeMap,
                                     List<GraphConfigVo> graphConfig, Map<String, GraphTemplateLegendSelectDto> templateMap, Map<String, SystemGraphTemplateStatus> templateStatusMap,
                                     Map<String, MenuConfigVo> menuConfigMap) {
        TemplateTreeVO tree = new TemplateTreeVO();
        String key = entry.getKey();
        List<TemplateTreeVO> children = new ArrayList<>();
        List<GraphConfigVo> graphConfigVos = codeObjectMap.get(key);
        Set<String> codeSet = graphConfigVos.stream().map(GraphConfigVo::getDiagramTypeOfTheDiagram).collect(Collectors.toSet());
        for (String code : codeSet) {
            Set<String> graphTypeSet = classCodeToGraphTypeMap.getOrDefault(key, Collections.emptySet());
            if (!graphTypeSet.contains(code)) {
                continue;
            }
            TemplateTreeVO vo = new TemplateTreeVO();
            boolean flag = buildChildren(vo, classCodeRwdObjectMap.get(key), key, code,
                    graphConfig, templateMap, templateStatusMap, menuConfigMap);
            if (flag) {
                vo.setCode(code);
                vo.setName(code);
                children.add(vo);
            }
        }
        if (CollectionUtils.isNotEmpty(children)) {
            tree.setCode(key);
            tree.setName(entry.getValue());
            tree.setChildren(children);
            return tree;
        }
        return null;
    }

    private void updateFlag(List<TemplateTreeVO> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        result.forEach(e -> {
            if (CollectionUtils.isNotEmpty(e.getChildren())) {
                updateFlag(e.getChildren());
                e.setUpdateFlag(e.getChildren().stream().filter(f -> f.getUpdateFlag() != null).anyMatch(TemplateTreeVO::getUpdateFlag));
            }
        });
    }


    private boolean buildChildren(TemplateTreeVO tree, List<DtpRwdObjectDTO> dtpRwdObjectList, String classCode, String parentId,
                                  List<GraphConfigVo> graphConfig, Map<String, GraphTemplateLegendSelectDto> templateMap, Map<String, SystemGraphTemplateStatus> templateStatusMap,
                                  Map<String, MenuConfigVo> menuConfigMap) {
        if (CollectionUtils.isEmpty(dtpRwdObjectList)) {
            return false;
        }
        List<TemplateTreeVO> children = new ArrayList<>();
        for (DtpRwdObjectDTO obj : dtpRwdObjectList) {
            String id = obj.getId();
            String graphName = systemGraphService.getGraphName(classCode, parentId, graphConfig, id);
            String tempKey = id + parentId;
            GraphTemplateLegendSelectDto templateLegendSelectDto = templateMap.get(tempKey);
            if (Objects.isNull(templateLegendSelectDto) || StringUtils.isEmpty(graphName)) {
                continue;
            }
            String templateKey = id + templateLegendSelectDto.getGraphTypeCode();
            MenuConfigVo menuConfigVo = menuConfigMap.get(graphName);
            String templateName = obj.getLocalName();
            if (menuConfigVo != null && StringUtils.isBlank(menuConfigVo.getDynamicName()) && StringUtils.isNotBlank(menuConfigVo.getThreeLevelMenu())) {
                templateName = menuConfigVo.getThreeLevelMenu();
            }
            SystemGraphTemplateStatus ts = templateStatusMap.get(templateKey);
            if (ts == null) {
                ts = systemGraphTemplateStatusService.saveTemplateStatus(templateLegendSelectDto.getTemplateOriId(), id, templateName, templateLegendSelectDto.getGraphTypeCode(), templateLegendSelectDto.getSequenceNumberCurrent());
                templateStatusMap.put(templateKey, ts);
            } else if (StringUtils.isEmpty(ts.getName()) || !StringUtils.equals(ts.getName(), templateName)) {
                LambdaUpdateWrapper<SystemGraphTemplateStatus> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(SystemGraphTemplateStatus::getGraphTypeCode, ts.getGraphTypeCode())
                        .eq(SystemGraphTemplateStatus::getObjectId, ts.getObjectId())
                        .set(SystemGraphTemplateStatus::getName, templateName);
                systemGraphTemplateStatusService.update(updateWrapper);
                ts.setName(templateName);
            }
            TemplateTreeVO vo = new TemplateTreeVO();
            vo.setCode(id);
            vo.setName(StringUtils.isNotBlank(ts.getAliasName()) ? ts.getAliasName() : ts.getName());
            vo.setSystemGraphCode(parentId);
            vo.setParentId(parentId);
            vo.setGraphName(graphName);
            //如果表更新字段为true，或者模板不一致，或者sequenceNumberCurrent不一致，都需要表示为更新
            vo.setUpdateFlag(ts.getUpdatedStatus() || !StringUtils.equals(templateLegendSelectDto.getTemplateOriId(), ts.getTemplateId()) || !Objects.equals(ts.getSequenceNumberCurrent(), templateLegendSelectDto.getSequenceNumberCurrent()));

            //如果updateFlag为true,则更新表
            if (vo.getUpdateFlag()) {
                systemGraphTemplateStatusService.updateTemplateStatus(templateLegendSelectDto.getTemplateOriId(), ts.getObjectId(), true, ts.getGraphTypeCode(), templateLegendSelectDto.getSequenceNumberCurrent());
            }
            children.add(vo);
        }
        tree.setChildren(children);
        return CollectionUtils.isNotEmpty(children);

    }


    /**
     * 初始化系统图
     *
     * @param dto   到
     * @param jobId 任务id
     * @return {@link List }<{@link SystemGraphDuplicateVO }>
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ApplicationException.class)
    public List<SystemGraphDuplicateVO> initSystemGraph(RequestSystemGraphDTO dto, String jobId) {
        RequestSystemGraphDTO graphGenerateLoadDto = new RequestSystemGraphDTO();
        //设置基准对象id
        String systemObjectId = dto.getReferenceObjectId();
//        String templateOriId = dto.getTemplateOriId();
        String graphTypeCode = dto.getGraphTypeCode();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setId(systemObjectId);
        DtpRwdObjectDTO dtpRwdObject = dtpRwdClientService.getObject(param);
        if (Objects.isNull(dtpRwdObject)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "实例id不存在", "实例id:" + systemObjectId + "不存在");
        }

        graphGenerateLoadDto.setReferenceObjectId(systemObjectId);
        boolean isNotEdit = getIsNotEdit(dtpRwdObject, graphTypeCode, systemObjectId);
        // 创建已配置的小附件
        graphGenerateLoadDto.setAttachEnable(true);
        //全量小部件用于作图人调试用，下游调用设为false
        graphGenerateLoadDto.setAttachShowAll(false);
//        模板id事先已经匹配过，根据模板id生成实例图
//        graphGenerateLoadDto.setTemplateOriId(templateOriId);
        graphGenerateLoadDto.setGraphTypeCode(graphTypeCode);
        graphGenerateLoadDto.setCacheRead(false);
        try {
            //先查询是否存在已发布的图纸
            List<SystemGraphDuplicate> systemGraphDuplicates = systemGraphDuplicateService.listSystemGraphDuplicate(systemObjectId, graphTypeCode, EnumSystemGraphStatus.PUBLISHED);
            //获取原始图纸
            List<SystemGraphDuplicate> originalGraphs = systemGraphDuplicates.stream().filter(s -> s.getGraphType() == EnumGraphType.ORIGINAL).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(originalGraphs)) {
                //如果有原始图纸发布，则下架图纸
                originalGraphs.forEach(e -> this.offlineSystemGraph(e.getId()));
            }
            SystemGraphInstanceVo vo = systemGraphService.initSystemGraphData(graphGenerateLoadDto, isNotEdit, jobId);
            String generateId = vo.getGenerateId();
            vo.setIsUpdateDetail(false);
            SystemGraphData data = systemGraphDataService.saveSystemGraphData(null, vo, EnumSystemGraphStatus.UN_PUBLISHED);
            SystemGraphDuplicate systemGraphDuplicate = systemGraphDuplicateService.saveDuplicate(systemObjectId, EnumGraphType.ORIGINAL,
                    dtpRwdObject.getClassCode(), graphTypeCode, data.getId(), isNotEdit, generateId);
            //更新更新标识
            systemGraphTemplateStatusService.updateTemplateStatusByObjectIdAndGraphCodeType(graphTypeCode, false, systemObjectId);
            //如果不能编辑，又有发布的图纸，就直接发布
            if (isNotEdit && CollectionUtils.isNotEmpty(systemGraphDuplicates)) {
                this.publishSystemGraph(systemGraphDuplicate.getId());
            }
        } catch (Exception e) {
            logProjectInstance().error("系统图生成失败", e);
            throw e;
        }

        //返回副本列表
        List<SystemGraphDuplicate> list = systemGraphDuplicateService.listSystemGraphDuplicate(dto.getReferenceObjectId(), dto.getGraphTypeCode(), null);
        return BeanUtil.copyToList(list, SystemGraphDuplicateVO.class);
    }

    @Override
    public boolean getIsNotEdit(DtpRwdObjectDTO dtpRwdObject, String graphTypeCode, String systemObjectId) {
        List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
        String graphName = systemGraphService.getGraphName(dtpRwdObject.getClassCode(), graphTypeCode, graphConfig, systemObjectId);
        GraphConfigVo graphConfigVo = graphConfig.stream().filter(e -> e.getMapTitle().equals(graphName)).findFirst().orElse(null);
        return graphConfigVo != null && graphConfigVo.getNotEdit() == 1;
    }

    /**
     * 获取副本列表
     *
     * @param dto 参数
     * @return {@link List }<{@link SystemGraphDuplicateVO }>
     */
    @Override
    public List<SystemGraphDuplicateVO> listSystemGraphDuplicate(ListSystemGraphDuplicateDTO dto) {
        String systemObjectId = dto.getSystemObjectId();
        DtpRwdObjectDTO rwdObjectDTO = dtpRwdClientService.getObject(new DtpRwdObjectQueryParam().setId(systemObjectId));
        List<SystemGraphDuplicate> list = systemGraphDuplicateService.listSystemGraphDuplicate(systemObjectId, dto.getGraphTypeCode(), null);
        List<SystemGraphDuplicateVO> systemGraphDuplicateVOS = BeanUtil.copyToList(list, SystemGraphDuplicateVO.class);
        if (!Objects.isNull(rwdObjectDTO)) {
            systemGraphDuplicateVOS.forEach(vo -> {
                vo.setObjType(rwdObjectDTO.getObjType());
            });
        }
        return systemGraphDuplicateVOS;
    }

    /**
     * 创建系统图副本
     *
     * @param duplicateId 副本id
     * @return {@link SystemGraphDuplicateVO }
     */
    @Override
    public SystemGraphDuplicateVO createSystemGraphDuplicate(Long duplicateId) {
        SystemGraphDuplicateVO systemGraphDuplicate = systemGraphDuplicateService.createSystemGraphDuplicate(duplicateId);
        DtpRwdObjectDTO rwdObjectDTO = dtpRwdClientService.getObject(new DtpRwdObjectQueryParam().setId(systemGraphDuplicate.getSystemObjectId()));
        if (rwdObjectDTO != null) {
            systemGraphDuplicate.setObjType(rwdObjectDTO.getObjType());
        }
        return systemGraphDuplicate;
    }

    /**
     * 重新计算管道
     *
     * @param duplicateId 模板id
     * @param dto         到
     * @return {@link SystemGraphDuplicateVO }
     */
    @Override
    public SystemGraphDuplicateVO recalculatePipeline(Long duplicateId, RequestSystemGraphDTO dto) {
        SystemGraphDuplicate duplicate = systemGraphDuplicateService.getById(duplicateId);
        if (duplicate == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "系统图副本不存在", "系统图副本不存在");
        }
        if (duplicate.getIsNotEdit()) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "该系统图不可编辑", "该系统图不可编辑");
        }
        if (duplicate.getStatus() == EnumSystemGraphStatus.PUBLISHED) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "已发布的系统图不能计算管线", "已发布的系统图不能计算管线");
        }
        dto.setCacheRead(!StringUtils.isEmpty(dto.getGenerateId()));
        //先保存参数
        systemGraphGenerateParameterService.saveAndUpdateSystemGraphGenerateParameter(duplicateId, dto);
        RequestSystemGraphDTO requestSystemGraphDTO = systemGraphGenerateParameterService.getParameterDataByDuplicateId(duplicateId);
        if (requestSystemGraphDTO == null) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "获取参数失败", "参数为空");
        }
        try {
            SystemGraphInstanceVo vo = systemGraphService.initSystemGraphData(requestSystemGraphDTO, false, null);
            vo.setIsUpdateDetail(false);
            // 重算后仅保留管线流向规则，其余 customizeContent 数据保持原有丢弃行为
            carryOverPipelineFlowRules(vo, duplicate);
            SystemGraphData systemGraphData = systemGraphDataService.saveSystemGraphData(null, vo, EnumSystemGraphStatus.UN_PUBLISHED);
            systemGraphDuplicateService.updateDuplicateDataById(duplicateId, systemGraphData.getId());
        } catch (Exception e) {
            logProjectInstance().error("系统图生成失败", e);
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "系统图生成失败，原因：" + e, "系统图生成失败");
        }
        SystemGraphDuplicate newDuplicate = systemGraphDuplicateService.getById(duplicateId);
        SystemGraphDuplicateVO vo = SystemGraphDuplicateVO.toVO(newDuplicate);
        DtpRwdObjectDTO rwdObjectDTO = dtpRwdClientService.getObject(new DtpRwdObjectQueryParam().setId(newDuplicate.getSystemObjectId()));
        if (rwdObjectDTO != null) {
            vo.setObjType(rwdObjectDTO.getObjType());
        }
        return vo;
    }


    /**
     * 从老系统图数据中搬运管线流向规则到新 vo
     *
     * <p>仅整块搬运 customizeContent.pipelineFlowRules，不做任何过滤；老数据不存在该 key 时跳过，
     * 行为与改动前一致。
     *
     * @param vo        新生成的系统图实例
     * @param duplicate 老系统图副本
     */
    private void carryOverPipelineFlowRules(SystemGraphInstanceVo vo, SystemGraphDuplicate duplicate) {
        SystemGraphData oldData = systemGraphDataService.getById(duplicate.getSystemGraphDataId());
        if (oldData == null || StringUtils.isEmpty(oldData.getData())) {
            return;
        }
        JSONObject oldJson = JSON.parseObject(CompressUtil.decompress(oldData.getData()));
        if (oldJson == null) {
            return;
        }
        JSONObject oldCustomize = oldJson.getJSONObject("customizeContent");
        if (oldCustomize == null) {
            return;
        }
        Object pipelineFlowRules = oldCustomize.get(PipelineFlowRulesMerger.PIPELINE_FLOW_RULES_KEY);
        if (pipelineFlowRules != null) {
            JSONObject newCustomize = new JSONObject();
            newCustomize.put(PipelineFlowRulesMerger.PIPELINE_FLOW_RULES_KEY, pipelineFlowRules);
            vo.setCustomizeContent(newCustomize);
        }
    }

    /**
     * 获取系统图数据（包含图标和详情数据）
     *
     * @param duplicateId  副本id
     * @param isDetailData 是否包含详情数据
     * @return {@link SystemGraphInstanceVo }
     */
    @Override
    public SystemGraphInstanceVo getSystemGraphData(Long duplicateId, boolean isDetailData) {
        SystemGraphInstanceVo vo = getSystemGraphDataNoDetailDataById(duplicateId);
        if (vo == null) {
            return null;
        }
        abstractGraphLegendServer.initGraphLegendAndIcons(vo);
        //匹配并联线组
        systemGraphService.matchParallelLines(vo);
        //匹配管线样式，包括母线，立管等
        systemGraphService.matchPipeLineStyle(vo);

        systemGraphService.getLineNode(vo);

        if (isDetailData) {
            List<SystemGraphDetail> systemGraphDetails = systemGraphDetailService.listSystemGraphDetailByDuplicateId(duplicateId);
            List<SystemGraphDetailVO> systemGraphDetailVOS = BeanUtil.copyToList(systemGraphDetails, SystemGraphDetailVO.class);
            for (SystemGraphDetailVO systemGraphDetailVO : systemGraphDetailVOS) {
                if (systemGraphDetailVO.getData() != null) {
                    systemGraphDetailVO.setData(JSONObject.parseObject(systemGraphDetailVO.getData().toString()));
                }
            }
            vo.setSystemGraphDetailVOs(systemGraphDetailVOS);
        }
        return vo;
    }

    /**
     * 通过id获取系统图数据无详细数据
     *
     * @param duplicateId 重复id
     * @return {@link SystemGraphInstanceVo }
     */
    @Override
    @Nullable
    public SystemGraphInstanceVo getSystemGraphDataNoDetailDataById(Long duplicateId) {
        SystemGraphDuplicate duplicate = systemGraphDuplicateService.getById(duplicateId);
        if (duplicate == null) {
            return null;
        }
        Long systemGraphDataId = duplicate.getSystemGraphDataId();
        SystemGraphData systemGraphData = systemGraphDataService.getById(systemGraphDataId);
        if (systemGraphData == null) {
            return null;
        }
        return JSON.parseObject(CompressUtil.decompress(systemGraphData.getData()), SystemGraphInstanceVo.class);
    }

    /**
     * 编辑系统图
     *
     * @param duplicateId 系统图数据id
     * @param dto         到
     * @return {@link Boolean }
     */
    @Override
    public Boolean editSystemGraph(Long duplicateId, List<UpdateGraphDTO> dto) {
        SystemGraphDuplicate duplicate = systemGraphDuplicateService.getById(duplicateId);
        if (duplicate.getStatus() == EnumSystemGraphStatus.PUBLISHED) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "已发布的系统图不能编辑", "已发布的系统图不能编辑");
        }
        Long systemGraphDataId = duplicate.getSystemGraphDataId();
        return systemGraphDataService.editSystemGraph(systemGraphDataId, dto);
    }

    /**
     * 发布系统图
     *
     * @param duplicateId 模板id
     * @return {@link Boolean }
     */
    @Override
    public Boolean publishSystemGraph(Long duplicateId) {
        return systemGraphDuplicateService.publishSystemGraph(duplicateId);
    }

    /**
     * 删除副本
     *
     * @param duplicateId 模板id
     * @return {@link Boolean }
     */
    @Override
    public Boolean deleteSystemGraphCopy(Long duplicateId) {
        SystemGraphDuplicate duplicate = systemGraphDuplicateService.getById(duplicateId);
        if (duplicate.getStatus() == EnumSystemGraphStatus.PUBLISHED) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "已发布的系统图不能删除", "已发布的系统图不能删除");
        }
        systemGraphDataService.removeById(duplicate.getSystemGraphDataId());
        return systemGraphDuplicateService.removeById(duplicateId);
    }

    /**
     * 下架图纸
     *
     * @param duplicateId 模板id
     * @return {@link Boolean }
     */
    @Override
    public Boolean offlineSystemGraph(Long duplicateId) {
        SystemGraphDuplicate graphDuplicate = systemGraphDuplicateService.getById(duplicateId);
        if (graphDuplicate.getStatus() == EnumSystemGraphStatus.PUBLISHED) {
            SystemGraphDuplicate systemGraphDuplicate = new SystemGraphDuplicate();
            systemGraphDuplicate.setId(duplicateId);
            systemGraphDuplicate.setStatus(EnumSystemGraphStatus.OFF_LINE);
            boolean b = systemGraphDuplicateService.updateById(systemGraphDuplicate);
            String graphTypeCode = graphDuplicate.getGraphTypeCode();
            String systemObjectId = graphDuplicate.getSystemObjectId();
            String classCode = graphDuplicate.getClassCode();
            systemGraphService.update(new LambdaUpdateWrapper<SystemGraph>()
                    .set(SystemGraph::getSystemGraphDuplicateId, null)
                    .eq(SystemGraph::getSystemObjectId, systemObjectId)
                    .eq(SystemGraph::getGraphTypeCode, graphTypeCode)
                    .eq(SystemGraph::getClassCode, classCode));
            return b;
        }
        return false;
    }

    @Override
    public SystemGraphDetailVO saveAndUpdateSystemGraphDetail(SystemGraphDetailVO systemGraphDetailVO) {

        // 前端页面加载系统图，带上系统图副本数据，如果有相关的细节调整的数据也会带上
        // 前端保存时候带上细节调整数据如果有细节id那么代表更新，如果没有id代表新增
        // 不论更新还是新增必须带上系统图副本的id。
        // 后端拿到这部分数据进行更新或者新增细节数据，如果是新增需要更新副本表中的细节id数据。如果更新则不去更新副本表的数据
        Long id = systemGraphDetailVO.getId();
        if (id == null) {
            SystemGraphDuplicate systemGraphDuplicate = systemGraphDuplicateService.getById(systemGraphDetailVO.getSystemGraphDuplicateId());
            if (systemGraphDuplicate == null) {
                throw new RuntimeException("未查询到系统图副本");
            }
            SystemGraphDetail systemGraphDetail = new SystemGraphDetail();
            BeanUtil.copyProperties(systemGraphDetailVO, systemGraphDetail);
            Object data = systemGraphDetailVO.getData();
            systemGraphDetail.setData(JSONObject.toJSONString(data));
            systemGraphDetailService.save(systemGraphDetail);
            systemGraphDetailVO.setId(systemGraphDetail.getId());
        } else {
            SystemGraphDetail systemGraphDetail = new SystemGraphDetail();
            BeanUtil.copyProperties(systemGraphDetailVO, systemGraphDetail);
            Object data = systemGraphDetailVO.getData();
            systemGraphDetail.setData(JSONObject.toJSONString(data));
            systemGraphDetailService.saveOrUpdate(systemGraphDetail);
            systemGraphDetailVO.setId(systemGraphDetail.getId());
        }
        return systemGraphDetailVO;
    }

    @Override
    public List<SystemGraphDetailVO> saveAndUpdateSystemGraphDetailBatch(List<SystemGraphDetailVO> systemGraphDetailVOs) {

        List<SystemGraphDetailVO> systemGraphDetailVOListNew = new ArrayList<>();
        for (SystemGraphDetailVO systemGraphDetailVO : systemGraphDetailVOs) {
            SystemGraphDetailVO systemGraphDetailVONew = saveAndUpdateSystemGraphDetail(systemGraphDetailVO);
            systemGraphDetailVOListNew.add(systemGraphDetailVONew);
        }
        return systemGraphDetailVOListNew;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean copyDetailData(@NotNull CopyDetailDataParam param) {
        Long sourceDuplicateId = param.getSourceDuplicateId();
        Long targetDuplicateId = param.getTargetDuplicateId();
        if (sourceDuplicateId.equals(targetDuplicateId)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), "同一个副本不能复制", "同一个副本不能复制");
        }
        //先清除目标副本的细节数据
        LambdaQueryWrapper<SystemGraphDetail> remoQw = new LambdaQueryWrapper<>();
        remoQw.eq(SystemGraphDetail::getSystemGraphDuplicateId, targetDuplicateId);
        systemGraphDetailService.remove(remoQw);
        //查询源副本的细节数据
        List<SystemGraphDetail> systemGraphDetails = systemGraphDetailService.listSystemGraphDetailByDuplicateId(sourceDuplicateId);
        if (CollectionUtils.isEmpty(systemGraphDetails)) {
            return true;
        }
        //清空id,并修改副本id
        systemGraphDetails.forEach(data -> {
            data.setId(null);
            data.setSystemGraphDuplicateId(targetDuplicateId);
            data.clearSaveParameter();
        });
        //批量保存
        return systemGraphDetailService.saveBatch(systemGraphDetails);
    }


    @Override
    public Boolean deleteSystemGraphDetail(SystemGraphDetailVO systemGraphDetailVO) {
        SystemGraphDetail systemGraphDetail = systemGraphDetailService.getById(systemGraphDetailVO.getId());
        if (systemGraphDetail == null) {
            throw new RuntimeException("未查询到系统图细节调整");
        }
        systemGraphDetailService.removeById(systemGraphDetailVO.getId());
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SystemGraphDetailVO> getSystemGraphDetailById(SystemGraphDetailVO systemGraphDetailVO) {
        List<SystemGraphDetail> systemGraphDetails = systemGraphDetailService.listSystemGraphDetailByDuplicateId(systemGraphDetailVO.getSystemGraphDuplicateId());
        if (CollectionUtils.isEmpty(systemGraphDetails)) {
            return new ArrayList<>();
        }
        List<SystemGraphDetailVO> systemGraphDetailVOList = new ArrayList<>();
        Map<String, Set<String>> objectAndInfoCodeMapDataAll = new HashMap<>(16);
        for (SystemGraphDetail systemGraphDetail : systemGraphDetails) {
            SystemGraphDetailVO systemGraphDetailVONew = SystemGraphDetailVO.systemGraphDetailToVO(systemGraphDetail);
            //每个细节图拿到的对象信息点数据，对象在多个细节图上是重复的
            Map<String, Set<String>> objectAndInfoCodeMapData = systemGraphDetailVONew.getObjectAndInfoCodeMapData();
            if (MapUtils.isNotEmpty(objectAndInfoCodeMapData)) {
                for (String localId : objectAndInfoCodeMapData.keySet()) {
                    Set<String> infoCodes = objectAndInfoCodeMapDataAll.get(localId);
                    if (CollectionUtils.isNotEmpty(infoCodes)) {
                        infoCodes.addAll(objectAndInfoCodeMapData.get(localId));
                    } else {
                        infoCodes = new HashSet<>(objectAndInfoCodeMapData.get(localId));
                    }
                    objectAndInfoCodeMapDataAll.put(localId, infoCodes);
                }
            }
            systemGraphDetailVOList.add(systemGraphDetailVONew);
        }
        Collection<Set<String>> infoCodeSetAll = objectAndInfoCodeMapDataAll.values();
        Set<String> infoCodeSets = new HashSet<>();
        for (Set<String> infoCodeSet : infoCodeSetAll) {
            infoCodeSets.addAll(infoCodeSet);
        }
        List<InfoCodeValueVO> queryIotRealDataListByList = abstractIotDataCacheServer.listInfoCodeValueInOneHour(
                Lists.newArrayList(objectAndInfoCodeMapDataAll.keySet()), infoCodeSets);
        if (CollectionUtils.isEmpty(queryIotRealDataListByList)) {
            return systemGraphDetailVOList;
        }

        List<DtpRwdObjectDTO> dtpRwdObjectDTOS = dtpRwdClientService
                .listObject(new DtpRwdObjectQueryParam().setId(objectAndInfoCodeMapDataAll.keySet())
                        .addWithColumn(DtpRwdObjectDTO.Fields.classCode, DtpRwdObjectDTO.Fields.id));
        if (CollectionUtils.isEmpty(dtpRwdObjectDTOS)) {
            return systemGraphDetailVOList;
        }
        Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> infoCodeAndDataClassCode = new HashMap<>(16);
        //拼接datasource
        Set<String> classCodeSet = new HashSet<>();
        Map<String, DtpRwdObjectDTO> dtpRwdObjectDTOMap = new HashMap<>();
        for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectDTOS) {
            classCodeSet.add(dtpRwdObjectDTO.getClassCode());
            dtpRwdObjectDTOMap.put(dtpRwdObjectDTO.getId(), dtpRwdObjectDTO);
        }

        Map<String, List<DtpKnowledgeInfoCodeDTO>> codeAndInfoCodeDefines = dtpKnowledgeClientService.mapClassCodeAndInfoCodeDefines(false, false, classCodeSet);
        for (Map.Entry<String, List<DtpKnowledgeInfoCodeDTO>> codeAndInfoCodeDefinesListEntry : codeAndInfoCodeDefines.entrySet()) {
            String classCode = codeAndInfoCodeDefinesListEntry.getKey();
            List<DtpKnowledgeInfoCodeDTO> codeAndInfoCodeDefinesList = codeAndInfoCodeDefinesListEntry.getValue();
            Map<String, DtpKnowledgeInfoCodeDTO> dtpKnowledgeInfoCodeDTOMap = new HashMap<>();
            for (DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO : codeAndInfoCodeDefinesList) {
                dtpKnowledgeInfoCodeDTOMap.put(dtpKnowledgeInfoCodeDTO.getCode(), dtpKnowledgeInfoCodeDTO);
            }
            infoCodeAndDataClassCode.put(classCode, dtpKnowledgeInfoCodeDTOMap);
        }

        Map<String, List<InfoCodeValueVO>> infoCodeValueVOAndObjectMap = new HashMap<>();
        Map<String, String> objectIdClassCode = new HashMap<>();
        for (InfoCodeValueVO infoCodeValueVO : queryIotRealDataListByList) {
            String infoCode = infoCodeValueVO.getInfoCode();
            Object infoCodeValue = infoCodeValueVO.getInfoCodeValue();
            infoCodeValueVO.setDataValue(infoCodeValue);
            String objectId = infoCodeValueVO.getObjectId();
            List<InfoCodeValueVO> infoCodes = infoCodeValueVOAndObjectMap.get(objectId);
            //拿datasource  objectId  classcode+ infocode
            DtpRwdObjectDTO dtpRwdObjectDTO = dtpRwdObjectDTOMap.get(objectId);
            if (dtpRwdObjectDTO == null) {
                continue;
            }
            String classCode = dtpRwdObjectDTO.getClassCode();
            Map<String, DtpKnowledgeInfoCodeDTO> stringDtpKnowledgeInfoCodeDTOMap = infoCodeAndDataClassCode.get(classCode);
            if (MapUtils.isEmpty(stringDtpKnowledgeInfoCodeDTOMap)) {
                continue;
            }
            DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = stringDtpKnowledgeInfoCodeDTOMap.getOrDefault(infoCode, new DtpKnowledgeInfoCodeDTO());
            List<DtpKnowledgeInfoCodeDataSourceDTO> dataSources = dtpKnowledgeInfoCodeDTO.getDataSources();
            if (CollectionUtils.isNotEmpty(dataSources)) {
                List<DtpKnowledgeInfoCdeDataSourceVO> dtpKnowledgeInfoCdeDataSourceVOS = BeanUtil.copyToList(dataSources, DtpKnowledgeInfoCdeDataSourceVO.class);
                infoCodeValueVO.setDataSources(dtpKnowledgeInfoCdeDataSourceVOS);
            }
            if (CollectionUtils.isEmpty(infoCodes)) {
                infoCodes = new ArrayList<>();
            }
            infoCodeValueVO.setInfoCodeValue(getInfoCodeValue(infoCodeValueVO, dtpKnowledgeInfoCodeDTO));
            infoCodeValueVO.setOriginDataType(dtpKnowledgeInfoCodeDTO.getOriginDataType());
            infoCodes.add(infoCodeValueVO);
            infoCodeValueVOAndObjectMap.put(objectId, infoCodes);
            objectIdClassCode.put(objectId, classCode);
        }
        for (SystemGraphDetailVO graphDetailVO : systemGraphDetailVOList) {
            Map<String, Set<String>> objectAndInfoCodeMapData = graphDetailVO.getObjectAndInfoCodeMapData();
            if (MapUtils.isEmpty(objectAndInfoCodeMapData)) {
                continue;
            }
            JSONObject jsonObjectData = JSONObject.parseObject(JSONObject.toJSONString(graphDetailVO.getData()));
            JSONArray equipmentIotInfoCodes = new JSONArray();
            for (String objectId : infoCodeValueVOAndObjectMap.keySet()) {
                JSONObject equipmentIotInfoCode = new JSONObject();
                equipmentIotInfoCode.put("equipmentId", objectId);
                equipmentIotInfoCode.put("classCode", objectIdClassCode.get(objectId));
                equipmentIotInfoCode.put("infoCodes", infoCodeValueVOAndObjectMap.get(objectId));
                equipmentIotInfoCodes.add(equipmentIotInfoCode);
            }
            jsonObjectData.put("equipmentIotInfoCodes", equipmentIotInfoCodes);
            graphDetailVO.setData(jsonObjectData);
            graphDetailVO.setObjectAndInfoCodeMapData(null);
        }
        return systemGraphDetailVOList;
    }

    /**
     * 获取信息代码值
     *
     * @param infoCodeValueVO         信息点值对象
     * @param dtpKnowledgeInfoCodeDTO 知识库
     * @return {@code Object }
     */
    private static Object getInfoCodeValue(InfoCodeValueVO infoCodeValueVO, DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO) {
        Object infoCodeValue = infoCodeValueVO.getInfoCodeValue();
        if (CollectionUtils.isNotEmpty(infoCodeValueVO.getDataSources()) && Objects.nonNull(infoCodeValue)) {
            for (int i = 0; i < infoCodeValueVO.getDataSources().size(); i++) {
                DtpKnowledgeInfoCdeDataSourceVO source = infoCodeValueVO.getDataSources().get(i);
                String code = source.getCode();
                if (infoCodeValue.equals(code)) {
                    return source.getName();
                }
            }
        }
        BigDecimal bigDecimalInfoCodeValue = new BigDecimal(String.valueOf(infoCodeValue));
        if (dtpKnowledgeInfoCodeDTO.getScale() != null) {
            bigDecimalInfoCodeValue = bigDecimalInfoCodeValue.setScale(dtpKnowledgeInfoCodeDTO.getScale(), RoundingMode.HALF_UP);
        }
        return bigDecimalInfoCodeValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ListInfoCodeVo> listInfoCode(ListInfoCodeDTO listInfoCodeDTO) {
        String projectId = listInfoCodeDTO.getProjectId();
        List<String> objectIdList = listInfoCodeDTO.getObjectIdList();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setId(objectIdList);
        // 查询对象实例信息
        List<DtpRwdObjectDTO> dtpRwdObjectList = dtpRwdClientService.listObject(param);
        if (CollectionUtils.isEmpty(dtpRwdObjectList)) {
            return new ArrayList<>();
        }
        // 图信息配置
        Map<String, List<String>> groupByClassCodeMap = dtpRwdObjectList.stream()
                .collect(Collectors.groupingBy(DtpRwdObjectDTO::getClassCode, Collectors.mapping(DtpRwdObjectDTO::getId, Collectors.toList())));
        Map<String, List<RcInfoCodeCfg>> infoCodeConfigsByRule = new HashMap<>(16);
        groupByClassCodeMap.forEach((classCode, idList) ->
                infoCodeConfigsByRule.putAll(
                        localRedisCacheHandler.getInfoCodeConfigsByRule(idList, ContextUtil.getGroupCode(), ContextUtil.getProjectId(), classCode)));

        Set<String> infoCodeList = infoCodeConfigsByRule.values().stream()
                .flatMap(Collection::stream).map(RcInfoCodeCfg::getInfoCode).collect(Collectors.toSet());

        // 一次性查出所有设备的所有信息点数据
        List<InfoCodeValueVO> list = this.abstractIotDataCacheServer.listInfoCodeValueInOneHour(objectIdList, infoCodeList);
        Map<String, Map<String, List<InfoCodeValueVO>>> groupedMap = new HashMap<>(16);
        if (CollectionUtils.isNotEmpty(list)) {
            // 按设备id->信息点分组->信息点数据
            groupedMap = list.stream()
                    .collect(Collectors.groupingBy(InfoCodeValueVO::getObjectId,
                            Collectors.groupingBy(InfoCodeValueVO::getInfoCode)));
        }

        return dataToVo(dtpRwdObjectList, infoCodeConfigsByRule, groupedMap);
    }

    /**
     * 数据转vo响应返回前端
     *
     * @param dtpRwdObjectList      对象信息
     * @param infoCodeConfigsByRule 孪生数据信息点过滤规则后的Map
     * @param graphInfoMap          关系图信息点数据Map
     * @return 图中设备已绑定的信息点全集
     */
    private List<ListInfoCodeVo> dataToVo(List<DtpRwdObjectDTO> dtpRwdObjectList,
                                          Map<String, List<RcInfoCodeCfg>> infoCodeConfigsByRule,
                                          Map<String, Map<String, List<InfoCodeValueVO>>> graphInfoMap) {
        List<ListInfoCodeVo> resultInfoCodeList = new ArrayList<>();
        Map<String, String> getEquipNumMap = getEquipNumMap(dtpRwdObjectList);
        // 根据rwd有的设备信息响应集合
        for (DtpRwdObjectDTO dtpRwdObjectDTO : dtpRwdObjectList) {
            ListInfoCodeVo listInfoCodeVo = new ListInfoCodeVo();
            String objectId = dtpRwdObjectDTO.getId();
            listInfoCodeVo.setId(objectId);
            listInfoCodeVo.setLocalName(dtpRwdObjectDTO.getLocalName());
            // 设备id无相关规则，则直接跳过信息点相关，直接返回名称
            if (!infoCodeConfigsByRule.containsKey(objectId)) {
                //组内异常标签不需要判断规则
                if (SHOW_EQUIP_GROUP_LABEL_CLASS_CODE_LIST.contains(dtpRwdObjectDTO.getClassCode())) {
                    //添加组内异常标签
                    List<SystemGraphEditorInfoCodeVO> rcInfoCodeList = Lists.newArrayList(systemGraphConverter.toSystemGraphVo(getEquipGroupAbnormalEventNum()));
                    listInfoCodeVo.setInfoCodeList(rcInfoCodeList);
                }
                resultInfoCodeList.add(listInfoCodeVo);
                continue;
            }
            LinkedList<String> allTagInfoCodeList = new LinkedList<>(TAG_INFO_STRING_LIST);
            //如果是要显示高低液位报警的classCode，则加入到标签列表中
            if (SHOW_HIGHT_LOW_LEVEL_ALARM_CLASS_CODE_LIST.contains(dtpRwdObjectDTO.getClassCode())) {
                allTagInfoCodeList.addAll(TAG_HIGHT_LOW_LEVEL_ALARM_LIST);
            }
            List<RcInfoCodeCfg> rcInfoCodeList = infoCodeConfigsByRule.get(objectId);
            // 标签信息
            List<SystemGraphEditorInfoCodeVO> infoCodeResultList = new ArrayList<>();
            Map<String, List<InfoCodeValueVO>> infoValueMap = new HashMap<>(16);
            if (graphInfoMap.containsKey(objectId)) {
                infoValueMap = graphInfoMap.get(objectId);
            }
            // 保存本地手动链接的信息点
            List<RcInfoCodeCfg> localAndManualLinkInfoCode = new ArrayList<>();
            List<RcInfoCodeCfg> equipGroupRunLinkInfoCode = new ArrayList<>();
            // 所有的信息点
            for (RcInfoCodeCfg rcInfoCodeCfg : rcInfoCodeList) {
                String infoCode = rcInfoCodeCfg.getInfoCode();
                if (LocaleMessageUtil.getMessage("key_54fedcd29d9e414391c96fa0b36a6b4f").equals(rcInfoCodeCfg.getParameterType())) {
                    // 业务配置表信息点转vo
                    SystemGraphEditorInfoCodeVO systemGraphVo = systemGraphConverter.toSystemGraphVo(rcInfoCodeCfg);
                    if (infoValueMap.containsKey(infoCode)) {
                        InfoCodeValueVO infoCodeValueVO = infoValueMap.get(infoCode).get(0);
                        systemGraphVo.setIpValue(
                                IotDataUtil.overrideDataValue(
                                        infoCode, String.valueOf(infoCodeValueVO.getInfoCodeValue()), rcInfoCodeCfg
                                )
                        );
                    }
                    // 非本地手动直接加入结果集
                    infoCodeResultList.add(systemGraphVo);
                    // 标签处理逻辑
                    if (allTagInfoCodeList.contains(infoCode)) {
                        rcInfoCodeCfg.setParameterType("标签");
                        if (infoCode.equals(InfoCodeEnum.LOCAL_REMOTZE_STATUS.getCode())
                                || infoCode.equals(InfoCodeEnum.MANUAL_AUTO_STATUS.getCode())) {
                            localAndManualLinkInfoCode.add(rcInfoCodeCfg);
                            continue;
                        }
                        if (SHOW_EQUIP_GROUP_LABEL_CLASS_CODE_LIST.contains(dtpRwdObjectDTO.getClassCode())
                                && InfoCodeEnum.GROUP_EQUIP_RUNING_NUM.getCode().equals(infoCode)) {
                            equipGroupRunLinkInfoCode.add(rcInfoCodeCfg);
                            continue;
                        }
                        // 非本地手动直接加入结果集
                        infoCodeResultList.add(systemGraphConverter.toSystemGraphVo(rcInfoCodeCfg));
                    }
                }
            }
            // 手自动聚合信息点
            if (CollectionUtils.isNotEmpty(localAndManualLinkInfoCode)) {
                infoCodeResultList.add(systemGraphConverter.toSystemGraphVo(getLocalAndManualInfo(localAndManualLinkInfoCode)));
            }
            if (SHOW_EQUIP_GROUP_LABEL_CLASS_CODE_LIST.contains(dtpRwdObjectDTO.getClassCode())) {

                equipGroupRunLinkInfoCode.add(getEquipGroupTotalEquipNum(getEquipNumMap, objectId));

                //添加组内运行标签
                infoCodeResultList.add(systemGraphConverter.toSystemGraphVo(getEquipGroupRunNumLabel(equipGroupRunLinkInfoCode)));
                //添加组内异常标签
                infoCodeResultList.add(systemGraphConverter.toSystemGraphVo(getEquipGroupAbnormalEventNum()));
            }

            listInfoCodeVo.setInfoCodeList(infoCodeResultList);
            resultInfoCodeList.add(listInfoCodeVo);
        }
        return resultInfoCodeList;
    }

    /**
     * 获取设备组下的设备数量和异常事件数量
     *
     * @param objects 对象列表
     * @return {@code Map<String, String> }
     * @since 7.250403.0
     */
    private Map<String, String> getEquipNumMap(List<DtpRwdObjectDTO> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return Maps.newHashMap();
        }
        Set<String> groupObjectIds = objects.stream().filter(e -> SHOW_EQUIP_GROUP_LABEL_CLASS_CODE_LIST.contains(e.getClassCode()))
                .map(DtpRwdObjectDTO::getId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(groupObjectIds)) {
            return Maps.newHashMap();
        }
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        param.setRelationTo(new DtpRwdObjectRelationQueryParam(DtpRwdRelationTypeEnum.EQUIP_IN_GROUP).setObjFrom(groupObjectIds));
        List<DtpRwdEquipmentDTO> equipmentDTOS = dtpRwdClientService.listEquipment(param);
        if (CollectionUtils.isEmpty(equipmentDTOS)) {
            return Maps.newHashMap();
        }
        Map<String, List<DtpRwdEquipmentDTO>> relIdObjectsMap = dtpRwdClientService.mapObjectIdAndRelObjects(equipmentDTOS,objects);
        //统计组下设备数量
        return groupObjectIds.stream().map(groupObjectId -> {
            List<DtpRwdEquipmentDTO> equipments = relIdObjectsMap.getOrDefault(groupObjectId, Collections.emptyList());
            return new AbstractMap.SimpleEntry<>(groupObjectId, String.valueOf(equipments.size()));
        }).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    }

    private RcInfoCodeCfg getLocalAndManualInfo(List<RcInfoCodeCfg> linkCodes) {
        // 本地手动合并信息点
        RcInfoCodeCfg localAndManualInfo = new RcInfoCodeCfg();
        localAndManualInfo.setParameterType("标签");
        localAndManualInfo.setInfoCode("localAndManualStatus");
        localAndManualInfo.setName(LocaleMessageUtil.getMessage("key_905da8bf127f4b3a96a5fb175cdac475"));
        localAndManualInfo.setDataSource(Lists.newArrayList(
                new DtpKnowledgeInfoCodeDataSourceDTO("0", LocaleMessageUtil.getMessage("key_0496c23144324b83b80df317faf857d6")),
                new DtpKnowledgeInfoCodeDataSourceDTO("0", LocaleMessageUtil.getMessage("key_a2341d9fc6b04ba2a3f5781eb747b14c")))
        );
        localAndManualInfo.setLinkCodes(linkCodes);
        return localAndManualInfo;
    }

    /**
     * 获取组内运行数量标签
     *
     * @param linkCodes 链接infocode
     * @return {@code RcInfoCodeCfg }
     * @since 7.250403.0
     */
    private RcInfoCodeCfg getEquipGroupRunNumLabel(List<RcInfoCodeCfg> linkCodes) {
        // 本地手动合并信息点
        RcInfoCodeCfg localAndManualInfo = new RcInfoCodeCfg();
        localAndManualInfo.setParameterType("标签");
        localAndManualInfo.setInfoCode(EQUIP_GROUP_RUN_NUM_LABEL);
        localAndManualInfo.setName(LocaleMessageUtil.getMessage("key_d40a5151d39049a7b03650770e58f28e"));
        localAndManualInfo.setLinkCodes(linkCodes);
        return localAndManualInfo;
    }

    /**
     * 获取设备组异常事件数量
     *
     * @return {@code RcInfoCodeCfg }
     * @since 7.250403.0
     */
    private RcInfoCodeCfg getEquipGroupAbnormalEventNum() {
        // 本地手动合并信息点
        RcInfoCodeCfg localAndManualInfo = new RcInfoCodeCfg();
        localAndManualInfo.setParameterType("标签");
        localAndManualInfo.setInfoCode(EQUIP_GROUP_ABNORMAL_EVENT_NUM_LABEL);
        localAndManualInfo.setName(LocaleMessageUtil.getMessage("key_be82b4b1145c437ca0cc2d6882a165e8"));
        //根据产品要求，默认返回1,不查询实际数量
        localAndManualInfo.setDataValue("1");
        return localAndManualInfo;
    }


    /**
     * 获取设备组下所有设备数量
     *
     * @return {@code RcInfoCodeCfg }
     * @since 7.250403.0
     */
    private RcInfoCodeCfg getEquipGroupTotalEquipNum(Map<String, String> getEquipGroupRunNumMap, String objectId) {
        // 本地手动合并信息点
        RcInfoCodeCfg totalEquipNumInfoCode = new RcInfoCodeCfg();
        totalEquipNumInfoCode.setParameterType("标签");
        totalEquipNumInfoCode.setInfoCode("equipGroupTotalEquipNum");
        totalEquipNumInfoCode.setName(LocaleMessageUtil.getMessage("key_3cd43253cf4c45efae9eda259873ff5d"));
        totalEquipNumInfoCode.setDataValue(getEquipGroupRunNumMap.getOrDefault(objectId, "0"));
        return totalEquipNumInfoCode;
    }

    @Override
    public Boolean editDuplicateName(UpdateDuplicateNameVO updateDuplicateNameVO) {
        return systemGraphDuplicateService.editDuplicateNameById(
                updateDuplicateNameVO.getDuplicateId(), updateDuplicateNameVO.getDuplicateName());
    }

    @Override
    public String updateTemplateName(UpdateTemplateNameDTO updateTemplateNameDTO) {
        boolean bool = systemGraphTemplateStatusService.updateTemplateName(updateTemplateNameDTO);
        String templateName = systemGraphTemplateStatusService.getTemplateName(updateTemplateNameDTO.getGraphCodeType(), updateTemplateNameDTO.getObjectId());
        if (!bool) {
            return templateName;
        }

        List<SystemGraphDuplicate> systemGraphDuplicates = systemGraphDuplicateService.listSystemGraphDuplicate(updateTemplateNameDTO.getObjectId(),
                updateTemplateNameDTO.getGraphCodeType(), EnumSystemGraphStatus.PUBLISHED);
        if (CollectionUtils.isEmpty(systemGraphDuplicates)) {
            return templateName;
        }
        SystemGraph systemGraph = systemGraphService.getSystemGraphByDuplicateId(systemGraphDuplicates.get(0).getId());
        if (Objects.isNull(systemGraph)) {
            return templateName;
        }
        systemGraphService.updateSystemGraphName(systemGraph.getId(), templateName);
        return templateName;
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "关系图编辑器Service";
    }
}
