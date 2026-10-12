package com.persagy.apccontrol.common.logtrace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.common.logtrace.assembler.LogNodeAssembler;
import com.persagy.apccontrol.common.logtrace.assembler.LogNodeExtendAssembler;
import com.persagy.apccontrol.common.logtrace.assembler.TreeAssembler;
import com.persagy.apccontrol.common.logtrace.dto.LogInfoAddDto;
import com.persagy.apccontrol.common.logtrace.dto.LogInfoDto;
import com.persagy.apccontrol.common.logtrace.entity.LogNodeEntity;
import com.persagy.apccontrol.common.logtrace.entity.LogNodeRelationEntity;
import com.persagy.apccontrol.common.logtrace.exception.BusinessNormalException;
import com.persagy.apccontrol.common.logtrace.persistence.LogNodeExtendRepository;
import com.persagy.apccontrol.common.logtrace.po.LogNodeExtendPo;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import com.persagy.apccontrol.common.logtrace.po.LogNodeRelationPo;
import com.persagy.apccontrol.common.logtrace.vo.LogInfoExceptionVo;
import com.persagy.apccontrol.common.logtrace.vo.LogNodeInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LogApplicationService {
    @Autowired
    private LogNodeAssembler logNodeAssembler;

    @Autowired
    private LogNodeExtendAssembler logNodeExtendAssembler;

    @Autowired
    private LogNodeService logNodeService;

    @Autowired
    private LogNodeRelationService relationService;

    @Autowired
    LogNodeExtendRepository logNodeExtendRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Transactional(rollbackFor = Exception.class)
    public List<LogInfoExceptionVo> saveLogInfo(LogInfoAddDto addDto) {
        if (!StringUtils.hasLength(addDto.getApplication())) {
            throw new BusinessNormalException("businessType is not  null");
        }
        List<LogInfoDto> infoList = addDto.getLogInfos();
        if (CollectionUtils.isEmpty(infoList))
            return new ArrayList<>();
        List<LogNodeEntity> logNodeEntities = infoList.stream().map(p -> {
                    LogNodeEntity entity = logNodeAssembler.nodeDtoToNodeEntities(p);
                    entity.setExtend(logNodeExtendAssembler.dtoToPoList(p.getData()));
                    entity.setApplication(addDto.getApplication());
                    entity.setCreateTime(LocalDateTime.now());
                    return entity;
                }
        ).collect(Collectors.toList());

        Set<String> noRepeatChildBusinessIds = new HashSet<>();
        Set<String> businessIds = new HashSet<>();
        infoList.forEach(p -> {
            businessIds.add(p.getBusinessId());
            if (!CollectionUtils.isEmpty(p.getBusinessChildIds())) {
                noRepeatChildBusinessIds.addAll(p.getBusinessChildIds());
            }
        });
        //去掉本次提交子节点对象就是本次提前的对象
        noRepeatChildBusinessIds.removeAll(businessIds);
        Map<String, Long> businessIdToNodeIdMap = logNodeService.batchSave(logNodeEntities);
        List<LogNodeExtendPo> allNodeExtends = new ArrayList<>();
        logNodeEntities.forEach(p -> {
            p.getExtend().forEach(po -> {
                po.setNodeId(businessIdToNodeIdMap.get(p.getBusinessId()));
                po.setBusinessTime(p.getBusinessTime());
                po.setNodeCode(p.getCode());
                po.setCreateTime(LocalDateTime.now());
                allNodeExtends.add(po);
            });
        });
        logNodeExtendRepository.saveBatch(allNodeExtends);
        //查询子节点对象 不在本次提交的节点
        Map<String, Long> childBusinessMap = logNodeService.queryByBusinessId(noRepeatChildBusinessIds, addDto.getApplication());

        //根据业务对象给出的子级对象找不到对应数据节点异常数据
        List<LogInfoExceptionVo> logInfoExceptionVos = new ArrayList<>();
        List<LogNodeRelationEntity> allRelationEntityList = new ArrayList<>();

        infoList.forEach(p -> {
            List<Long> childrenNodeIds = new ArrayList<>();
            if (!CollectionUtils.isEmpty(p.getBusinessChildIds())) {
                p.getBusinessChildIds().forEach(t -> {
                    //优先从本次提交中查询
                    Long nodeId = (businessIdToNodeIdMap.get(t) == null) ? childBusinessMap.get(t) : businessIdToNodeIdMap.get(t);
                    //单个业务对象下  子节点不存列表
                    List<String> noBusinessIdTemp = new ArrayList<>();
                    LogInfoExceptionVo vo = new LogInfoExceptionVo();
                    vo.setBusinessId(p.getBusinessId());
                    if (nodeId == null) {
                        noBusinessIdTemp.add(t);
                    } else {
                        childrenNodeIds.add(nodeId);
                    }
                    if (!CollectionUtils.isEmpty(noBusinessIdTemp)) {
                        vo.setBusinessChildIds(noBusinessIdTemp);
                        logInfoExceptionVos.add(vo);
                    }
                });
            }
            if (!CollectionUtils.isEmpty(childrenNodeIds)) {
                allRelationEntityList.addAll(builderNodeRelation(businessIdToNodeIdMap.get(p.getBusinessId()), childrenNodeIds));
            }
        });
        relationService.saveRelation(allRelationEntityList);
        return logInfoExceptionVos;

    }

    /**
     * 查询所有的数据顶节点
     *
     * @return
     */
    public List<LogNodeInfoVo> queryAllTopNode() {
        List<LogNodePo> res = logNodeService.queryAllTopNode();
        if (CollectionUtils.isEmpty(res)) {
            return new ArrayList<>();
        }
        return res.stream().map(p -> logNodeAssembler.nodePoToVo(p)).collect(Collectors.toList());
    }

    List<LogNodeRelationEntity> builderNodeRelation(Long nodeId, List<Long> childrenNodeIds) {
        if (nodeId == null || CollectionUtils.isEmpty(childrenNodeIds))
            return new ArrayList<>();
        List<LogNodeRelationEntity> relationEntityList = new ArrayList<>();
        childrenNodeIds.forEach(p -> {
            LogNodeRelationEntity relation = new LogNodeRelationEntity();
            relation.setNodeId(p);
            relation.setCreateTime(LocalDateTime.now());
            relation.setParentNodeId(nodeId);
            relationEntityList.add(relation);
        });
        return relationEntityList;
    }


    public LogNodePo getTreeByTopNodeId(Long topNodeId) {
        Set<Long> treeNodeIds = new HashSet<>();
        List<LogNodeRelationPo> relationPos = relationService.getTreeByParentId(topNodeId, treeNodeIds);
        List<LogNodePo> nodeInfos = logNodeService.getLogNodeByNodeIds(treeNodeIds);
        LogNodePo po = null;
        if (CollectionUtils.isNotEmpty(nodeInfos)) {
            Map<Long, LogNodePo> nodeMap = TreeAssembler.buildNodeMap(nodeInfos);
            TreeAssembler.setChildrenForNodes(nodeMap, relationPos);
            po = nodeMap.get(topNodeId);
        }
        return po;
        //return TreeAssembler.buildTree(relationPos, nodeInfos);

    }

    public Map<String, List<Long>>  getAllNodeByTopNodeId(Long topNodeId,Set<String>filterNodeCodes) {
        Set<Long> treeNodeIds = new HashSet<>();
        Map<String, List<Long>> mapResult = new HashMap<>();
        List<LogNodeRelationPo> relationPos = relationService.getTreeByParentId(topNodeId, treeNodeIds);
        List<LogNodePo> nodeInfos = logNodeService.getLogNodeByNodeIdsAndCodes(treeNodeIds,filterNodeCodes);
        if(CollectionUtils.isEmpty(nodeInfos)){
            return mapResult;
        }
        Map<String, List<Long>> nodeCodeToIdMap = nodeInfos.stream()
                .collect(Collectors.groupingBy(
                        LogNodePo::getCode, // 分组依据
                        Collectors.mapping(
                                LogNodePo::getId, // 映射到id
                                Collectors.toList() // 收集为List
                        )
                ));
       return nodeCodeToIdMap;

        //return TreeAssembler.buildTree(relationPos, nodeInfos);

    }




}
