package com.persagy.apccontrol.common.logtrace.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.persagy.apccontrol.common.logtrace.assembler.LogNodeRelationAssembler;
import com.persagy.apccontrol.common.logtrace.entity.LogNodeRelationEntity;
import com.persagy.apccontrol.common.logtrace.mapper.LogNodeRelationMapper;
import com.persagy.apccontrol.common.logtrace.persistence.LogNodeRelationRepository;
import com.persagy.apccontrol.common.logtrace.po.LogNodeRelationPo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogNodeRelationService {

    @Autowired
    private LogNodeRelationAssembler relationAssembler;

    @Autowired
    private LogNodeRelationRepository relationRepository;




    @Autowired
    private LogNodeRelationMapper logNodeRelationMapper;

    public List<LogNodeRelationPo> getTreeByParentId(Long parentId, Set<Long> treeIds) {
        treeIds.add(parentId);
        List<LogNodeRelationPo> children = logNodeRelationMapper.selectList(Wrappers.<LogNodeRelationPo>lambdaQuery()
                .eq(LogNodeRelationPo::getParentNodeId, parentId));
        for (LogNodeRelationPo child : children) {
            treeIds.add(child.getNodeId());
            child.setChildren(getTreeByParentId(child.getNodeId(),treeIds));
        }
        return children;
    }

    void saveRelation(List<LogNodeRelationEntity> relations) {
        if (!CollectionUtils.isEmpty(relations)) {
            List<LogNodeRelationPo> relationPos = relations.stream().map(p -> relationAssembler.entityToPo(p)).collect(Collectors.toList());
            relationRepository.saveBatch(relationPos);
        }
    }

}
