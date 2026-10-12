package com.persagy.apccontrol.basic.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.dto.ExtendSetting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDateTime;

/**
    * 对象信息点设置扩展表
 * @author yuecaipu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@TableName(value = InfoCodeSetExtend.TABLE_NAME, autoResultMap = true)
public class InfoCodeSetExtend {

    public static final String TABLE_NAME = "info_code_set_extend";


    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 系统ID
     */
    @TableField(value = "system_id")
    private String systemId;

    /**
     * 设备对象ID
     */
    @TableField(value = "obj_id")
    private String objId;

    /**
     * 信息点编码
     */
    @TableField(value = "info_code")
    private String infoCode;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


    /**
     * 扩展字段
     */
    @TableField(value = "extend", typeHandler = JacksonTypeHandler.class)
    private ExtendSetting extend;

    public static final String COL_ID = "id";

    public static final String COL_GROUP_CODE = "group_code";

    public static final String COL_PROJECT_ID = "project_id";

    public static final String COL_OBJ_ID = "obj_id";

    public static final String COL_INFO_CODE = "info_code";

    public static final String COL_CREATION_TIME = "creation_time";

    public static final String COL_MODIFIED_TIME = "modified_time";

    public static final String COL_MODIFIER = "modifier";

    public static final String COL_EXTEND = "extend";
}