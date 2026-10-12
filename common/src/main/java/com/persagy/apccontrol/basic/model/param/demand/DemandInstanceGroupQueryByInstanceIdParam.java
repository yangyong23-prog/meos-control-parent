package com.persagy.apccontrol.basic.model.param.demand;

import com.persagy.apccontrol.basic.structure.demand.enumeration.DemandTemplateTypeEnum;
import lombok.Data;
import org.jspecify.annotations.NonNull;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class DemandInstanceGroupQueryByInstanceIdParam {


    /**
     * 模板类型
     */
    private DemandTemplateTypeEnum templateType;

    /**
     * 标签
     */
    private String tag;
    /**
     * 实例id列表
     */
    @NotNull(message = "{key_c3206cc869e14bb1be4f86b8a4be17a2}")
    private Set<@NonNull String> instanceIds;

    /**
     * 查询字段列表
     */
    private String[] withColumns;

}
