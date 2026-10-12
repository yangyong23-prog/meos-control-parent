package com.persagy.apccontrol.basic.model.dto.systemgraph.adm;

import lombok.Data;

@Data
public class GraphAsyncGenerateDTO {


    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 模板信息
     */
    private GraphTemplateDto template;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 应用ID
     */
    private String appId;
    /**
     * 任务ID
     */
    private String jobId;
    private String groupCode;
    private String projectId;
    private String dicVersion;
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
    private int resultTryCount;
    /**
     * 任务是否完成
     */
    private boolean resultFinish;
    /**
     * 任务是否成功
     */
    private boolean resultSuccess;
    private String resultMessage;
}
