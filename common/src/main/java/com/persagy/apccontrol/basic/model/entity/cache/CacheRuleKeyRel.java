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
 * 缓存键与去重规则关联实体。
 *
 * <p>关联记录只表达单个缓存 Key 的规则写入归属。业务查询直接读取规则主表；本表用于
 * 替换或删除当前 Key 的关联，并在没有其他 Key 引用时清理孤儿规则。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@TableName(value = "cache_rule_key_rel")
public class CacheRuleKeyRel extends BaseAssignIdDO implements Serializable {

    /**
     * 序列化版本号。
     *
     * @since v7.260701.0
     */
    private static final long serialVersionUID = -6388129521931034890L;

    /**
     * 项目 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 缓存键。
     *
     * @since v7.260701.0
     */
    @TableField(value = "cache_key")
    private String cacheKey;

    /**
     * 规则主表 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "rule_id")
    private Long ruleId;

}
