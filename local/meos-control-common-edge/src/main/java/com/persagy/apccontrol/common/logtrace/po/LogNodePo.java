package com.persagy.apccontrol.common.logtrace.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("logtrace_node_info")
public class LogNodePo implements Serializable {

    @TableId(
            type = IdType.ASSIGN_ID
    )
    private Long id;

    /**
     * 树节点业务编码
     */
    private String code;

    /**
     * 业务对象id
     */
    private String  businessId;

    private LocalDateTime createTime;

    private LocalDateTime businessTime;

    /**
     * 数据存储
     */

//    @TableField(typeHandler = JacksonTypeHandler.class)
//    private JsonNode extend;

    /**
     * 业务标识
     */
    private String application;

    @TableField(exist = false)
    private List<LogNodePo> children;

    /**
     * 节点描述
     */

    private String description;

}
