package com.persagy.apccontrol.basic.model.entity.cache;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseInputIdDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 缓存对象实体。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@TableName(value = "cache_object", autoResultMap = true)
public class CacheObject extends BaseInputIdDO implements Serializable {

    /**
     * 序列化版本号。
     *
     * @since v7.260701.0
     */
    private static final long serialVersionUID = 5413540907459218249L;

    /**
     * 项目 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 对象本地 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "local_id")
    private String localId;

    /**
     * 对象本地名称。
     *
     * @since v7.260701.0
     */
    @TableField(value = "local_name")
    private String localName;

    /**
     * 对象类编码。
     *
     * @since v7.260701.0
     */
    @TableField(value = "class_code")
    private String classCode;

    /**
     * 对象类型编码。
     *
     * @since v7.260701.0
     */
    @TableField(value = "object_type")
    private String objectType;

    /**
     * 楼层 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "floor_id", updateStrategy = FieldStrategy.IGNORED)
    private String floorId;

    /**
     * 建筑 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "building_id", updateStrategy = FieldStrategy.IGNORED)
    private String buildingId;

    /**
     * 监测监控状态码。
     *
     * <p>{@code null} 表示 RWD 未返回该字段，非空时保存 RWD 返回的单个 code。</p>
     *
     * @since v7.260701.0
     */
    @TableField(value = "monitoring_surveillance_status", updateStrategy = FieldStrategy.IGNORED)
    private String monitoringSurveillanceStatus;

    /**
     * 按对象类字段配置裁剪后的扩展 JSON。
     *
     * @since v7.260701.0
     */
    @TableField(value = "object_data", updateStrategy = FieldStrategy.IGNORED)
    private String objectData;
}
