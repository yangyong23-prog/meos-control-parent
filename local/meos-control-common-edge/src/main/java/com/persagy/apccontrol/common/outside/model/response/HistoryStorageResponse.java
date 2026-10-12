package com.persagy.apccontrol.common.outside.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryStorageResponse {

    private String system_id;

    private LocalDateTime timestamp;

    /**
     * 存储标签 1 蓄冷 2 是蓄热
     */
    private String storageTag;

    /**
     *  0 不蓄 1 是蓄
     */
    private String flag;


}
