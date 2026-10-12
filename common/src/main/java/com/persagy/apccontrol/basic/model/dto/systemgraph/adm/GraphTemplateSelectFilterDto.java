package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 *
 * @author houyuanzhi
 * @date 2023/8/22 18:02
 */
@Data
public class GraphTemplateSelectFilterDto {

    /**
     * 专业
     */
    private String majorCode;

    /**
     * 关系图类(中文)
     */
    private String graphTypeCode;

    /**
     * 关系图子类（中文）
     */
    private String graphSubTypeCode;

    /**
     * 模板状态（中文）
     */
    private String templateStatus;
}
