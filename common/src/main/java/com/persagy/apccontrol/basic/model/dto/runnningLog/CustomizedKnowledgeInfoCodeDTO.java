package com.persagy.apccontrol.basic.model.dto.runnningLog;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDTO;
import com.persagy.apccontrol.basic.outside.dto.ObjectsExistRule;
import lombok.Data;
import org.apache.commons.collections.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义信息点
 */
@Data
public class CustomizedKnowledgeInfoCodeDTO {
    
    //系统信息点规则
    private List<ObjectsExistRule> sysExistRules;
    
    //对象信息点 定义
    private Map<String, List<DtpKnowledgeInfoCodeDTO>> knowledgeInfoCodeMap;
    
    //索引参数
    //通过 info_code + 系统 ID 获取系统信息点定义
    private Map<String, ObjectsExistRule> sysExistRulesMap = new HashMap<>();
    //通过 class_code+info_code 获取对象信息点定义
    private Map<String, DtpKnowledgeInfoCodeDTO> objKnowledgeInfoCodeListMap = new HashMap<>();
    
    
    public CustomizedKnowledgeInfoCodeDTO(List<ObjectsExistRule> sysExistRules, Map<String,
            List<DtpKnowledgeInfoCodeDTO>> knowledgeInfoCodeMap) {
        this.sysExistRules = sysExistRules;
        this.knowledgeInfoCodeMap = knowledgeInfoCodeMap;
        
        if (CollectionUtils.isNotEmpty(sysExistRules)) {
            // info_code + 系统 ID 分组如果有多个取第一个
            sysExistRules.forEach(item -> sysExistRulesMap.put(item.getInfoCode() + item.getObjectId(), item));
        }
        if (MapUtils.isNotEmpty(knowledgeInfoCodeMap)) {
            knowledgeInfoCodeMap.forEach((key, value) -> value.forEach(item -> objKnowledgeInfoCodeListMap.put(item.getClassCode() + item.getCode(), item)));
        }
    }
    
    //通过 info_code + 系统 ID 获取系统信息点定义
    public ObjectsExistRule getSysExistRule(String infoCode, String systemId) {
        return sysExistRulesMap.get(infoCode + systemId);
    }
    
    
    //通过 class_code+info_code 获取对象信息点定义
    public DtpKnowledgeInfoCodeDTO getObjKnowledgeInfoCode(String classCode, String infoCode) {
        return objKnowledgeInfoCodeListMap.get(classCode + infoCode);
    }
    
}
