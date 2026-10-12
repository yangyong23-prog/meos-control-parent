package com.persagy.apccontrol.basic.model.entity.infocodeconfig;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorSecondTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.RunningMonitorTypeEnum;
import com.persagy.apccontrol.basic.model.enumtype.dtprwd.DtpRwdObjectTypeEnum;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTemplateTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 设备模版类型配置
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "object_info_code_template_type")
public class InfoCodeTemplateType extends BaseAssignIdDO implements Serializable {

    public static final InfoCodeTemplateType EMPTY = new InfoCodeTemplateType();

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
     * 模版编码
     */
    @TableField(value = "template_type")
    private InfoCodeTemplateTypeEnum templateType;

    /**
     * 对象类型
     */
    @TableField(value = "object_type")
    private DtpRwdObjectTypeEnum objectType;

    /**
     * 设备类编码
     */
    @TableField(value = "class_code")
    private String classCode;

    /**
     * 信息点筛选
     */
    @TableField(value = "info_code_filter")
    private String infoCodeFilter;

    /**
     * 设备类顺序
     */
    @TableField(value = "sequence")
    private Integer sequence;

    @Override
    protected Supplier<Boolean> checkAddEntitySupplier() {
        return () -> ObjectUtils.anyNull(templateType, classCode);
    }

    @Override
    protected Supplier<Boolean> checkDelEntitySupplier() {
        return () -> Objects.isNull(runningMonitorType);
    }
}