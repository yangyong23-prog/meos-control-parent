package com.persagy.apccontrol.common.logtrace.assembler;

import com.persagy.apccontrol.common.logtrace.entity.LogGroupTemplateEntity;
import com.persagy.apccontrol.common.logtrace.po.LogNodePo;
import com.persagy.apccontrol.common.logtrace.po.LogNodeRelationPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateGroupRelationPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateHeaderRelationPo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplatePo;
import com.persagy.apccontrol.common.logtrace.po.LogQueryTemplateRelationPo;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreeAssembler {


    public static Map<Long, LogNodePo> buildNodeMap(List<LogNodePo> nodes) {
        Map<Long, LogNodePo> nodeMap = new HashMap<>();
        for (LogNodePo node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        return nodeMap;
    }



    public static void setChildrenForLogGroupEntity(Map<Long, LogGroupTemplateEntity> entityMap, List<LogQueryTemplateGroupRelationPo> relations) {
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
                setChildrenForLogGroupEntity(entityMap, relation.getChildren());
            }
        }
    }


    public static void setChildrenForLogTemplateEntity(Map<Long, LogGroupTemplateEntity> entityMap, List<LogQueryTemplateRelationPo> relations) {
        for (LogQueryTemplateRelationPo relation : relations) {
            LogGroupTemplateEntity group = entityMap.get(relation.getTemplateId());
            LogGroupTemplateEntity parentGroup = entityMap.get(relation.getTemplateParentId());
            if (group != null && parentGroup != null) {
                if (parentGroup.getChildren() == null) {
                    parentGroup.setChildren(new ArrayList<>());
                }
                parentGroup.getChildren().add(group);
                childOrder(parentGroup);
            }
            if (!CollectionUtils.isEmpty(relation.getChildren())) {
                setChildrenForLogTemplateEntity(entityMap, relation.getChildren());
            }
        }
    }

    public static void setChildrenForLogTemplateHeaderEntity(Map<Long, LogGroupTemplateEntity> entityMap, List<LogQueryTemplateHeaderRelationPo> relations) {
        for (LogQueryTemplateHeaderRelationPo relation : relations) {
            LogGroupTemplateEntity header = entityMap.get(relation.getHeaderId());
            LogGroupTemplateEntity parentHeader = entityMap.get(relation.getHeaderParentId());
            if (header != null && parentHeader != null) {
                if (parentHeader.getChildren() == null) {
                    parentHeader.setChildren(new ArrayList<>());
                }
                parentHeader.getChildren().add(header);
                childOrder(parentHeader);
            }
            if (!CollectionUtils.isEmpty(relation.getChildren())) {
                setChildrenForLogTemplateHeaderEntity(entityMap, relation.getChildren());
            }
        }
    }

    public static void childOrder(LogGroupTemplateEntity group) {
        if (!CollectionUtils.isEmpty(group.getChildren())) {
            List<LogGroupTemplateEntity> children = group.getChildren().stream()
                    .sorted(Comparator.comparing(LogGroupTemplateEntity::getOrderNum))
                    .collect(Collectors.toList());
            group.setChildren(children);

        }
    }




    public static void setChildrenForNodes(Map<Long, LogNodePo> nodeMap, List<LogNodeRelationPo> relations) {
        for (LogNodeRelationPo relation : relations) {
            LogNodePo node = nodeMap.get(relation.getNodeId());
            LogNodePo parentNode = nodeMap.get(relation.getParentNodeId());
            if (parentNode != null && node != null) {
                if (parentNode.getChildren() == null) {
                    parentNode.setChildren(new ArrayList<>());
                }
                parentNode.getChildren().add(node);
            }
            if (!CollectionUtils.isEmpty(relation.getChildren())) {
                setChildrenForNodes(nodeMap, relation.getChildren());
            }
        }
    }


    public static void setChildrenForGroups(Map<Long, LogQueryTemplateGroupPo> entityMap, List<LogQueryTemplateGroupRelationPo> relations) {
        for (LogQueryTemplateGroupRelationPo relation : relations) {
            LogQueryTemplateGroupPo group = entityMap.get(relation.getGroupId());
            LogQueryTemplateGroupPo parentGroup = entityMap.get(relation.getGroupParentId());
            if (group != null && parentGroup != null) {
                if (parentGroup.getChildren() == null) {
                    parentGroup.setChildren(new ArrayList<>());
                }
                parentGroup.getChildren().add(group);
                childGroupOrder(parentGroup);
            }
            if (!CollectionUtils.isEmpty(relation.getChildren())) {
                setChildrenForGroups(entityMap, relation.getChildren());
            }
        }
    }

    public static void setChildrenForTemplate(Map<Long, LogQueryTemplatePo> entityMap, List<LogQueryTemplateRelationPo> relations) {
        for (LogQueryTemplateRelationPo relation : relations) {
            LogQueryTemplatePo templatePo = entityMap.get(relation.getTemplateId());
            LogQueryTemplatePo parentTemplate = entityMap.get(relation.getTemplateParentId());
            if (templatePo != null && parentTemplate != null) {
                if (parentTemplate.getChildren() == null) {
                    parentTemplate.setChildren(new ArrayList<>());
                }
                parentTemplate.getChildren().add(templatePo);
                childTemplateOrder(parentTemplate);
            }
            if (!CollectionUtils.isEmpty(relation.getChildren())) {
                setChildrenForTemplate(entityMap, relation.getChildren());
            }
        }
    }
    public static  void childGroupOrder(LogQueryTemplateGroupPo group) {
        if (!CollectionUtils.isEmpty(group.getChildren())) {
            List<LogQueryTemplateGroupPo> children = group.getChildren().stream()
                    .sorted(Comparator.comparing(LogQueryTemplateGroupPo::getOrderNum))
                    .collect(Collectors.toList());
            group.setChildren(children);

        }
    }

    public static  void childTemplateOrder(LogQueryTemplatePo template) {
        if (!CollectionUtils.isEmpty(template.getChildren())) {
            List<LogQueryTemplatePo> children = template.getChildren().stream()
                    .sorted(Comparator.comparing(LogQueryTemplatePo::getOrderNum))
                    .collect(Collectors.toList());
            template.setChildren(children);

        }
    }


    public static List<LogNodePo> buildTree(List<LogNodeRelationPo> relations, List<LogNodePo> nodes) {
        Map<Long, LogNodePo> nodeMap = buildNodeMap(nodes);
        setChildrenForNodes(nodeMap, relations);

        // Find root nodes (nodes without parents)
        List<LogNodePo> roots = new ArrayList<>();
        for (LogNodePo node : nodes) {
            if (nodeMap.get(node.getId()).getId() == null || !nodeMap.containsKey(node.getId())) {
                roots.add(node);
            }
        }

        return roots;
    }
}
