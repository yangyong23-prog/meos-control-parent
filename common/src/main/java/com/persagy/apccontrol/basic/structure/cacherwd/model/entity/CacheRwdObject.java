package com.persagy.apccontrol.basic.structure.cacherwd.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.dto.rwd.DtpRwdObjectDTO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.utils.ObjectMapperUtils;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Optional;

/**
 * 物理世界对象缓存表实体类
 * 对应表：cache_rwd_object
 *
 * @author wxf
 * @date 2026-02-26
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cache_rwd_object")
public class CacheRwdObject extends BaseAssignIdDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 实例类型
     */
    @TableField(value = "object_type")
    private DtpRwdObjectTypeEnum objectType;

    /**
     * 实例ID
     */
    @TableField(value = "object_id")
    private String objectId;

    /**
     * 分类编码
     */
    @TableField(value = "class_code")
    private String classCode;

    /**
     * 监测类型
     */
    @TableField(value = "running_monitor_type")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 自定义索引 key，方法入参md5
     */
    @TableField(value = "custom_index_key")
    private String customIndexKey;

    /**
     * 数据 JSON
     */
    @TableField(value = "object_data")
    private String objectData;

    /**
     * 项目id
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 转换为Rwd对象
     *
     * @return Rwd对象
     * @since 7.260302.0
     */
    public @Nullable DtpRwdObjectDTO toRwdObject() {
        return Optional.ofNullable(this.objectData)
                .map(data -> ObjectMapperUtils.readValueFailSafe(data, DtpRwdObjectDTO.class))
                .orElse(null);
    }

    /**
     * 从Rwd对象转换为CacheRwdObject
     *
     * @param templateTypeModel 模板类型模型
     * @param object            Rwd对象
     * @return CacheRedObject
     * @since 7.260302.0
     */
    public static @NonNull CacheRwdObject toAddObject(
            @NonNull InfoCodeTemplateTypeModel templateTypeModel,
            @NonNull DtpRwdObjectDTO object) {
        CacheRwdObject cacheObject = new CacheRwdObject();
        cacheObject.setObjectType(templateTypeModel.getObjectType());
        cacheObject.setObjectId(object.getId());
        cacheObject.setClassCode(object.getClassCode());
        cacheObject.setRunningMonitorType(templateTypeModel.getRunningMonitorType());
        cacheObject.setCustomIndexKey(templateTypeModel.getTemplateTypeKey());
        cacheObject.setObjectData(ObjectMapperUtils.writeValueAsStringFailSafe(object));
        cacheObject.setProjectId(ContextUtil.getProjectId());
        return cacheObject;
    }
}
