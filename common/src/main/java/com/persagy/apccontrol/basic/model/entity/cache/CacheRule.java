package com.persagy.apccontrol.basic.model.entity.cache;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.structure.cache.enumeration.CacheIotRuleTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 去重后的缓存 IOT 规则实体。
 *
 * <p>规则主表保存对象级规则业务键和云边规则类型，业务读取直接按对象、信息点和规则
 * 类型查询。{@link CacheRuleKeyRel} 仅记录单 Key 替换所需的写入归属，并用于共享引用和
 * 孤儿规则清理。</p>
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@TableName(value = "cache_rule")
public class CacheRule extends BaseAssignIdDO implements Serializable {

    /**
     * 序列化版本号。
     *
     * @since v7.260701.0
     */
    private static final long serialVersionUID = 7618654166571473937L;

    /**
     * 项目 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 规则所属对象 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "object_id")
    private String objectId;

    /**
     * 规则信息点编码。
     *
     * @since v7.260701.0
     */
    @TableField(value = "info_code")
    private String infoCode;

    /**
     * 云端或边缘端规则类型。
     *
     * @since v7.260701.0
     */
    @TableField(value = "type")
    private CacheIotRuleTypeEnum type;
}
