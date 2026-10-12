package com.persagy.apccontrol.basic.model.dto;

import com.persagy.apccontrol.basic.model.enumtype.RabbitMqEnumOpCode;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RabbitMq 消息同步处理类
 *
 * @author zhangqiankun
 * @version 1.0.0
 * @company persagy
 * @date 2024/4/20 11:36
 */
@Data
@NoArgsConstructor
public class RabbitMqMessageDTO<T> {

    /**
     * 操作类型
     */
    private RabbitMqEnumOpCode opCode;

    /**
     * 集团编码
     */
    private String groupCode;

    /**
     * 项目ID
     */
    private String projectId;

    // 状态位，在不同的opCode下，可以赋予不同的含义
    private Integer status;

    /**
     * 传输内容
     */
    private List<T> content;

    public RabbitMqMessageDTO(String groupCode, String projectId, RabbitMqEnumOpCode opCode, List<T> content) {
        this.groupCode = groupCode;
        this.projectId = projectId;
        this.opCode = opCode;
        this.content = content;
    }

}
