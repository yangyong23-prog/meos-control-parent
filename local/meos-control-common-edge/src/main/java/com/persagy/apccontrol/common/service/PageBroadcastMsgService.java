package com.persagy.apccontrol.common.service;

public interface PageBroadcastMsgService {

    /**
     * 推送页面播报内容给前端页面
     *
     * @param projectId
     * @param systemId
     * @param modelType 为空，取header
     */
    void pushMessageToWeb(String projectId, String systemId, String modelType);
}
