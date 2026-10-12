package com.persagy.apccontrol.basic.model.dto;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.persagy.apccontrol.basic.constant.LocalCacheConstant;
import com.persagy.apccontrol.basic.model.entity.RcInfoCodeCfg;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 信息点配置中分组信息
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2023/8/17 19:47
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RcInfoCodeGroupDTO implements Comparable<RcInfoCodeGroupDTO> {
    
    public static final List<String> GROUP_NAME_EFFICIENCY = Arrays.asList(LocaleMessageUtil.getMessage("key_dffc2b76b95c4377b51ce50f74838680"), LocaleMessageUtil.getMessage("key_4e4ccd5210dc4978ae7f01fa8e8f7d94"));
    

    // 分组ID
    private String groupId;

    // 分组名称
    private String groupName;

    // 上级分组ID
    private String parentId;

    private Integer ordered;

    // 下一级分组信息
    private List<RcInfoCodeGroupDTO> children;

    // 本级分组对应的信息点集合
    private List<RcInfoCodeCfg> infoCodes;

    @Override
    public int compareTo(@NotNull RcInfoCodeGroupDTO o) {
        if (this.ordered == null || o.getOrdered() == null) {
            return -1;
        }
        return this.ordered.compareTo(o.getOrdered());
    }

    /**
     * 深度拷贝，暂无效率可言
     *
     * @param rcInfoCodeGroupDTO
     * @return
     */
    public static RcInfoCodeGroupDTO copyInfoCodeGroupTree(RcInfoCodeGroupDTO rcInfoCodeGroupDTO) {
        Gson gson = new GsonBuilder().create();
        String text = gson.toJson(rcInfoCodeGroupDTO);
        return gson.fromJson(text, RcInfoCodeGroupDTO.class);
    }

    /**
     * 取出 RcInfoCodeGroupDTO 对象中得 infoCodeCfgs
     *
     * @param rcInfoCodeCfgs
     * @param rcInfoCodeGroupDTOs
     */
    public static void collectInfoCodeConfigsByTree(List<RcInfoCodeCfg> rcInfoCodeCfgs, Collection<RcInfoCodeGroupDTO> rcInfoCodeGroupDTOs) {
        if (CollectionUtil.isEmpty(rcInfoCodeGroupDTOs)) {
            return;
        }
        for (RcInfoCodeGroupDTO rcInfoCodeGroupDTO : rcInfoCodeGroupDTOs) {
            if (CollectionUtil.isNotEmpty(rcInfoCodeGroupDTO.getInfoCodes())) {
                rcInfoCodeCfgs.addAll(rcInfoCodeGroupDTO.getInfoCodes());
            }
            if (CollectionUtil.isNotEmpty(rcInfoCodeGroupDTO.getChildren())) {
                collectInfoCodeConfigsByTree(rcInfoCodeCfgs, rcInfoCodeGroupDTO.getChildren());
            }
        }
    }

    /**
     * 将对应的信息点配置信息，放入对应的树形节点中去
     *
     * @param rcInfoCodeCfg
     */
    public static void loadInfoCodeGroupTree(RcInfoCodeCfg rcInfoCodeCfg, RcInfoCodeGroupDTO rcInfoCodeGroupDTO) {
        if (Objects.isNull(rcInfoCodeCfg) || rcInfoCodeGroupDTO.getInfoCodes() == null) {
            rcInfoCodeGroupDTO.setInfoCodes(new ArrayList<>());
        }
        if (rcInfoCodeCfg.getGroupInfo().equals(rcInfoCodeGroupDTO.getGroupId())) {
            rcInfoCodeGroupDTO.getInfoCodes().add(rcInfoCodeCfg);
            rcInfoCodeGroupDTO.getInfoCodes().sort(Comparator.comparing(RcInfoCodeCfg::getOrdered, Integer::compare));
        } else {
            List<RcInfoCodeGroupDTO> groupDTOChildren = rcInfoCodeGroupDTO.getChildren();
            if (CollectionUtil.isNotEmpty(groupDTOChildren)) {
                for (RcInfoCodeGroupDTO groupDTOChild : groupDTOChildren) {
                    loadInfoCodeGroupTree(rcInfoCodeCfg, groupDTOChild);
                }
            }
        }
    }

    /**
     * 将list集合转为树
     *
     * @param treeList
     * @return
     */
    public static List<RcInfoCodeGroupDTO> convertGroupInfoTree(List<RcInfoCodeGroupDTO> treeList) {
        Map<String, List<RcInfoCodeGroupDTO>> groupInfoTree = treeList.stream().collect(Collectors.groupingBy(RcInfoCodeGroupDTO::getParentId));
        treeList.forEach(node -> node.setChildren(groupInfoTree.get(node.getGroupId())));
        return treeList.stream().filter(v -> v.getParentId().equals("0")).collect(Collectors.toList());
    }

    public static void removeEmptyGroupInfo(List<RcInfoCodeGroupDTO> treeList) {
        if (CollectionUtil.isEmpty(treeList)) {
            return;
        }
        for (int i = treeList.size() - 1; i >= 0; i--) {
            RcInfoCodeGroupDTO rcInfoCodeGroupDTO = treeList.get(i);
            if (CollectionUtil.isNotEmpty(rcInfoCodeGroupDTO.getChildren())) {
                removeEmptyGroupInfo(rcInfoCodeGroupDTO.getChildren());
            }
            if (CollectionUtil.isEmpty(rcInfoCodeGroupDTO.getInfoCodes()) && CollectionUtil.isEmpty(rcInfoCodeGroupDTO.getChildren())) {
                treeList.remove(i);
            }
        }
    }

    /**
     * 给每一个groupId 分配上对应的树形结构
     *
     * @param treeList
     */
    public static void initLocalCaheGroupInfo(List<RcInfoCodeGroupDTO> treeList) {
        for (RcInfoCodeGroupDTO top : treeList) {
            LocalCacheConstant.NODE_TOP_ID.put(top.getGroupId(), top.getGroupId());
            LocalCacheConstant.TOP_GROUP_TREE.put(top.getGroupId(), top);
            if (CollectionUtil.isNotEmpty(top.getChildren())) {
                initLocalCaheGroupInfo(top.getChildren(), top);
            }
        }
    }

    private static void initLocalCaheGroupInfo(List<RcInfoCodeGroupDTO> treeList, RcInfoCodeGroupDTO top) {
        for (RcInfoCodeGroupDTO rcInfoCodeGroupDTO : treeList) {
            LocalCacheConstant.NODE_TOP_ID.put(rcInfoCodeGroupDTO.getGroupId(), top.getGroupId());
            if (CollectionUtil.isNotEmpty(rcInfoCodeGroupDTO.getChildren())) {
                initLocalCaheGroupInfo(rcInfoCodeGroupDTO.getChildren(), top);
            }
        }
    }

}
