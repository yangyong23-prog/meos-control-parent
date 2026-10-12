package com.persagy.apccontrol.common.logtrace.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("logtrace_node_extend")
public class LogNodeExtendPo implements Serializable {

    @TableId(
            type = IdType.ASSIGN_ID
    )
    private Long id;

    private Long nodeId;

    private String nodeCode;

    private String extendCode;

    private String extendName;

    private String extendValue;

    private LocalDateTime businessTime;

    private LocalDateTime createTime;


}
