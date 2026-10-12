package com.persagy.apccontrol.common.logtrace.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class LogNodeRelationEntity implements Serializable {

    private Long id;

    private Long nodeId;

    private Long parentNodeId;

    private LocalDateTime createTime;

}
