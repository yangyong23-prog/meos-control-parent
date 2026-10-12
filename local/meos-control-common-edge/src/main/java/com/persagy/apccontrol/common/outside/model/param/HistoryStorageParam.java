package com.persagy.apccontrol.common.outside.model.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 获取蓄冷蓄热标签，时间区间 前闭后开
 * {
 * "projectId": "Pj9170576282",
 * "systemId": "Sy1623177617811714049",
 * "groupCode":"TYXK",
 * "startTime":"2024120101",
 * "endTime":"2024120102"
 * }
 */
@Data
public class HistoryStorageParam {

    private String projectId;
    private Set<String> systemId;
    private String groupCode;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime endTime;
}
