package com.persagy.apccontrol.basic.model.entity.cache;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseInputIdDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 缓存对象模型视图实体。
 *
 * @author JiYinchuan
 * @since v7.260701.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@TableName(value = "cache_object_model_view")
public class CacheObjectModelView extends BaseInputIdDO implements Serializable {

    /**
     * 序列化版本号。
     *
     * @since v7.260701.0
     */
    private static final long serialVersionUID = 578021786919874206L;

    /**
     * 项目 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 对象名称。
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
     * 建筑 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "building_id")
    private String buildingId;

    /**
     * 楼层 ID。
     *
     * @since v7.260701.0
     */
    @TableField(value = "floor_id")
    private String floorId;

    /**
     * 按设备类字段配置裁剪后的模型视图 JSON。
     *
     * @since v7.260701.0
     */
    @TableField(value = "view_data")
    private String viewData;
}
