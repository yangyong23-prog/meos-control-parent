package com.persagy.apccontrol.common.logtrace.entity;

import java.time.LocalDateTime;
import java.util.List;

public class LogNodeRelationListEntity {

    private List<Long> childrenIds;

    private Long parentNodeId;

    private LocalDateTime createTime;
}
