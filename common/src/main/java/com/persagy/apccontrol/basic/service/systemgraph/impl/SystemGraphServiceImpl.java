package com.persagy.apccontrol.basic.service.systemgraph.impl;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.abstracts.AbstractCommonServer;
import com.persagy.apccontrol.basic.convert.RcInfoCodeCfgConvert;
import com.persagy.apccontrol.basic.handler.LocalRedisCacheHandler;
import com.persagy.apccontrol.basic.mapper.SystemGraphMapper;
import com.persagy.apccontrol.basic.model.base.InfoCodeEnum;
import com.persagy.apccontrol.basic.model.dto.DirectRelationDTO;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.GraphJobParam;
import com.persagy.apccontrol.basic.model.dto.systemgraph.LineNode;
import com.persagy.apccontrol.basic.model.dto.systemgraph.RequestSystemGraphDTO;
import com.persagy.apccontrol.basic.model.dto.systemgraph.adm.*;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraph;
import com.persagy.apccontrol.basic.model.entity.systemgraph.SystemGraphAsyncJob;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdClassCodeEnum;
import com.persagy.apccontrol.basic.model.enumtype.systemgraph.EnumJobStatus;
import com.persagy.apccontrol.basic.model.vo.systemgraph.*;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphConfigVo;
import com.persagy.apccontrol.basic.model.vo.systemgraph.config.GraphInformationVo;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectQueryParam;
import com.persagy.apccontrol.basic.outside.feign.model.param.rwd.DtpRwdObjectRelationQueryParam;
import com.persagy.apccontrol.basic.outside.feign.service.DtpKnowledgeClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRelationalGraphClientService;
import com.persagy.apccontrol.basic.outside.feign.service.DtpRwdClientService;
import com.persagy.apccontrol.basic.resource.SystemGraphAsyncJobService;
import com.persagy.apccontrol.basic.service.common.CommonConfigService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphIconService;
import com.persagy.apccontrol.basic.service.systemgraph.GraphLegendService;
import com.persagy.apccontrol.basic.service.systemgraph.SystemGraphService;
import com.persagy.apccontrol.basic.utils.systemgraph.ObjTypeUtil;
import com.persagy.framework.common.context.ContextUtil;
import com.persagy.framework.common.enums.ResponseCode;
import com.persagy.framework.common.exception.BusinessException;
import com.persagy.validate.utility.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.hutool.core.text.StrPool.UNDERLINE;

/**
 * @author persagy
 * @description 针对表【system_graph】的数据库操作Service实现
 * @createDate 2024-06-25 11:55:25
 */
@Service
@Slf4j
@RefreshScope
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SystemGraphServiceImpl extends ServiceImpl<SystemGraphMapper, SystemGraph>
        implements SystemGraphService {

    private final DtpRelationalGraphClientService dtpRelationalGraphClientService;
    private final CommonConfigService commonConfigService;
    private final DtpRwdClientService dtpRwdClientService;
    private final LocalRedisCacheHandler localRedisCacheHandler;
    private final GraphLegendService graphLegendService;
    private final GraphIconService graphIconService;
    private final AbstractCommonServer abstractCommonServer;
    private final DtpKnowledgeClientService dtpKnowledgeClientService;
    private final RcInfoCodeCfgConvert rcInfoCodeCfgConvert;
    private final SystemGraphAsyncJobService systemGraphAsyncJobService;
    @Value("${system.legend.icon.host:${external-url.bdtp.dtp-relational-graph.service:}")
    private String legendIconHost;
    @Value("${system.graph.async.timeout:120}")
    private Long graphAsyncTimeout;
    @Value("${system.graph.templateOriId.switch:true}")
    //实例化入参是否需要传模板id
    private Boolean graphTemplateOriIdSwitch;


    @Override
    public SystemGraphInstanceVo initSystemGraphData(@NotNull RequestSystemGraphDTO graphGenerateLoadDto, Boolean isNotEdit, String jobId) {
        Map<String, GraphLegendVo> legendUrlMap = graphLegendService.getGraphLegendMap();
        Map<String, List<GraphIconVo>> iconMap = graphIconService.getGraphIconMap();
        String graphTypeCode = graphGenerateLoadDto.getGraphTypeCode();
        String referenceObjectId = graphGenerateLoadDto.getReferenceObjectId();
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setId(graphGenerateLoadDto.getReferenceObjectId());
        DtpRwdObjectDTO dtpRwdObject = dtpRwdClientService.getObject(param);
        if (Objects.isNull(dtpRwdObject)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_cfc59d93e3a24812b02496ab5f2f9642"), LocaleMessageUtil.getMessage("key_8e8c163cbc924a59a21e0fb30ead3f60") + referenceObjectId + LocaleMessageUtil.getMessage("key_789305724b5b443bafd980ca0fb5b2c8"));
        }

        //装配锚点偏移量数据
//        putLegendDescMap(legendUrlMap, templateOriId, graphGenerateLoadDto, graphTypeCode, dtpRwdObject.getClassCode());

        List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
        String graphName = getGraphName(dtpRwdObject.getClassCode(), graphTypeCode, graphConfig, referenceObjectId);
        SystemGraphInstanceVo result = new SystemGraphInstanceVo();
        GraphGenerateInstanceDto graphGenerateInstanceDto = getGraphGenerateInstanceDto(graphGenerateLoadDto, referenceObjectId, jobId, isNotEdit);
        //处理图标信息
        List<LegendIconVO> legendIcons = graphGenerateInstanceDto.getLegendIcons();
        final Map<String, LegendIconVO> legendIconMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(legendIcons)) {
            legendIcons.forEach(e -> {
                List<InfoValueVO> infoValues = e.getInfoValues();
                if (CollectionUtils.isEmpty(infoValues)) {
                    return;
                }
                infoValues.forEach(infoValueVO -> {
                    infoValueVO.setHost(legendIconHost);
                });
            });
            legendIconMap.putAll(legendIcons.stream().collect(Collectors.toMap(LegendIconVO::getLegendOriId, Function.identity(), (v1, v2) -> v2)));
        }
        //设备节点分组添加资源库上传的图标信息
        List<GraphGroupVo> graphGroupVos = new ArrayList<>();
        List<GraphGroupDto> groups = graphGenerateInstanceDto.getGroups();
        if (CollectionUtils.isNotEmpty(groups)) {
            Set<String> classCodeSet = groups.stream().map(e -> e.getDataObject().getString("classCode")).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            Map<String, Set<String>> objectRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(ContextUtil.getGroupCode(),
                    ContextUtil.getProjectId(), classCodeSet);
            groups.forEach(nodeDto -> {
                GraphGroupVo groupVo = new GraphGroupVo();
                BeanUtils.copyProperties(nodeDto, groupVo);
                if (legendUrlMap.containsKey(nodeDto.getLegendOriId())) {
                    GraphLegendVo graphLegendVo = BeanUtil.copyProperties(legendUrlMap.get(nodeDto.getLegendOriId()), GraphLegendVo.class);
                    groupVo.setLegendUrlVo(Collections.singletonList(graphLegendVo));
                }
                if (legendIconMap.containsKey(nodeDto.getLegendOriId())) {
                    LegendNodeDto legendNode = nodeDto.getLegendNode();
                    legendNode.setLegendIcon(legendIconMap.get(nodeDto.getLegendOriId()));
                    groupVo.setLegendNode(legendNode);
                }
                //添加固定标签
                buildFixedLabelByGroup(groupVo, objectRuleMap);
                graphGroupVos.add(groupVo);
            });
        }

        //处理小附件图标
        Map<String, List<EquipmentNodeVo>> attachMap = graphGenerateInstanceDto.getAttachPairMap();
        Map<String, List<EquipmentNodeVo>> attachPairMap = new HashMap<>();
        if (MapUtils.isNotEmpty(attachMap)) {
            attachMap.forEach((k, v) -> {
                v.forEach(nodeVo -> {
                    String legendOriId = nodeVo.getLegendOriId();
                    if (legendUrlMap.containsKey(legendOriId)) {
                        GraphLegendVo graphLegendVo = BeanUtil.copyProperties(legendUrlMap.get(legendOriId), GraphLegendVo.class);
                        nodeVo.setLegendUrlVo(Collections.singletonList(graphLegendVo));
                    }
                    if (legendIconMap.containsKey(nodeVo.getLegendOriId())) {
                        LegendNodeDto legendNode = nodeVo.getLegendNode();
                        legendNode.setLegendIcon(legendIconMap.get(nodeVo.getLegendOriId()));
                        nodeVo.setLegendNode(legendNode);
                    }
                });
                attachPairMap.put(k, v);
            });
        }

        //小附件添加资源库上传的图标信息
        List<EquipmentNodeVo> passBys = new ArrayList<>();
        List<EquipmentNodeDto> oriPassBys = graphGenerateInstanceDto.getPassBys();
        if (CollectionUtils.isNotEmpty(oriPassBys)) {
            //获取所有阀门的classCode
            Set<String> classCodeSet = oriPassBys.stream().map(EquipmentNodeDto::getObjClassCode).filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());
            Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeMap = dtpKnowledgeClientService.mapClassCodeInfoCodeAndDefine(false, false, classCodeSet);
            Map<String, Set<String>> objectRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(ContextUtil.getGroupCode(),
                    ContextUtil.getProjectId(), classCodeSet);
            oriPassBys.forEach(nodeDto -> {
                EquipmentNodeVo passBy = new EquipmentNodeVo();
                BeanUtils.copyProperties(nodeDto, passBy);
                if (legendUrlMap.containsKey(nodeDto.getLegendOriId())) {
                    GraphLegendVo graphLegendVo = BeanUtil.copyProperties(legendUrlMap.get(nodeDto.getLegendOriId()), GraphLegendVo.class);
                    passBy.setLegendUrlVo(Collections.singletonList(graphLegendVo));
                }
                if (legendIconMap.containsKey(nodeDto.getLegendOriId())) {
                    LegendNodeDto legendNode = nodeDto.getLegendNode();
                    legendNode.setLegendIcon(legendIconMap.get(nodeDto.getLegendOriId()));
                    passBy.setLegendNode(legendNode);
                }
                passBys.add(passBy);
                //构建固定标签
                buildFixedLabelByPassBy(passBy, objectRuleMap, classCodeInfoCodeMap);
            });
        }

        //设备实例添加资源库上传的图标信息, 设备组添加配置信息
        List<EquipmentNodeVo> equipmentNodeVos = new ArrayList<>();
        List<EquipmentNodeDto> equipmentNodes = graphGenerateInstanceDto.getEquipmentNodes();
        if (CollectionUtils.isNotEmpty(equipmentNodes)) {
            Set<String> classCodeSet = equipmentNodes.stream().map(EquipmentNodeDto::getObjClassCode).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
            Map<String, Set<String>> objectRuleMap = this.abstractCommonServer.queryObjectsExistRuleMap(ContextUtil.getGroupCode(),
                    ContextUtil.getProjectId(), classCodeSet);
            //获取信息点配置
            Map<String, List<GraphInformationVo>> graphInformationGroupByClassCode = commonConfigService.getGraphInformationGroupByClassCode();
            Map<String, List<RcInfoCodeCfg>> mapListInfoCodeConfigs = localRedisCacheHandler.mapListInfoCodeConfigs(classCodeSet);
            equipmentNodes.forEach(nodeDto -> {
                EquipmentNodeVo vo = new EquipmentNodeVo();
                BeanUtils.copyProperties(nodeDto, vo);
                if (legendUrlMap.containsKey(nodeDto.getLegendOriId())) {
                    GraphLegendVo graphLegendVo = BeanUtil.copyProperties(legendUrlMap.get(nodeDto.getLegendOriId()), GraphLegendVo.class);
                    //构建图标信息
                    buildIconUrlVos(graphLegendVo, iconMap);
                    vo.setLegendUrlVo(Collections.singletonList(graphLegendVo));
                }
                if (legendIconMap.containsKey(nodeDto.getLegendOriId())) {
                    LegendNodeDto legendNode = nodeDto.getLegendNode();
                    legendNode.setLegendIcon(legendIconMap.get(nodeDto.getLegendOriId()));
                    vo.setLegendNode(legendNode);
                }

                if (Objects.equals(nodeDto.getCompType(), "packNode")) {
                    vo.setStyleCode("Area001");
                }

                //构建固定标签
                buildFixedLabel(vo, objectRuleMap, graphInformationGroupByClassCode);

                buildDrawerAllInfoCodes(vo, objectRuleMap, mapListInfoCodeConfigs);

                //构建信息点配置列表
                boolean isShowPanelNode = buildPanelNodeInformationList(vo, graphName, objectRuleMap, mapListInfoCodeConfigs);
                //如果返回false，则不显示信息板
                if (!isShowPanelNode) {
                    vo.setPanelNode(null);
                }

                equipmentNodeVos.add(vo);
            });

        }

        if (CollectionUtils.isEmpty(equipmentNodes) &&
                CollectionUtils.isEmpty(groups) &&
                CollectionUtils.isEmpty(oriPassBys)) {
            throw new BusinessException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_5852d676720940a6a9dc10fac080ebb2") + referenceObjectId + LocaleMessageUtil.getMessage("key_4efea380c9994d99a84726fd7b4c2a80"));
        }

        BeanUtil.copyProperties(graphGenerateInstanceDto, result);
        result.setEquipmentNodeVos(equipmentNodeVos);
        result.setEquipmentNodes(null);
//        result.setTextMap(graphGenerateInstanceDto.getInfoObject());
        result.setPassByVos(passBys);
        result.setAttachPairMap(attachPairMap);
        result.setPassBys(null);
        result.setGroupVos(graphGroupVos);
        result.setGroups(null);
        result.setLegendIcons(legendIcons);
        //处理并联线组
        matchParallelLines(result);
        //匹配管线样式，包括母线，立管等
        matchPipeLineStyle(result);

        //生成管道节点(节点标明管道样式)
        getLineNode(result);

        //匹配区域样式
        matchAreaStyle(result);

        //【系统图名称】由于系统图引擎不会给图纸实例命名，所以我们按照下列规则来命名：系统实例名称+关系图类名
        result.setName(dtpRwdObject.getLocalName() + graphTypeCode);
        return result;
    }

    @Override
    public void matchParallelLines(GraphGenerateInstanceDto graphGenerateInstanceDto) {
        //处理并联线组
        List<ParallelLine> parallelLines = graphGenerateInstanceDto.getParallelLines();
        if (CollectionUtils.isNotEmpty(parallelLines)) {
            Map<String, PipelineRelationConfigVo> graphRelMap = getGraphRelMap();
            for (ParallelLine parallelLine : parallelLines) {
                String graphCode = parallelLine.getGraphCode();
                String relCode = parallelLine.getRelCode();
                String key = graphCode + UNDERLINE + relCode;
                List<LineDto> linePaths = parallelLine.getLinePaths();
                if (CollectionUtils.isNotEmpty(linePaths)) {
                    for (LineDto linePath : linePaths) {
                        if (graphRelMap != null && graphRelMap.containsKey(key)) {
                            PipelineRelationConfigVo pipelineRelationConfigVo = graphRelMap.get(key);
                            linePath.setPipelineStyleCode(pipelineRelationConfigVo.getPipelineStyleCode());
                            linePath.setLineName(pipelineRelationConfigVo.getPipeName());
                        }
                    }

                }
            }
            graphGenerateInstanceDto.setParallelLines(parallelLines);
        }
    }

    /**
     * 管道节点计算
     */
    @Override
    public void getLineNode(SystemGraphInstanceVo systemGraphInstanceVo) {
        List<MainPipeDto> mainPipes = systemGraphInstanceVo.getMainPipes();
        if (CollectionUtils.isEmpty(mainPipes)) {
            return;
        }

        //主管道的节点坐标信息
        List<LineNode> mainLineNodes = new LinkedList<>();
        //同一管道的弯管节点坐标信息
        List<LineNode> bendLineNodes = new LinkedList<>();

        mainPipes.forEach(mainPipeDto -> {
            //主管道结构为二重数组，目前数据只有一个
            List<CoordinateDto> mainPoints = mainPipeDto.getLocationPath().get(0);
            mainPoints.forEach(it -> {
                LineNode lineNode = new LineNode();
                lineNode.setCoordinateDto(it);
                mainLineNodes.add(lineNode);
            });
        });

        Map<Integer, List<LineNode>> corrXLineNodeMap = mainLineNodes.stream().collect(Collectors.groupingBy(it -> it.getCoordinateDto().getX()));

        Map<Integer, List<LineNode>> corrYLineNodeMap = mainLineNodes.stream().collect(Collectors.groupingBy(it -> it.getCoordinateDto().getY()));

        //遍历所有线路，标记节点方向数据
        for (LineVo lineVo : systemGraphInstanceVo.getLineVos()) {
            List<CoordinateDto> locationPaths = lineVo.getLocationPath();
            String pipelineStyleCode = lineVo.getPipelineStyleCode();
            //前一个节点
            CoordinateDto last = null;
            //前两个节点
            CoordinateDto secondLast = null;

            //遍历线路里所有节点
            for (CoordinateDto now : locationPaths) {
                //第一个节点不计算
                if (last == null) {
                    last = now;
                    continue;
                }
                //标记管道上所有线路节点的向量
                //当前点和前一个点之间有没有线路节点
                this.getLineNodeVector(now, last, corrXLineNodeMap, corrYLineNodeMap, pipelineStyleCode);

                //弯管节点在主管道节点上时，不处理
                if (!(corrXLineNodeMap.containsKey(last.getX()) && corrYLineNodeMap.containsKey(last.getY()))) {
                    //一条管道的拐点处，一定是弯管
                    bendLineNodes.add(getBendLineNode(secondLast, last, now, lineVo));
                }

                //前一个节点变成前两个节点
                secondLast = last;
                //当前点变成前一个节点
                last = now;

            }

        }

        mainLineNodes.addAll(bendLineNodes.stream().filter(Objects::nonNull).collect(Collectors.toList()));

        systemGraphInstanceVo.setLineNodeVos(BeanUtil.copyToList(mainLineNodes.stream().filter(LineNode::filterLineNode).collect(Collectors.toList()), LineNodeVo.class));

    }


    @NotNull
    public GraphGenerateInstanceDto getGraphGenerateInstanceDto(RequestSystemGraphDTO graphGenerateLoadDto, String referenceObjectId, String jobId, Boolean isNotEdit) {
        logProjectInstance().info("关系图请求参数", Arrays.asList(Pair.of("基准对象", referenceObjectId), Pair.of("参数：", JSON.toJSONString(graphGenerateLoadDto))));
        // 是否是异步获取，如果是
        GraphGenerateInstanceDto response = null;
        // 使用 CompletableFuture 实现超时控制
        if (StringUtils.isBlank(jobId)) {
            CompletableFuture<GraphGenerateInstanceDto> future = CompletableFuture.supplyAsync(() -> dtpRelationalGraphClientService.loadGraphGenerate(graphGenerateLoadDto));
            // 设置超时时间为2分钟，超时后调用异步接口
            try {
                response = future.get(graphAsyncTimeout, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                GraphAsyncJobDTO graphAsyncJobDTO = dtpRelationalGraphClientService.asyncJobCommit(graphGenerateLoadDto);
                //如果任务存在，就删除创建新任务
                SystemGraphAsyncJob isExists = systemGraphAsyncJobService.getBySystemObjectIdAndGraphTypeCodeAndTemplateOriId(referenceObjectId,
                        graphGenerateLoadDto.getGraphTypeCode());
                if (Objects.nonNull(isExists)) {
                    systemGraphAsyncJobService.removeById(isExists.getId());
                }
                SystemGraphAsyncJob job = new SystemGraphAsyncJob();
                job.setJobParam(JSON.toJSONString(graphGenerateLoadDto));
                job.setJobId(graphAsyncJobDTO.getJobId());
                job.setSystemObjectId(referenceObjectId);
                job.setGraphTypeCode(graphGenerateLoadDto.getGraphTypeCode());
                job.setProjectId(ContextUtil.getProjectId());
                job.setNotEdit(isNotEdit);
                job.setJobStatus(EnumJobStatus.RUNNING);
                if (systemGraphAsyncJobService.save(job)) {
                    throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_2fb1b81ad12e48bb910611fd06bf26aa") + JSON.toJSONString(job), LocaleMessageUtil.getMessage("key_7b1db0dbb1fc4f64bd9ec18f518ce8ed"));
                }
            } catch (Exception e) {
                throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_4f87bcffbf2f4b75a4e764822f3af3b8") + graphGenerateLoadDto.getGraphTypeCode() + LocaleMessageUtil.getMessage("key_38fde9bc28064c2083dc9693024ccdc3") + e
                        + referenceObjectId + LocaleMessageUtil.getMessage("key_2000439ee7ce4d008c4b97280c68d4ab") + JSON.toJSONString(graphGenerateLoadDto), LocaleMessageUtil.getMessage("key_eb8bc5c7cfe84879b0d6d4486d73dc79"));
            }
        } else {
            response = dtpRelationalGraphClientService.asyncJobResult(new GraphJobParam(jobId));
        }
        if (Objects.isNull(response)) {
            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), LocaleMessageUtil.getMessage("key_4f87bcffbf2f4b75a4e764822f3af3b8") + graphGenerateLoadDto.getGraphTypeCode() + LocaleMessageUtil.getMessage("key_38fde9bc28064c2083dc9693024ccdc3") + referenceObjectId + LocaleMessageUtil.getMessage("key_2000439ee7ce4d008c4b97280c68d4ab") + JSON.toJSONString(graphGenerateLoadDto), LocaleMessageUtil.getMessage("key_eb8bc5c7cfe84879b0d6d4486d73dc79"));
        }
        return response;
    }

    @Override
    public void buildFixedLabelByPassBy(EquipmentNodeVo vo, Map<String, Set<String>> objectRuleMap, Map<String, Map<String, DtpKnowledgeInfoCodeDTO>> classCodeInfoCodeMap) {
        if (MapUtils.isEmpty(objectRuleMap) || MapUtils.isEmpty(classCodeInfoCodeMap)) {
            return;
        }
        Map<String, DtpKnowledgeInfoCodeDTO> infoCodeDTOMap = classCodeInfoCodeMap.get(vo.getObjClassCode());
        if (MapUtils.isEmpty(infoCodeDTOMap)) {
            return;
        }
        String dataObjectId = vo.getDataObjectId();
        String classCode = vo.getObjClassCode();
        Set<String> infoCodeSet = objectRuleMap.get(dataObjectId);
        if (StringUtils.isEmpty(dataObjectId) || CollectionUtils.isEmpty(infoCodeSet)) {
            return;
        }
        List<String> allInfoCodeList = new ArrayList<>(FIXED_PASSBY_INFOCODE_LIST);
        if (ALL_VALVE_CLASS_CODES.contains(classCode)) {
            allInfoCodeList.addAll(FIXED_SWITCH_INFOCODE_LIST);
        }
        List<RcInfoCodeCfg> labelList = new ArrayList<>();
        allInfoCodeList.forEach(infoCode -> {
            DtpKnowledgeInfoCodeDTO dtpKnowledgeInfoCodeDTO = infoCodeDTOMap.get(infoCode);
            if (!infoCodeSet.contains(infoCode) || Objects.isNull(dtpKnowledgeInfoCodeDTO)) {
                return;
            }
            labelList.add(rcInfoCodeCfgConvert.to(dtpKnowledgeInfoCodeDTO));
        });
        vo.setFixedInfoCodes(labelList);
    }

    /**
     * 生成固定标签
     *
     * @param vo            vo
     * @param objectRuleMap 对象规则
     */
    @Override
    public void buildFixedLabel(EquipmentNodeVo vo, Map<String, Set<String>> objectRuleMap, Map<String, List<GraphInformationVo>> graphInformationGroupByClassCode) {
        String classCode = vo.getObjClassCode();
        String dataObjectId = vo.getDataObjectId();
        List<GraphInformationVo> graphInformationVos = graphInformationGroupByClassCode.get(classCode);
        //柜号不需要判断规则
        List<RcInfoCodeCfg> labelList = new ArrayList<>();
        if (SHOW_PANEL_NO_CLASS_CODE_LIST.contains(classCode)) {
            RcInfoCodeCfg rcInfoCodeCfg = localRedisCacheHandler.getInfoCodeConfig(classCode, InfoCodeEnum.PANEL_NO.getCode());
            if (Objects.nonNull(rcInfoCodeCfg)) {
                labelList.add(rcInfoCodeCfg);
                vo.setFixedInfoCodes(labelList);
            }
        }
        if (DtpRwdClassCodeEnum.SETDDD.getValue().equals(classCode)) {
            //如果是配电抽屉，需要添加ct信息点 iTransRatedIRatio
            RcInfoCodeCfg rcInfoCodeCfg = localRedisCacheHandler.getInfoCodeConfig(classCode, InfoCodeEnum.I_TRANS_RATED_I_RATIO.getCode());
            if (Objects.nonNull(rcInfoCodeCfg)) {
                labelList.add(rcInfoCodeCfg);
                vo.setFixedInfoCodes(labelList);
            }
        }
        Set<String> infoCodeSet = objectRuleMap.get(dataObjectId);
        if (StringUtils.isEmpty(dataObjectId) || CollectionUtils.isEmpty(infoCodeSet)) {
            return;
        }
        List<String> allInfoCodeList = new ArrayList<>(fixedLabelInfoCodeList);
        if (SPECIAL_CLASS_CODE_LIST.contains(classCode)) {
            allInfoCodeList.addAll(SPECIAL_INFO_CODE_LIST);
        }
        // 组内设备运行数量
        if (SHOW_EQUIP_GROUP_LABEL_CLASS_CODE_LIST.contains(classCode)) {
            allInfoCodeList.add(InfoCodeEnum.GROUP_EQUIP_RUNING_NUM.getCode());
        }
        Set<String> breakInfoSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(graphInformationVos)) {
            Set<String> infoSet = graphInformationVos.stream().filter(e -> e.getDisplayMode() != 2).map(GraphInformationVo::getInfoCode).collect(Collectors.toSet());
            breakInfoSet.addAll(graphInformationVos.stream().filter(e -> e.getDisplayMode() == 2).map(GraphInformationVo::getInfoCode).collect(Collectors.toSet()));
            allInfoCodeList.addAll(infoSet);
            allInfoCodeList.addAll(breakInfoSet);
        }
        Set<String> filterInfoCodes = allInfoCodeList.stream().filter(infoCodeSet::contains).collect(Collectors.toSet());
        Map<String, RcInfoCodeCfg> rcInfoCodeCfgMap = localRedisCacheHandler.mapInfoCodeConfigs(classCode, filterInfoCodes);
        filterInfoCodes.forEach(infoCode -> {
            RcInfoCodeCfg rcInfoCodeCfg = rcInfoCodeCfgMap.get(infoCode);
            if (Objects.nonNull(rcInfoCodeCfg)) {
                if (breakInfoSet.contains(infoCode)) {
                    rcInfoCodeCfg.setDisplayMode(1);
                }
                labelList.add(rcInfoCodeCfg);
            }
        });
        vo.setFixedInfoCodes(labelList);
    }

    @Override
    public void buildFixedLabelByGroup(GraphGroupVo vo, Map<String, Set<String>> objectRuleMap) {
        String classCode = vo.getDataObject().getString("classCode");
        String dataObjectId = vo.getDataObjectId();
        List<RcInfoCodeCfg> labelList = new ArrayList<>();
        //柜号不需要判断规则
        if (SHOW_PANEL_NO_CLASS_CODE_LIST.contains(classCode)) {
            RcInfoCodeCfg rcInfoCodeCfg = localRedisCacheHandler.getInfoCodeConfig(classCode, InfoCodeEnum.PANEL_NO.getCode());
            if (Objects.nonNull(rcInfoCodeCfg)) {
                labelList.add(rcInfoCodeCfg);
                vo.setFixedInfoCodes(labelList);
            }
        }
        Set<String> infoCodeSet = objectRuleMap.getOrDefault(dataObjectId, Sets.newHashSet());
        if (CollectionUtils.isEmpty(infoCodeSet)) {
            return;
        }
        List<String> allInfoCodeList = new ArrayList<>();
        if (SPECIAL_CLASS_CODE_LIST.contains(classCode)) {
            allInfoCodeList.addAll(SPECIAL_INFO_CODE_LIST);
        }
        if (RUN_STATUS_CLASS_CODE_LIST.contains(classCode)) {
            allInfoCodeList.add(InfoCodeEnum.RUN_STATUS.getCode());
        }

        if (CollectionUtils.isEmpty(allInfoCodeList)) {
            return;
        }
        Set<String> filterInfoCodes = allInfoCodeList.stream().filter(infoCodeSet::contains).collect(Collectors.toSet());
        Map<String, RcInfoCodeCfg> rcInfoCodeCfgMap = localRedisCacheHandler.mapInfoCodeConfigs(classCode, filterInfoCodes);
        filterInfoCodes.forEach(infoCode -> {
            RcInfoCodeCfg rcInfoCodeCfg = rcInfoCodeCfgMap.get(infoCode);
            if (Objects.nonNull(rcInfoCodeCfg)) {
                labelList.add(rcInfoCodeCfg);
            }
        });
        vo.setFixedInfoCodes(labelList);
    }

//    /**
//     * 装配锚点偏移量数据
//     *
//     * @param legendUrlMap
//     * @param templateId
//     * @param requestSystemGraphDTO
//     */
//    private void putLegendDescMap(Map<String, GraphLegendVo> legendUrlMap, String templateId, RequestSystemGraphDTO requestSystemGraphDTO, String graphTypeCode,
//                                  String classCode) {
//        //查询模板详情，取模板实例id和小附件信息
//        GraphTemplateLegendSelectDto dto = new GraphTemplateLegendSelectDto();
//        dto.setTemplateOriId(templateId);
//        dto.setGraphTypeCode(graphTypeCode);
//        dto.setReferenceObjectTypeCode(classCode);
//        GraphTemplateDto graphTemplateDto;
//        try {
//            CustomerResponse<GraphTemplateDto> response = dtpRelationalGraphClient.loadTemplate(dto);
//            graphTemplateDto = CustomerResponse.handleResult(response);
//
//        } catch (Exception e) {
//            String errorMessage = LocaleMessageUtil.getMessage("key_4f87bcffbf2f4b75a4e764822f3af3b8") + graphTypeCode + ",读取模板ID为 " + templateId + " 的详情信息失败";
//            throw new ApplicationException(ResponseCode.USER_ERROR_A0400.getCode(), errorMessage, "查询模板失败");
//        }
//        List<String> templateLegendIds = graphTemplateDto.getTemplateContent().getTemplateLegendList()
//                .stream()
//                .map(TemplateLegendDto::getLegendOriId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<String> attachOriIds = new LinkedList<>();
//        graphTemplateDto.getTemplateContent().getAttachs().forEach(it -> {
//            it.getAttachNodes().forEach(attach -> attachOriIds.add(attach.getLegendOriId()));
//        });
//
//
//        if (CollectionUtils.isNotEmpty(attachOriIds)) {
//            templateLegendIds.addAll(attachOriIds);
//        }
//
//
//        //装配锚点偏移量数据
//        JSONObject legendDescMap = requestSystemGraphDTO.getLegendDescMap() == null ? new JSONObject() : requestSystemGraphDTO.getLegendDescMap();
//
//
//        templateLegendIds.forEach(templateLegendId -> {
//            if (!legendUrlMap.containsKey(templateLegendId)) {
//                return;
//            }
//            //如果已存相关数据，需获取已存数据
//            JSONObject templateLegend = new JSONObject();
//            if (legendDescMap.containsKey(templateLegendId)) {
//                templateLegend = legendDescMap.getJSONObject(templateLegendId);
//            }
//
//            GraphLegendVo graphLegend = legendUrlMap.get(templateLegendId);
//            JSONObject anchorOffset = new JSONObject();
//            if (CollectionUtils.isEmpty(graphLegend.getAnchors())) {
//                return;
//            }
//            graphLegend.getAnchors().forEach(legendAnchor -> {
//                CoordinateDto coordinateDto = legendAnchor.getCoordinateDto();
//                JSONObject coordinates = new JSONObject();
//                coordinates.put("x", coordinateDto.getX());
//                coordinates.put("y", coordinateDto.getY());
//                anchorOffset.put(legendAnchor.getCode(), coordinates);
//            });
//            //优先使用定制保存的数据
//            if (!templateLegend.containsKey("anchorOffset")) {
//                templateLegend.put("anchorOffset", anchorOffset);
//            }
//            if (!templateLegend.containsKey("hidden")) {
//                templateLegend.put("hidden", false);
//            }
//            //以meoi资源库修改的偏移量为准
//            legendDescMap.put(templateLegendId, templateLegend);
//        });
//
//        requestSystemGraphDTO.setLegendDescMap(legendDescMap);
//
//    }

    public void buildIconUrlVos(GraphLegendVo vo, Map<String, List<GraphIconVo>> iconMap) {
        String legendId = vo.getLegendId();
        List<GraphIconVo> icons = iconMap.get(legendId);
        if (CollectionUtils.isNotEmpty(icons)) {
            vo.setIconVoList(icons);
        }
    }

    /**
     * 获取图形名称映射
     *
     * @param classCode   类代码
     * @param graphConfig 图配置
     * @param objectId    对象id
     * @return {@link String}
     */
    @Override
    public String getGraphName(String classCode, String graphTypeCode, List<GraphConfigVo> graphConfig, String objectId) {
        String graphName = null;
        Map<String, List<GraphConfigVo>> groupByMap = graphConfig.stream().collect(Collectors.groupingBy(config -> config.getDiagramBaseObjectCode() + config.getDiagramTypeOfTheDiagram()));
        List<GraphConfigVo> graphConfigVos = groupByMap.get(classCode + graphTypeCode);
        if (CollectionUtils.isEmpty(graphConfigVos)) {
            return null;
        }
        if (StringUtils.isEmpty(objectId) || graphConfigVos.size() == 1) {
            graphName = graphConfigVos.get(0).getMapTitle();
        } else {
            for (GraphConfigVo graphConfigVo : graphConfigVos) {
                DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
                param.setId(objectId);
                JSONObject systemJudgmentCondition = graphConfigVo.getSystemJudgmentCondition();
                param.setInfoCodes(systemJudgmentCondition);
                DtpRwdObjectDTO rwdObject = dtpRwdClientService.getObject(param);
                if (rwdObject != null) {
                    graphName = graphConfigVo.getMapTitle();
                    break;
                }
            }

        }
        return graphName;
    }

    private void matchAreaStyle(SystemGraphInstanceVo result) {
        //查询区域样式配置
//        List<AreaStyleConfigVo> areaStyleConfigVos = areaStyleConfigVos();
//        try {
//            areaStyleConfigVos().stream().collect(Collectors.toMap(AreaStyleConfigVo::get))
//        }
        List<AreaVo> areaVos = BeanUtil.copyToList(result.getAreas(), AreaVo.class);
        areaVos.forEach(it -> {
            //text转为textmap
            Map<String, JSONObject> textMap = new HashMap<>();
            textMap.put(it.getText().getString("textCode"), it.getText());
            it.setTextMap(textMap);
            if (Objects.equals(it.getType(), "floor")) {
                it.setStyleCode("Area002");
            } else if (Objects.equals(it.getType(), "shaft")) {
                it.setStyleCode("Area003");
            }
        });
        result.setAreaVos(areaVos);
        result.setAreas(null);
    }

    /**
     * 构建信息点列表
     *
     * @param vo                     vo
     * @param graphName              图形名称
     * @param objectRuleMap          对象规则图
     * @param mapListInfoCodeConfigs 信息点配置map
     * @return boolean 是否展示信息板，true 展示，false 不展示
     */
    @Override
    public boolean buildPanelNodeInformationList(EquipmentNodeVo vo, String graphName, Map<String, Set<String>> objectRuleMap, Map<String, List<RcInfoCodeCfg>> mapListInfoCodeConfigs) {
        PanelNodeDTO panelNode = vo.getPanelNode();

        if (panelNode == null || MapUtils.isEmpty(objectRuleMap)) {
            return false;
        }
        JSONObject dataObject = vo.getDataObject();
        if (dataObject.containsKey("items")) {
            dataObject = dataObject.getJSONArray("items").getJSONObject(0);
        }
        String classCode = dataObject.getString("classCode");
        if (StringUtils.isBlank(classCode)) {
            return false;
        }
        List<RcInfoCodeCfg> infoCodeConfigs = mapListInfoCodeConfigs.get(classCode);
        Set<String> infoCodeSet = objectRuleMap.get(dataObject.getString("id"));
        if (CollectionUtils.isEmpty(infoCodeSet) || CollectionUtils.isEmpty(infoCodeConfigs)) {
            return false;
        }
        List<RcInfoCodeCfg> sortInfoConfigs = infoCodeConfigs.stream().map(e -> {
                    RcInfoCodeCfg newE = new RcInfoCodeCfg();
                    BeanUtils.copyProperties(e, newE);
                    return newE;
                }).filter(e -> StringUtils.isNotEmpty(e.getImportanceLevel())
                        && !e.getImportanceLevel().equals("0")
                        && infoCodeSet.contains(e.getInfoCode()))
                .sorted(Comparator.comparing(RcInfoCodeCfg::getImportanceLevel).thenComparing(RcInfoCodeCfg::getInfoCode)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sortInfoConfigs)) {
            return false;
        }
        //如果总条数少于10条，直接放入
        if (sortInfoConfigs.size() < 10) {
            panelNode.setInfoCodes(sortInfoConfigs);
            return true;
        }
        //没有聚合的infoCode
        Map<String, List<RcInfoCodeCfg>> importanceLevelMap = sortInfoConfigs.stream().collect(Collectors.groupingBy(RcInfoCodeCfg::getImportanceLevel, LinkedHashMap::new, Collectors.toList()));
        if (importanceLevelMap.size() < 10) {
            panelNode.setInfoCodes(sortInfoConfigs);
        } else {
            //importanceLevelMap 取前10个，然后合并成一个列表
            List<RcInfoCodeCfg> collect = importanceLevelMap.keySet().stream().limit(10).map(importanceLevelMap::get)
                    .flatMap(List::stream).sorted(Comparator.comparing(RcInfoCodeCfg::getImportanceLevel).thenComparing(RcInfoCodeCfg::getInfoCode))
                    .collect(Collectors.toList());
            panelNode.setInfoCodes(collect);
        }

        return true;
    }


    /**
     * 构建交互式配置
     *
     * @param vo                vo
     * @param interactiveConfig 交互配置
     */
    @Override
    public void buildInteractiveConfig(EquipmentNodeVo vo, String graphName, List<InteractiveConfigVo> interactiveConfig) {
        JSONObject dataObject = vo.getDataObject();
        if (dataObject.containsKey("items")) {
            dataObject = dataObject.getJSONArray("items").getJSONObject(0);
        }
        String classCode = dataObject.getString("classCode");
        if (StringUtils.isBlank(classCode)) {
            return;
        }
        Optional<InteractiveConfigVo> first = interactiveConfig.stream().filter(it -> it.getInteractiveObjectCode().equals(classCode)
                && it.getMapTitle().equals(graphName)).findFirst();
        if (first.isPresent()) {
            InteractiveConfigVo interactiveConfigVo = new InteractiveConfigVo();
            BeanUtils.copyProperties(first.get(), interactiveConfigVo);
            String relation = interactiveConfigVo.getRelation();
            String limitingCondition = interactiveConfigVo.getLimitingCondition();
            if (StringUtils.isNotBlank(limitingCondition)) {
                JSONObject limitingConditionJson = JSONObject.parseObject(limitingCondition);
                for (Map.Entry<String, Object> entry : limitingConditionJson.entrySet()) {
                    if (!dataObject.containsKey(entry.getKey())) {
                        //如果对象中不包含该字段，则不生成交互配置
                        return;
                    }
                    if (ObjectUtil.notEqual(entry.getValue(), dataObject.get(entry.getKey()))) {
                        //如果对象中包含该字段，但是值不一致，则不生成交互配置
                        return;
                    }
                }
            }
            //直接跳转
            if ("direct".equalsIgnoreCase(relation)) {
                generateInteractiveConfig(vo, vo.getDataObjectId(), classCode, interactiveConfigVo);
                return;
            }
            //任意跳转
            if ("any".equalsIgnoreCase(relation)) {
                List<GraphConfigVo> graphConfig = commonConfigService.getGraphConfig();
                Map<String, GraphConfigVo> graphnNameMap = graphConfig.stream()
                        .collect(Collectors.toMap(GraphConfigVo::getMapTitle, Function.identity(), (v1, v2) -> v1));
                GraphConfigVo graphConfigVo = graphnNameMap.get(graphName);
                if (graphConfigVo == null) {
                    return;
                }
                String objectType = graphConfigVo.getObjectType();
                if ("SY".equals(objectType)) {
                    String diagramBaseObjectCode = graphConfigVo.getDiagramBaseObjectCode();
                    DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setClassCode(diagramBaseObjectCode);
                    List<DtpRwdObjectDTO> dtpRwdObjectDTOs = dtpRwdClientService.listObject(param);
                    if (CollectionUtil.isNotEmpty(dtpRwdObjectDTOs)) {
                        for (DtpRwdObjectDTO rwdObject : dtpRwdObjectDTOs) {
                            generateInteractiveConfig(vo, rwdObject.getId(), rwdObject.getClassCode(), interactiveConfigVo);
                            //如果找到一个可跳转的跳转对象，则跳出循环
                            if (vo.getInteractiveConfigVo().isCanJump()) {
                                return;
                            }
                        }

                    }
                    return;
                }
                if ("PJ".equals(objectType)) {
                    generateInteractiveConfig(vo, ContextUtil.getProjectId(), graphConfigVo.getDiagramBaseObjectCode(), interactiveConfigVo);
                    return;
                }
                if ("EQ".equals(objectType)) {
                    generateInteractiveConfig(vo, vo.getDataObjectId(), classCode, interactiveConfigVo);
                    return;
                }
                return;
            }
            //按系统关系跳转
            DirectRelationDTO directRelationDTO = ObjTypeUtil.parseRule(relation);
            if (Objects.isNull(directRelationDTO)) {
                return;
            }
            DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam().setClassCode(directRelationDTO.getToClassCode()).addWithColumnWithSimple();
            List<DtpRwdObjectDTO> rwdObjectDTOS = dtpRwdClientService.listObject(param);
            List<String> systemIds = rwdObjectDTOS.stream().map(DtpRwdObjectDTO::getId).collect(Collectors.toList());
            List<DtpRwdObjectDTO> list = queryRelation(vo.getDataObjectId(), directRelationDTO, directRelationDTO.getFromClassCode(), systemIds);
            if (CollectionUtil.isEmpty(list)) {
                return;
            }
            DtpRwdObjectDTO rwdObject = list.get(0);
            Set<String> relObjIds = rwdObject.getRelObjIds();
            if (CollectionUtil.isEmpty(relObjIds)) {
                return;
            }
            for (String relId : relObjIds) {
                generateInteractiveConfig(vo, relId, directRelationDTO.getToClassCode(), interactiveConfigVo);
                //如果找到一个可跳转的跳转对象，则跳出循环
                if (vo.getInteractiveConfigVo().isCanJump()) {
                    return;
                }
            }
        }
    }

    private List<DtpRwdObjectDTO> queryRelation(String id, DirectRelationDTO relationDTO, String classCode, Collection<String> objFrom) {
        DtpRwdObjectQueryParam param = new DtpRwdObjectQueryParam();
        if (StringUtils.isNotBlank(id)) {
            param.setId(Lists.newArrayList(id));
        }
        param.setClassCode(classCode);
        DtpRwdObjectRelationQueryParam relationTo = new DtpRwdObjectRelationQueryParam();
        relationTo.setObjFrom(objFrom);
        relationTo.setGraphCode(relationDTO.getGraphCode());
        relationTo.setRelCode(relationDTO.getRelCode());
        param.setRelationTo(relationTo);

        return dtpRwdClientService.listObject(param);
    }

    @Override
    public void generateInteractiveConfig(EquipmentNodeVo vo, String dataObjectId, String classCode, InteractiveConfigVo interactiveConfigVo) {
        interactiveConfigVo.setSystemObjectId(dataObjectId);
        SystemGraph pos = this.getSystemGraph(dataObjectId,
                classCode, interactiveConfigVo.getGraphTypeCode());
        //没有副本id说明未发布，则不跳转
        if (pos != null && pos.getSystemGraphDuplicateId() == null) {
            pos = null;
        }
        interactiveConfigVo.setCanJump(pos != null);
        interactiveConfigVo.setGraphName(pos != null ? pos.getGraphName() : "");
        interactiveConfigVo.setInteractiveObjectCode(classCode);
        vo.setInteractiveConfigVo(interactiveConfigVo);
    }

    @Override
    public void matchPipeLineStyle(SystemGraphInstanceVo result) {

        List<LineDto> lineDtoList = result.getLines();
        Map<String, PipelineRelationConfigVo> graphRelMap;
        Map<String, PipelineRelationConfigVo> riserClassCodeMap;
        List<PipelineRelationConfigVo> pipelineRelationConfigVos = commonConfigService.pipelineRelationConfigVos();

        try {
            riserClassCodeMap = pipelineRelationConfigVos
                    .stream()
                    .filter(it -> StringUtils.isNotBlank(it.getPipelineCode()) && Objects.equals(it.getRiserMark(), LocaleMessageUtil.getMessage("key_42bb8d323c484d7fa6be700b8d5f55fd")))
                    .collect(Collectors.toMap(PipelineRelationConfigVo::getPipelineCode, it -> it));
        } catch (DuplicateKeyException e) {
            logProjectInstance().warn("管道关系颜色映射配置错误，管道类编码不能重复。" + e);
            return;
        }

        graphRelMap = getGraphRelMap();
        if (graphRelMap == null) {
            return;
        }
        List<LineVo> lineVos;
        if (CollectionUtils.isNotEmpty(lineDtoList)) {
            lineVos = BeanUtil.copyToList(lineDtoList, LineVo.class);
        } else {
            lineVos = result.getLineVos();
        }

        if (CollectionUtils.isEmpty(lineVos)) {
            return;
        }

        lineVos.forEach(lineVo -> {

            String key = lineVo.getDataObject().getGraphCode() + UNDERLINE + lineVo.getDataObject().getRelCode();
            if (graphRelMap.containsKey(key)) {
                PipelineRelationConfigVo pipelineRelationConfigVo = graphRelMap.get(key);
                lineVo.setPipelineStyleCode(pipelineRelationConfigVo.getPipelineStyleCode());
                lineVo.setLineName(pipelineRelationConfigVo.getPipeName());
            }
        });

        List<MainPipeDto> mainPipes = result.getMainPipes();
        if (CollectionUtils.isNotEmpty(mainPipes)) {
            List<MainPipeVo> mainPipeVos = BeanUtil.copyToList(mainPipes, MainPipeVo.class);
            mainPipeVos.forEach(mainPipeVo -> {
                //只配置母线（Bus）和整根立管(RiserWhole)的样式信息
                if (Objects.equals("Bus", mainPipeVo.getLineType()) || Objects.equals("RiserWhole", mainPipeVo.getLineType())) {
                    //获取对象类信息
                    if (CollectionUtils.isNotEmpty(mainPipeVo.getEquipmentTypes())) {
                        mainPipeVo.getEquipmentTypes().forEach(classCode -> {
                            if (riserClassCodeMap.containsKey(classCode)) {
                                mainPipeVo.setPipelineStyleCode(riserClassCodeMap.get(classCode).getPipelineStyleCode());

                            }
                        });

                    }
                }

            });

            result.setMainPipeVos(mainPipeVos);
//            result.setMainPipes(null);
        }

        result.setLineVos(lineVos);
        result.setLines(null);

    }

    @Nullable
    private Map<String, PipelineRelationConfigVo> getGraphRelMap() {
        List<PipelineRelationConfigVo> pipelineRelationConfigVos = commonConfigService.pipelineRelationConfigVos();
        Map<String, PipelineRelationConfigVo> graphRelMap;
        try {
            graphRelMap = pipelineRelationConfigVos
                    .stream()
                    .filter(it -> StringUtils.isNotBlank(it.getRelCode()) && StringUtils.isNotBlank(it.getGraphCode()))
                    .collect(Collectors.toMap(it -> it.getGraphCode() + UNDERLINE + it.getRelCode(), it -> it));
        } catch (DuplicateKeyException e) {
            logProjectInstance().warn("管道关系颜色映射配置错误，边类型和图类型编码不能完全一致。" + e);
            return null;
        }
        return graphRelMap;
    }

    /**
     * 标记管道上所有线路节点的向量
     * 前提条件为两个点连成的线段必须横平竖直
     *
     * @param now              当前点
     * @param last             终点
     * @param corrXLineNodeMap xMap
     * @param corrYLineNodeMap yMap
     */
    private void getLineNodeVector(CoordinateDto now, CoordinateDto last, Map<Integer, List<LineNode>> corrXLineNodeMap,
                                   Map<Integer, List<LineNode>> corrYLineNodeMap, String pipelineStyleCode) {
        //判断管道方向是水平还是垂直
        if (Objects.equals(now.getX(), last.getX())) {
            if (!corrXLineNodeMap.containsKey(now.getX())) {
                return;
            }
            List<LineNode> lineNodes = corrXLineNodeMap.get(now.getX());
            //标记在两点之间所有线路节点的向量
            lineNodes.forEach(lineNode -> lineNode.changeLineMarkY(now.getY(), last.getY(), pipelineStyleCode));
        } else if (Objects.equals(now.getY(), last.getY())) {
            if (!corrYLineNodeMap.containsKey(now.getY())) {
                return;
            }
            List<LineNode> lineNodes = corrYLineNodeMap.get(now.getY());
            //标记在两点之间所有线路节点的向量
            lineNodes.forEach(lineNode -> lineNode.changeLineMarkX(now.getX(), last.getX(), pipelineStyleCode));
        } else {
            logProjectInstance().warn("系统图线路错误，点：" + now + "和点：" + last + "之间连线为斜线");
        }

    }

    /**
     * 创建弯管节点数据
     *
     * @param secondLast 坐标
     * @param last       最后
     * @param now        当前
     * @param lineVo     线
     * @return LineNode
     */
    private LineNode getBendLineNode(CoordinateDto secondLast, CoordinateDto last, CoordinateDto now, LineVo lineVo) {
        if (secondLast == null) {
            return null;
        }
        LineNode lineNode = new LineNode();
        //前一个节点为拐点
        lineNode.setCoordinateDto(last);

        //对比坐标不同的值
        if (getDirection(secondLast, last, lineVo, lineNode)) {
            return null;
        }

        if (getDirection(last, now, lineVo, lineNode)) {
            return null;
        }


        return lineNode;
    }

    private boolean getDirection(CoordinateDto last, CoordinateDto now, LineVo lineVo, LineNode lineNode) {
        if (!now.getX().equals(last.getX()) && now.getY().equals(last.getY())) {
            lineNode.changeLineMarkX(now.getX(), last.getX(), lineVo.getPipelineStyleCode());
        } else if (!now.getY().equals(last.getY()) && now.getX().equals(last.getX())) {
            lineNode.changeLineMarkY(now.getY(), last.getY(), lineVo.getPipelineStyleCode());
        } else {
            return true;
        }
        return false;
    }

    /**
     * 存储系统图
     *
     * @param graphName              图形名称
     * @param graphTypeCode          图形类型代码
     * @param classCode              类代码
     * @param systemObjectId         系统对象id
     * @param systemGraphDuplicateId 系统图重复id
     * @return {@link SystemGraph }
     */
    @Override
    public SystemGraph saveSystemGraph(String graphName, String graphTypeCode, String classCode, String systemObjectId, Long systemGraphDuplicateId) {
        SystemGraph systemGraph = new SystemGraph();
        systemGraph.setGraphName(graphName);
        systemGraph.setGraphTypeCode(graphTypeCode);
        systemGraph.setClassCode(classCode);
        systemGraph.setSystemGraphDuplicateId(systemGraphDuplicateId);
        systemGraph.setSystemObjectId(systemObjectId);
        systemGraph.setProjectId(ContextUtil.getProjectId());
        systemGraph.setStatus(true);
        systemGraph.setPassBy(true);
        save(systemGraph);
        return systemGraph;
    }

    /**
     * 获取系统图
     *
     * @param systemObjectId 基准对象id
     * @param classCode      设备类
     * @param graphTypeCode  图类型
     * @return {@code SystemGraph }
     */
    @Override
    public SystemGraph getSystemGraph(String systemObjectId, String classCode, String graphTypeCode) {
        if (StringUtils.isBlank(systemObjectId)) {
            return null;
        }
        List<SystemGraph> list = lambdaQuery()
                .eq(SystemGraph::getSystemObjectId, systemObjectId)
                .eq(SystemGraph::getStatus, true)
                .eq(SystemGraph::getPassBy, true)
                .eq(SystemGraph::getClassCode, classCode)
                .eq(SystemGraph::getProjectId, ContextUtil.getProjectId())
                .eq(StringUtils.isNotBlank(graphTypeCode), SystemGraph::getGraphTypeCode, graphTypeCode)
                .select(SystemGraph::getId, SystemGraph::getGraphName, SystemGraph::getSystemGraphDuplicateId,
                        SystemGraph::getSystemObjectId, SystemGraph::getGraphTypeCode)
                .list();
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<SystemGraph> listSystemGraph(String systemObjectId, String classCode) {
        if (StringUtils.isBlank(systemObjectId)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .eq(SystemGraph::getSystemObjectId, systemObjectId)
                .eq(SystemGraph::getStatus, true)
                .eq(SystemGraph::getPassBy, true)
                .eq(SystemGraph::getClassCode, classCode)
                .eq(SystemGraph::getProjectId, ContextUtil.getProjectId())
                .select(SystemGraph::getId, SystemGraph::getGraphName, SystemGraph::getSystemGraphDuplicateId,
                        SystemGraph::getSystemObjectId, SystemGraph::getGraphTypeCode)
                .list();
    }

    /**
     * 系统图列表
     *
     * @param systemObjectId 系统对象id
     * @param classCodes     类代码
     * @param graphTypeCodes 图类型
     * @return {@code List<SystemGraph> }
     * @since 7.250501.0
     */
    @Override
    public List<SystemGraph> listSystemGraph(String systemObjectId, Collection<String> classCodes, Collection<String> graphTypeCodes) {
        return lambdaQuery()
                .eq(StringUtils.isNotBlank(systemObjectId), SystemGraph::getSystemObjectId, systemObjectId)
                .eq(SystemGraph::getStatus, true)
                .eq(SystemGraph::getPassBy, true)
                .in(CollectionUtils.isNotEmpty(classCodes), SystemGraph::getClassCode, classCodes)
                .in(CollectionUtils.isNotEmpty(graphTypeCodes), SystemGraph::getGraphTypeCode, graphTypeCodes)
                .eq(SystemGraph::getProjectId, ContextUtil.getProjectId())
                .select(SystemGraph::getId, SystemGraph::getGraphName, SystemGraph::getSystemGraphDuplicateId,
                        SystemGraph::getSystemObjectId, SystemGraph::getGraphTypeCode)
                .list();
    }

    @Override
    public boolean updateSystemGraphDuplicateIdById(Long id, Long systemGraphDuplicateId, String graphName) {
        SystemGraph systemGraph = new SystemGraph();
        systemGraph.setId(id);
        systemGraph.setGraphName(graphName);
        systemGraph.setSystemGraphDuplicateId(systemGraphDuplicateId);
        return updateById(systemGraph);
    }

    @Override
    public SystemGraph getSystemGraphByDuplicateId(Long id) {
        return lambdaQuery().eq(SystemGraph::getSystemGraphDuplicateId, id).one();
    }

    @Override
    public boolean updateSystemGraphName(Long id, String graphName) {
        SystemGraph graph = new SystemGraph();
        graph.setId(id);
        graph.setGraphName(graphName);
        return updateById(graph);
    }

    @Override
    public void buildDrawerAllInfoCodes(EquipmentNodeVo vo, Map<String, Set<String>> objectRuleMap, Map<String, List<RcInfoCodeCfg>> mapListInfoCodeConfigs) {
        String classCode = vo.getObjClassCode();
        //目前只有低压配电抽屉有可变信息点
        if (StringUtils.isBlank(classCode) || !DtpRwdClassCodeEnum.SETDDD.getValue().equals(classCode)) {
            return;
        }
        List<RcInfoCodeCfg> infoCodeConfigs = mapListInfoCodeConfigs.getOrDefault(classCode, new ArrayList<>());
        Set<String> infoCodeSet = objectRuleMap.getOrDefault(vo.getDataObjectId(), new HashSet<>());
        List<RcInfoCodeCfg> sortInfoConfigs = infoCodeConfigs.stream().map(e -> {
                    RcInfoCodeCfg newE = new RcInfoCodeCfg();
                    BeanUtils.copyProperties(e, newE);
                    return newE;
                }).filter(e -> StringUtils.isNotEmpty(e.getImportanceLevel())
                        && !e.getImportanceLevel().equals("0")
                        && infoCodeSet.contains(e.getInfoCode()))
                .sorted(Comparator.comparing(RcInfoCodeCfg::getImportanceLevel).thenComparing(RcInfoCodeCfg::getInfoCode)).collect(Collectors.toCollection(LinkedList::new));
        if (CollectionUtils.isEmpty(sortInfoConfigs)) {
            return;
        }
        vo.setDrawerAllInfoCodes(sortInfoConfigs);
    }

    /**
     * 在系统图中获取所有设备
     *
     * @param instanceVo 系统图实例vo
     * @return {@code Set<String> }
     * @since 7.250501.0
     */
    @Override
    public Set<String> getAllEquipmentInSystemGraph(SystemGraphInstanceVo instanceVo) {
        List<EquipmentNodeVo> equipmentNodeVos = instanceVo.getEquipmentNodeVos();
        List<EquipmentNodeVo> passByVos = instanceVo.getPassByVos();
        List<GraphGroupVo> graphGroupVos = instanceVo.getGroupVos();
        Set<String> allObjectIds = new HashSet<>();
        if (CollectionUtils.isNotEmpty(equipmentNodeVos)) {
            equipmentNodeVos.stream().filter(e -> Objects.nonNull(e.getDataObject())).forEach(e -> {
                if (StringUtils.equals(e.getDataObject().getString("compType"), "packNode")) {
                    List<JSONObject> items = e.getDataObject().getJSONArray("items").toJavaList(JSONObject.class);
                    allObjectIds.addAll(items.stream().map(item -> item.getString("id")).collect(Collectors.toSet()));
                } else {
                    allObjectIds.add(e.getDataObjectId());
                }
            });
        }
        if (CollectionUtils.isNotEmpty(passByVos)) {
            allObjectIds.addAll(passByVos.stream().map(EquipmentNodeDto::getDataObjectId).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        if (CollectionUtils.isNotEmpty(graphGroupVos)) {
            allObjectIds.addAll(graphGroupVos.stream().map(GraphGroupDto::getDataObjectId).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        return allObjectIds;
    }

    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 5.240501.0
     */
    @Override
    public String logTag() {
        return "关系图Service";
    }
}




