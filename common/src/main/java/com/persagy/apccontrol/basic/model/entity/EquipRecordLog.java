package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serializable;

/**
 * @TableName equip_record_log
 */
@TableName(value = "equip_record_log")
@Data
public class EquipRecordLog implements Serializable {
    private static final long serialVersionUID = 3321944090257913627L;
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 系统ID(无系统场景为空 如 公共照明系统 无系统 ID 承载)
     */
    private String systemId;

    /**
     * 系统编码(无系统场景为 类型编码:PUBLIC_AREA_LIGHTING 公共照明系统 DemandTemplateTypeEnum)
     */
    private String systemCode;

    /**
     * 设备对象ID
     */
    private String objId;

    /**
     * 变更内容
     */
    @TableField(value = "change_content", typeHandler = JacksonTypeHandler.class)
    private ChangeContent changeContent;

    /**
     * 执行状态
     * 默认 -1 没有执行
     * 0：执行失败
     * 1：执行成功
     */
    private Integer execStatus;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private String creationTime;

    /**
     * 修改时间
     */
    private String modifiedTime;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 扩展字段
     */
    private Object extend;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private String infoCode;
}