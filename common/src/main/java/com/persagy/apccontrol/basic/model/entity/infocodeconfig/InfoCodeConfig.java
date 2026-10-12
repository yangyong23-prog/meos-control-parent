package com.persagy.apccontrol.basic.model.entity.infocodeconfig;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.config.dao.handler.JacksonSetWithLongTypeHandler;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 信息点配置表
 *
 * @author JiYinchuan
 * @since 5.240602.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "object_info_code_config", autoResultMap = true)
public class InfoCodeConfig extends BaseAssignIdDO implements Serializable {

    /**
     * 信息点编码
     */
    @TableField(value = "info_code")
    private String infoCode;

    /**
     * 信息点名称
     */
    @TableField(value = "info_code_name")
    private String infoCodeName;

    /**
     * 信息点别名
     */
    @TableField(value = "info_code_alias_name")
    private String infoCodeAliasName;

    /**
     * 信息点类型
     */
    @TableField(value = "info_code_type")
    private InfoCodeTypeEnum infoCodeType;

    /**
     * 取值逻辑
     */
    @TableField(value = "tack_value_logic", typeHandler = JacksonTypeHandler.class)
    private JsonNode tackValueLogic;

    /**
     * 展示样式
     */
    @TableField(value = "show_style")
    private String showStyle;

    /**
     * 信息点顺序
     */
    @TableField(value = "sequence")
    private Integer sequence;

    /**
     * 关联信息点配置ID
     */
    @TableField(value = "rel_info_code_config_ids", typeHandler = JacksonSetWithLongTypeHandler.class)
    private Set<Long> relInfoCodeConfigIds;

    /**
     * 关联分组编码
     */
    @TableField(value = "rel_group_code")
    private String relGroupCode;

    /**
     * 关联模版类型ID
     */
    @TableField(value = "rel_template_type_id")
    private Long relTemplateTypeId;

    @Override
    protected Supplier<Boolean> checkAddEntitySupplier() {
        return () -> ObjectUtils.anyNull(infoCode, infoCodeType, sequence, relGroupCode, relTemplateTypeId);
    }
}