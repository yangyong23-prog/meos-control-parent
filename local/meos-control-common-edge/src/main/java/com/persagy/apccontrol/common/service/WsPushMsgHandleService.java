package com.persagy.apccontrol.common.service;

import com.persagy.apccontrol.common.model.dto.PageBroadcastMsgDto;

public interface WsPushMsgHandleService {

    /**
     * 获取播报内容
     * @param msgDto
     * @return
     */
    String getContent(PageBroadcastMsgDto msgDto);
}
