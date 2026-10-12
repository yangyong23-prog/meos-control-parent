package com.persagy.apccontrol.common.logtrace.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.persagy.apccontrol.common.logtrace.assembler.TreeAssembler;
import com.persagy.apccontrol.common.logtrace.entity.LogGroupTemplateEntity;
import com.persagy.apccontrol.common.logtrace.mapper.LogQueryTemplateHeaderRelationMapper;
import com.persagy.apccontrol.common.logtrace.persistence.LogQueryTemplateHeaderRepository;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderRelationPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LogQueryTemplateHeaderService {

    @Autowired
    private LogQueryTemplateHeaderRelationMapper relationMapper;

    @Autowired
    private LogQueryTemplateHeaderRepository headerRepository;


    public List<LogQueryTemplateHeaderRelationPo> getTreeByParentId(Set<Long> parentIds, Set<Long> treeIds) {
        treeIds.addAll(parentIds);
        List<LogQueryTemplateHeaderRelationPo> children = relationMapper.selectList(Wrappers.<LogQueryTemplateHeaderRelationPo>lambdaQuery()
                .in(LogQueryTemplateHeaderRelationPo::getHeaderParentId, parentIds));
        for (LogQueryTemplateHeaderRelationPo child : children) {
            treeIds.add(child.getHeaderId());
            Set<Long> parentIdsSet = new HashSet<>();
            parentIdsSet.add(child.getHeaderId());
            child.setChildren(getTreeByParentId(parentIdsSet, treeIds));
        }
        return children;
    }

    /**
     * 获取父级为动态表头的
     * @param ids
     * @return
     */
    public List<LogQueryTemplateHeaderPo> getParentDynamicHeaderByIds(Collection<Long>ids){
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
       return headerRepository.getParentDynamicHeaderByIds(ids);
    }

    public List<LogQueryTemplateHeaderPo> getAllQueryTemplateHeaderByTemplateId(Long templateId) {
        if (Objects.isNull(templateId))
            return new ArrayList<>();
        return headerRepository.getHeaderByTemplateId(templateId);
    }


    public List<LogGroupTemplateEntity> getQueryTemplateHeaderTree(Long templateId) {
        List<LogGroupTemplateEntity> result = new ArrayList<LogGroupTemplateEntity>();
        if (templateId == null) {
            return result;
        }
        List<LogQueryTemplateHeaderPo> headers = headerRepository.getHeaderByTemplateId(templateId);
        if (CollectionUtils.isEmpty(headers)) {
            return result;
        }
        Map<Long, LogGroupTemplateEntity> groupMap = headers.stream().map(p -> {
            LogGroupTemplateEntity entity = new LogGroupTemplateEntity();
            entity.setId(p.getId());
            entity.setOrderNum(p.getOrderNum());
            entity.setType(p.getHeaderType());
//            String jsonString = JSON.toJSONString(p);
            entity.setName(p.getHeaderName());
            entity.setContent(p.getHeaderCode());
            return entity;
        }).collect(Collectors.toMap(LogGroupTemplateEntity::getId, p -> p, (p1, p2) -> p1));

        List<LogQueryTemplateHeaderPo> topHeaderPos = headerRepository.queryAllTopQueryTemplateHeaderByTemplateId(templateId);
        Set<Long> topHeaderIds = topHeaderPos.stream().map(p -> p.getId()).collect(Collectors.toSet());
        List<LogQueryTemplateHeaderRelationPo> relationPos = getTreeByParentId(topHeaderIds, new HashSet<>());
        if (!CollectionUtils.isEmpty(relationPos)) {
            TreeAssembler.setChildrenForLogTemplateHeaderEntity(groupMap, relationPos);
        }
        List<LogGroupTemplateEntity> resultTemp = new ArrayList<LogGroupTemplateEntity>();
        //  返回顶节点的group
        groupMap.forEach((key, value) -> {
            if (topHeaderIds.contains(key)) {
                resultTemp.add(value);
            }
        });
        result = resultTemp.stream().sorted(Comparator.comparing(LogGroupTemplateEntity::getOrderNum))
                .collect(Collectors.toList());
        return result;
    }


    public  void traverseTree(LogGroupTemplateEntity node,Map<Long, List<LogGroupTemplateEntity>> groupToTemplate) {
        if (node != null) {
            // 访问当前节点
//            System.out.println("ID: " + node.getId() + ", Type: " + node.getType() + ", Order Num: " + node.getOrderNum() + ", Object: " + node.getObject());
            // 递归遍历子节点列表
            if(groupToTemplate.isEmpty()){
                return;
            }
//            List<LogGroupTemplateEntity> childrenTemp = node.getChildren();
//            if(childrenTemp==null){
//                node.setChildren((List)groupToTemplate.get(node.getId()));
//            }else {
//                if(!CollectionUtils.isEmpty(groupToTemplate.get(node.getId()))){
//                    childrenTemp.addAll(groupToTemplate.get(node.getId()));
//                }
//            }

            List<LogGroupTemplateEntity> children = node.getChildren();
            groupToTemplate.forEach((key,value)->{
                int beginIndex = 0;
                if(!CollectionUtils.isEmpty(children)){
                    //更改以动态表头为父的节点
                    List<LogGroupTemplateEntity>  entityList =  children.stream().filter(p->p.getId().equals(key)).collect(Collectors.toList());
                    if(!CollectionUtils.isEmpty(entityList)){
                        List<LogGroupTemplateEntity> newChildren =new ArrayList<>(children);
                        for(int i=0;i<children.size();i++ ) {
                           if(key.equals(newChildren.get(i).getId())){
                               beginIndex = i;
                           }
                        }
                        if(!CollectionUtils.isEmpty(value)){
                            //删除动态表头配置
                            newChildren.remove(beginIndex);
                            for(int j=0;j<value.size();j++ ){
                                //添加真实的表头数据
                                newChildren.add(beginIndex+j,value.get(j));
                            }
                            node.setChildren(newChildren);
                        }
                    }
                }
                //更改动态表头的子
                if(node.getId().equals(key)){
                    if(!CollectionUtils.isEmpty(value)){
                        value.forEach(p->{
                            p.setChildren(node.getChildren());
                        });
                    }
                }
            });
            if (children != null) {
                for (LogGroupTemplateEntity child : children) {
                    traverseTree(child,groupToTemplate); // 递归调用
                }
            }
        }
    }



}
