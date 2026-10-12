package com.persagy.apccontrol.basic.model.vo.demand;

import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * 需求管理空间功能类型树
 *
 * @author LiuYang
 * @version 1.0.0
 * @date 2025/2/28 10:56
 * @since 7.250202.0
 */
@Data
@Schema(description = "需求管理空间功能类型树VO")
public class DemandTreeSpaceRoomFuncTypeVO implements Serializable {

    private static final long serialVersionUID = 7535508415546821837L;

    @Schema(description = "空间功能类型")
    private String funcType;

    @Schema(description = "空间功能类型名称")
    private String funcTypeName;

    @Schema(description = "子集")
    private List<DemandTreeSpaceRoomFuncTypeVO> children;


    /**
     * 过滤树结构，只保留funcType在validNodes集合中的节点，以及它们的父节点（如果父节点的子节点中有有效的）。
     *
     * @param nodes      待修剪的节点列表
     * @param validNodes 有效的funcType集合
     * @return 修剪后的节点列表
     */
    public static List<DemandTreeSpaceRoomFuncTypeVO> pruneTree(List<DemandTreeSpaceRoomFuncTypeVO> nodes, Set<String> validNodes) {
        List<DemandTreeSpaceRoomFuncTypeVO> pruned = new ArrayList<>();
        for (DemandTreeSpaceRoomFuncTypeVO node : nodes) {
            // 如果当前节点的funcType在有效集合中，则保留该节点
            if (validNodes.contains(node.getFuncType())) {
                pruned.add(node);
            } else {
                // 如果当前节点不在有效集合中，获取其子节点列表
                List<DemandTreeSpaceRoomFuncTypeVO> dataChildrenList = node.getChildren();
                // 如果子节点列表为空或不存在，则跳过当前节点
                if (dataChildrenList == null || dataChildrenList.isEmpty()) {
                    continue;
                }
                // 递归过滤子节点
                List<DemandTreeSpaceRoomFuncTypeVO> children = pruneTree(dataChildrenList, validNodes);
                // 如果过滤后的子节点列表不为空，则更新当前节点的子节点，并保留当前节点
                if (!children.isEmpty()) {
                    node.setChildren(children);
                    pruned.add(node);
                }
            }
        }
        // pruned.sort(Comparator.comparing(DemandTreeSpaceRoomFuncTypeVO::getFuncType));
        return pruned;
    }

    /**
     * 构建空间房间功能类型树的方法
     *
     * @param dataList 包含数据源的数据列表
     * @return 返回构建好的需求树空间房间功能类型列表
     */
    public static List<DemandTreeSpaceRoomFuncTypeVO> buildTreeSpaceRoomFuncType(List<DtpKnowledgeInfoCodeDataSourceDTO> dataList) {
        // 创建映射表，用于快速查找节点
        Map<String, DemandTreeSpaceRoomFuncTypeVO> nodeMap = new HashMap<>();
        List<DemandTreeSpaceRoomFuncTypeVO> roots = new ArrayList<>();

        // 1. 将 ObjectA 转换为 DemandTreeSpaceRoomFuncTypeVO，并存入 Map
        for (DtpKnowledgeInfoCodeDataSourceDTO codeDataSourceDTO : dataList) {
            DemandTreeSpaceRoomFuncTypeVO spaceRoomFuncTypeVO = new DemandTreeSpaceRoomFuncTypeVO();
            spaceRoomFuncTypeVO.setFuncType(codeDataSourceDTO.getCode());       // code → funcType
            spaceRoomFuncTypeVO.setFuncTypeName(codeDataSourceDTO.getName());   // name → funcTypeName
            spaceRoomFuncTypeVO.setChildren(new ArrayList<>());    // 初始化子集
            nodeMap.put(codeDataSourceDTO.getCode(), spaceRoomFuncTypeVO);
        }

        // 2. 构建树结构
        for (DtpKnowledgeInfoCodeDataSourceDTO codeDataSourceDTO : dataList) {
            String parentCode = codeDataSourceDTO.getParentCode();
            if (parentCode == null || parentCode.isEmpty()) {
                // 无父节点，作为根节点
                roots.add(nodeMap.get(codeDataSourceDTO.getCode()));
            } else {
                // 找到父节点，将当前节点加入其子集
                DemandTreeSpaceRoomFuncTypeVO parent = nodeMap.get(parentCode);
                if (parent != null) {
                    parent.getChildren().add(nodeMap.get(codeDataSourceDTO.getCode()));
                }
            }
        }

        // 3. 限制树的最大深度为3层
        limitTreeDepth(roots, 1);

        return roots;
    }

    /**
     * 递归限制树的最大深度
     *
     * @param nodes        当前层的节点列表
     * @param currentDepth 当前深度（根节点为第1层）
     */
    private static void limitTreeDepth(List<DemandTreeSpaceRoomFuncTypeVO> nodes, int currentDepth) {
        if (nodes == null || nodes.isEmpty()) {
            return;
        }

        for (DemandTreeSpaceRoomFuncTypeVO node : nodes) {
            if (currentDepth >= 3) {
                // 当前是第3层，清空子集（确保无第4层）
                node.setChildren(null);  // 或设置为空列表 new ArrayList<>()
            } else {
                // 递归处理子节点，深度+1
                limitTreeDepth(node.getChildren(), currentDepth + 1);
            }
        }
    }

}