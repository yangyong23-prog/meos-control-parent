package com.persagy.apccontrol.basic.model.entity.cache;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 缓存对象关系实体。
 *
 * <p>{@code objectId} 固定对应 RWD 物理关系的 {@code objFrom/fromId}，
 * {@code relObjectId} 固定对应 {@code objTo/toId}。反向查询只改变查询入口，
 * 不交换实体中的关系方向。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@TableName(value = "cache_object_rel")
public class CacheObjectRel extends BaseAssignIdDO implements Serializable {

    /**
     * 序列化版本号。
     *
     * @since v7.260701.0
     */
    private static final long serialVersionUID = 4179576081938379441L;

    /**
     * 项目 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 关系主体对象 ID，对应 RWD 物理关系的 {@code objFrom/fromId}。
     *
     * @since v7.260701.0
     */
    @TableField(value = "object_id")
    private String objectId;

    /**
     * 图谱编码。
     *
     * @since v7.260701.0
     */
    @TableField(value = "graph_code")
    private String graphCode;

    /**
     * 关系编码。
     *
     * @since v7.260701.0
     */
    @TableField(value = "rel_code")
    private String relCode;

    /**
     * 关系目标对象 ID，对应 RWD 物理关系的 {@code objTo/toId}。
     *
     * @since v7.260701.0
     */
    @TableField(value = "rel_object_id")
    private String relObjectId;
}
