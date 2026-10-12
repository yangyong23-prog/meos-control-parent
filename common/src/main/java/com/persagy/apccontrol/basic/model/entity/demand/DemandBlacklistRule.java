package com.persagy.apccontrol.basic.model.entity.demand;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.persagy.apccontrol.basic.model.base.BaseAssignIdDO;
import com.persagy.apccontrol.basic.model.vo.demand.DemandBlacklistRuleVO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * 需求黑名单规则表
 *
 * @TableName demand_blacklist_rule
 */
@TableName(value = "demand_blacklist_rule", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class DemandBlacklistRule extends BaseAssignIdDO implements Serializable {


    private static final long serialVersionUID = 1203306477011167174L;
    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 需求类型
     */
    @TableField(value = "demand_type")
    private DemandTypeEnum demandType;

    /**
     * 需求模板类型
     */
    @TableField(value = "demand_template_type")
    private DemandTemplateTypeEnum demandTemplateType;

    /**
     * 配置数据
     */
    @TableField(value = "config_data", typeHandler = JacksonTypeHandler.class, updateStrategy = FieldStrategy.IGNORED)
    private Map<String, JsonNode> configData;


    public DemandBlacklistRuleVO toVO() {
        DemandBlacklistRuleVO vo = new DemandBlacklistRuleVO();
        vo.setDemandType(demandType);
        vo.setDemandTemplateType(demandTemplateType);
        vo.setProjectId(projectId);
        vo.setConfigData(configData);
        return vo;
    }

}