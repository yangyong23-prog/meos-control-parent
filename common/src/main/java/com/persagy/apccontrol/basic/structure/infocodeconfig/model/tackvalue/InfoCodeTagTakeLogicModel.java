package com.persagy.apccontrol.basic.structure.infocodeconfig.model.tackvalue;

import com.persagy.apccontrol.basic.outside.feign.model.param.knowledge.DtpKnowledgeInfoCodeByTagQueryParam;
import com.persagy.apccontrol.basic.structure.infocodeconfig.enumeration.InfoCodeTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 信息点标签取值
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfoCodeTagTakeLogicModel extends BaseInfoCodeTackValueLogicModel {
    
    /**
     * 信息点类型
     */
    private InfoCodeTypeEnum infoCodeType;


    /**
     * 标签值
     */
    private List<DtpKnowledgeInfoCodeByTagQueryParam.TagParam> tagParams;
    /**
     * 获取日志标签
     *
     * @return 日志标签
     * @since 7.251202.0
     */
    @Override
    public String logTag() {
        return "信息点标签解析逻辑";
    }
}
