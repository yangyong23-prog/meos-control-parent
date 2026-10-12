package com.persagy.apccontrol.common.logtrace.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persagy.apccontrol.common.logtrace.assembler.TreeAssembler;
import com.persagy.apccontrol.common.logtrace.entity.LogGroupTemplateEntity;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateRelationMapper;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateRelationRepository;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateRepository;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplatePo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateRelationPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LogQueryTemplateService {
    @Autowired
    private LogQueryTemplateRepository templateRepository;

    @Autowired
    private LogQueryTemplateRelationRepository templateRelationRepository;

    @Autowired
    private LogQueryTemplateRelationMapper templateRelationMapper;

    @Autowired
    private ObjectMapper objectMapper;


    public List<LogQueryTemplateRelationPo> getTreeByParentId(Set<Long> parentIds, Set<Long> treeIds) {
        treeIds.addAll(parentIds);
        List<LogQueryTemplateRelationPo> children = templateRelationMapper.selectList(Wrappers.<LogQueryTemplateRelationPo>lambdaQuery()
                .in(LogQueryTemplateRelationPo::getTemplateParentId, parentIds));
        for (LogQueryTemplateRelationPo child : children) {
            treeIds.add(child.getTemplateId());
            Set<Long> parentIdsSet = new HashSet<>();
            parentIdsSet.add(child.getTemplateId());
            child.setChildren(getTreeByParentId(parentIdsSet, treeIds));
        }
        return children;
    }

    public LogQueryTemplatePo getQueryTemplateById(Long templateId) {
        return templateRepository.getById(templateId);
    }

    public Collection<LogGroupTemplateEntity> getQueryTemplateByGroupIdAndApp(Long groupId, String application) {
        List<LogGroupTemplateEntity> result = new ArrayList<>();
        if (groupId == null) {
            return result;
        }
        List<LogQueryTemplatePo> allTopTemplate = StringUtils.hasLength(application) ? templateRepository.queryAllTopQueryTemplate(groupId, application)
                : templateRepository.queryAllTopQueryTemplateByGroupId(groupId);
        if (CollectionUtils.isEmpty(allTopTemplate)) {
            return result;
        }
        Set<Long> topTemplateIds = allTopTemplate.stream().map(p -> p.getId()).collect(Collectors.toSet());
        Set<Long> allQuery = new HashSet<>();
        allQuery.addAll(topTemplateIds);
        List<LogQueryTemplateRelationPo> relationPos = getTreeByParentId(topTemplateIds, allQuery);
        List<LogQueryTemplatePo> templatePos = templateRepository.queryTemplateByIds(allQuery);
//        Map<Long, LogQueryTemplatePo> groupMap = templatePos.stream().collect(Collectors.toMap(LogQueryTemplatePo::getId, p -> p, (p1, p2) -> p1));


        Map<Long, LogGroupTemplateEntity> groupMap = templatePos.stream().map(p -> {
            LogGroupTemplateEntity entity = new LogGroupTemplateEntity();
            entity.setId(p.getId());
            entity.setOrderNum(p.getOrderNum());
            entity.setType(2);
//            String jsonString = JSON.toJSONString(p);
            entity.setName(p.getName());
            entity.setContent(p.getQueryParam());
            return entity;
        }).collect(Collectors.toMap(LogGroupTemplateEntity::getId, p -> p, (p1, p2) -> p1));

        if (!CollectionUtils.isEmpty(relationPos)) {
            TreeAssembler.setChildrenForLogTemplateEntity(groupMap, relationPos);
        }
        List<LogGroupTemplateEntity> resultTemp = new ArrayList<LogGroupTemplateEntity>();
        //  返回顶节点的group
        groupMap.forEach((key, value) -> {
            if (topTemplateIds.contains(key)) {
                resultTemp.add(value);
            }
        });
        result = resultTemp.stream().sorted(Comparator.comparing(LogGroupTemplateEntity::getOrderNum))
                .collect(Collectors.toList());
        return result;

    }


}
