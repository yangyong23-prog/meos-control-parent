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
@TableName("logtrace_node_relation")
public class LogNodeRelationPo implements Serializable {

    @TableId(
            type = IdType.ASSIGN_ID
    )
    private Long id;

    private Long nodeId;

    @TableField(exist = false)
    private List<LogNodeRelationPo> children;

    private Long parentNodeId;

    private LocalDateTime createTime;

}
