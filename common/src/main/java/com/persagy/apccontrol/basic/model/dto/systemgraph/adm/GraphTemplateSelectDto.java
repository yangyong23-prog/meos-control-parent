package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

/**
 * @author houyuanzhi
 * @date 2023/8/22 1755
 */
@Data
public class GraphTemplateSelectDto {

    /**
     * id
     */
//    private String id;

    private String oriId;

    /**
     * 专业
     */
    private String majorCod;
    /**
     * 空间范围
     */
    private String spacialScale;

    /**
     * 基准对象（系统类code）
     */
    private String referenceObjectTypeCode;
    /**
     * 关系图类
     */
    private String graphTypeCode;
    /**
     * 关系图子类
     */
    private String graphSubTypeCode;
    /**
     * 版本
     */
    private String version;
    /**
     * 模板code
     */
    private String templateCode;
    /**
     * 模板状态
     */
    private String templateStatus;

    /**
     * 说明
     */
    private String remark;
}
