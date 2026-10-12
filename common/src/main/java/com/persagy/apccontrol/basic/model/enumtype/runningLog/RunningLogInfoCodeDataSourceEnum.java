package com.persagy.apccontrol.basic.model.enumtype.runningLog;

import com.persagy.apccontrol.basic.model.dto.knowledge.DtpKnowledgeInfoCodeDataSourceDTO;
import com.persagy.apccontrol.basic.utils.i18n.LocaleMessageUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行日志自定义信息点
 */
@Getter
public enum RunningLogInfoCodeDataSourceEnum {
    
    //编码对应"ZDYairConditioningCapacitySet"，数据源为"供冷"、"供热"
    ZDY_AIR_CONDITIONING_CAPACITY_SET(
            "ZDYairConditioningCapacitySet",
            new ArrayList<DtpKnowledgeInfoCodeDataSourceDTO>() {{
                add(new DtpKnowledgeInfoCodeDataSourceDTO("1", LocaleMessageUtil.getMessage("key_runninglog_cooling")));
                add(new DtpKnowledgeInfoCodeDataSourceDTO("2", LocaleMessageUtil.getMessage("key_runninglog_heat_addition")));
            }}
    ),
    // 可在此处追加其他信息点配置（动态扩展）
    
    ;
    
    /**
     * 信息点编码
     */
    private final String infoCode;
    /**
     * 该信息点对应的数据源列表
     */
    private final List<DtpKnowledgeInfoCodeDataSourceDTO> dataSources;
    
    // 构造方法
    RunningLogInfoCodeDataSourceEnum(String infoCode, List<DtpKnowledgeInfoCodeDataSourceDTO> dataSources) {
        this.infoCode = infoCode;
        this.dataSources = dataSources;
    }
    
    // 静态方法：根据信息点编码获取对应的枚举实例
    public static RunningLogInfoCodeDataSourceEnum getByInfoCode(String infoCode) {
        for (RunningLogInfoCodeDataSourceEnum enumItem : values()) {
            if (enumItem.infoCode.equals(infoCode)) {
                return enumItem;
            }
        }
        return null; // 无匹配配置时返回null
    }
    
}
