package com.persagy.apccontrol.basic.model.entity.infocodeconfig;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 信息点分组配置表
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "object_info_code_group", autoResultMap = true)
public class InfoCodeGroup extends BaseAssignIdDO implements Serializable {

    /**
     * 分组编码
     */
    @TableField(value = "group_code")
    private String groupCode;

    /**
     * 分组名称
     */
    @TableField(value = "group_name")
    private String groupName;

    /**
     * 展示样式
     */
    @TableField(value = "show_style", typeHandler = JacksonTypeHandler.class)
    private JsonNode showStyle;

    /**
     * 分组排序
     */
    @TableField(value = "sequence")
    private Integer sequence;

    /**
     * 关联上级分组ID
     */
    @Nullable
    @TableField(value = "rel_parent_id")
    private Long relParentId;

    /**
     * 获取所有上级分组ID
     *
     * @param groups 分组对象
     * @return 所有上级分组ID
     * @since 5.240602.0
     */
    public static Set<Long> listAllRelParentIds(@Nullable Collection<InfoCodeGroup> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return Sets.newHashSet();
        }
        return groups.stream().map(InfoCodeGroup::getRelParentId).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    protected Supplier<Boolean> checkAddEntitySupplier() {
        return () -> ObjectUtils.anyNull(groupCode, groupName, sequence);
    }
}