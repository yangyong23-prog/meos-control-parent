package com.persagy.apccontrol.common.logtrace.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.common.logtrace.assembler.TreeAssembler;
import com.persagy.apccontrol.common.logtrace.entity.LogGroupTemplateEntity;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateGroupRelationMapper;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateGroupRelationRepository;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateGroupRepository;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateRepository;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupRelationPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LogQueryTemplateGroupService {

    @Autowired
    private LogQueryTemplateGroupRepository templateGroupRepository;

    @Autowired
    private LogQueryTemplateGroupRelationRepository relationRepository;

    @Autowired
    private LogQueryTemplateGroupRelationMapper relationMapper;

    @Autowired
    private LogQueryTemplateRepository queryTemplateRepository;
    @Autowired
    private LogQueryTemplateService logQueryTemplateService;

    @Autowired
    private ObjectMapper objectMapper;

    public List<LogQueryTemplateGroupRelationPo> getTreeByParentId(Set<Long> parentIds, Set<Long> treeIds) {
        treeIds.addAll(parentIds);
        List<LogQueryTemplateGroupRelationPo> children = relationMapper.selectList(Wrappers.<LogQueryTemplateGroupRelationPo>lambdaQuery()
                .in(LogQueryTemplateGroupRelationPo::getGroupParentId, parentIds));
        for (LogQueryTemplateGroupRelationPo child : children) {
            treeIds.add(child.getGroupId());
            Set<Long> parentIdsSet = new HashSet<>();
            parentIdsSet.add(child.getGroupId());
            child.setChildren(getTreeByParentId(parentIdsSet, treeIds));
        }
        return children;
    }

    public Collection<LogQueryTemplateGroupPo> getAllQueryGroupByApplication(String application) {

        List<LogQueryTemplateGroupPo> result = new ArrayList<LogQueryTemplateGroupPo>();
        if (!StringUtils.hasLength(application)) {
            return result;
        }
        List<LogQueryTemplateGroupPo> groups = StringUtils.hasLength(application)?templateGroupRepository.queryAllTopGroupByApp(application)
                :templateGroupRepository.queryAllTopGroup();
        if (CollectionUtils.isEmpty(groups)) {
            return result;
        }
        Set<Long> topGroupIds = groups.stream().map(p -> p.getId()).collect(Collectors.toSet());
        Set<Long> allQuery = new HashSet<>();
        allQuery.addAll(topGroupIds);
        List<LogQueryTemplateGroupRelationPo> relationPos = getTreeByParentId(topGroupIds, allQuery);
        List<LogQueryTemplateGroupPo> groupPos = templateGroupRepository.getLogQueryGroupByIds(allQuery);
        Map<Long, LogQueryTemplateGroupPo> groupMap = groupPos.stream().collect(Collectors.toMap(LogQueryTemplateGroupPo::getId, p -> p, (p1, p2) -> p1));
        if (!CollectionUtils.isEmpty(relationPos)) {
            TreeAssembler.setChildrenForGroups(groupMap, relationPos);
        }
        List<LogQueryTemplateGroupPo> resultTemp = new ArrayList<LogQueryTemplateGroupPo>();

//  返回顶节点的group
        groupMap.forEach((key, value) -> {
            if (topGroupIds.contains(key)) {
                resultTemp.add(value);
            }
        });
       result = resultTemp.stream().sorted(Comparator.comparing(LogQueryTemplateGroupPo::getOrderNum))
                .collect(Collectors.toList());
        return result;
    }

    public Collection<LogGroupTemplateEntity> getAllQueryGroupByTemp(String application) {

        List<LogGroupTemplateEntity> result = new ArrayList<LogGroupTemplateEntity>();
        List<LogQueryTemplateGroupPo> groups = StringUtils.hasLength(application)?templateGroupRepository.queryAllTopGroupByApp(application)
                :templateGroupRepository.queryAllTopGroup();
        if (CollectionUtils.isEmpty(groups)) {
            return result;
        }


        Set<Long> topGroupIds = groups.stream().map(p -> p.getId()).collect(Collectors.toSet());
        Set<Long> allQuery = new HashSet<>();
        allQuery.addAll(topGroupIds);

        //查询每个group下的模板
        List<LogQueryTemplateGroupPo> allGroup = templateGroupRepository.queryAllGroup(application);
        if(CollectionUtils.isEmpty(allGroup)){
            return result;
        }
        Map<Long,Collection<LogGroupTemplateEntity>> groupToTemplate = new HashMap<>();
        allGroup.forEach(group->{
            Collection<LogGroupTemplateEntity> singleGroupTemplate =  logQueryTemplateService.getQueryTemplateByGroupIdAndApp(group.getId(),application);
            if(!CollectionUtils.isEmpty(singleGroupTemplate)){
                groupToTemplate.put(group.getId(),singleGroupTemplate);
            }
        });
//        List<LogQueryTemplatePo> templatePos =  queryTemplateRepository.queryAllQueryTemplate(null,application);
//        if(CollectionUtils.isEmpty(templatePos)){
//            templatePos = new ArrayList<>();
//        }

        //过滤有分组的模板
//        List<LogQueryTemplatePo> groupTemplates = templatePos.stream().filter(p->p.getGroupId()!=null).collect(Collectors.toList());
//        Map<Long,Long> templateToGroupMap = null;
//        if(!CollectionUtils.isEmpty(templatePos)){
//            templateToGroupMap = groupTemplates.stream().collect(Collectors.toMap(LogQueryTemplatePo::getId,p->p.getGroupId(),(p1,p2)->p1));
//        }
        List<LogQueryTemplateGroupRelationPo> relationPos = getTreeByParentId(topGroupIds, allQuery);
        List<LogQueryTemplateGroupPo> groupPos = templateGroupRepository.getLogQueryGroupByIds(allQuery);
        Map<Long, LogGroupTemplateEntity> groupMap = groupPos.stream().map(p ->{
            LogGroupTemplateEntity entity = new LogGroupTemplateEntity();
            entity.setId(p.getId());
            entity.setType(1);
            entity.setName(p.getGroupName());
            entity.setOrderNum(p.getOrderNum());
            return entity;
        }).collect(Collectors.toMap(LogGroupTemplateEntity::getId, p -> p, (p1, p2) -> p1));

        //Map<Long, LogQueryTemplateGroupPo> groupMap = groupPos.stream().collect(Collectors.toMap(LogQueryTemplateGroupPo::getId, p -> p, (p1, p2) -> p1));
        if (!CollectionUtils.isEmpty(relationPos)) {
            TreeAssembler.setChildrenForLogGroupEntity(groupMap, relationPos);
        }
        List<LogGroupTemplateEntity> resultTemp = new ArrayList<LogGroupTemplateEntity>();

//  返回顶节点的group
        groupMap.forEach((key, value) -> {
            if (topGroupIds.contains(key)) {
                resultTemp.add(value);
            }
        });
        result = resultTemp.stream().sorted(Comparator.comparing(LogGroupTemplateEntity::getOrderNum))
                .collect(Collectors.toList());
        if(!groupToTemplate.isEmpty()){
            result.forEach(p->{
                traverseTree(p,groupToTemplate);
            });
        }
        return result;
    }

    /**
     * 将模板放到分组下的树结构中
     * @param node
     * @param groupToTemplate
     */
    public  void traverseTree(LogGroupTemplateEntity node,Map<Long,Collection<LogGroupTemplateEntity>> groupToTemplate) {
        if (node != null) {
            // 访问当前节点
//            System.out.println("ID: " + node.getId() + ", Type: " + node.getType() + ", Order Num: " + node.getOrderNum() + ", Object: " + node.getObject());
            // 递归遍历子节点列表
            if(groupToTemplate.isEmpty()){
                return;
            }
            List<LogGroupTemplateEntity> childrenTemp = node.getChildren();
            if(childrenTemp==null){
                node.setChildren((List)groupToTemplate.get(node.getId()));
            }else {
                if(!CollectionUtils.isEmpty(groupToTemplate.get(node.getId()))){
                    childrenTemp.addAll(groupToTemplate.get(node.getId()));
                }
            }
            List<LogGroupTemplateEntity> children = node.getChildren();
            if (children != null) {
                for (LogGroupTemplateEntity child : children) {
                    traverseTree(child,groupToTemplate); // 递归调用
                }
            }
        }
    }

    public  void setChildrenForLogGroupTemplateEntity(Map<Long, LogGroupTemplateEntity> entityMap, List<LogQueryTemplateGroupRelationPo> relations) {
        for (LogQueryTemplateGroupRelationPo relation : relations) {
            LogGroupTemplateEntity group = entityMap.get(relation.getGroupId());
            LogGroupTemplateEntity parentGroup = entityMap.get(relation.getGroupParentId());
            if (group != null && parentGroup != null) {
                if (parentGroup.getChildren() == null) {
                    parentGroup.setChildren(new ArrayList<>());
                }
                parentGroup.getChildren().add(group);
                childOrder(parentGroup);
            }
            if (!CollectionUtils.isEmpty(relation.getChildren())) {
                setChildrenForLogGroupTemplateEntity(entityMap, relation.getChildren());
            }
        }
    }
    public void childOrder(LogGroupTemplateEntity group) {
        if (!CollectionUtils.isEmpty(group.getChildren())) {
            List<LogGroupTemplateEntity> children = group.getChildren().stream()
                    .sorted(Comparator.comparing(LogGroupTemplateEntity::getOrderNum))
                    .collect(Collectors.toList());
            group.setChildren(children);

        }
    }


    public static void traverseTreeRecursively(LogQueryTemplateGroupRelationPo root) {
        if (root != null) {
            // 递归遍历子节点
            List<LogQueryTemplateGroupRelationPo> children = root.getChildren();
            if (children != null) {
                for (LogQueryTemplateGroupRelationPo child : children) {
                    traverseTreeRecursively(child);
                }
            }
        }
    }



}
