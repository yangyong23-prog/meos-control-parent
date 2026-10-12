package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

@Data
public class GraphAsyncJobDTO {

    /**
     * 任务ID
     */
    private String jobId;
    /**
     * 生成id
     */
    private String generateId;
    /**
     * 模板id
     */
    private String templateId;
    /**
     * 模板原始id
     */
    private String templateOriId;
    /**
     * 基准对象id
     */
    private String referenceObjectId;
}
