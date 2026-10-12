package com.persagy.apccontrol.basic.structure.infocodeconfig.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.persagy.apccontrol.basic.model.base.domain.ValueObject;
import lombok.Data;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * 信息点分组配置模型
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
public class InfoCodeGroupModel implements ValueObject {
    
    public final static String DEFAULT = "default";
    
    /**
     * 对象状态分组
     */
    public static final List<String> OBJECT_STATUS_GROUP_NAMES =
            Lists.newArrayList("equipStatus", "spaceStatus");

    /**
     * 设定参数分组
     */
    public static final List<String> IOT_SETTING_GROUP_NAMES = Lists.newArrayList(
            "equipParameterSet", "parameterSetOperation", "controlledParameterSet");

    /**
     * 设定参数条件分组
     */
    public static final List<String> IOT_SETTING_CONDITION_GROUP_NAMES = Lists.newArrayList(
            "equipParameterSetCondition", "parameterSetOperationCondition", "controlledParameterSetCondition");

    /**
     * 当父级不存在规则被移除时将关联信息点一并移除
     */
    public static final List<String> DYNAMIC_REMOVE_RELATION_INFO_CODE_GROUP_NAME_WHITELIST = Lists.newArrayList(
            "parameterSetOperation", "customComponents");

    /**
     * 对象拓展信息点分组
     */
    public static final List<String> OBJECT_ADDITIONAL_INFO = Lists.newArrayList("objectAdditionalInfo");

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 分组编码
     */
    private String groupCode;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组顺序
     */
    private Integer sequence;

    /**
     * 展示样式
     */
    private JsonNode showStyle;

    /**
     * 上级分组, 为 {@code null} 时则为顶级分组
     */
    @Nullable
    private InfoCodeGroupModel relParent;

    /**
     * 是否为IOT设定信息点条件分组
     *
     * @return 是否为IOT设定信息点条件分组
     * @since 5.241001.0
     */
    public boolean isIotSettingConditionGroup() {
        return IOT_SETTING_CONDITION_GROUP_NAMES.contains(groupCode);
    }

    /**
     * 解析顶级分组模型
     *
     * @param currentGroupModel 当前分组模型
     * @return 顶级分组模型
     * @since 5.240602.0
     */
    public static InfoCodeGroupModel analyzeTopGroupModel(
            @NonNull InfoCodeGroupModel currentGroupModel) {
        InfoCodeGroupModel parentGroupModel = currentGroupModel.getRelParent();
        if (Objects.isNull(parentGroupModel)) {
            return currentGroupModel;
        }
        InfoCodeGroupModel parentsGroupModel = parentGroupModel.getRelParent();
        if (Objects.isNull(parentsGroupModel)) {
            return parentGroupModel;
        } else {
            return analyzeTopGroupModel(parentsGroupModel);
        }
    }
}
