package com.persagy.apccontrol.basic.model.vo.demand;

import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.persagy.apccontrol.basic.model.dto.demand.blacklist.BasicsDemandBlacklistRuleDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTypeEnum;
import lombok.Data;

import java.util.Map;

@Data
public class DemandBlacklistRuleVO {
    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 需求类型
     */
    private DemandTypeEnum demandType;

    /**
     * 需求模板类型
     */
    private DemandTemplateTypeEnum demandTemplateType;

    /**
     * 配置数据
     */
    private Map<String, JsonNode> configData;

    public void putConfigData(Map<String, BasicsDemandBlacklistRuleDTO> dtoMap) {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        Map<String, JsonNode> newConfigData = Maps.newHashMap();
        dtoMap.forEach((type, rule) -> {
            newConfigData.put(type, objectMapper.valueToTree(rule));
        });
        this.configData = newConfigData;
    }

}
