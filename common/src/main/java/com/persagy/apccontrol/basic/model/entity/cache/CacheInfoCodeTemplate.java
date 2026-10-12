package com.persagy.apccontrol.basic.model.entity.cache;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.InfoCodeTemplateTypeModel;
import com.persagy.apccontrol.basic.structure.infocodeconfig.model.valueobject.InfoCodeFilterValueObject;
import com.persagy.framework.common.context.ContextUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * 缓存-信息点模版表
 *
 * @author JiYinchuan
 * @since 7.260302.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "cache_info_code_template", autoResultMap = true)
public class CacheInfoCodeTemplate extends BaseAssignIdDO implements Serializable {

    /**
     * 项目ID
     */
    @TableField(value = "project_id")
    private String projectId;

    /**
     * 运行监测类型
     */
    @TableField(value = "running_monitor_type")
    private RunningMonitorTypeEnum runningMonitorType;

    /**
     * 运行监测二级类型
     */
    @TableField(value = "running_monitor_second_type")
    private RunningMonitorSecondTypeEnum runningMonitorSecondType;

    /**
     * 模版唯一 Key
     */
    @TableField(value = "custom_key")
    private String customKey;

    /**
     * 数据
     */
    @TableField(value = "data_json", typeHandler = JacksonTypeHandler.class)
    private @Nullable InfoCodeFilterValueObject dataJson;

    /**
     * 关联信息点模版ID
     */
    @TableField(value = "rel_template_id")
    private Long relTemplateId;

    /**
     * 从信息点模版类型模型转换为缓存信息点模版
     *
     * @param templateTypeModel 信息点模版类型模型
     * @return 缓存信息点模版
     * @since 7.260302.0
     */
    public static CacheInfoCodeTemplate from(@NonNull InfoCodeTemplateTypeModel templateTypeModel) {
        CacheInfoCodeTemplate cacheInfoCodeTemplate = new CacheInfoCodeTemplate();
        cacheInfoCodeTemplate.setProjectId(ContextUtil.getProjectId());
        cacheInfoCodeTemplate.setRunningMonitorType(templateTypeModel.getRunningMonitorType());
        cacheInfoCodeTemplate.setRunningMonitorSecondType(templateTypeModel.getRunningMonitorSecondType());
        cacheInfoCodeTemplate.setCustomKey(templateTypeModel.getTemplateTypeKey());
        // 写入侧强制实时解析, 跳过旧快照复用; 主查询参数缺失时不固化缓存快照, 写入空值由读取侧实时解析兜底
        InfoCodeFilterValueObject dataJson = templateTypeModel.parseFilterValueObject(true);
        cacheInfoCodeTemplate.setDataJson(Objects.nonNull(dataJson) && Objects.nonNull(dataJson.getQueryParam())
                ? dataJson : null);
        cacheInfoCodeTemplate.setRelTemplateId(templateTypeModel.getTemplateTypeId());
        return cacheInfoCodeTemplate;
    }
}
