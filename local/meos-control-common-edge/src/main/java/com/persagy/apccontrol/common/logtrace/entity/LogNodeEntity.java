package com.persagy.apccontrol.common.logtrace.entity;


import com.persagy.apccontrol.common.logtrace.po.LogNodeExtendPo;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LogNodeEntity implements Serializable {

    private Long id;

    /**
     * 树节点业务编码
     */
    private String code;
    /**
     * 业务对象id
     */
    private String  businessId;

    private LocalDateTime businessTime;

    private LocalDateTime createTime;

    /**
     * 数据存储
     */
    private List<LogNodeExtendPo> extend;

    /**
     * 业务标识
     */
    private String application;

    /**
     * 节点描述
     */
    private String description;

}
