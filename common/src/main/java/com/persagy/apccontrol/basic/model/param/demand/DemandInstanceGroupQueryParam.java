package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.model.dto.demand.DemandBelongInstanceDTO;
import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class DemandInstanceGroupQueryParam {

    /**
     * ID
     */
    private List<Long> ids;

    /**
     * 需求模版类型
     */
    private DemandTemplateTypeEnum templateType;

    /**
     * 模板类型列表
     */
    private List<DemandTemplateTypeEnum> templateTypes;
    /**
     * 附加查询字段
     */
    private List<String> additionalWithColumns;
    /**
     * 所属对象
     */
    private List<DemandBelongInstanceDTO> belongInstance;
    /**
     * 关键字搜索
     */
    private String keyword;
    /**
     * 分组标签
     */
    private List<String> tags;


}
